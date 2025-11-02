package com.example.litterboom.data

data class User(
    val id: Int = 0,              // Kept for compatibility (not used by Firebase)
    val username: String,         // Treat as email for Firebase Auth
    val password: String,         // Only used when creating/signing-in (not stored in Firestore)
    val role: String = "User",
    val isActive: Boolean = true
)
