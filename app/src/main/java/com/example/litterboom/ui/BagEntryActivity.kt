package com.example.litterboom.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.example.litterboom.data.CurrentUserManager
import com.example.litterboom.WasteWorkerActivity
import com.example.litterboom.data.AppDatabase
import com.example.litterboom.data.Bag
import com.example.litterboom.data.api.ApiClient
import com.example.litterboom.ui.theme.LitterboomTheme
import kotlinx.coroutines.launch
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete

/**
 * BagEntryActivity is a screen where users can enter, view, and manage collected bags for a specific event.
 * Admins have additional privileges to approve or reject all bags.
 */
class BagEntryActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val eventId = intent.getIntExtra("EVENT_ID", 0)
        val eventName = intent.getStringExtra("SELECTED_EVENT_NAME") ?: "Event"
        setContent {
            LitterboomTheme {
                BagEntryScreen(eventId = eventId, eventName = eventName)
            }
        }
    }
}
/**
 * Composable function for the Bag Entry screen.
 * It displays a form to add bags, a list of entered bags, and controls for managing them.
 * @param eventId The ID of the current cleanup event.
 * @param eventName The name of the current cleanup event.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BagEntryScreen(eventId: Int, eventName: String) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val db = AppDatabase.getDatabase(context)
    var bags by remember { mutableStateOf<List<Bag>>(emptyList()) }
    var isApproved by remember { mutableStateOf(false) }
    var bagNumber by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var showRejectAllDialog by remember { mutableStateOf(false) }

    // LaunchedEffect to fetch initial bag data and approval status from the local database.
    LaunchedEffect(Unit) {
        bags = db.bagDao().getBagsByEvent(eventId)
        isApproved = db.bagDao().areBagsApproved(eventId)
    }

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
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { (context as? Activity)?.finish() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Bag Entry - $eventName",
                    style = TextStyle(
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            }

            if (errorMessage.isNotEmpty()) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // The input form for adding new bags is hidden if the bags for this event have already been approved.
            if (!isApproved) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = bagNumber,
                        onValueChange = { bagNumber = it },
                        label = { Text("Bag Number (1-200)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            ,
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedLabelColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedLabelColor = MaterialTheme.colorScheme.onSurface,
                            focusedBorderColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedBorderColor = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    OutlinedTextField(
                        value = weight,
                        onValueChange = { weight = it },
                        label = { Text("Weight (kg)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            ,
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedLabelColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedLabelColor = MaterialTheme.colorScheme.onSurface,
                            focusedBorderColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedBorderColor = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
                Button(
                    onClick = {
                        scope.launch {
                            val num = bagNumber.toIntOrNull()
                            val wt = weight.toDoubleOrNull()
                            when {
                                num == null || num !in 1..200 -> {
                                    errorMessage = "Bag number must be between 1 and 200."
                                }
                                wt == null || wt <= 0 -> {
                                    errorMessage = "Weight must be greater than 0."
                                }
                                bags.any { it.bagNumber == num } -> {
                                    errorMessage = "Bag number $num already exists."
                                }
                                else -> {
                                    db.bagDao().insertBag(Bag(eventId = eventId, bagNumber = num, weight = wt))
                                    bags = db.bagDao().getBagsByEvent(eventId)
                                    bagNumber = ""
                                    weight = ""
                                    errorMessage = ""
                                    Toast.makeText(context, "Bag added successfully", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    },
                    modifier = Modifier.align(Alignment.End),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text("Add Bag")
                }
            }
            // A white block background for the list of bag entries to improve contrast and readability.
            Text("Bag Entries", style = MaterialTheme.typography.titleMedium, color = Color.White)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .padding(8.dp)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (bags.isEmpty()) {
                        item {
                            Text("No bags entered.", modifier = Modifier.padding(8.dp), color = Color.Black)
                        }
                    } else {
                        items(bags) { bag ->
                            var showEditDialog by remember { mutableStateOf(false) }
                            var showDeleteDialog by remember { mutableStateOf(false) }

                            // Dialog for editing a bag's details.
                            if (showEditDialog) {
                                EditBagDialog(
                                    bag = bag,
                                    // onDismiss is called when the user cancels the edit operation.
                                    onDismiss = { showEditDialog = false },
                                    onSave = { updatedBag ->
                                        scope.launch {
                                            try {
                                                ApiClient.apiService.updateBag(bag.bagId, updatedBag)
                                                bags = db.bagDao().getBagsByEvent(eventId)
                                                Toast.makeText(context, "Bag updated", Toast.LENGTH_SHORT).show()
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "Update failed", Toast.LENGTH_SHORT).show()
                                            }
                                            showEditDialog = false
                                        }
                                    }
                                )
                            }

                            // Confirmation dialog for deleting a bag.
                            if (showDeleteDialog) {
                                AlertDialog(
                                    onDismissRequest = { showDeleteDialog = false },
                                    title = { Text("Delete Bag #${bag.bagNumber}?") },
                                    text = { Text("This cannot be undone.") },
                                    confirmButton = {
                                        TextButton(onClick = {
                                            scope.launch {
                                                try {
                                                    ApiClient.apiService.deleteBag(bag.bagId)
                                                    bags = db.bagDao().getBagsByEvent(eventId)
                                                    Toast.makeText(context, "Bag deleted", Toast.LENGTH_SHORT).show()
                                                } catch (e: Exception) {
                                                    Toast.makeText(context, "Delete failed", Toast.LENGTH_SHORT).show()
                                                }
                                                showDeleteDialog = false
                                            }
                                        }) {
                                            Text("Delete", color = MaterialTheme.colorScheme.error)
                                        }
                                    },
                                    dismissButton = {
                                        TextButton(onClick = { showDeleteDialog = false }) {
                                            Text("Cancel")
                                        }
                                    }
                                )
                            }

                            // Card representing a single bag entry in the list.
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Bag ${bag.bagNumber}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                        Text("${bag.weight} kg", color = MaterialTheme.colorScheme.primary)
                                    }

                                    // Edit and Delete buttons are only shown if bags are not yet approved.
                                    if (!isApproved) {
                                        Row {
                                            IconButton(onClick = { showEditDialog = true }) {
                                                Icon(Icons.Default.Edit, "Edit", tint = Color.Black)
                                            }
                                            IconButton(onClick = { showDeleteDialog = true }) {
                                                Icon(Icons.Default.Delete, "Delete", tint = Color.Red)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }


            // Summary section displaying the total number of bags and their combined weight.
            val totalBags = bags.size
            val totalWeight = bags.sumOf { it.weight }.toString()
            Column {
                Text("Total Bags: $totalBags", color = Color.White)
                Text("Total Weight: $totalWeight kg", color = Color.White)
            }

            // Admin-only controls for approving or rejecting all bags for the event.
            if (CurrentUserManager.isAdmin() && !isApproved) {
                Column {
                    Row(
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { showRejectAllDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Red,
                                contentColor = Color.White
                            )
                        ) {
                            Text("Reject Bags")
                        }

                        Button(
                            onClick = {
                                scope.launch {
                                    db.bagDao().approveBags(eventId)
                                    isApproved = true
                                    Toast.makeText(context, "Bags approved", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Text("Approve Bags")
                        }
                    }

                    if (showRejectAllDialog) {
                        AlertDialog(
                            onDismissRequest = { showRejectAllDialog = false },
                            title = { Text("Reject All Bags?") },
                            text = { Text("This will permanently delete all ${bags.size} bags.") },
                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        showRejectAllDialog = false
                                        scope.launch {
                                            try {
                                                bags.forEach { bag ->
                                                    ApiClient.apiService.deleteBag(bag.bagId)
                                                }
                                                bags = db.bagDao().getBagsByEvent(eventId)
                                                Toast.makeText(context, "All bags rejected and removed", Toast.LENGTH_SHORT).show()
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "Failed to reject bags", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }
                                ) {
                                    Text("Delete All", color = MaterialTheme.colorScheme.error)
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showRejectAllDialog = false }) {
                                    Text("Cancel")
                                }
                            }
                        )
                    }
                }
            }

            // If bags are approved, a button appears to navigate to the WasteWorkerActivity.
            if (isApproved) {
                Button(
                    onClick = {
                        val intent = Intent(context, WasteWorkerActivity::class.java).apply {
                            putExtra("EVENT_ID", eventId)
                            putExtra("SELECTED_EVENT_NAME", eventName)
                            flags = Intent.FLAG_ACTIVITY_FORWARD_RESULT
                        }
                        context.startActivity(intent)
                        (context as? Activity)?.finish()
                    },
                    modifier = Modifier.align(Alignment.End),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text("Proceed to Waste Entry")
                }
            }
        }
    }
}

/**
 * A dialog composable for editing an existing bag's number and weight.
 * Includes validation for the input fields.
 * @param bag The [Bag] object to be edited.
 * @param onDismiss Lambda function to call when the dialog is dismissed.
 * @param onSave Lambda function to call with the updated [Bag] when the save button is clicked.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditBagDialog(bag: Bag, onDismiss: () -> Unit, onSave: (Bag) -> Unit) {
    var bagNumber by remember { mutableStateOf(bag.bagNumber.toString()) }
    var weight by remember { mutableStateOf(bag.weight.toString()) }
    var error by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Bag #${bag.bagNumber}") },
        text = {
            Column {
                OutlinedTextField(
                    value = bagNumber,
                    onValueChange = { bagNumber = it },
                    label = { Text("Bag Number (1-200)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = error.contains("number"),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = weight,
                    onValueChange = { weight = it },
                    label = { Text("Weight (kg)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = error.contains("weight"),
                    modifier = Modifier.fillMaxWidth()
                )
                if (error.isNotEmpty()) {
                    Text(error, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 4.dp))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val num = bagNumber.toIntOrNull()
                val wt = weight.toDoubleOrNull()
                when {
                    num == null || num !in 1..200 -> error = "Bag number must be 1-200"
                    wt == null || wt <= 0 -> error = "Weight must be > 0"
                    else -> onSave(bag.copy(bagNumber = num, weight = wt))
                }
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

/**
 * A preview composable for the [BagEntryScreen].
 * This allows for easy visualization of the screen layout in Android Studio's preview pane.
 */
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun BagEntryScreenPreview() {
    LitterboomTheme {
        BagEntryScreen(eventId = 1, eventName = "Sample Event")
    }
}