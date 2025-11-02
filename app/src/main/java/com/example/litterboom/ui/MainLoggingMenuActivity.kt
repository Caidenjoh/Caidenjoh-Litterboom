/**
 * This file contains the UI components for the main waste logging menu.
 * It allows users to select a main waste category and then a specific sub-category to log.
 * The activity manages the navigation between these two steps and handles the result of the logging process.
 */
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

/**
 * Activity for the main waste logging menu.
 * It hosts the Composable functions that guide the user through selecting a waste category
 * and then a sub-category. It sets up the initial UI and handles the overall window settings.
 */
class MainLoggingMenuActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Allow the content to draw behind the system bars for an edge-to-edge experience.
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            LitterboomTheme {
                MainLoggingMenuScreen()
            }
        }
    }
}

/**
 * The main screen composable for logging waste.
 * It manages the state切换 between the main category grid and the sub-category list.
 * It also handles the result from the `FieldLoggingActivity` to finish the logging process.
 */
@Composable
fun MainLoggingMenuScreen() {
    val context = LocalContext.current
    val activity = context as? Activity
    // State to hold the currently selected main waste category.
    var selectedCategory by remember { mutableStateOf<WasteCategory?>(null) }

    // Launcher for starting the FieldLoggingActivity and handling its result.
    val fieldLoggingLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        // If the FieldLoggingActivity returns with a successful result,
        // pass the result back to the calling activity (e.g., MainActivity) and finish this one.
        if (result.resultCode == Activity.RESULT_OK) {
            activity?.setResult(Activity.RESULT_OK, result.data)
            activity?.finish()
        }
    }

    Crossfade(targetState = selectedCategory, label = "CategoryCrossfade") { category ->
        // If no main category is selected, show the main category grid.
        if (category == null) {
            MainCategoryGrid(onCategorySelected = { selectedCategory = it })
        } else {
            // If a main category is selected, show the list of sub-categories for it.
            SubCategoryList(
                category = category,
                onBack = { selectedCategory = null },
                onSubCategorySelected = { subCategory, hasFields ->
                    // If the selected sub-category has additional fields to fill out,
                    // launch the FieldLoggingActivity.
                    if (hasFields) {
                        val intent = Intent(context, FieldLoggingActivity::class.java).apply {
                            putExtra("SUB_CATEGORY_ID", subCategory.id)
                            putExtra("SUB_CATEGORY_NAME", subCategory.name)
                            putExtra("MAIN_CATEGORY_NAME", category.name)
                        }
                        fieldLoggingLauncher.launch(intent)
                    } else {
                        // If there are no additional fields, create the result intent directly
                        // and finish the activity.
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

/**
 * A Composable that displays a grid of main waste categories.
 * Fetches categories from the database and allows the user to select one.
 *
 * @param onCategorySelected A callback function invoked when a category button is clicked.
 */
@Composable
fun MainCategoryGrid(onCategorySelected: (WasteCategory) -> Unit) {
    val context = LocalContext.current
    // State to hold the list of waste categories fetched from the database.
    var categories by remember { mutableStateOf<List<WasteCategory>>(emptyList()) }

    // Fetch all waste categories from the database when the composable is first launched.
    LaunchedEffect(Unit) {
        categories = AppDatabase.getDatabase(context).wasteDao().getAllCategories()
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Image(
            painter = painterResource(id = R.drawable.litterboom_logo__2_),
            contentDescription = "Litterboom Logo",
            modifier = Modifier.height(60.dp).padding(vertical = 8.dp)
        )
        // A non-functional filter field, for UI demonstration.
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

        // Grid to display category buttons.
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

/**
 * A Composable that displays a list of sub-categories for a given main category.
 * It includes a filter input and handles the selection of a sub-category.
 *
 * @param category The parent `WasteCategory` for which to display sub-categories.
 * @param onBack A callback function to navigate back to the main category grid.
 * @param onSubCategorySelected A callback function invoked when a sub-category is selected,
 *                              providing the sub-category and a boolean indicating if it has required fields.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubCategoryList(
    category: WasteCategory,
    onBack: () -> Unit,
    onSubCategorySelected: (WasteSubCategory, Boolean) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    // State to hold the list of sub-categories fetched from the database.
    var subCategories by remember { mutableStateOf<List<WasteSubCategory>>(emptyList()) }

    // State for the text in the filter input field.
    var filterText by remember { mutableStateOf("") }

    // Fetch active sub-categories for the selected main category when it changes.
    LaunchedEffect(category) {
        subCategories = AppDatabase.getDatabase(context).wasteDao().getActiveSubCategoriesForCategory(category.id)
    }

    // Derived state: a filtered list of sub-categories based on the filterText.
    val filteredSubCategories = remember(filterText, subCategories) {
        if (filterText.isBlank()) {
            subCategories
        } else {
            subCategories.filter {
                it.name.contains(filterText, ignoreCase = true)
            }
        }
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
        // Column to hold the filter input field and the list of sub-categories.
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {

            // Text field for filtering sub-categories.
            OutlinedTextField(
                value = filterText,
                onValueChange = { filterText = it },
                label = { Text("Filter a sub-category?") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                singleLine = true
            )

            Spacer(Modifier.height(16.dp))

            // LazyColumn to efficiently display the list of sub-category buttons.
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Items are sourced from the filtered list.
                items(filteredSubCategories) { item ->
                    Button(
                        onClick = {
                            scope.launch {
                                // Check if the selected sub-category has any required fields.
                                val requiredFields = AppDatabase.getDatabase(context).wasteDao().getFieldsForSubCategory(item.id)
                                // Trigger the callback with the sub-category and the result of the check.
                                onSubCategorySelected(item, requiredFields.isNotEmpty())
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(text = item.name) }
                }
            }
        }
    }
}
