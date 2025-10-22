package com.example.litterboom.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.example.litterboom.R
import com.example.litterboom.data.AppDatabase
import com.example.litterboom.data.WasteCategory
import com.example.litterboom.data.WasteSubCategory
import com.example.litterboom.ui.theme.LitterboomTheme
import kotlinx.coroutines.launch

class MainLoggingMenuActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            LitterboomTheme {
                MainLoggingMenuScreen()
            }
        }
    }
}

@Composable
fun MainLoggingMenuScreen() {
    val context = LocalContext.current
    val activity = context as? Activity
    var selectedCategory by remember { mutableStateOf<WasteCategory?>(null) }

    val fieldLoggingLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            activity?.setResult(Activity.RESULT_OK, result.data)
            activity?.finish()
        }
    }

    Crossfade(targetState = selectedCategory, label = "CategoryCrossfade") { category ->
        if (category == null) {
            MainCategoryGrid(onCategorySelected = { selectedCategory = it })
        } else {
            SubCategoryList(
                category = category,
                onBack = { selectedCategory = null },
                onSubCategorySelected = { subCategory, hasFields ->
                    if (hasFields) {
                        val intent = Intent(context, FieldLoggingActivity::class.java).apply {
                            putExtra("SUB_CATEGORY_ID", subCategory.id)
                            putExtra("SUB_CATEGORY_NAME", subCategory.name)
                            putExtra("MAIN_CATEGORY_NAME", category.name)
                        }
                        fieldLoggingLauncher.launch(intent)
                    } else {
                        val resultIntent = Intent().apply {
                            putExtra("LOGGED_CATEGORY", category.name)
                            putExtra("LOGGED_DESCRIPTION", subCategory.name)
                            putExtra("LOGGED_DETAILS", HashMap<String, String>())
                        }
                        activity?.setResult(Activity.RESULT_OK, resultIntent)
                        activity?.finish()
                        Toast.makeText(context, "${subCategory.name} Logged", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }
    }
}

@Composable
fun MainCategoryGrid(onCategorySelected: (WasteCategory) -> Unit) {
    val context = LocalContext.current
    var categories by remember { mutableStateOf<List<WasteCategory>>(emptyList()) }

    LaunchedEffect(Unit) {
        categories = AppDatabase.getDatabase(context).wasteDao().getAllCategories()
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Image(
            painter = painterResource(id = R.drawable.litterboom_logo__2_),
            contentDescription = "Litterboom Logo",
            modifier = Modifier.height(60.dp).padding(vertical = 8.dp)
        )
        OutlinedTextField(
            value = "",
            onValueChange = {},
            label = { Text("Filter a category?") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))
        Text(
            "Click a category below to begin logging",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(16.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(categories) { category ->
                Button(
                    onClick = { onCategorySelected(category) },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(60.dp)
                ) {
                    Text(text = category.name)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubCategoryList(
    category: WasteCategory,
    onBack: () -> Unit,
    onSubCategorySelected: (WasteSubCategory, Boolean) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var subCategories by remember { mutableStateOf<List<WasteSubCategory>>(emptyList()) }

    LaunchedEffect(category) {
        subCategories = AppDatabase.getDatabase(context).wasteDao().getSubCategoriesForCategory(category.id)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Log: ${category.name}") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.padding(innerPadding).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(subCategories) { item ->
                Button(
                    onClick = {
                        scope.launch {
                            val requiredFields = AppDatabase.getDatabase(context).wasteDao().getFieldsForSubCategory(item.id)
                            onSubCategorySelected(item, requiredFields.isNotEmpty())
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text(text = item.name) }
            }
        }
    }
}
