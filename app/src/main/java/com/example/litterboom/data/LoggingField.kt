package com.example.litterboom.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "logging_fields")
data class LoggingField(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val fieldName: String,
    val isActive: Boolean = true
)