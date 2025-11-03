package com.example.litterboom.data

import okhttp3.ResponseBody

interface LoggedWasteDao {
    suspend fun insertLoggedWaste(item: LoggedWaste): Long
    suspend fun updateLoggedWaste(item: LoggedWaste)
    suspend fun deleteLoggedWaste(item: LoggedWaste)
    suspend fun uploadPhotoForLoggedWaste(wasteId: Int, base64Image: String): retrofit2.Response<ResponseBody>
    suspend fun getWasteForEvent(eventId: Int): List<LoggedWaste>
    suspend fun getLoggedWasteById(id: Int): LoggedWaste?
}
