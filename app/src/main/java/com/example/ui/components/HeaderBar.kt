package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Functions
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.Fa97ThemeTokens
import com.example.ui.theme.SpaceGroteskFontFamily

@Composable
fun HeaderBar(
  isDarkTheme: Boolean,
  isScientificMode: Boolean,
  isHistoryOpen: Boolean,
  historyCount: Int,
  showHistoryButton: Boolean,
  onToggleScientific: () -> Unit,
  onToggleHistory: () -> Unit,
  onToggleTheme: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val extended = Fa97ThemeTokens.colors

  Row(
    modifier = modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 4.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween,
  ) {
    // Stylish FA97 logo badge
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.testTag("fa97_logo"),
    ) {
      Box(
        modifier =
          Modifier.size(36.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(
              Brush.linearGradient(
                colors = listOf(extended.equalsStart, extended.equalsEnd)
              )
            ),
        contentAlignment = Alignment.Center,
      ) {
        Text(
          text = "97",
          fontFamily = SpaceGroteskFontFamily,
          fontWeight = FontWeight.Bold,
          fontSize = 15.sp,
          color = extended.onEquals,
        )
      }
      Spacer(modifier = Modifier.width(10.dp))
      Text(
        text = "FA97",
        style = MaterialTheme.typography.headlineSmall,
        color = MaterialTheme.colorScheme.onBackground,
        letterSpacing = 1.5.sp,
      )
      Spacer(modifier = Modifier.width(6.dp))
      Surface(
        shape = RoundedCornerShape(6.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
      ) {
        Text(
          text = if (isScientificMode) stringResource(R.string.sci_label) else stringResource(R.string.std_label),
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.primary,
          modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
        )
      }
    }

    // Right controls
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      // Scientific mode toggle pill
      val sciBg by
        animateColorAsState(
          targetValue =
            if (isScientificMode) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surfaceVariant,
          label = "sci_bg",
        )
      val sciContent by
        animateColorAsState(
          targetValue =
            if (isScientificMode) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurfaceVariant,
          label = "sci_fg",
        )

      Box(
        modifier =
          Modifier.minimumInteractiveComponentSize()
            .height(40.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(sciBg)
            .border(
              width = 1.dp,
              color =
                if (isScientificMode) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
              shape = RoundedCornerShape(20.dp),
            )
            .clickable(
              role = Role.Switch,
              onClickLabel = stringResource(R.string.cd_toggle_scientific),
              onClick = onToggleScientific,
            )
            .padding(horizontal = 12.dp)
            .testTag("toggle_scientific_button"),
        contentAlignment = Alignment.Center,
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Rounded.Functions,
            contentDescription = stringResource(R.string.cd_toggle_scientific),
            tint = sciContent,
            modifier = Modifier.size(18.dp),
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = stringResource(R.string.sci_label),
            style = MaterialTheme.typography.labelLarge,
            color = sciContent,
          )
        }
      }

      // History toggle (hidden on wide two-pane where history is always visible)
      if (showHistoryButton) {
        Box(contentAlignment = Alignment.TopEnd) {
          IconButton(
            onClick = onToggleHistory,
            modifier =
              Modifier.minimumInteractiveComponentSize()
                .clip(CircleShape)
                .background(
                  if (isHistoryOpen) MaterialTheme.colorScheme.primaryContainer
                  else MaterialTheme.colorScheme.surfaceVariant
                )
                .testTag("toggle_history_button"),
          ) {
            Icon(
              imageVector = Icons.Rounded.History,
              contentDescription = stringResource(R.string.cd_toggle_history),
              tint =
                if (isHistoryOpen) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurface,
            )
          }
          if (historyCount > 0) {
            Box(
              modifier =
                Modifier.padding(top = 6.dp, end = 6.dp)
                  .size(8.dp)
                  .clip(CircleShape)
                  .background(MaterialTheme.colorScheme.primary)
            )
          }
        }
      }

      // Light / Dark theme toggle
      IconButton(
        onClick = onToggleTheme,
        modifier =
          Modifier.minimumInteractiveComponentSize()
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .testTag("toggle_theme_button"),
      ) {
        Icon(
          imageVector = if (isDarkTheme) Icons.Rounded.LightMode else Icons.Rounded.DarkMode,
          contentDescription = stringResource(R.string.cd_toggle_theme),
          tint = MaterialTheme.colorScheme.primary,
        )
      }
    }
  }
}
