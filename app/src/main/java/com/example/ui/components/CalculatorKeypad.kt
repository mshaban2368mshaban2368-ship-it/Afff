package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Backspace
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.Fa97ThemeTokens
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.SpaceGroteskFontFamily

enum class KeyStyle {
  NUMERIC,
  UTILITY,
  OPERATOR,
  EQUALS,
  SCIENTIFIC,
}

data class KeypadCallbacks(
  val onDigit: (String) -> Unit,
  val onDecimal: () -> Unit,
  val onOperator: (String) -> Unit,
  val onPostfix: (String) -> Unit,
  val onFunction: (String) -> Unit,
  val onConstant: (String) -> Unit,
  val onOpenParen: () -> Unit,
  val onCloseParen: () -> Unit,
  val onToggleSign: () -> Unit,
  val onClear: () -> Unit,
  val onBackspace: () -> Unit,
  val onEquals: () -> Unit,
)

@Composable
fun CalculatorKeypad(
  isScientificMode: Boolean,
  callbacks: KeypadCallbacks,
  compactHeight: Boolean,
  modifier: Modifier = Modifier,
) {
  val keyHeight = if (compactHeight) 56.dp else 66.dp
  val sciKeyHeight = if (compactHeight) 46.dp else 50.dp
  val rowGap = if (compactHeight) 8.dp else 10.dp

  Column(
    modifier = modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(rowGap),
  ) {
    // Expandable Scientific Strip (12 keys in 2 rows of 6)
    AnimatedVisibility(
      visible = isScientificMode,
      enter =
        expandVertically(
          animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
        ) + fadeIn(),
      exit =
        shrinkVertically(
          animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
        ) + fadeOut(),
    ) {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          CalcButton(
            label = "sin",
            tag = "key_sin",
            style = KeyStyle.SCIENTIFIC,
            height = sciKeyHeight,
            modifier = Modifier.weight(1f),
            onClick = { callbacks.onFunction("sin(") },
          )
          CalcButton(
            label = "cos",
            tag = "key_cos",
            style = KeyStyle.SCIENTIFIC,
            height = sciKeyHeight,
            modifier = Modifier.weight(1f),
            onClick = { callbacks.onFunction("cos(") },
          )
          CalcButton(
            label = "tan",
            tag = "key_tan",
            style = KeyStyle.SCIENTIFIC,
            height = sciKeyHeight,
            modifier = Modifier.weight(1f),
            onClick = { callbacks.onFunction("tan(") },
          )
          CalcButton(
            label = "log",
            tag = "key_log",
            style = KeyStyle.SCIENTIFIC,
            height = sciKeyHeight,
            modifier = Modifier.weight(1f),
            onClick = { callbacks.onFunction("log(") },
          )
          CalcButton(
            label = "ln",
            tag = "key_ln",
            style = KeyStyle.SCIENTIFIC,
            height = sciKeyHeight,
            modifier = Modifier.weight(1f),
            onClick = { callbacks.onFunction("ln(") },
          )
          CalcButton(
            label = "√",
            tag = "key_sqrt",
            style = KeyStyle.SCIENTIFIC,
            height = sciKeyHeight,
            modifier = Modifier.weight(1f),
            onClick = { callbacks.onFunction("√(") },
          )
        }
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          CalcButton(
            label = "x²",
            tag = "key_square",
            style = KeyStyle.SCIENTIFIC,
            height = sciKeyHeight,
            modifier = Modifier.weight(1f),
            onClick = { callbacks.onPostfix("²") },
          )
          CalcButton(
            label = "xʸ",
            tag = "key_power",
            style = KeyStyle.SCIENTIFIC,
            height = sciKeyHeight,
            modifier = Modifier.weight(1f),
            onClick = { callbacks.onOperator("^") },
          )
          CalcButton(
            label = "π",
            tag = "key_pi",
            style = KeyStyle.SCIENTIFIC,
            height = sciKeyHeight,
            modifier = Modifier.weight(1f),
            onClick = { callbacks.onConstant("π") },
          )
          CalcButton(
            label = "e",
            tag = "key_e",
            style = KeyStyle.SCIENTIFIC,
            height = sciKeyHeight,
            modifier = Modifier.weight(1f),
            onClick = { callbacks.onConstant("e") },
          )
          CalcButton(
            label = "(",
            tag = "key_open_paren",
            style = KeyStyle.SCIENTIFIC,
            height = sciKeyHeight,
            modifier = Modifier.weight(1f),
            onClick = callbacks.onOpenParen,
          )
          CalcButton(
            label = ")",
            tag = "key_close_paren",
            style = KeyStyle.SCIENTIFIC,
            height = sciKeyHeight,
            modifier = Modifier.weight(1f),
            onClick = callbacks.onCloseParen,
          )
        }
      }
    }

    // Row 1: C, ⌫, %, ÷
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      CalcButton(
        label = "C",
        tag = "key_clear",
        style = KeyStyle.UTILITY,
        height = keyHeight,
        modifier = Modifier.weight(1f),
        textColorOverride = MaterialTheme.colorScheme.error,
        onClick = callbacks.onClear,
      )
      CalcButton(
        label = "",
        icon = Icons.AutoMirrored.Rounded.Backspace,
        contentDescription = stringResource(R.string.cd_backspace),
        tag = "key_backspace",
        style = KeyStyle.UTILITY,
        height = keyHeight,
        modifier = Modifier.weight(1f),
        onClick = callbacks.onBackspace,
      )
      CalcButton(
        label = "%",
        tag = "key_percent",
        style = KeyStyle.UTILITY,
        height = keyHeight,
        modifier = Modifier.weight(1f),
        onClick = { callbacks.onPostfix("%") },
      )
      CalcButton(
        label = "÷",
        tag = "key_divide",
        style = KeyStyle.OPERATOR,
        height = keyHeight,
        modifier = Modifier.weight(1f),
        onClick = { callbacks.onOperator("÷") },
      )
    }

    // Row 2: 7, 8, 9, ×
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      CalcButton(
        label = "7",
        tag = "key_7",
        style = KeyStyle.NUMERIC,
        height = keyHeight,
        modifier = Modifier.weight(1f),
        onClick = { callbacks.onDigit("7") },
      )
      CalcButton(
        label = "8",
        tag = "key_8",
        style = KeyStyle.NUMERIC,
        height = keyHeight,
        modifier = Modifier.weight(1f),
        onClick = { callbacks.onDigit("8") },
      )
      CalcButton(
        label = "9",
        tag = "key_9",
        style = KeyStyle.NUMERIC,
        height = keyHeight,
        modifier = Modifier.weight(1f),
        onClick = { callbacks.onDigit("9") },
      )
      CalcButton(
        label = "×",
        tag = "key_multiply",
        style = KeyStyle.OPERATOR,
        height = keyHeight,
        modifier = Modifier.weight(1f),
        onClick = { callbacks.onOperator("×") },
      )
    }

    // Row 3: 4, 5, 6, −
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      CalcButton(
        label = "4",
        tag = "key_4",
        style = KeyStyle.NUMERIC,
        height = keyHeight,
        modifier = Modifier.weight(1f),
        onClick = { callbacks.onDigit("4") },
      )
      CalcButton(
        label = "5",
        tag = "key_5",
        style = KeyStyle.NUMERIC,
        height = keyHeight,
        modifier = Modifier.weight(1f),
        onClick = { callbacks.onDigit("5") },
      )
      CalcButton(
        label = "6",
        tag = "key_6",
        style = KeyStyle.NUMERIC,
        height = keyHeight,
        modifier = Modifier.weight(1f),
        onClick = { callbacks.onDigit("6") },
      )
      CalcButton(
        label = "−",
        tag = "key_minus",
        style = KeyStyle.OPERATOR,
        height = keyHeight,
        modifier = Modifier.weight(1f),
        onClick = { callbacks.onOperator("−") },
      )
    }

    // Row 4: 1, 2, 3, +
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      CalcButton(
        label = "1",
        tag = "key_1",
        style = KeyStyle.NUMERIC,
        height = keyHeight,
        modifier = Modifier.weight(1f),
        onClick = { callbacks.onDigit("1") },
      )
      CalcButton(
        label = "2",
        tag = "key_2",
        style = KeyStyle.NUMERIC,
        height = keyHeight,
        modifier = Modifier.weight(1f),
        onClick = { callbacks.onDigit("2") },
      )
      CalcButton(
        label = "3",
        tag = "key_3",
        style = KeyStyle.NUMERIC,
        height = keyHeight,
        modifier = Modifier.weight(1f),
        onClick = { callbacks.onDigit("3") },
      )
      CalcButton(
        label = "+",
        tag = "key_plus",
        style = KeyStyle.OPERATOR,
        height = keyHeight,
        modifier = Modifier.weight(1f),
        onClick = { callbacks.onOperator("+") },
      )
    }

    // Row 5: ±, 0, ., =
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      CalcButton(
        label = "±",
        tag = "key_sign",
        style = KeyStyle.NUMERIC,
        height = keyHeight,
        modifier = Modifier.weight(1f),
        onClick = callbacks.onToggleSign,
      )
      CalcButton(
        label = "0",
        tag = "key_0",
        style = KeyStyle.NUMERIC,
        height = keyHeight,
        modifier = Modifier.weight(1f),
        onClick = { callbacks.onDigit("0") },
      )
      CalcButton(
        label = ".",
        tag = "key_decimal",
        style = KeyStyle.NUMERIC,
        height = keyHeight,
        modifier = Modifier.weight(1f),
        onClick = callbacks.onDecimal,
      )
      CalcButton(
        label = "=",
        tag = "key_equals",
        style = KeyStyle.EQUALS,
        height = keyHeight,
        modifier = Modifier.weight(1f),
        onClick = callbacks.onEquals,
      )
    }
  }
}

@Composable
private fun CalcButton(
  label: String,
  tag: String,
  style: KeyStyle,
  height: Dp,
  modifier: Modifier = Modifier,
  icon: ImageVector? = null,
  contentDescription: String? = null,
  textColorOverride: Color? = null,
  onClick: () -> Unit,
) {
  val extended = Fa97ThemeTokens.colors
  val haptics = LocalHapticFeedback.current
  val interactionSource = remember { MutableInteractionSource() }
  val isPressed by interactionSource.collectIsPressedAsState()
  val isHovered by interactionSource.collectIsHoveredAsState()

  val scale by
    animateFloatAsState(
      targetValue =
        when {
          isPressed -> 0.92f
          isHovered -> 1.02f
          else -> 1f
        },
      animationSpec = spring(dampingRatio = 0.65f, stiffness = Spring.StiffnessMedium),
      label = "key_press_scale",
    )

  val cornerRadius = if (style == KeyStyle.SCIENTIFIC) 16.dp else 20.dp
  val shape = RoundedCornerShape(cornerRadius)

  val bgColor =
    when (style) {
      KeyStyle.NUMERIC -> extended.keyNumeric
      KeyStyle.UTILITY -> extended.keyUtility
      KeyStyle.OPERATOR -> extended.keyOperatorContainer
      KeyStyle.SCIENTIFIC -> extended.keyScientific
      KeyStyle.EQUALS -> extended.equalsStart
    }

  val fgColor =
    textColorOverride
      ?: when (style) {
        KeyStyle.NUMERIC -> MaterialTheme.colorScheme.onSurface
        KeyStyle.UTILITY -> MaterialTheme.colorScheme.onSurface
        KeyStyle.OPERATOR -> extended.keyOperatorContent
        KeyStyle.SCIENTIFIC -> extended.scientificAccent
        KeyStyle.EQUALS -> extended.onEquals
      }

  val borderColor =
    when (style) {
      KeyStyle.OPERATOR -> extended.keyOperatorContent.copy(alpha = 0.35f)
      KeyStyle.SCIENTIFIC -> extended.scientificAccent.copy(alpha = 0.25f)
      KeyStyle.EQUALS -> Color.Transparent
      else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
    }

  val backgroundModifier =
    if (style == KeyStyle.EQUALS) {
      Modifier.background(
        Brush.linearGradient(colors = listOf(extended.equalsStart, extended.equalsEnd))
      )
    } else {
      Modifier.background(bgColor)
    }

  Box(
    modifier =
      modifier
        .minimumInteractiveComponentSize()
        .height(height)
        .graphicsLayer {
          scaleX = scale
          scaleY = scale
        }
        .clip(shape)
        .then(backgroundModifier)
        .border(width = 1.dp, color = borderColor, shape = shape)
        .hoverable(interactionSource)
        .clickable(
          interactionSource = interactionSource,
          indication = ripple(color = fgColor.copy(alpha = 0.25f)),
          role = Role.Button,
          onClickLabel = contentDescription ?: label,
        ) {
          haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
          onClick()
        }
        .testTag(tag),
    contentAlignment = Alignment.Center,
  ) {
    if (icon != null) {
      Icon(
        imageVector = icon,
        contentDescription = contentDescription,
        tint = fgColor,
        modifier = Modifier.fillMaxSize(0.42f),
      )
    } else {
      Text(
        text = label,
        fontFamily =
          if (style == KeyStyle.SCIENTIFIC) SpaceGroteskFontFamily else JetBrainsMonoFontFamily,
        fontWeight =
          when (style) {
            KeyStyle.EQUALS, KeyStyle.OPERATOR -> FontWeight.Bold
            KeyStyle.SCIENTIFIC -> FontWeight.SemiBold
            else -> FontWeight.Medium
          },
        fontSize =
          when (style) {
            KeyStyle.SCIENTIFIC -> 14.sp
            KeyStyle.OPERATOR, KeyStyle.EQUALS -> 24.sp
            else -> 22.sp
          },
        color = fgColor,
      )
    }
  }
}
