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
import com.example.litterboom.ui.theme.LitterboomTheme
import kotlinx.coroutines.launch

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

            //input form hides if approved
            if (!isApproved) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = bagNumber,
                        onValueChange = { bagNumber = it },
                        label = { Text("Bag Number (1-65)") },
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
                                num == null || num !in 1..65 -> {
                                    errorMessage = "Bag number must be between 1 and 65."
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
            //bag list with white block background
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
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Bag ${bag.bagNumber}", color = MaterialTheme.colorScheme.primary)
                                    Text("${bag.weight} kg", color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
            }


            //total summary
            val totalBags = bags.size
            val totalWeight = bags.sumOf { it.weight }.toString()
            Column {
                Text("Total Bags: $totalBags", color = Color.White)
                Text("Total Weight: $totalWeight kg", color = Color.White)
            }

            //admin approval button
            if (CurrentUserManager.isAdmin() && !isApproved) {
                Button(
                    onClick = {
                        scope.launch {
                            db.bagDao().approveBags(eventId)
                            isApproved = true
                            Toast.makeText(context, "Bags approved", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.align(Alignment.End),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text("Approve Bags")
                }
            }

            //navigation to WasteWorkerActivity if bags are approved
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

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun BagEntryScreenPreview() {
    LitterboomTheme {
        BagEntryScreen(eventId = 1, eventName = "Sample Event")
    }
}