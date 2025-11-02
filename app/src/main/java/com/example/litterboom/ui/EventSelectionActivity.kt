package com.example.litterboom.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.example.litterboom.WasteWorkerActivity
import com.example.litterboom.data.AppDatabase
import com.example.litterboom.data.Event
import com.example.litterboom.ui.theme.LitterboomTheme
import java.text.SimpleDateFormat
import java.util.*
import kotlin.jvm.java

/**
 * An Activity that displays a list of available cleanup events for a waste worker to select.
 * This activity is responsible for setting up the Jetpack Compose content view.
 */
class EventSelectionActivity : ComponentActivity() {
    /**
     * Called when the activity is first created. This is where you should do all of your normal
     * static set up: create views, bind data to lists, etc.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being
     * shut down then this Bundle contains the data it most recently supplied in onSaveInstanceState(Bundle).
     * Note: Otherwise it is null.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            LitterboomTheme {
                EventSelectionScreen()
            }
        }
    }
}

/**
 * A composable function that represents the entire screen for event selection.
 * It fetches and displays a list of open events from the database.
 * Users can tap on an event to proceed to the bag entry screen for that event.
 */
@Composable
fun EventSelectionScreen() {
    val context = LocalContext.current
    var events by remember { mutableStateOf<List<Event>>(emptyList()) }

    // LaunchedEffect to fetch the list of open events from the database once when the composable enters the composition.
    LaunchedEffect(Unit) {
        events = AppDatabase.getDatabase(context).eventDao().getOpenEvents()
    }

    // Main container with a gradient background
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.secondary,
                        MaterialTheme.colorScheme.primary
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Top app bar with back button and title
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { (context as? Activity)?.finish() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text("Select an Event to Log For", style = TextStyle(
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                ), color = Color.White)
            }
            Spacer(modifier = Modifier.height(24.dp))

            // Conditional content: Show a message if no events are found, otherwise display the list of events.
            if (events.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No events found. Please ask an admin to create one.", color = Color.White)
                }
            } else {
                // LazyColumn for efficiently displaying a potentially long list of events.
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(events) { event ->
                        Button(
                            onClick = {
                                // On click, create an intent for BagEntryActivity and pass the selected event's ID and name.
                                val intent = Intent(context, BagEntryActivity::class.java).apply {
                                    putExtra("EVENT_ID", event.id)
                                    putExtra("SELECTED_EVENT_NAME", event.name)
                                }
                                context.startActivity(intent)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                // Display event name
                                Text(event.name, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Text(
                                    // Format and display event date and location
                                    "${SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(event.date))} - ${event.location}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * A preview composable for visualizing the [EventSelectionScreen] in Android Studio's design view.
 */
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun EventSelectionScreenPreview() {
    LitterboomTheme {
        EventSelectionScreen()
    }
}