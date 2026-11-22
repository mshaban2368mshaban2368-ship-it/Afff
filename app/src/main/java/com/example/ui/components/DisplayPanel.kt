package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.engine.ErrorType
import com.example.ui.theme.Fa97ThemeTokens
import com.example.ui.theme.JetBrainsMonoFontFamily

@Composable
fun DisplayPanel(
  expression: String,
  liveResult: String,
  errorType: ErrorType?,
  isJustEvaluated: Boolean,
  isScientificMode: Boolean,
  isDegreeMode: Boolean,
  onToggleDegreeMode: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val extended = Fa97ThemeTokens.colors
  val exprScrollState = rememberScrollState()
  val resultScrollState = rememberScrollState()

  LaunchedEffect(expression) {
    exprScrollState.animateScrollTo(exprScrollState.maxValue)
  }

  LaunchedEffect(liveResult, errorType) {
    resultScrollState.scrollTo(resultScrollState.maxValue)
  }

  val errorMessage =
    when (errorType) {
      ErrorType.DIVIDE_BY_ZERO -> stringResource(R.string.error_divide_by_zero)
      ErrorType.DOMAIN_ERROR -> stringResource(R.string.error_domain)
      ErrorType.INVALID_EXPRESSION -> stringResource(R.string.error_invalid_expression)
      ErrorType.OVERFLOW -> stringResource(R.string.error_overflow)
      null -> null
    }

  Box(
    modifier =
      modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(24.dp))
        .background(
          Brush.verticalGradient(
            colors =
              listOf(
                MaterialTheme.colorScheme.surface,
                MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
              )
          )
        )
        .background(
          Brush.radialGradient(
            colors = listOf(extended.displayGlow, Color.Transparent),
          )
        )
        .border(
          width = 1.dp,
          color =
            if (errorMessage != null) MaterialTheme.colorScheme.error.copy(alpha = 0.65f)
            else MaterialTheme.colorScheme.outline.copy(alpha = 0.7f),
          shape = RoundedCornerShape(24.dp),
        )
        .padding(horizontal = 20.dp, vertical = 16.dp)
  ) {
    Column(
      modifier = Modifier.fillMaxWidth(),
      verticalArrangement = Arrangement.SpaceBetween,
    ) {
      // Top indicator row (DEG/RAD toggle + status)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Box(
          modifier =
            Modifier.minimumInteractiveComponentSize()
              .clip(RoundedCornerShape(10.dp))
              .background(
                if (isScientificMode) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
              )
              .clickable(role = Role.Button, onClick = onToggleDegreeMode)
              .padding(horizontal = 10.dp, vertical = 5.dp)
              .testTag("degree_radian_toggle"),
          contentAlignment = Alignment.Center,
        ) {
          Text(
            text =
              if (isDegreeMode) stringResource(R.string.deg_label)
              else stringResource(R.string.rad_label),
            style = MaterialTheme.typography.labelSmall,
            color =
              if (isScientificMode) MaterialTheme.colorScheme.primary
              else MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }

        val unclosedCount =
          (expression.count { it == '(' } - expression.count { it == ')' }).coerceAtLeast(0)
        if (unclosedCount > 0) {
          Text(
            text = "${")".repeat(unclosedCount)} auto",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Live expression line on top
      Box(
        modifier = Modifier.fillMaxWidth().horizontalScroll(exprScrollState),
        contentAlignment = Alignment.CenterEnd,
      ) {
        val displayExpr = expression.ifEmpty { "0" }
        val exprFontSize =
          when {
            displayExpr.length > 22 -> 20.sp
            displayExpr.length > 14 -> 24.sp
            else -> 28.sp
          }
        Text(
          text =
            highlightExpression(
              expr = displayExpr,
              isPlaceholder = expression.isEmpty(),
              baseColor =
                if (expression.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
                else if (isJustEvaluated) MaterialTheme.colorScheme.onSurfaceVariant
                else MaterialTheme.colorScheme.onSurface,
              operatorColor = MaterialTheme.colorScheme.primary,
              funcColor = extended.scientificAccent,
            ),
          fontFamily = JetBrainsMonoFontFamily,
          fontWeight = if (isJustEvaluated) FontWeight.Normal else FontWeight.Medium,
          fontSize = exprFontSize,
          maxLines = 1,
          softWrap = false,
          textAlign = TextAlign.End,
          modifier = Modifier.testTag("expression_display"),
        )
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Result or Error line below expression
      Box(
        modifier = Modifier.fillMaxWidth().height(54.dp).horizontalScroll(resultScrollState),
        contentAlignment = Alignment.CenterEnd,
      ) {
        AnimatedContent(
          targetState = errorMessage ?: liveResult,
          transitionSpec = { fadeIn() togetherWith fadeOut() },
          label = "result_anim",
        ) { shownText ->
          if (errorMessage != null) {
            Text(
              text = errorMessage,
              fontFamily = JetBrainsMonoFontFamily,
              fontWeight = FontWeight.SemiBold,
              fontSize = 20.sp,
              color = MaterialTheme.colorScheme.error,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
              modifier = Modifier.testTag("error_display"),
            )
          } else {
            val resultFontSize =
              when {
                shownText.length > 14 -> 28.sp
                shownText.length > 10 -> 34.sp
                else -> 42.sp
              }
            Text(
              text = if (shownText.isNotEmpty()) "= $shownText" else "",
              fontFamily = JetBrainsMonoFontFamily,
              fontWeight = if (isJustEvaluated) FontWeight.Bold else FontWeight.SemiBold,
              fontSize = resultFontSize,
              color =
                if (isJustEvaluated) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant,
              maxLines = 1,
              softWrap = false,
              modifier = Modifier.testTag("result_display"),
            )
          }
        }
      }
    }
  }
}

private fun highlightExpression(
  expr: String,
  isPlaceholder: Boolean,
  baseColor: Color,
  operatorColor: Color,
  funcColor: Color,
): AnnotatedString {
  if (isPlaceholder) {
    return buildAnnotatedString {
      withStyle(SpanStyle(color = baseColor)) { append(expr) }
    }
  }
  val operators = setOf('+', '−', '-', '×', '*', '÷', '/', '^', '%', '²')
  val parensAndConst = setOf('(', ')', 'π', 'e', '√')
  return buildAnnotatedString {
    var i = 0
    while (i < expr.length) {
      val ch = expr[i]
      when {
        ch in operators -> {
          withStyle(SpanStyle(color = operatorColor, fontWeight = FontWeight.SemiBold)) {
            append(ch)
          }
          i++
        }
        ch in parensAndConst -> {
          withStyle(SpanStyle(color = funcColor, fontWeight = FontWeight.SemiBold)) {
            append(ch)
          }
          i++
        }
        ch.isLetter() && ch != 'E' -> {
          val start = i
          while (i < expr.length && expr[i].isLetter()) i++
          withStyle(SpanStyle(color = funcColor, fontWeight = FontWeight.SemiBold)) {
            append(expr.substring(start, i))
          }
        }
        else -> {
          withStyle(SpanStyle(color = baseColor)) { append(ch) }
          i++
        }
      }
    }
  }
}
