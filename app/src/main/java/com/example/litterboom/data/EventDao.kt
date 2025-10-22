package com.example.litterboom.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface EventDao {
    @Insert
    suspend fun insertEvent(event: Event)

    @Query("SELECT * FROM events ORDER BY date ASC")
    suspend fun getAllEvents(): List<Event>

    @Query("SELECT * FROM events WHERE date BETWEEN :start AND :end ORDER BY date ASC")
    suspend fun getEventsBetween(start: Long, end: Long): List<Event>

    @Query("SELECT * FROM events WHERE isOpen = 1")
    suspend fun getOpenEvents(): List<Event>

    @Update
    suspend fun updateEvent(event: Event)
}
