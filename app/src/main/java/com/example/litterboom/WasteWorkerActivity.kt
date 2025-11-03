package com.example.litterboom

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.example.litterboom.data.AppDatabase
import com.example.litterboom.data.CurrentUserManager
import com.example.litterboom.data.LoggedWaste
import com.example.litterboom.ui.MainLoggingMenuActivity
import com.example.litterboom.ui.theme.LitterboomTheme
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.Serializable
import android.util.Base64
import java.io.ByteArrayOutputStream
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import retrofit2.Response

/**
 * `WasteWorkerActivity` is a ComponentActivity that serves as the main screen for waste workers.
 * It displays a list of logged waste entries for a specific event and allows workers to add,
 * edit, or delete entries.
 *
 * The activity receives event details (name and ID) via an Intent.
 */
class WasteWorkerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Makes the app content draw behind the system bars (like status bar).
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Retrieve event details passed from the previous activity.
        // Default to "No Event Selected" if the name is not provided.
        val eventName = intent.getStringExtra("SELECTED_EVENT_NAME") ?: "No Event Selected"
        // Default to -1 if the ID is not provided.
        val eventId = intent.getIntExtra("EVENT_ID", -1)

        // Set the content of the activity to be the WasteWorkerScreen composable.
        // This is the entry point for the UI of this screen.
        setContent {
            // Apply the app's theme to the composable content.
            LitterboomTheme {
                WasteWorkerScreen(eventName = eventName, eventId = eventId)
            }
        }
    }
}

/**
 * Data class representing a single logged waste entry displayed in the UI.
 * It holds information about the waste category, a descriptive subtitle, and any additional
 * details in a map format. This class is serializable to be passed between activities.
 *
 * @property id The unique identifier of the log entry in the database.
 * @property category The main category of the waste (e.g., "Plastic", "Glass").
 * @property description A short description or sub-category of the waste (e.g., "Bottles", "Bags").
 * @property details A map containing any extra key-value details about the logged item (e.g., "Weight": "5kg").
 */
data class LoggedEntry(
    val id: Int,
    val category: String,
    val description: String,
    val details: Map<String, String>,
    val photoUrl: String? = null
) : Serializable

/**
 * The main composable function for the Waste Worker screen, setting up the overall layout with a top app bar and content area.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WasteWorkerScreen(eventName: String, eventId: Int) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Image(painterResource(R.drawable.litterboom_logo__2_), "Litterboom Logo", modifier = Modifier.height(40.dp)) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.statusBarsPadding()
            )
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().background(brush = Brush.verticalGradient(colors = listOf(MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.primary)))) {
            WasteWorkerContent(innerPadding, eventName, eventId)
        }
    }
}

/**
 * Composable function that defines the content of the Waste Worker screen.
 * It includes the welcome message, event name, a filterable list of logged waste entries,
 * and a button to add new entries.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WasteWorkerContent(contentPadding: PaddingValues, eventName: String, eventId: Int) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var currentSessionEntries by remember { mutableStateOf<List<LoggedEntry>>(emptyList()) }
    var entryToDelete by remember { mutableStateOf<LoggedEntry?>(null) }

    // State for the filter dropdown
    var selectedCategoryFilter by remember { mutableStateOf("All Categories") }
    var filterMenuExpanded by remember { mutableStateOf(false) }

    // Get a unique list of categories from the logged items
    val categoriesInUse by remember {
        derivedStateOf {
            listOf("All Categories") + currentSessionEntries.map { it.category }.distinct().sorted()
        }
    }

    // final filtered list to be displayed
    val filteredEntries by remember {
        derivedStateOf {
            if (selectedCategoryFilter == "All Categories") {
                currentSessionEntries
            } else {
                currentSessionEntries.filter { it.category == selectedCategoryFilter }
            }
        }
    }

    // Function to refresh waste entries from API
    suspend fun refreshWasteEntries() {
        if (eventId != -1) {
            try {
                val db = AppDatabase.getDatabase(context)
                val loggedItemsFromApi = db.loggedWasteDao().getWasteForEvent(eventId)
                val mappedEntries = loggedItemsFromApi.map { loggedWaste ->
                    val detailsMap = mutableMapOf<String, String>()
                    try {
                        val detailsJson = JSONObject(loggedWaste.details)
                        detailsJson.keys().forEach { key -> detailsMap[key] = detailsJson.getString(key) }
                    } catch (e: Exception) { /* Handle error if JSON is invalid */ }
                    LoggedEntry(loggedWaste.id, loggedWaste.category, loggedWaste.subCategory, detailsMap, loggedWaste.photoUrl.takeIf { !it.isNullOrEmpty() })
                }
                currentSessionEntries = mappedEntries
            } catch (e: Exception) {
                // If API fails, fall back to local database
                try {
                    val db = AppDatabase.getDatabase(context)
                    val loggedItemsFromDb = db.loggedWasteDao().getWasteForEvent(eventId)
                    val mappedEntries = loggedItemsFromDb.map { loggedWaste ->
                        val detailsMap = mutableMapOf<String, String>()
                        try {
                            val detailsJson = JSONObject(loggedWaste.details)
                            detailsJson.keys().forEach { key -> detailsMap[key] = detailsJson.getString(key) }
                        } catch (e: Exception) { /* Handle error if JSON is invalid */ }
                        LoggedEntry(loggedWaste.id, loggedWaste.category, loggedWaste.subCategory, detailsMap, loggedWaste.photoUrl.takeIf { !it.isNullOrEmpty() })
                    }
                    currentSessionEntries = mappedEntries
                } catch (dbException: Exception) {
                    // If both API and DB fail, show error
                    Toast.makeText(context, "Failed to refresh data", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Fetch previously logged data for this event
    LaunchedEffect(eventId) {
        refreshWasteEntries()
    }

    val loggingActivityLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.let { intent ->
                val category = intent.getStringExtra("LOGGED_CATEGORY") ?: ""
                val description = intent.getStringExtra("LOGGED_DESCRIPTION") ?: ""
                val editedId = intent.getIntExtra("EDITED_WASTE_ID", -1)

                val rawMap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getSerializableExtra("LOGGED_DETAILS", HashMap::class.java)
                } else { @Suppress("DEPRECATION") intent.getSerializableExtra("LOGGED_DETAILS") as? HashMap<*, *> }
                val detailsMap = mutableMapOf<String, String>()
                rawMap?.forEach { (key, value) -> detailsMap[key.toString()] = value.toString() }

                val capturedPhotoUri = intent.getStringExtra("CAPTURED_PHOTO_URI")?.let { Uri.parse(it) }

                scope.launch {
                    val userId = CurrentUserManager.currentUser?.id ?: -1
                    val detailsJson = JSONObject(detailsMap as Map<*, *>).toString()

                    if (editedId != -1) {
                        try {
                            val existing = AppDatabase.getDatabase(context).loggedWasteDao().getLoggedWasteById(editedId)
                            val userIdForUpdate = existing?.userId ?: userId  // Preserve original userId, fallback to current
                            val updatedEntry = LoggedEntry(editedId, category, description, detailsMap)
                            currentSessionEntries = currentSessionEntries.map { entry ->
                                if (entry.id == editedId) updatedEntry else entry
                            }
                            val loggedWaste = LoggedWaste(editedId, eventId, userIdForUpdate, category, description, detailsJson)
                            AppDatabase.getDatabase(context).loggedWasteDao().updateLoggedWaste(loggedWaste)
                            Toast.makeText(context, "Entry updated!", Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            // If API call fails, just update locally
                            val updatedEntry = LoggedEntry(editedId, category, description, detailsMap)
                            currentSessionEntries = currentSessionEntries.map { entry ->
                                if (entry.id == editedId) updatedEntry else entry
                            }
                            Toast.makeText(context, "Entry updated locally!", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        val loggedWaste = LoggedWaste(0, eventId, userId, category, description, detailsJson)
                        val newId = AppDatabase.getDatabase(context).loggedWasteDao().insertLoggedWaste(loggedWaste).toInt()

                        val newEntry = LoggedEntry(newId, category, description, detailsMap, null)
                        currentSessionEntries = listOf(newEntry) + currentSessionEntries

                        // Upload photo if available
                        if (capturedPhotoUri != null) {
                            try {
                                val base64Image = convertUriToBase64(context, capturedPhotoUri)
                                if (base64Image != null) {
                                    val response = AppDatabase.getDatabase(context).loggedWasteDao().uploadPhotoForLoggedWaste(newId, base64Image)
                                    if (response.isSuccessful) {
                                        val responseBody = response.body()
                                        val photoUrl = responseBody?.string()
                                        if (!photoUrl.isNullOrEmpty()) {
                                            // Update the logged waste with the photo URL
                                            val updatedWaste = loggedWaste.copy(id = newId, photoUrl = photoUrl)
                                            AppDatabase.getDatabase(context).loggedWasteDao().updateLoggedWaste(updatedWaste)
                                            // Update the specific entry in the UI with the photo URL
                                            currentSessionEntries = currentSessionEntries.map { entry ->
                                                if (entry.id == newId) {
                                                    entry.copy(photoUrl = photoUrl)
                                                } else {
                                                    entry
                                                }
                                            }
                                        } else {
                                            Toast.makeText(context, "Photo upload failed: Empty response", Toast.LENGTH_SHORT).show()
                                        }
                                    } else {
                                        Toast.makeText(context, "Photo upload failed: ${response.message()}", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    Toast.makeText(context, "Failed to process photo", Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, "Photo upload failed: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                        Toast.makeText(context, "Entry saved!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier.padding(contentPadding).fillMaxSize().padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val currentUser = CurrentUserManager.currentUser
        Text("Welcome back, ${currentUser?.username ?: "User"}!", style = MaterialTheme.typography.headlineLarge, color = Color.White)
        Text(
            "Not you? Logout",
            color = Color.White.copy(alpha = 0.8f),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 16.dp).clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                val intent = Intent(context, MainActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK }
                context.startActivity(intent)
                (context as? Activity)?.finish()
            }
        )
        Text("Logging for: $eventName", style = MaterialTheme.typography.titleMedium, color = Color.White)
        Spacer(modifier = Modifier.height(8.dp))

        // Filter dropdown
        ExposedDropdownMenuBox(
            expanded = filterMenuExpanded,
            onExpandedChange = { filterMenuExpanded = !filterMenuExpanded },
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        ) {
            OutlinedTextField(
                value = selectedCategoryFilter,
                onValueChange = {},
                readOnly = true,
                label = { Text("Filter by Category") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = filterMenuExpanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    disabledContainerColor = MaterialTheme.colorScheme.surface,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = MaterialTheme.colorScheme.primary,
                    unfocusedTextColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(12.dp),
                textStyle = TextStyle(fontWeight = FontWeight.Bold)
            )
            ExposedDropdownMenu(
                expanded = filterMenuExpanded,
                onDismissRequest = { filterMenuExpanded = false }
            ) {
                categoriesInUse.forEach { category ->
                    DropdownMenuItem(
                        text = { Text(category) },
                        onClick = {
                            selectedCategoryFilter = category
                            filterMenuExpanded = false
                        }
                    )
                }
            }
        }

        Column(modifier = Modifier.weight(1f).clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.surface)) {
            Row(
                modifier = Modifier.fillMaxWidth().background(Color.White.copy(alpha = 0.3f))
                    .padding(12.dp)
            ) {
                Text(
                    "Item",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Empty State
            if (currentSessionEntries.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No items logged for this event yet.", color = Color.Gray, modifier = Modifier.padding(16.dp), textAlign = TextAlign.Center)
                }
            } else if (filteredEntries.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No items found for '$selectedCategoryFilter'.", color = Color.Gray, modifier = Modifier.padding(16.dp), textAlign = TextAlign.Center)
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {

                    items(filteredEntries) { entry ->
                        Row(
                            modifier = Modifier.padding(start = 12.dp, top = 12.dp, bottom = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(entry.category, fontWeight = FontWeight.Bold)
                                Text(entry.description, style = MaterialTheme.typography.bodySmall)
                                if (entry.details.isNotEmpty()) {
                                    Text(
                                        text = entry.details.map { "${it.key}: ${it.value}" }
                                            .joinToString("\n"),
                                        style = MaterialTheme.typography.bodySmall,
                                        lineHeight = 14.sp
                                    )
                                }
                                // Display photo download link if photo exists
                                if (!entry.photoUrl.isNullOrEmpty()) {
                                    Text(
                                        text = "📷 Download Photo",
                                        color = Color.Blue,
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                        modifier = Modifier
                                            .padding(top = 4.dp)
                                            .clickable(
                                                indication = null,
                                                interactionSource = null
                                            ) {
                                                // Open photo in browser for download
                                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(entry.photoUrl))
                                                context.startActivity(intent)
                                            }
                                    )
                                }
                            }
                            Row {
                                IconButton(onClick = {
                                    // Launch FieldLoggingActivity in edit mode directly
                                    scope.launch {
                                        try {
                                            val loggedWaste = AppDatabase.getDatabase(context).loggedWasteDao().getLoggedWasteById(entry.id)
                                            if (loggedWaste != null) {
                                                val categories = AppDatabase.getDatabase(context).wasteDao().getAllCategories()
                                                val category = categories.find { it.name == loggedWaste.category }
                                                if (category != null) {
                                                    val subCategories = AppDatabase.getDatabase(context).wasteDao().getActiveSubCategoriesForCategory(category.id)
                                                    val subCategory = subCategories.find { it.name == loggedWaste.subCategory }
                                                    if (subCategory != null) {
                                                        val intent = Intent(context, com.example.litterboom.ui.FieldLoggingActivity::class.java).apply {
                                                            putExtra("SUB_CATEGORY_ID", subCategory.id)
                                                            putExtra("SUB_CATEGORY_NAME", subCategory.name)
                                                            putExtra("MAIN_CATEGORY_NAME", loggedWaste.category)
                                                            putExtra("LOGGED_WASTE_ID", loggedWaste.id)
                                                        }
                                                        loggingActivityLauncher.launch(intent)
                                                    }
                                                }
                                            }
                                        } catch (e: Exception) {
                                            // If API call fails, show error
                                            Toast.makeText(context, "Unable to edit entry", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }) {
                                    Icon(Icons.Default.Edit, "Edit")
                                }
                                IconButton(onClick = { entryToDelete = entry }) {
                                    Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                        Divider()
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                val intent = Intent(context, MainLoggingMenuActivity::class.java)
                loggingActivityLauncher.launch(intent)
            },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Add New Entry", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(16.dp))
    }

    if (entryToDelete != null) {
        AlertDialog(
            onDismissRequest = { entryToDelete = null },
            title = { Text("Confirm Deletion") },
            text = { Text("Are you sure you want to delete this entry? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            try {
                                // Try to delete from API first
                                val db = AppDatabase.getDatabase(context)
                                db.loggedWasteDao().deleteLoggedWaste(LoggedWaste(entryToDelete!!.id, 0, 0, "", "", ""))
                                currentSessionEntries = currentSessionEntries.filter { it.id != entryToDelete!!.id }
                                Toast.makeText(context, "Entry deleted", Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                // If API delete fails, still remove from local list
                                currentSessionEntries = currentSessionEntries.filter { it.id != entryToDelete!!.id }
                                Toast.makeText(context, "Entry removed locally", Toast.LENGTH_SHORT).show()
                            }
                            entryToDelete = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                Button(onClick = { entryToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

/**
 * Converts a URI to a base64 encoded string.
 */
fun convertUriToBase64(context: android.content.Context, uri: Uri): String? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        Base64.encodeToString(byteArray, Base64.NO_WRAP)
    } catch (e: Exception) {
        null
    }
}
