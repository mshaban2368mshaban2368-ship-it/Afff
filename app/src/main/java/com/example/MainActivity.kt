package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.CalculatorRepository
import com.example.data.Fa97Database
import com.example.ui.CalculatorScreen
import com.example.ui.CalculatorViewModel
import com.example.ui.theme.Fa97Theme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge(
      statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
      navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
    )

    val database = Fa97Database.getInstance(applicationContext)
    val repository = CalculatorRepository(database.calculationDao(), applicationContext)
    val factory = CalculatorViewModel.Factory(repository)

    setContent {
      val viewModel: CalculatorViewModel = viewModel(factory = factory)
      val uiState by viewModel.uiState.collectAsStateWithLifecycle()

      LaunchedEffect(uiState.isDarkTheme) {
        if (uiState.isDarkTheme) {
          enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
          )
        } else {
          enableEdgeToEdge(
            statusBarStyle =
              SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT,
              ),
            navigationBarStyle =
              SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT,
              ),
          )
        }
      }

      Fa97Theme(darkTheme = uiState.isDarkTheme) {
        CalculatorScreen(uiState = uiState, viewModel = viewModel)
      }
    }
  }
}
