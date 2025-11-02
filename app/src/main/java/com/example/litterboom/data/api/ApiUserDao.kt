package com.example.litterboom.data.api

import com.example.litterboom.data.User
import com.example.litterboom.data.UserDao

class ApiUserDao : UserDao {
    private val apiService = ApiClient.apiService

    override suspend fun insertUser(user: User) {
        // Note: API may require authorization, assume it's handled (e.g., by adding auth headers later)
        apiService.createUser(user)
    }

    override suspend fun getUser(username: String, password: String): User? {
        // Not supported - login is handled separately via API auth
        throw UnsupportedOperationException("Use API login instead of this method")
    }

    override suspend fun getUsersByRole(role: String): List<User> {
        return apiService.getUsersByRole(role)
    }

    override suspend fun getAllUsers(): List<User> {
        return apiService.getUsers()
    }

    override suspend fun getUserById(id: Int): User? {
        return try {
            apiService.getUserById(id)
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun toggleUserActive(id: Int): User {
        return apiService.toggleUserActive(id)
    }
}
