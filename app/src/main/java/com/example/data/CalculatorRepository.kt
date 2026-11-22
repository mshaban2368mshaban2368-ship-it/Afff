package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.fa97DataStore: DataStore<Preferences> by preferencesDataStore(name = "fa97_prefs")

data class UserPreferences(
  val isDarkTheme: Boolean = true,
  val isScientificMode: Boolean = false,
  val isDegreeMode: Boolean = true,
)

class CalculatorRepository(
  private val calculationDao: CalculationDao,
  private val context: Context,
) {
  private val keyDarkTheme = booleanPreferencesKey("dark_theme")
  private val keyScientificMode = booleanPreferencesKey("scientific_mode")
  private val keyDegreeMode = booleanPreferencesKey("degree_mode")

  val history: Flow<List<CalculationEntity>> = calculationDao.observeHistory()

  val preferences: Flow<UserPreferences> =
    context.fa97DataStore.data.map { prefs ->
      UserPreferences(
        isDarkTheme = prefs[keyDarkTheme] ?: true,
        isScientificMode = prefs[keyScientificMode] ?: false,
        isDegreeMode = prefs[keyDegreeMode] ?: true,
      )
    }

  suspend fun saveCalculation(expression: String, result: String) {
    calculationDao.insertCalculation(
      CalculationEntity(
        expression = expression,
        result = result,
        timestamp = System.currentTimeMillis(),
      )
    )
  }

  suspend fun deleteCalculation(id: Long) {
    calculationDao.deleteById(id)
  }

  suspend fun clearHistory() {
    calculationDao.clearAll()
  }

  suspend fun setDarkTheme(isDark: Boolean) {
    context.fa97DataStore.edit { prefs -> prefs[keyDarkTheme] = isDark }
  }

  suspend fun setScientificMode(isScientific: Boolean) {
    context.fa97DataStore.edit { prefs -> prefs[keyScientificMode] = isScientific }
  }

  suspend fun setDegreeMode(isDegree: Boolean) {
    context.fa97DataStore.edit { prefs -> prefs[keyDegreeMode] = isDegree }
  }
}
