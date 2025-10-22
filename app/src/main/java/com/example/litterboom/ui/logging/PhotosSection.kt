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

@Composable
fun PhotosForSubCategorySection(
    db: AppDatabase,
    subCategoryId: Int,
    onRequestCamera: () -> Unit
) {
    val dao = db.wasteDao()
    val photos by dao.photosFor(subCategoryId).collectAsState(initial = emptyList())

    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text("Photos", style = MaterialTheme.typography.titleMedium)
        TextButton(onClick = onRequestCamera) { Text("Take photo") }
    }
    Spacer(Modifier.height(8.dp))

    if (photos.isEmpty()) {
        Text("No photos yet.")
    } else {
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
