package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.key.utf16CodePoint
import androidx.compose.ui.unit.dp
import com.example.ui.components.CalculatorKeypad
import com.example.ui.components.DisplayPanel
import com.example.ui.components.HeaderBar
import com.example.ui.components.HistoryPanelContent
import com.example.ui.components.KeypadCallbacks

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(
  uiState: CalculatorUiState,
  viewModel: CalculatorViewModel,
  modifier: Modifier = Modifier,
) {
  val focusRequester = remember { FocusRequester() }
  LaunchedEffect(Unit) { focusRequester.requestFocus() }

  val keypadCallbacks =
    remember(viewModel) {
      KeypadCallbacks(
        onDigit = viewModel::onDigit,
        onDecimal = viewModel::onDecimal,
        onOperator = viewModel::onBinaryOperator,
        onPostfix = viewModel::onPostfix,
        onFunction = viewModel::onFunction,
        onConstant = viewModel::onConstant,
        onOpenParen = viewModel::onOpenParen,
        onCloseParen = viewModel::onCloseParen,
        onToggleSign = viewModel::onToggleSign,
        onClear = viewModel::onClear,
        onBackspace = viewModel::onBackspace,
        onEquals = viewModel::onEquals,
      )
    }

  BoxWithConstraints(
    modifier =
      modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)
        .windowInsetsPadding(WindowInsets.safeDrawing)
        .focusRequester(focusRequester)
        .focusable()
        .onPreviewKeyEvent { event -> handleKeyboardEvent(event, viewModel) }
  ) {
    val isWideScreen = maxWidth >= 720.dp
    val isCompactHeight = maxHeight < 680.dp

    if (isWideScreen) {
      // Expanded / Tablet two-pane layout: Calculator + Persistent History Sidebar
      Row(
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Box(
          modifier = Modifier.weight(1f).fillMaxHeight(),
          contentAlignment = Alignment.Center,
        ) {
          CalculatorMainColumn(
            uiState = uiState,
            viewModel = viewModel,
            callbacks = keypadCallbacks,
            showHistoryButton = false,
            compactHeight = isCompactHeight,
            modifier = Modifier.widthIn(max = 520.dp).fillMaxHeight(),
          )
        }

        Box(
          modifier =
            Modifier.width(320.dp)
              .fillMaxHeight()
              .clip(RoundedCornerShape(24.dp))
              .background(MaterialTheme.colorScheme.surface)
              .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                shape = RoundedCornerShape(24.dp),
              )
        ) {
          HistoryPanelContent(
            history = uiState.history,
            onReuseEntry = viewModel::onReuseHistoryEntry,
            onDeleteEntry = viewModel::onDeleteHistoryEntry,
            onClearHistory = viewModel::onClearHistory,
            onClose = null,
          )
        }
      }
    } else {
      // Mobile-first vertical layout
      Box(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.TopCenter,
      ) {
        CalculatorMainColumn(
          uiState = uiState,
          viewModel = viewModel,
          callbacks = keypadCallbacks,
          showHistoryButton = true,
          compactHeight = isCompactHeight,
          modifier = Modifier.widthIn(max = 520.dp).fillMaxHeight(),
        )
      }

      if (uiState.isHistoryOpen) {
        BackHandler { viewModel.onDismissHistory() }
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
          onDismissRequest = viewModel::onDismissHistory,
          sheetState = sheetState,
          containerColor = MaterialTheme.colorScheme.surface,
          contentColor = MaterialTheme.colorScheme.onSurface,
        ) {
          Box(modifier = Modifier.fillMaxWidth().height(460.dp)) {
            HistoryPanelContent(
              history = uiState.history,
              onReuseEntry = viewModel::onReuseHistoryEntry,
              onDeleteEntry = viewModel::onDeleteHistoryEntry,
              onClearHistory = viewModel::onClearHistory,
              onClose = viewModel::onDismissHistory,
            )
          }
        }
      }
    }
  }
}

@Composable
private fun CalculatorMainColumn(
  uiState: CalculatorUiState,
  viewModel: CalculatorViewModel,
  callbacks: KeypadCallbacks,
  showHistoryButton: Boolean,
  compactHeight: Boolean,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier.verticalScroll(rememberScrollState()),
    verticalArrangement = Arrangement.SpaceBetween,
  ) {
    Column(modifier = Modifier.fillMaxWidth()) {
      HeaderBar(
        isDarkTheme = uiState.isDarkTheme,
        isScientificMode = uiState.isScientificMode,
        isHistoryOpen = uiState.isHistoryOpen,
        historyCount = uiState.history.size,
        showHistoryButton = showHistoryButton,
        onToggleScientific = viewModel::onToggleScientific,
        onToggleHistory = viewModel::onToggleHistory,
        onToggleTheme = viewModel::onToggleTheme,
      )

      Spacer(modifier = Modifier.height(if (compactHeight) 8.dp else 14.dp))

      DisplayPanel(
        expression = uiState.expression,
        liveResult = uiState.liveResult,
        errorType = uiState.errorType,
        isJustEvaluated = uiState.isJustEvaluated,
        isScientificMode = uiState.isScientificMode,
        isDegreeMode = uiState.isDegreeMode,
        onToggleDegreeMode = viewModel::onToggleDegreeMode,
      )
    }

    Spacer(modifier = Modifier.height(if (compactHeight) 10.dp else 16.dp))

    CalculatorKeypad(
      isScientificMode = uiState.isScientificMode,
      callbacks = callbacks,
      compactHeight = compactHeight,
    )
  }
}

private fun handleKeyboardEvent(
  event: KeyEvent,
  viewModel: CalculatorViewModel,
): Boolean {
  if (event.type != KeyEventType.KeyDown) return false

  // Handle special keys first
  when (event.key) {
    Key.Enter, Key.NumPadEnter -> {
      viewModel.onEquals()
      return true
    }
    Key.Escape, Key.Delete -> {
      viewModel.onClear()
      return true
    }
    Key.Backspace -> {
      viewModel.onBackspace()
      return true
    }
    Key.NumPadAdd -> {
      viewModel.onBinaryOperator("+")
      return true
    }
    Key.NumPadSubtract -> {
      viewModel.onBinaryOperator("−")
      return true
    }
    Key.NumPadMultiply -> {
      viewModel.onBinaryOperator("×")
      return true
    }
    Key.NumPadDivide -> {
      viewModel.onBinaryOperator("÷")
      return true
    }
    Key.NumPadDot -> {
      viewModel.onDecimal()
      return true
    }
    Key.NumPadEquals -> {
      viewModel.onEquals()
      return true
    }
    else -> Unit
  }

  // Handle printable UTF-16 characters (covers Shift combinations like +, *, (, ), %, ^)
  val codePoint = event.utf16CodePoint
  if (codePoint != 0) {
    when (val ch = codePoint.toChar()) {
      in '0'..'9' -> {
        viewModel.onDigit(ch.toString())
        return true
      }
      '.' -> {
        viewModel.onDecimal()
        return true
      }
      '+' -> {
        viewModel.onBinaryOperator("+")
        return true
      }
      '-', '−' -> {
        viewModel.onBinaryOperator("−")
        return true
      }
      '*', '×' -> {
        viewModel.onBinaryOperator("×")
        return true
      }
      '/', '÷' -> {
        viewModel.onBinaryOperator("÷")
        return true
      }
      '^' -> {
        viewModel.onBinaryOperator("^")
        return true
      }
      '%' -> {
        viewModel.onPostfix("%")
        return true
      }
      '(' -> {
        viewModel.onOpenParen()
        return true
      }
      ')' -> {
        viewModel.onCloseParen()
        return true
      }
      '=' -> {
        viewModel.onEquals()
        return true
      }
      'p', 'P' -> {
        viewModel.onConstant("π")
        return true
      }
      'e', 'E' -> {
        viewModel.onConstant("e")
        return true
      }
      's', 'S' -> {
        viewModel.onFunction("sin(")
        return true
      }
      'c', 'C' -> {
        viewModel.onFunction("cos(")
        return true
      }
      't', 'T' -> {
        viewModel.onFunction("tan(")
        return true
      }
      'l', 'L' -> {
        viewModel.onFunction("ln(")
        return true
      }
      'r', 'R' -> {
        viewModel.onFunction("√(")
        return true
      }
    }
  }

  return false
}
