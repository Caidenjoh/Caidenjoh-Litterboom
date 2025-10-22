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
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.example.litterboom.ui.MainLoggingMenuActivity
import com.example.litterboom.ui.theme.LitterboomTheme
import java.io.Serializable
import com.example.litterboom.data.AppDatabase
import com.example.litterboom.data.CurrentUserManager
import com.example.litterboom.data.LoggedWaste
import kotlinx.coroutines.launch
import org.json.JSONObject
import kotlin.jvm.java

class WasteWorkerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val eventName = intent.getStringExtra("SELECTED_EVENT_NAME") ?: "No Event Selected"
        val eventId = intent.getIntExtra("EVENT_ID", -1)

        setContent {
            LitterboomTheme {
                WasteWorkerScreen(eventName = eventName, eventId = eventId)
            }
        }
    }
}

data class LoggedEntry(
    val id: Int,
    val category: String,
    val description: String,
    val details: Map<String, String>
) : Serializable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WasteWorkerScreen(eventName: String, eventId: Int) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Image(painterResource(R.drawable.litterboom_logo__2_), "Litterboom Logo", modifier = Modifier.height(40.dp)) },
                navigationIcon = { IconButton(onClick = { /* Handle Menu Click */ }) { Icon(Icons.Default.Menu, "Menu") } },
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

@Composable
fun WasteWorkerContent(contentPadding: PaddingValues,  eventName: String, eventId: Int) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val currentSessionEntries = remember { mutableStateListOf<LoggedEntry>() }
    var entryToDelete by remember { mutableStateOf<LoggedEntry?>(null) }

    // Fetch previously logged data for this event
    LaunchedEffect(eventId) {
        if (eventId != -1) {
            val db = AppDatabase.getDatabase(context)
            val loggedItemsFromDb = db.loggedWasteDao().getWasteForEvent(eventId)
            val mappedEntries = loggedItemsFromDb.map { loggedWaste ->
                val detailsMap = mutableMapOf<String, String>()
                try {
                    val detailsJson = JSONObject(loggedWaste.details)
                    detailsJson.keys().forEach { key -> detailsMap[key] = detailsJson.getString(key) }
                } catch (e: Exception) { /* Handle error if JSON is invalid */ }
                LoggedEntry(loggedWaste.id, loggedWaste.category, loggedWaste.subCategory, detailsMap)
            }
            currentSessionEntries.addAll(mappedEntries)
        }
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

                scope.launch {
                    val userId = CurrentUserManager.currentUser?.id ?: -1
                    val detailsJson = JSONObject(detailsMap as Map<*, *>).toString()

                    if (editedId != -1) {
                        val updatedEntry = LoggedEntry(editedId, category, description, detailsMap)
                        val index = currentSessionEntries.indexOfFirst { it.id == editedId }
                        if (index != -1) {
                            currentSessionEntries[index] = updatedEntry
                        }
                        val loggedWaste = LoggedWaste(editedId, eventId, userId, category, description, detailsJson)
                        AppDatabase.getDatabase(context).loggedWasteDao().updateLoggedWaste(loggedWaste)
                        Toast.makeText(context, "Entry updated!", Toast.LENGTH_SHORT).show()
                    } else {
                        val loggedWaste = LoggedWaste(0, eventId, userId, category, description, detailsJson)
                        val newId = AppDatabase.getDatabase(context).loggedWasteDao().insertLoggedWaste(loggedWaste).toInt()
                        val newEntry = LoggedEntry(newId, category, description, detailsMap)
                        currentSessionEntries.add(0, newEntry)
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
            modifier = Modifier.padding(bottom = 16.dp).clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                val intent = Intent(context, MainActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK }
                context.startActivity(intent)
                (context as? Activity)?.finish()
            }
        )
        Text("Logging for: $eventName", style = MaterialTheme.typography.titleMedium, color = Color.White)
        Spacer(modifier = Modifier.height(8.dp))

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
                Text(
                    "Details",
                    modifier = Modifier.weight(1.5f),
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.End
                )
            }

            if (currentSessionEntries.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No items logged for this event yet.", color = Color.Gray)
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(currentSessionEntries) { entry ->
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
                            }
                            Row {
                                IconButton(onClick = {
                                    // Launch FieldLoggingActivity in edit mode
                                    val intent =
                                        Intent(context, MainLoggingMenuActivity::class.java).apply {
                                            putExtra("EDIT_ENTRY_ID", entry.id)
                                        }
                                    loggingActivityLauncher.launch(intent)
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
                            val db = AppDatabase.getDatabase(context)
                            // Find the corresponding item in the database using its unique ID
                            val itemToDeleteFromDb = db.loggedWasteDao().getLoggedWasteById(entryToDelete!!.id)
                            if (itemToDeleteFromDb != null) {
                                db.loggedWasteDao().deleteLoggedWaste(itemToDeleteFromDb)
                                currentSessionEntries.remove(entryToDelete)
                                Toast.makeText(context, "Entry deleted", Toast.LENGTH_SHORT).show()
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