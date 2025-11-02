package com.example.litterboom.data.api

import com.example.litterboom.data.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    // Auth
    @POST("api/auth/login")
    suspend fun login(@Body loginUser: User): AuthResponse

    // Users
    @POST("api/users")
    suspend fun createUser(@Body user: User): User

    @GET("api/users")
    suspend fun getUsers(): List<User>

    @GET("api/users/byId/{id}")
    suspend fun getUserById(@Path("id") id: Int): User

    @GET("api/users/{role}")
    suspend fun getUsersByRole(@Path("role") role: String): List<User>

    @PATCH("api/users/{id}/toggle-active")
    suspend fun toggleUserActive(@Path("id") id: Int): User

    // Events
    @GET("api/events")
    suspend fun getEvents(): List<Event>

    @GET("api/events/open")
    suspend fun getOpenEvents(): List<Event>

    @GET("api/events/range")
    suspend fun getEventsBetween(@Query("start") start: Long, @Query("end") end: Long): List<Event>

    @GET("api/events/{id}")
    suspend fun getEvent(@Path("id") id: Int): Event

    @POST("api/events")
    suspend fun createEvent(@Body event: Event): Event

    @PUT("api/events/{id}")
    suspend fun updateEvent(@Path("id") id: Int, @Body event: Event): Response<Unit>

    // Bags
    @GET("api/bags")
    suspend fun getBags(@Query("eventId") eventId: Int): List<Bag>

    @POST("api/bags")
    suspend fun postBag(@Body bag: Bag): Bag

    @GET("api/bags/approved")
    suspend fun getBagsApproved(@Query("eventId") eventId: Int): Boolean

    @POST("api/bags/approve")
    suspend fun approveBags(@Body eventId: Int): Unit

    @PUT("api/bags/{bagId}")
    suspend fun updateBag(@Path("bagId") bagId: Int, @Body bag: Bag): Response<Void>

    @DELETE("api/bags/{bagId}")
    suspend fun deleteBag(@Path("bagId") bagId: Int): Response<Void>

    // Waste Categories
    @GET("api/WasteCategories")
    suspend fun getWasteCategories(): List<WasteCategory>

    @GET("api/WasteCategories/active")
    suspend fun getActiveWasteCategories(): List<WasteCategory>

    @POST("api/WasteCategories")
    suspend fun createWasteCategory(@Body category: WasteCategory): WasteCategory

    @PUT("api/WasteCategories/{id}")
    suspend fun updateWasteCategory(@Path("id") id: Int, @Body category: WasteCategory): Response<Unit>

    // Waste SubCategories
    @GET("api/WasteSubCategories")
    suspend fun getWasteSubCategories(@Query("categoryId") categoryId: Int): List<WasteSubCategory>

    @GET("api/WasteSubCategories/active")
    suspend fun getActiveWasteSubCategories(@Query("categoryId") categoryId: Int): List<WasteSubCategory>

    @POST("api/WasteSubCategories")
    suspend fun createWasteSubCategory(@Body subCategory: WasteSubCategory): WasteSubCategory

    @PUT("api/WasteSubCategories/{id}")
    suspend fun updateWasteSubCategory(@Path("id") id: Int, @Body subCategory: WasteSubCategory): Response<Unit>

    // Logging Fields
    @GET("api/LoggingFields")
    suspend fun getLoggingFields(): List<LoggingField>

    @GET("api/LoggingFields/active")
    suspend fun getActiveLoggingFields(): List<LoggingField>

    @POST("api/LoggingFields")
    suspend fun createLoggingField(@Body field: LoggingField): LoggingField

    @PUT("api/LoggingFields/{id}")
    suspend fun updateLoggingField(@Path("id") id: Int, @Body field: LoggingField): Response<Unit>

    // SubCategory Fields (Relationships)
    @GET("api/SubCategoryFields")
    suspend fun getFieldsForSubCategory(@Query("subCategoryId") subCategoryId: Int): List<LoggingField>

    @POST("api/SubCategoryFields")
    suspend fun createSubCategoryField(@Body field: SubCategoryField): SubCategoryField

    // Logged Waste
    @GET("api/LoggedWaste")
    suspend fun getWasteForEvent(@Query("eventId") eventId: Int): List<LoggedWaste>

    @GET("api/LoggedWaste/{id}")
    suspend fun getLoggedWasteById(@Path("id") id: Int): LoggedWaste

    @POST("api/LoggedWaste")
    suspend fun createLoggedWaste(@Body loggedWaste: LoggedWaste): LoggedWaste

    @PUT("api/LoggedWaste/{id}")
    suspend fun updateLoggedWaste(@Path("id") id: Int, @Body loggedWaste: LoggedWaste): Response<Unit>

    @POST("api/LoggedWaste/{id}/photo")
    suspend fun uploadPhotoForLoggedWaste(@Path("id") id: Int, @Body base64Image: String): Response<String>

    @DELETE("api/LoggedWaste/{id}")
    suspend fun deleteLoggedWaste(@Path("id") id: Int): Response<Unit>

    // Add more endpoints as needed, etc.
}
