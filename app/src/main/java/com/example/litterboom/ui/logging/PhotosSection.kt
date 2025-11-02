package com.example.litterboom.ui.logging

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.litterboom.data.AppDatabase
import android.util.Base64
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.text.font.FontWeight

/**
 * A composable that displays a section for photos related to a specific sub-category.
 * It shows a title, a button to take a new photo, and a horizontal list of existing photos.
 *
 * @param db The application's database instance, used to fetch photos.
 * @param subCategoryId The ID of the sub-category for which to display photos.
 * @param onRequestCamera A callback function to be invoked when the "Take photo" button is clicked.
 */
@Composable
fun PhotosForSubCategorySection(
    db: AppDatabase,
    subCategoryId: Int,
    onRequestCamera: () -> Unit
) {
    // Access the Data Access Object (DAO) for waste-related database operations.
    val dao = db.wasteDao()
    // Observe the photos for the given sub-category from the database as a state.
    // `collectAsState` ensures the UI recomposes when the list of photos changes.
    val photos by dao.photosFor(subCategoryId).collectAsState(initial = emptyList())

    // A row containing the section title and the "Take photo" button.
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text("Photos", style = MaterialTheme.typography.titleMedium)
        TextButton(onClick = onRequestCamera) { Text("Take photo") }
    }
    Spacer(Modifier.height(8.dp))

    // Display a message if there are no photos, otherwise display the photo gallery.
    if (photos.isEmpty()) {
        Text("No photos yet.")
    } else {
        // A horizontally scrolling row to display the images.
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(photos) { p ->
                AsyncImage(
                    model = Uri.parse(p.uri),
                    contentDescription = null,
                    modifier = Modifier.size(96.dp)
                )
            }
        }
    }
}

/**
 * A composable that displays a section for a single photo related to a logged waste entry.
 * It shows a title, a button to take or retake a photo, and displays the current photo if available.
 *
 * @param currentPhotoUrl The current photo URL for the logged waste entry, or null if none.
 * @param onRequestCamera A callback function to be invoked when the "Take photo" or "Retake photo" button is clicked.
 */
@Composable
fun PhotoForLoggedWasteSection(
    currentPhotoUrl: String?,
    onRequestCamera: () -> Unit
) {
    // A row containing the section title and the photo button.
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text("Photo", style = MaterialTheme.typography.titleMedium)
        Button(
            onClick = onRequestCamera,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            )
        ) {
            Text(if (currentPhotoUrl.isNullOrEmpty()) "Take photo" else "Retake photo", fontWeight = FontWeight.Bold)
        }
    }
    Spacer(Modifier.height(8.dp))

    // Display the photo if available
    if (!currentPhotoUrl.isNullOrEmpty()) {
        AsyncImage(
            model = currentPhotoUrl,
            contentDescription = "Logged waste photo",
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        )
    } else {
        Text("No photo taken yet.")
    }
}
