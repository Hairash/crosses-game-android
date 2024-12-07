package com.example.crossesgame.helpers

import android.content.Context

//object PreferencesHelper {
//    private const val PREFS_NAME = "game_prefs"
//    private const val GRID_SIZE_KEY = "grid_size_key"
//    private const val DEFAULT_GRID_SIZE = 1
//
//    fun saveGridSize(context: Context, gridSize: Int) {
//        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
//        prefs.edit().putInt(GRID_SIZE_KEY, gridSize).apply()
//    }
//
//    fun loadGridSize(context: Context): Int {
//        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
//        return prefs.getInt(GRID_SIZE_KEY, DEFAULT_GRID_SIZE)
//    }
//}


object PreferencesHelper {
    private const val PREFS_NAME = "game_prefs"

    // Generic save function
    fun save(context: Context, key: String, value: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt(key, value).apply()
    }

    // Generic load function with a default value
    fun load(context: Context, key: String, defaultValue: Int): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(key, defaultValue)
    }
}
