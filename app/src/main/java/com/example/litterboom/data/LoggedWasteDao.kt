package com.example.litterboom.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface LoggedWasteDao {
    @Insert
    suspend fun insertLoggedWaste(item: LoggedWaste):Long

    @Update
    suspend fun updateLoggedWaste(item: LoggedWaste)

    @Delete
    suspend fun deleteLoggedWaste(item: LoggedWaste)

    @Query("SELECT * FROM logged_waste WHERE eventId = :eventId ORDER BY id DESC") // Order by newest first
    suspend fun getWasteForEvent(eventId: Int): List<LoggedWaste>

    @Query("SELECT * FROM logged_waste WHERE id = :id")
    suspend fun getLoggedWasteById(id: Int): LoggedWaste?
}