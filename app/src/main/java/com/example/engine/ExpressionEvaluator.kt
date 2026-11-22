package com.example.engine

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

enum class ErrorType {
  DIVIDE_BY_ZERO,
  DOMAIN_ERROR,
  INVALID_EXPRESSION,
  OVERFLOW,
}

sealed interface EvalResult {
  data class Success(val value: Double, val formatted: String) : EvalResult

  data class Error(val type: ErrorType) : EvalResult
}

private class CalcEvalException(val errorType: ErrorType) : RuntimeException()

/**
 * Pure-Kotlin recursive-descent expression parser and evaluator for FA97.
 *
 * Grammar:
 *   expr       := term (('+' | '−') term)*
 *   term       := unary (('*' | '×' | '/' | '÷') unary)*
 *   unary      := ('+' | '−') unary | power
 *   power      := postfix ('^' unary)?     // right-associative; allows 2^-3
 *   postfix    := primary ('%' | '²')*
 *   primary    := NUMBER | 'π' | 'e' | FUNC '(' expr ')' | '√' primary | '(' expr ')'
 */
object ExpressionEvaluator {

  private sealed interface Token {
    data class Num(val value: Double) : Token

    data class Op(val symbol: Char) : Token // +, -, *, /, ^, %, ²

    data class Func(val name: String) : Token // sin, cos, tan, log, ln, sqrt

    data object LParen : Token

    data object RParen : Token

    data object Pi : Token

    data object Euler : Token
  }

  fun evaluate(expression: String, isDegreeMode: Boolean = true): EvalResult {
    val trimmed = expression.trim()
    if (trimmed.isEmpty()) {
      return EvalResult.Error(ErrorType.INVALID_EXPRESSION)
    }
    return try {
      val rawTokens = tokenize(trimmed)
      if (rawTokens.isEmpty()) {
        return EvalResult.Error(ErrorType.INVALID_EXPRESSION)
      }
      val tokens = closeUnmatchedParens(insertImplicitMultiplication(rawTokens))
      val parser = Parser(tokens, isDegreeMode)
      val result = parser.parseExpression()
      if (!parser.isAtEnd()) {
        throw CalcEvalException(ErrorType.INVALID_EXPRESSION)
      }
      if (result.isInfinite()) {
        throw CalcEvalException(ErrorType.OVERFLOW)
      }
      if (result.isNaN()) {
        throw CalcEvalException(ErrorType.DOMAIN_ERROR)
      }
      val cleaned = if (abs(result) < 1e-13) 0.0 else result
      EvalResult.Success(cleaned, formatNumber(cleaned))
    } catch (e: CalcEvalException) {
      EvalResult.Error(e.errorType)
    } catch (_: Exception) {
      EvalResult.Error(ErrorType.INVALID_EXPRESSION)
    }
  }

  /**
   * Formats a Double cleanly without IEEE-754 noise (e.g. 0.1 + 0.2 -> "0.3").
   */
  fun formatNumber(value: Double): String {
    if (value == 0.0) return "0"
    if (value.isInfinite()) return "∞"
    if (value.isNaN()) return "NaN"

    val absVal = abs(value)
    val bd = BigDecimal.valueOf(value).round(MathContext(12, RoundingMode.HALF_UP))
    val stripped = bd.stripTrailingZeros()

    return if (absVal >= 1e12 || (absVal > 0.0 && absVal < 1e-8)) {
      val sci = BigDecimal.valueOf(value).round(MathContext(8, RoundingMode.HALF_UP)).stripTrailingZeros()
      sci.toString().replace("E+", "E")
    } else {
      stripped.toPlainString()
    }
  }

  private fun tokenize(input: String): List<Token> {
    val tokens = mutableListOf<Token>()
    var i = 0
    val len = input.length

    while (i < len) {
      val ch = input[i]
      when {
        ch.isWhitespace() -> i++
        ch.isDigit() || ch == '.' -> {
          val start = i
          var hasDot = false
          while (i < len && (input[i].isDigit() || (input[i] == '.' && !hasDot))) {
            if (input[i] == '.') hasDot = true
            i++
          }
          // Support scientific notation with uppercase 'E' (e.g. 1.5E12 or 1.5E-8)
          if (i < len && input[i] == 'E') {
            val expStart = i
            i++
            if (i < len && (input[i] == '+' || input[i] == '-' || input[i] == '−')) {
              i++
            }
            val digitStart = i
            while (i < len && input[i].isDigit()) {
              i++
            }
            if (i == digitStart) {
              // Not a valid exponent; roll back
              i = expStart
            }
          }
          val numStr = input.substring(start, i).replace('−', '-')
          if (numStr == ".") throw CalcEvalException(ErrorType.INVALID_EXPRESSION)
          val num = numStr.toDoubleOrNull() ?: throw CalcEvalException(ErrorType.INVALID_EXPRESSION)
          tokens.add(Token.Num(num))
        }
        ch == '+' -> {
          tokens.add(Token.Op('+'))
          i++
        }
        ch == '-' || ch == '−' -> {
          tokens.add(Token.Op('-'))
          i++
        }
        ch == '*' || ch == '×' -> {
          tokens.add(Token.Op('*'))
          i++
        }
        ch == '/' || ch == '÷' -> {
          tokens.add(Token.Op('/'))
          i++
        }
        ch == '^' -> {
          tokens.add(Token.Op('^'))
          i++
        }
        ch == '%' -> {
          tokens.add(Token.Op('%'))
          i++
        }
        ch == '²' -> {
          tokens.add(Token.Op('²'))
          i++
        }
        ch == '(' -> {
          tokens.add(Token.LParen)
          i++
        }
        ch == ')' -> {
          tokens.add(Token.RParen)
          i++
        }
        ch == 'π' -> {
          tokens.add(Token.Pi)
          i++
        }
        ch == 'e' -> {
          tokens.add(Token.Euler)
          i++
        }
        ch == '√' -> {
          tokens.add(Token.Func("sqrt"))
          i++
        }
        ch.isLetter() -> {
          val start = i
          while (i < len && input[i].isLetter()) {
            i++
          }
          when (val word = input.substring(start, i).lowercase()) {
            "sin", "cos", "tan", "log", "ln", "sqrt" -> tokens.add(Token.Func(word))
            "pi" -> tokens.add(Token.Pi)
            else -> throw CalcEvalException(ErrorType.INVALID_EXPRESSION)
          }
        }
        else -> throw CalcEvalException(ErrorType.INVALID_EXPRESSION)
      }
    }
    return tokens
  }

  private fun insertImplicitMultiplication(tokens: List<Token>): List<Token> {
    if (tokens.size < 2) return tokens
    val result = mutableListOf<Token>()
    for (idx in tokens.indices) {
      val curr = tokens[idx]
      if (idx > 0) {
        val prev = tokens[idx - 1]
        val prevEndsValue =
          prev is Token.Num ||
            prev is Token.RParen ||
            prev is Token.Pi ||
            prev is Token.Euler ||
            (prev is Token.Op && (prev.symbol == '%' || prev.symbol == '²'))

        val currStartsValue =
          curr is Token.LParen ||
            curr is Token.Func ||
            curr is Token.Pi ||
            curr is Token.Euler ||
            (curr is Token.Num && prev !is Token.Num)

        if (prevEndsValue && currStartsValue) {
          result.add(Token.Op('*'))
        }
      }
      result.add(curr)
    }
    return result
  }

  private fun closeUnmatchedParens(tokens: List<Token>): List<Token> {
    var openCount = 0
    for (t in tokens) {
      when (t) {
        Token.LParen -> openCount++
        Token.RParen -> openCount--
        else -> Unit
      }
    }
    if (openCount < 0) throw CalcEvalException(ErrorType.INVALID_EXPRESSION)
    if (openCount == 0) return tokens
    return tokens + List(openCount) { Token.RParen }
  }

  private class Parser(
    private val tokens: List<Token>,
    private val isDegreeMode: Boolean,
  ) {
    private var pos = 0

    fun isAtEnd(): Boolean = pos >= tokens.size

    private fun peek(): Token? = tokens.getOrNull(pos)

    private fun consume(): Token = tokens[pos++]

    fun parseExpression(): Double {
      var value = parseTerm()
      while (true) {
        val next = peek()
        if (next is Token.Op && (next.symbol == '+' || next.symbol == '-')) {
          consume()
          val right = parseTerm()
          value = if (next.symbol == '+') value + right else value - right
        } else {
          break
        }
      }
      return value
    }

    private fun parseTerm(): Double {
      var value = parseUnary()
      while (true) {
        val next = peek()
        if (next is Token.Op && (next.symbol == '*' || next.symbol == '/')) {
          consume()
          val right = parseUnary()
          if (next.symbol == '*') {
            value *= right
          } else {
            if (abs(right) == 0.0) {
              throw CalcEvalException(ErrorType.DIVIDE_BY_ZERO)
            }
            value /= right
          }
        } else {
          break
        }
      }
      return value
    }

    private fun parseUnary(): Double {
      val next = peek()
      if (next is Token.Op && (next.symbol == '+' || next.symbol == '-')) {
        consume()
        val operand = parseUnary()
        return if (next.symbol == '-') -operand else operand
      }
      return parsePower()
    }

    private fun parsePower(): Double {
      val base = parsePostfix()
      val next = peek()
      if (next is Token.Op && next.symbol == '^') {
        consume()
        val exponent = parseUnary()
        if (base == 0.0 && exponent < 0.0) {
          throw CalcEvalException(ErrorType.DIVIDE_BY_ZERO)
        }
        val powered = base.pow(exponent)
        if (powered.isNaN()) throw CalcEvalException(ErrorType.DOMAIN_ERROR)
        if (powered.isInfinite()) throw CalcEvalException(ErrorType.OVERFLOW)
        return powered
      }
      return base
    }

    private fun parsePostfix(): Double {
      var value = parsePrimary()
      while (true) {
        val next = peek()
        if (next is Token.Op && (next.symbol == '%' || next.symbol == '²')) {
          consume()
          value =
            if (next.symbol == '%') {
              value / 100.0
            } else {
              value * value
            }
        } else {
          break
        }
      }
      return value
    }

    private fun parsePrimary(): Double {
      val token = peek() ?: throw CalcEvalException(ErrorType.INVALID_EXPRESSION)
      return when (token) {
        is Token.Num -> {
          consume()
          token.value
        }
        Token.Pi -> {
          consume()
          Math.PI
        }
        Token.Euler -> {
          consume()
          Math.E
        }
        Token.LParen -> {
          consume()
          if (peek() == Token.RParen) {
            throw CalcEvalException(ErrorType.INVALID_EXPRESSION)
          }
          val inner = parseExpression()
          if (peek() != Token.RParen) {
            throw CalcEvalException(ErrorType.INVALID_EXPRESSION)
          }
          consume()
          inner
        }
        is Token.Func -> {
          consume()
          val arg =
            if (peek() == Token.LParen) {
              consume()
              if (peek() == Token.RParen) {
                throw CalcEvalException(ErrorType.INVALID_EXPRESSION)
              }
              val inner = parseExpression()
              if (peek() != Token.RParen) {
                throw CalcEvalException(ErrorType.INVALID_EXPRESSION)
              }
              consume()
              inner
            } else if (token.name == "sqrt") {
              // Allow √9 without parentheses
              parseUnary()
            } else {
              throw CalcEvalException(ErrorType.INVALID_EXPRESSION)
            }
          evaluateFunction(token.name, arg)
        }
        else -> throw CalcEvalException(ErrorType.INVALID_EXPRESSION)
      }
    }

    private fun evaluateFunction(name: String, arg: Double): Double {
      return when (name) {
        "sin" -> {
          val rad = if (isDegreeMode) Math.toRadians(arg) else arg
          val res = sin(rad)
          if (abs(res) < 1e-13) 0.0 else res
        }
        "cos" -> {
          val rad = if (isDegreeMode) Math.toRadians(arg) else arg
          val res = cos(rad)
          if (abs(res) < 1e-13) 0.0 else res
        }
        "tan" -> {
          val rad = if (isDegreeMode) Math.toRadians(arg) else arg
          val cosVal = cos(rad)
          if (abs(cosVal) < 1e-12) {
            throw CalcEvalException(ErrorType.DOMAIN_ERROR)
          }
          val res = tan(rad)
          if (abs(res) < 1e-13) 0.0 else res
        }
        "log" -> {
          if (arg <= 0.0) throw CalcEvalException(ErrorType.DOMAIN_ERROR)
          log10(arg)
        }
        "ln" -> {
          if (arg <= 0.0) throw CalcEvalException(ErrorType.DOMAIN_ERROR)
          ln(arg)
        }
        "sqrt" -> {
          if (arg < 0.0) throw CalcEvalException(ErrorType.DOMAIN_ERROR)
          sqrt(arg)
        }
        else -> throw CalcEvalException(ErrorType.INVALID_EXPRESSION)
      }
    }
  }
}
