package com.example

import com.example.engine.ErrorType
import com.example.engine.EvalResult
import com.example.engine.ExpressionEditor
import com.example.engine.ExpressionEvaluator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

  @Test
  fun `basic arithmetic and operator precedence`() {
    val res = ExpressionEvaluator.evaluate("2+3×4−6÷2")
    assertTrue(res is EvalResult.Success)
    assertEquals("11", (res as EvalResult.Success).formatted)
  }

  @Test
  fun `floating point decimal precision cleanup`() {
    val res = ExpressionEvaluator.evaluate("0.1+0.2")
    assertTrue(res is EvalResult.Success)
    assertEquals("0.3", (res as EvalResult.Success).formatted)
  }

  @Test
  fun `percentage and square postfix operators`() {
    val pct = ExpressionEvaluator.evaluate("200×15%")
    assertEquals("30", (pct as EvalResult.Success).formatted)

    val sq = ExpressionEvaluator.evaluate("5²+3²")
    assertEquals("34", (sq as EvalResult.Success).formatted)
  }

  @Test
  fun `power operator is right associative`() {
    val res = ExpressionEvaluator.evaluate("2^3^2")
    assertEquals("512", (res as EvalResult.Success).formatted)
  }

  @Test
  fun `scientific trig deg and rad modes`() {
    val sin30Deg = ExpressionEvaluator.evaluate("sin(30)", isDegreeMode = true)
    assertEquals("0.5", (sin30Deg as EvalResult.Success).formatted)

    val cos180Deg = ExpressionEvaluator.evaluate("cos(180)", isDegreeMode = true)
    assertEquals("-1", (cos180Deg as EvalResult.Success).formatted)

    val sinPiRad = ExpressionEvaluator.evaluate("sin(π÷2)", isDegreeMode = false)
    assertEquals("1", (sinPiRad as EvalResult.Success).formatted)
  }

  @Test
  fun `logarithms square root and implicit multiplication`() {
    val logRes = ExpressionEvaluator.evaluate("log(100)+ln(e)")
    assertEquals("3", (logRes as EvalResult.Success).formatted)

    val sqrtRes = ExpressionEvaluator.evaluate("2√(9)+3(4−1)")
    assertEquals("15", (sqrtRes as EvalResult.Success).formatted)
  }

  @Test
  fun `error handling for divide by zero domain and syntax`() {
    val divZero = ExpressionEvaluator.evaluate("12÷0")
    assertEquals(EvalResult.Error(ErrorType.DIVIDE_BY_ZERO), divZero)

    val negSqrt = ExpressionEvaluator.evaluate("√(−4)")
    assertEquals(EvalResult.Error(ErrorType.DOMAIN_ERROR), negSqrt)

    val tan90 = ExpressionEvaluator.evaluate("tan(90)", isDegreeMode = true)
    assertEquals(EvalResult.Error(ErrorType.DOMAIN_ERROR), tan90)

    val logZero = ExpressionEvaluator.evaluate("log(0)")
    assertEquals(EvalResult.Error(ErrorType.DOMAIN_ERROR), logZero)

    val badSyntax = ExpressionEvaluator.evaluate("12+×3")
    assertEquals(EvalResult.Error(ErrorType.INVALID_EXPRESSION), badSyntax)
  }

  @Test
  fun `expression editor smart backspace and sign toggle`() {
    assertEquals("12+", ExpressionEditor.backspace("12+sin("))
    assertEquals("−42", ExpressionEditor.toggleSign("42"))
    assertEquals("42", ExpressionEditor.toggleSign("−42"))
    assertEquals("12+(−5)", ExpressionEditor.toggleSign("12+5"))
    assertEquals("12+5", ExpressionEditor.toggleSign("12+(−5)"))
    assertEquals("0.5", ExpressionEditor.appendDecimal("0.5"))
  }
}
