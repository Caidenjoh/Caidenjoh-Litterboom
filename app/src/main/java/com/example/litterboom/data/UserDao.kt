package com.example.litterboom.data

interface UserDao {
    suspend fun insertUser(user: User)
    suspend fun getUser(username: String, password: String): User?
    suspend fun getUsersByRole(role: String): List<User>
    suspend fun getAllUsers(): List<User>
    suspend fun getUserById(id: Int): User?
    suspend fun toggleUserActive(id: Int): User
}
