package com.example.litterboom.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "waste_categories")
data class WasteCategory(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val isActive: Boolean = true
)