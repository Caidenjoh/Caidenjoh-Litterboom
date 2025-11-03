package com.example.litterboom.data.api

import com.example.litterboom.data.*
import okhttp3.ResponseBody

class ApiLoggedWasteDao : LoggedWasteDao {
    override suspend fun insertLoggedWaste(item: LoggedWaste): Long {
        val created = ApiClient.apiService.createLoggedWaste(item)
        return created.id.toLong()
    }
    override suspend fun updateLoggedWaste(item: LoggedWaste) {
        val response = ApiClient.apiService.updateLoggedWaste(item.id, item)
        if (!response.isSuccessful) {
            throw Exception("Failed to update logged waste: ${response.message()}")
        }
    }
    override suspend fun deleteLoggedWaste(item: LoggedWaste) {
        val response = ApiClient.apiService.deleteLoggedWaste(item.id)
        if (!response.isSuccessful) {
            throw Exception("Failed to delete logged waste: ${response.message()}")
        }
    }

    override suspend fun uploadPhotoForLoggedWaste(wasteId: Int, base64Image: String): retrofit2.Response<ResponseBody> {
        return ApiClient.apiService.uploadPhotoForLoggedWaste(wasteId, base64Image)
    }

    override suspend fun getWasteForEvent(eventId: Int): List<LoggedWaste> = ApiClient.apiService.getWasteForEvent(eventId)

    override suspend fun getLoggedWasteById(id: Int): LoggedWaste? = ApiClient.apiService.getLoggedWasteById(id)
}
