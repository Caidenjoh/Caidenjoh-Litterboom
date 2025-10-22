package com.example.litterboom.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "item_photos",
    foreignKeys = [
        ForeignKey(
            entity = WasteSubCategory::class,
            parentColumns = ["id"],
            childColumns = ["subCategoryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("subCategoryId")]
)
data class ItemPhoto(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val subCategoryId: Int,
    val uri: String,
    val takenAt: Long = System.currentTimeMillis()
)
