package com.example.litterboom.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "logged_waste")
data class LoggedWaste(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val eventId: Int,
    val userId: Int,
    val category: String,
    val subCategory: String,
    val details: String //to map the fields dynamically according to admin
)