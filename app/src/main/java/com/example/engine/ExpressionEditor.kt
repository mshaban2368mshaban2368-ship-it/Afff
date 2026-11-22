package com.example.engine

/**
 * Pure helper functions for editing calculator expression strings cleanly.
 */
object ExpressionEditor {

  private val binaryOperators = setOf('+', '−', '×', '÷', '^')
  private val multiCharTokens = listOf("sin(", "cos(", "tan(", "log(", "ln(", "√(")

  fun appendDigit(expr: String, digit: String): String {
    return expr + digit
  }

  fun appendDecimal(expr: String): String {
    if (expr.isEmpty()) return "0."
    val last = expr.last()
    if (last in binaryOperators || last == '(') {
      return expr + "0."
    }
    if (last == ')' || last == 'π' || last == 'e' || last == '%' || last == '²') {
      return expr + "×0."
    }
    // Check if the current trailing number already contains a decimal point
    var idx = expr.length - 1
    while (idx >= 0 && (expr[idx].isDigit() || expr[idx] == '.')) {
      if (expr[idx] == '.') return expr
      idx--
    }
    return expr + "."
  }

  fun appendBinaryOperator(expr: String, op: String): String {
    if (expr.isEmpty()) {
      return if (op == "−") "−" else ""
    }
    val last = expr.last()
    if (last == '(') {
      return if (op == "−") expr + "−" else expr
    }
    if (last in binaryOperators) {
      // Allow replacing trailing binary operator, unless it's a lone leading minus
      if (expr.length == 1 && last == '−') return expr
      // Avoid replacing if previous was '(' before '−'
      if (expr.length >= 2 && expr[expr.length - 2] == '(' && last == '−') return expr
      return expr.dropLast(1) + op
    }
    return expr + op
  }

  fun appendPostfix(expr: String, symbol: String): String {
    if (expr.isEmpty()) return expr
    val last = expr.last()
    if (last in binaryOperators || last == '(' || last == '.') return expr
    return expr + symbol
  }

  fun appendFunction(expr: String, funcPrefix: String): String {
    return expr + funcPrefix
  }

  fun appendConstant(expr: String, constant: String): String {
    return expr + constant
  }

  fun appendOpenParen(expr: String): String {
    return expr + "("
  }

  fun appendCloseParen(expr: String): String {
    val openCount = expr.count { it == '(' }
    val closeCount = expr.count { it == ')' }
    if (openCount <= closeCount) return expr
    if (expr.isEmpty()) return expr
    val last = expr.last()
    if (last in binaryOperators || last == '(') return expr
    return expr + ")"
  }

  fun toggleSign(expr: String): String {
    if (expr.isEmpty()) return "−"
    if (expr == "−") return ""

    // Case 1: ends with "(−<digits/const>)" -> unwrap
    val negWrappedRegex = Regex("""\(−([0-9.πe]+)\)$""")
    val wrappedMatch = negWrappedRegex.find(expr)
    if (wrappedMatch != null) {
      val inner = wrappedMatch.groupValues[1]
      return expr.substring(0, wrappedMatch.range.first) + inner
    }

    // Case 2: entire expression is "−<digits/const>" -> remove leading minus
    val leadingNegRegex = Regex("""^−([0-9.πe]+)$""")
    val leadingMatch = leadingNegRegex.find(expr)
    if (leadingMatch != null) {
      return leadingMatch.groupValues[1]
    }

    // Case 3: trailing number or constant
    val trailingTokenRegex = Regex("""([0-9.]+|[πe])$""")
    val trailingMatch = trailingTokenRegex.find(expr)
    if (trailingMatch != null) {
      val token = trailingMatch.groupValues[1]
      val start = trailingMatch.range.first
      return if (start == 0) {
        "−$token"
      } else {
        expr.substring(0, start) + "(−$token)"
      }
    }

    // Case 4: ends with "(−" -> remove "(−"
    if (expr.endsWith("(−")) {
      return expr.dropLast(2)
    }

    // Default: open negative group
    return expr + "(−"
  }

  fun backspace(expr: String): String {
    if (expr.isEmpty()) return ""
    for (token in multiCharTokens) {
      if (expr.endsWith(token)) {
        return expr.dropLast(token.length)
      }
    }
    return expr.dropLast(1)
  }
}
