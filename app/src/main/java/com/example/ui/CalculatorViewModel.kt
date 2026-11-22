package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.CalculationEntity
import com.example.data.CalculatorRepository
import com.example.data.UserPreferences
import com.example.engine.ErrorType
import com.example.engine.EvalResult
import com.example.engine.ExpressionEditor
import com.example.engine.ExpressionEvaluator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CalculatorUiState(
  val expression: String = "",
  val liveResult: String = "",
  val errorType: ErrorType? = null,
  val isJustEvaluated: Boolean = false,
  val isDarkTheme: Boolean = true,
  val isScientificMode: Boolean = false,
  val isDegreeMode: Boolean = true,
  val isHistoryOpen: Boolean = false,
  val history: List<CalculationEntity> = emptyList(),
)

private data class WorkingState(
  val expression: String = "",
  val liveResult: String = "",
  val errorType: ErrorType? = null,
  val isJustEvaluated: Boolean = false,
  val isHistoryOpen: Boolean = false,
)

class CalculatorViewModel(
  private val repository: CalculatorRepository,
) : ViewModel() {

  private val workingState = MutableStateFlow(WorkingState())
  private val prefsState = MutableStateFlow(UserPreferences())

  init {
    viewModelScope.launch {
      repository.preferences.collect { saved ->
        prefsState.value = saved
      }
    }
  }

  val uiState: StateFlow<CalculatorUiState> =
    combine(
        workingState,
        prefsState,
        repository.history.onStart { emit(emptyList()) },
      ) { work, prefs, historyList ->
        CalculatorUiState(
          expression = work.expression,
          liveResult = work.liveResult,
          errorType = work.errorType,
          isJustEvaluated = work.isJustEvaluated,
          isDarkTheme = prefs.isDarkTheme,
          isScientificMode = prefs.isScientificMode,
          isDegreeMode = prefs.isDegreeMode,
          isHistoryOpen = work.isHistoryOpen,
          history = historyList,
        )
      }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CalculatorUiState(),
      )

  fun onDigit(digit: String) {
    updateExpression(resetIfEvaluated = true) { expr ->
      ExpressionEditor.appendDigit(expr, digit)
    }
  }

  fun onDecimal() {
    updateExpression(resetIfEvaluated = true) { expr ->
      ExpressionEditor.appendDecimal(expr)
    }
  }

  fun onBinaryOperator(op: String) {
    updateExpression(continueFromResultIfEvaluated = true) { expr ->
      ExpressionEditor.appendBinaryOperator(expr, op)
    }
  }

  fun onPostfix(symbol: String) {
    updateExpression(continueFromResultIfEvaluated = true) { expr ->
      ExpressionEditor.appendPostfix(expr, symbol)
    }
  }

  fun onFunction(funcPrefix: String) {
    updateExpression(resetIfEvaluated = true) { expr ->
      ExpressionEditor.appendFunction(expr, funcPrefix)
    }
  }

  fun onConstant(constant: String) {
    updateExpression(resetIfEvaluated = true) { expr ->
      ExpressionEditor.appendConstant(expr, constant)
    }
  }

  fun onOpenParen() {
    updateExpression(resetIfEvaluated = true) { expr ->
      ExpressionEditor.appendOpenParen(expr)
    }
  }

  fun onCloseParen() {
    updateExpression(resetIfEvaluated = false) { expr ->
      ExpressionEditor.appendCloseParen(expr)
    }
  }

  fun onToggleSign() {
    updateExpression(continueFromResultIfEvaluated = true) { expr ->
      ExpressionEditor.toggleSign(expr)
    }
  }

  fun onBackspace() {
    workingState.update { state ->
      val nextExpr = ExpressionEditor.backspace(state.expression)
      val preview = computeLivePreview(nextExpr, prefsState.value.isDegreeMode)
      state.copy(
        expression = nextExpr,
        liveResult = preview.first,
        errorType = preview.second,
        isJustEvaluated = false,
      )
    }
  }

  fun onClear() {
    workingState.update { state ->
      state.copy(
        expression = "",
        liveResult = "",
        errorType = null,
        isJustEvaluated = false,
      )
    }
  }

  fun onEquals() {
    val current = workingState.value
    val expr = current.expression.trim()
    if (expr.isEmpty()) return

    when (val eval = ExpressionEvaluator.evaluate(expr, prefsState.value.isDegreeMode)) {
      is EvalResult.Success -> {
        val formatted = eval.formatted
        workingState.update {
          it.copy(
            liveResult = formatted,
            errorType = null,
            isJustEvaluated = true,
          )
        }
        if (expr != formatted) {
          viewModelScope.launch { repository.saveCalculation(expr, formatted) }
        }
      }
      is EvalResult.Error -> {
        workingState.update {
          it.copy(
            liveResult = "",
            errorType = eval.type,
            isJustEvaluated = false,
          )
        }
      }
    }
  }

  fun onToggleTheme() {
    val next = !prefsState.value.isDarkTheme
    prefsState.update { it.copy(isDarkTheme = next) }
    viewModelScope.launch { repository.setDarkTheme(next) }
  }

  fun onToggleScientific() {
    val next = !prefsState.value.isScientificMode
    prefsState.update { it.copy(isScientificMode = next) }
    viewModelScope.launch { repository.setScientificMode(next) }
  }

  fun onToggleDegreeMode() {
    val nextDegree = !prefsState.value.isDegreeMode
    prefsState.update { it.copy(isDegreeMode = nextDegree) }
    viewModelScope.launch { repository.setDegreeMode(nextDegree) }
    workingState.update { state ->
      if (state.expression.isBlank()) {
        state
      } else {
        val preview = computeLivePreview(state.expression, nextDegree)
        state.copy(
          liveResult = preview.first,
          errorType = if (state.errorType != null) preview.second else null,
        )
      }
    }
  }

  fun onToggleHistory() {
    workingState.update { it.copy(isHistoryOpen = !it.isHistoryOpen) }
  }

  fun onDismissHistory() {
    workingState.update { it.copy(isHistoryOpen = false) }
  }

  fun onReuseHistoryEntry(entry: CalculationEntity) {
    workingState.update { state ->
      val nextExpr = entry.expression
      val preview = computeLivePreview(nextExpr, prefsState.value.isDegreeMode)
      state.copy(
        expression = nextExpr,
        liveResult = preview.first.ifEmpty { entry.result },
        errorType = preview.second,
        isJustEvaluated = false,
        isHistoryOpen = false,
      )
    }
  }

  fun onDeleteHistoryEntry(id: Long) {
    viewModelScope.launch { repository.deleteCalculation(id) }
  }

  fun onClearHistory() {
    viewModelScope.launch { repository.clearHistory() }
  }

  private fun updateExpression(
    resetIfEvaluated: Boolean = false,
    continueFromResultIfEvaluated: Boolean = false,
    transform: (String) -> String,
  ) {
    workingState.update { state ->
      val baseExpr =
        when {
          state.isJustEvaluated && resetIfEvaluated -> ""
          state.isJustEvaluated && continueFromResultIfEvaluated && state.liveResult.isNotEmpty() ->
            state.liveResult
          else -> state.expression
        }
      val nextExpr = transform(baseExpr)
      val preview = computeLivePreview(nextExpr, prefsState.value.isDegreeMode)
      state.copy(
        expression = nextExpr,
        liveResult = preview.first,
        errorType = preview.second,
        isJustEvaluated = false,
      )
    }
  }

  private fun computeLivePreview(
    expression: String,
    isDegreeMode: Boolean,
  ): Pair<String, ErrorType?> {
    if (expression.isBlank()) return "" to null
    return when (val eval = ExpressionEvaluator.evaluate(expression, isDegreeMode)) {
      is EvalResult.Success -> {
        val hasCalculation =
          expression.any {
            it == '+' ||
              it == '−' ||
              it == '-' ||
              it == '×' ||
              it == '*' ||
              it == '÷' ||
              it == '/' ||
              it == '^' ||
              it == '%' ||
              it == '²' ||
              it == '(' ||
              it == 'π' ||
              it == 'e' ||
              it == '√' ||
              it.isLetter()
          }
        val previewText = if (hasCalculation) eval.formatted else ""
        previewText to null
      }
      is EvalResult.Error -> {
        val liveErr =
          when (eval.type) {
            ErrorType.DIVIDE_BY_ZERO,
            ErrorType.DOMAIN_ERROR,
            ErrorType.OVERFLOW -> eval.type
            ErrorType.INVALID_EXPRESSION -> null
          }
        "" to liveErr
      }
    }
  }

  class Factory(private val repository: CalculatorRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      return CalculatorViewModel(repository) as T
    }
  }
}
