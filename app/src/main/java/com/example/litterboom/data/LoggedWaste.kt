package com.example.litterboom.data

data class LoggedWaste(
    val id: Int = 0,
    val eventId: Int,
    val userId: Int,
    val category: String,
    val subCategory: String,
    val details: String, // JSON string
    val photoUrl: String = "",
    val userName: String = ""
)
