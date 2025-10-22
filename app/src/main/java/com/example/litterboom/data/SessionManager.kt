package com.example.litterboom.data

import android.content.Context

object SessionManager {

    private const val PREFERENCES_FILE = "com.example.litterboom.PREFERENCES"
    private const val USER_ID_KEY = "logged_in_user_id"

    fun saveUserSession(context: Context, user: User) {
        val prefs = context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE)
        with(prefs.edit()) {
            putInt(USER_ID_KEY, user.id)
            apply()
        }
    }

    fun clearSession(context: Context) {
        val prefs = context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE)
        with(prefs.edit()) {
            remove(USER_ID_KEY)
            apply()
        }
    }

    fun getSavedUserId(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE)
        return prefs.getInt(USER_ID_KEY, -1)
    }
}