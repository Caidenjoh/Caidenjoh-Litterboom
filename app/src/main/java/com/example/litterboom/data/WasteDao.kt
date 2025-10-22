package com.example.litterboom.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.OnConflictStrategy
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface WasteDao {
    // --- Category Functions ---
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCategory(category: WasteCategory): Long

    @Update
    suspend fun updateCategory(category: WasteCategory)

    @Query("SELECT * FROM waste_categories ORDER BY name ASC")
    suspend fun getAllCategories(): List<WasteCategory> // For Admin

    @Query("SELECT * FROM waste_categories WHERE isActive = 1 ORDER BY name ASC")
    suspend fun getActiveCategories(): List<WasteCategory> // For Worker

    @Query("SELECT * FROM waste_categories WHERE name = :name COLLATE NOCASE LIMIT 1")
    suspend fun getCategoryByName(name: String): WasteCategory?

    // --- Sub-Category Functions ---
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSubCategory(subCategory: WasteSubCategory): Long

    @Update
    suspend fun updateSubCategory(subCategory: WasteSubCategory)

    @Query("SELECT * FROM waste_subcategories WHERE categoryId = :categoryId ORDER BY name ASC")
    suspend fun getSubCategoriesForCategory(categoryId: Int): List<WasteSubCategory> // For Admin

    @Query("SELECT * FROM waste_subcategories WHERE categoryId = :categoryId AND isActive = 1 ORDER BY name ASC")
    suspend fun getActiveSubCategoriesForCategory(categoryId: Int): List<WasteSubCategory> // For Worker

    @Query("SELECT * FROM waste_subcategories WHERE name = :name AND categoryId = :categoryId COLLATE NOCASE LIMIT 1")
    suspend fun getSubCategoryByName(name: String, categoryId: Int): WasteSubCategory?

    // --- Logging Field Functions ---
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertLoggingField(field: LoggingField): Long

    @Update
    suspend fun updateField(field: LoggingField)

    @Query("SELECT * FROM logging_fields ORDER BY fieldName ASC")
    suspend fun getAllLoggingFields(): List<LoggingField> // For Admin

    @Query("SELECT * FROM logging_fields WHERE isActive = 1 ORDER BY fieldName ASC")
    suspend fun getActiveLoggingFields(): List<LoggingField> // For Worker

    @Query("SELECT * FROM logging_fields WHERE fieldName = :name COLLATE NOCASE LIMIT 1")
    suspend fun getFieldByName(name: String): LoggingField?

    // --- Relationship Functions ---
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun assignFieldToSubCategory(join: SubCategoryField)

    @Query("SELECT lf.* FROM logging_fields lf INNER JOIN subcategory_fields scf ON lf.id = scf.fieldId WHERE scf.subCategoryId = :subCategoryId")
    suspend fun getFieldsForSubCategory(subCategoryId: Int): List<LoggingField>

    @Query("SELECT COUNT(*) FROM subcategory_fields WHERE subCategoryId = :subCategoryId AND fieldId = :fieldId")
    suspend fun isFieldAssignedToSubCategory(subCategoryId: Int, fieldId: Int): Int

    // --- Photos ---
    @Insert
    suspend fun insertPhoto(photo: ItemPhoto)

    @Query("SELECT * FROM item_photos WHERE subCategoryId = :subCategoryId ORDER BY takenAt DESC")
    fun photosFor(subCategoryId: Int): Flow<List<ItemPhoto>>
}
