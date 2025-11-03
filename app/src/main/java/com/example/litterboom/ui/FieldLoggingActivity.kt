package com.example.litterboom.ui

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.example.litterboom.data.AppDatabase
import com.example.litterboom.data.ItemPhoto
import com.example.litterboom.data.LoggingField
import com.example.litterboom.ui.logging.PhotoForLoggedWasteSection
import com.example.litterboom.ui.theme.DarkJungleGreen
import com.example.litterboom.ui.theme.LightTeal
import com.example.litterboom.ui.theme.LitterboomTheme
import kotlinx.coroutines.launch
import org.json.JSONObject
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import java.util.Locale
import android.util.Base64
import java.io.ByteArrayOutputStream
import android.graphics.BitmapFactory
import java.io.File
import java.io.FileOutputStream



/**
 * Formats a given string to Title Case.
 * Each word in the input string is transformed so that its first letter is uppercase
 * and the remaining letters are lowercase.
 *
 * Example: "hello world" becomes "Hello World".
 * @param input The string to be formatted.
 * @return The Title Cased version of the input string.
 */
private fun formatToTitleCase(input: String): String {
    return input.split(" ").joinToString(" ") { word ->
        if (word.isNotEmpty()) {
            // Capitalise first letter, lowercase the rest
            word.first().uppercase() + word.drop(1).lowercase()
        } else {
            "" // Handle potential multiple spaces
        }
    }
}


/**
 * FieldLoggingActivity is an Android Activity responsible for displaying a screen
 * where users can log specific details for a selected waste sub-category.
 * It dynamically generates input fields based on the requirements for that sub-category,
 * fetched from the database. It also handles capturing and associating photos with the item.
 * The activity can operate in two modes: creating a new log entry or editing an existing one.
 *
 */
class FieldLoggingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val subCategoryId = intent.getIntExtra("SUB_CATEGORY_ID", -1)
        val subCategoryName = intent.getStringExtra("SUB_CATEGORY_NAME") ?: "Item"
        val mainCategoryName = intent.getStringExtra("MAIN_CATEGORY_NAME") ?: "Waste"
        val loggedWasteId = intent.getIntExtra("LOGGED_WASTE_ID", -1)

        setContent {
            LitterboomTheme {
                FieldLoggingScreen(subCategoryId, subCategoryName, mainCategoryName, loggedWasteId)
            }
        }
    }
}

/**
 * A composable function that provides the UI for logging or editing details of a waste item.
 * It displays a dynamic form based on the fields required for the given sub-category,
 * handles user input with appropriate validation and formatting, and allows photo capture.
 * @param subCategoryId The ID of the waste sub-category being logged.
 * @param subCategoryName The name of the waste sub-category, used for display purposes.
 * @param mainCategoryName The name of the main waste category.
 * @param loggedWasteId The ID of the logged waste item if in edit mode; otherwise, -1. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FieldLoggingScreen(subCategoryId: Int, subCategoryName: String, mainCategoryName: String, loggedWasteId: Int) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val db = remember { AppDatabase.getDatabase(context) }

    var requiredFields by remember { mutableStateOf<List<LoggingField>>(emptyList()) }
    val fieldInputValues = remember { mutableStateMapOf<Int, String>() }
    val isEditMode = loggedWasteId != -1
    var currentPhotoUrl by remember { mutableStateOf<String?>(null) }
    var capturedPhotoUri by remember { mutableStateOf<android.net.Uri?>(null) }

    // Camera launcher - now uses TakePicturePreview for reliable bitmap capture
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            // Save bitmap to internal storage and create URI
            scope.launch {
                try {
                    val fileName = "waste_${System.currentTimeMillis()}.jpg"
                    val file = File(context.cacheDir, fileName)
                    FileOutputStream(file).use { out ->
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out) // Increased quality from 90 to 95
                    }
                    capturedPhotoUri = Uri.fromFile(file)
                    // Clear current photo URL when new photo is taken (important for edit mode)
                    currentPhotoUrl = null
                    Toast.makeText(context, "Photo captured successfully", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Failed to save photo: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(context, "Photo capture cancelled", Toast.LENGTH_SHORT).show()
        }
    }

    // Camera permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            // Camera permission granted, launch camera
            cameraLauncher.launch(null)
        } else {
            Toast.makeText(context, "Camera permission is required", Toast.LENGTH_SHORT).show()
        }
    }

    fun requestCamera() {
        val cameraGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (cameraGranted) {
            // Permission already granted, launch camera directly
            cameraLauncher.launch(null)
        } else {
            // Request camera permission
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    LaunchedEffect(subCategoryId) {
        // Fetch required fields from the database when the subCategoryId changes.
        if (subCategoryId != -1) {
            val db = AppDatabase.getDatabase(context)
            requiredFields = db.wasteDao().getFieldsForSubCategory(subCategoryId)

            if (isEditMode && loggedWasteId != -1) {
                // If we are editing, load the existing data
                val existingItem = db.loggedWasteDao().getLoggedWasteById(loggedWasteId)
                if (existingItem != null) {
                    val detailsJson = JSONObject(existingItem.details)
                    requiredFields.forEach { field ->
                        fieldInputValues[field.id] = detailsJson.optString(field.fieldName, "")
                    }
                    currentPhotoUrl = existingItem.photoUrl.takeIf { !it.isNullOrEmpty() }
                }
            } else {
                // Otherwise, initialise with empty values for a new entry.
                requiredFields.forEach { field -> fieldInputValues[field.id] = "" }
            }
        }
    }

    val gradient = Brush.verticalGradient(listOf(LightTeal, DarkJungleGreen, LightTeal))

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp)
    ) {
        Column(Modifier.fillMaxSize()) {
            // Top app bar with back button and title
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { (context as? Activity)?.finish() }) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Spacer(Modifier.width(16.dp))
                Text(
                    text = "Log Details for $subCategoryName",
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.White
                )
            }

            Spacer(Modifier.height(16.dp))

            LazyColumn(modifier = Modifier.weight(1f)) {

                items(requiredFields) { field ->
                    // Determine field type for specific keyboard and validation logic.
                    val isWeightField = field.fieldName.contains("weight", ignoreCase = true) ||
                            field.fieldName.contains("kg", ignoreCase = true)
                    val isPiecesField = field.fieldName.equals("Pieces", ignoreCase = true)
                    val isNumericField = isWeightField || isPiecesField

                    val keyboardType = when {
                        isWeightField -> KeyboardType.Decimal
                        isPiecesField -> KeyboardType.Number // Use Number for integers
                        else -> KeyboardType.Text
                    }

                    val decimalRegex = remember { Regex("^\\d*\\.?\\d*\$") }
                    val integerRegex = remember { Regex("^\\d*\$") } // Regex for integers only

                    OutlinedTextField(
                        value = fieldInputValues[field.id] ?: "",
                        onValueChange = { newValue ->
                            when {
                                isWeightField -> {
                                    // Allow valid decimal input
                                    if (newValue.isEmpty() || newValue.matches(decimalRegex)) {
                                        fieldInputValues[field.id] = newValue
                                    }
                                }

                                isPiecesField -> {
                                    // Allow valid integer input
                                    if (newValue.isEmpty() || newValue.matches(integerRegex)) {
                                        fieldInputValues[field.id] = newValue
                                    }
                                }

                                else -> {
                                    // Apply auto-formatting for text fields
                                    fieldInputValues[field.id] = formatToTitleCase(newValue)
                                }
                            }
                        },
                        label = {
                            Text(
                                field.fieldName,
                                style = MaterialTheme.typography.labelLarge
                            )
                        },
                        textStyle = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),


                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),


                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White.copy(alpha = 0.98f),
                            unfocusedContainerColor = Color.White.copy(alpha = 0.95f),
                            disabledContainerColor = Color.White.copy(alpha = 0.95f)
                        )
                    )
                }

                // 2) photos after fields
                item {
                    Spacer(Modifier.height(8.dp))
                    Card(
                        Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.98f))
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            PhotoForLoggedWasteSection(
                                currentPhotoUrl = currentPhotoUrl ?: capturedPhotoUri?.toString(),
                                onRequestCamera = { requestCamera() }
                            )
                        }
                    }
                }

                item { Spacer(Modifier.height(16.dp)) }
            }
            // This button finalises the logging process, creating a result intent with the logged data
            // and finishing the activity.
            Button(
                onClick = {
                    val activity = context as? Activity
                    val resultIntent = Intent().apply {
                        putExtra("LOGGED_CATEGORY", mainCategoryName)
                        putExtra("LOGGED_DESCRIPTION", subCategoryName)
                        if (isEditMode) {
                            putExtra("EDITED_WASTE_ID", loggedWasteId)
                        }
                        val detailsMap = HashMap<String, String>()
                        requiredFields.forEach { field ->
                            val value = fieldInputValues[field.id]
                            if (!value.isNullOrBlank()) {
                                detailsMap[field.fieldName] = value
                            }
                        }
                        putExtra("LOGGED_DETAILS", detailsMap)
                        capturedPhotoUri?.let { putExtra("CAPTURED_PHOTO_URI", it.toString()) }
                    }
                    activity?.setResult(Activity.RESULT_OK, resultIntent)
                    activity?.finish()
                },
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                // Change button text based on mode
                Text(
                    if (isEditMode) "Update Entry" else "Complete Entry",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
