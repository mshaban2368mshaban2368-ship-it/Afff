package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [CalculationEntity::class], version = 1, exportSchema = false)
abstract class Fa97Database : RoomDatabase() {
  abstract fun calculationDao(): CalculationDao

  companion object {
    @Volatile private var INSTANCE: Fa97Database? = null

    fun getInstance(context: Context): Fa97Database {
      return INSTANCE
        ?: synchronized(this) {
          INSTANCE
            ?: Room.databaseBuilder(
                context.applicationContext,
                Fa97Database::class.java,
                "fa97_calculator_db",
              )
              .build()
              .also { INSTANCE = it }
        }
    }
  }
}
