package com.example.litterboom.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.core.view.WindowCompat
import com.example.litterboom.data.AppDatabase
import com.example.litterboom.data.ItemPhoto
import com.example.litterboom.data.LoggingField
import com.example.litterboom.ui.camera.CameraCaptureScreen
import com.example.litterboom.ui.logging.PhotosForSubCategorySection
import com.example.litterboom.ui.theme.DarkJungleGreen
import com.example.litterboom.ui.theme.LightTeal
import com.example.litterboom.ui.theme.LitterboomTheme
import kotlinx.coroutines.launch
import org.json.JSONObject

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FieldLoggingScreen(subCategoryId: Int, subCategoryName: String, mainCategoryName: String, loggedWasteId: Int) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val db = remember { AppDatabase.getDatabase(context) }

    var requiredFields by remember { mutableStateOf<List<LoggingField>>(emptyList()) }
    val fieldInputValues = remember { mutableStateMapOf<Int, String>() }
    val isEditMode = loggedWasteId != -1
    var showCamera by remember { mutableStateOf(false) }

    LaunchedEffect(subCategoryId) {
        if (subCategoryId != -1) {
            val db = AppDatabase.getDatabase(context)
            requiredFields = db.wasteDao().getFieldsForSubCategory(subCategoryId)

            if (isEditMode) {
                // If we are editing, load the existing data
                val existingItem = db.loggedWasteDao().getWasteForEvent(0).find { it.id == loggedWasteId }
                if (existingItem != null) {
                    val detailsJson = JSONObject(existingItem.details)
                    requiredFields.forEach { field ->
                        fieldInputValues[field.id] = detailsJson.optString(field.fieldName, "")
                    }
                }
            } else {
                // else initialise with empty values
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { (context as? Activity)?.finish() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
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
                // 1) admin-defined fields
                items(requiredFields) { field ->
                    OutlinedTextField(
                        value = fieldInputValues[field.id] ?: "",
                        onValueChange = { fieldInputValues[field.id] = it },
                        label = { Text(field.fieldName, style = MaterialTheme.typography.labelLarge) },
                        textStyle = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
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
                            PhotosForSubCategorySection(
                                db = db,
                                subCategoryId = subCategoryId,
                                onRequestCamera = { showCamera = true }
                            )
                        }
                    }
                }

                item { Spacer(Modifier.height(16.dp)) }
            }

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
                    }
                    activity?.setResult(Activity.RESULT_OK, resultIntent)
                    activity?.finish()
                },
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                // Change button text based on mode
                Text(if (isEditMode) "Update Entry" else "Complete Entry", fontWeight = FontWeight.Bold)
            }
        }
    }

        // Full-screen camera overlay
        if (showCamera) {
            Box(Modifier.fillMaxSize()) {
                CameraCaptureScreen(
                    onCaptured = { uri ->
                        scope.launch {
                            db.wasteDao().insertPhoto(
                                ItemPhoto(
                                    subCategoryId = subCategoryId,
                                    uri = uri.toString()
                                )
                            )
                            showCamera = false
                        }
                    },
                    onClose = { showCamera = false }
                )
            }
        }
    }
