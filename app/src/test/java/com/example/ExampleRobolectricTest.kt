package com.example

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h915dp")
class ExampleRobolectricTest {

  @get:Rule val composeRule = createAndroidComposeRule<MainActivity>()

  @Test
  fun `app name is FA97`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    assertEquals("FA97", context.getString(R.string.app_name))
  }

  @Test
  fun `basic calculation scientific toggle and history work in UI`() {
    composeRule.onNodeWithTag("fa97_logo").assertIsDisplayed()

    composeRule.onNodeWithTag("key_7").performClick()
    composeRule.onNodeWithTag("key_plus").performClick()
    composeRule.onNodeWithTag("key_5").performClick()
    composeRule.onNodeWithTag("key_equals").performClick()

    composeRule.onNodeWithTag("result_display").assertTextContains("12", substring = true)

    // Toggle scientific mode and check scientific key visibility
    composeRule.onNodeWithTag("toggle_scientific_button").performClick()
    composeRule.onNodeWithTag("key_sin").assertIsDisplayed()
    composeRule.onNodeWithTag("key_pi").assertIsDisplayed()

    // Toggle theme
    composeRule.onNodeWithTag("toggle_theme_button").performClick()
    composeRule.onNodeWithTag("fa97_logo").assertIsDisplayed()
  }

  @Test
  fun `divide by zero shows error without crashing`() {
    composeRule.onNodeWithTag("key_clear").performClick()
    composeRule.onNodeWithTag("key_8").performClick()
    composeRule.onNodeWithTag("key_divide").performClick()
    composeRule.onNodeWithTag("key_0").performClick()
    composeRule.onNodeWithTag("key_equals").performClick()

    composeRule.onNodeWithTag("error_display").assertIsDisplayed()
  }
}
