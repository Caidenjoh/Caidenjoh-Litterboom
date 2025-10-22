package com.example.litterboom.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object CurrentUserManager {

    var currentUser by mutableStateOf<User?>(null)
        private set

    fun login(user: User) {
        currentUser = user
    }

    fun logout() {
        currentUser = null
    }

    fun isLoggedIn(): Boolean = currentUser != null

    fun isAdmin(): Boolean = currentUser?.role == "Admin"
}