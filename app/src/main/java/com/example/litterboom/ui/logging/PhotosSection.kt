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
