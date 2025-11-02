package com.example.litterboom

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material.icons.filled.WbIncandescent
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.core.view.WindowCompat
import com.example.litterboom.data.AppDatabase
import com.example.litterboom.data.Bag
import com.example.litterboom.data.CurrentUserManager
import com.example.litterboom.data.Event
import com.example.litterboom.data.LoggedWaste
import com.example.litterboom.data.LoggingField
import com.example.litterboom.data.SessionManager
import com.example.litterboom.data.SubCategoryField
import com.example.litterboom.data.User
import com.example.litterboom.data.WasteCategory
import com.example.litterboom.data.WasteSubCategory
import com.example.litterboom.data.api.ApiClient
import com.example.litterboom.ui.EventSelectionActivity
import com.example.litterboom.ui.theme.LitterboomTheme
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.xssf.usermodel.XSSFCellStyle
import org.apache.poi.xssf.usermodel.XSSFFont
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.json.JSONObject
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


/**
 * The main entry point of the application.
 * This activity hosts the entire Jetpack Compose UI, including navigation,
 * login, and various informational and administrative screens.
 * It also handles the initialization of essential services like Google Places API.
 */
@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    /**
     * Called when the activity is first created. This is where you should do all of your normal
     * static set up: create views, bind data to lists, etc.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        try {
            if (!Places.isInitialized()) {
                // Access the key safely from the generated BuildConfig
                val apiKey = BuildConfig.MAPS_API_KEY
                if (!apiKey.isEmpty() && apiKey != "YOUR_API_KEY_HERE") {
                    Places.initialize(applicationContext, apiKey)
                } else {
                    // Places API key not set, skip initialization
                }
            }
        } catch (e: Exception) {
            // Log and display any errors during Places SDK initialization.
            e.printStackTrace()
            Toast.makeText(this, "Error initializing Places SDK: ${e.message}", Toast.LENGTH_LONG).show()
        }
        AppDatabase.getDatabase(this)


        setContent {
            LitterboomTheme {
                AppWithNavDrawer()
            }
        }
    }
}

/**
 * The root composable that sets up the main application structure with a [ModalNavigationDrawer].
 * It manages the current screen state and handles navigation logic between different screens.
 * It also restores the user session if a user was previously logged in.
 */
@Composable
fun AppWithNavDrawer() {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var currentScreen by remember { mutableStateOf("Source to Sea") }
    var loggedIn by rememberSaveable { mutableStateOf(false) }
    val isAdmin = CurrentUserManager.isAdmin()
    val context = LocalContext.current

    // Dynamically generate the list of navigation items based on login and admin status.
    val navItems = remember(loggedIn, isAdmin) {
        listOf("Source to Sea", "Interception", "Education", "Innovation", "Our Story", "The Team", "Contact").plus(
            if (loggedIn && isAdmin) listOf("Admin Panel") else emptyList()
        ).plus(if (loggedIn) listOf("Event Selection", "Logout") else emptyList())
    }

    // On first composition, check for a saved user session and log them in automatically.
    LaunchedEffect(Unit) {
        val savedUserId = SessionManager.getSavedUserId(context)
        if (savedUserId != -1) {
            val db = AppDatabase.getDatabase(context)
            val user = db.userDao().getUserById(savedUserId)
            if (user != null) {
                CurrentUserManager.login(user)
                loggedIn = true
            }
        }
    }

    // The main navigation drawer container.
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawerContent(
                currentScreen = currentScreen,
                navItems = navItems,
                isAdmin = isAdmin,
                onItemClick = { selectedItem ->
                    scope.launch { drawerState.close() }
                    when (selectedItem) {
                        // Handle logout action.
                        "Logout" -> {
                            SessionManager.clearSession(context)
                            CurrentUserManager.logout()
                            loggedIn = false
                            currentScreen = "Source to Sea"
                        }
                        // Handle navigation to other screens.
                        "Admin Panel" -> currentScreen = "Admin Panel"
                        "Event Selection" -> {
                            if (loggedIn) {
                                val intent = Intent(context, EventSelectionActivity::class.java)
                                context.startActivity(intent)
                            } else {
                                Toast.makeText(context, "Please log in to access Event Selection", Toast.LENGTH_SHORT).show()
                            }
                        }
                        else -> currentScreen = selectedItem
                    }
                }
            )
        }
    ) {
        // Use Crossfade to animate transitions between different screens.
        Crossfade(targetState = currentScreen, label = "ScreenCrossfade") { screen ->
            when (screen) {
                "Source to Sea" -> LoginScreenWithSwipeableSheet(loggedIn, { loggedIn = it }, { scope.launch { drawerState.open() } },
                    { currentScreen = it })
                "Interception" -> InterceptionScreen(onBackClick = { currentScreen = "Source to Sea" })
                "Education" -> EducationScreen(onBackClick = { currentScreen = "Source to Sea" })
                "Innovation" -> InnovationScreen(onBackClick = { currentScreen = "Source to Sea" })
                "Our Story" -> OurStoryScreen(onBackClick = { currentScreen = "Source to Sea" })
                "The Team" -> TheTeamScreen(onBackClick = { currentScreen = "Source to Sea" })
                "Contact" -> ContactScreen(onBackClick = { currentScreen = "Source to Sea" })
                "Admin Panel" -> {
                    if (isAdmin) {
                        AdminPanelScreen({ scope.launch { drawerState.open() } }, { newScreen -> currentScreen = newScreen })
                    } else {
                        //Redirect non-admins
                        currentScreen = "Source to Sea"
                        Toast.makeText(LocalContext.current, "Admin access required", Toast.LENGTH_SHORT).show()
                    }
                }
                "Add User" -> AddUserScreen(onBackClick = { currentScreen = "Admin Panel" })
                "Create Event" -> CreateEventScreen({ currentScreen = "Event List" }, { currentScreen = "Admin Panel" })
                "Event List" -> EventListScreen(onBackClick = { currentScreen = "Admin Panel" })
                "Manage Categories" -> ManageCategoriesScreen { currentScreen = "Admin Panel" }
                "Manage Fields" -> ManageFieldsScreen { currentScreen = "Admin Panel" }
                "Bag Logs" -> AdminBagLogScreen(onBackClick = { currentScreen = "Admin Panel" })
                "Event Logs" -> EventLogsScreen(onBackClick = { currentScreen = "Admin Panel" })
                else -> {
                    // Fallback screen for any unhandled navigation state.
                    Box(modifier = Modifier.fillMaxSize().background(Color.LightGray), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = screen, fontSize = 24.sp)
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { currentScreen = "Source to Sea" }) { Text("Go Back") }
                        }
                    }
                }
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)

/**
 * A screen that displays information about The Litterboom Project's interception technology.
 * It includes a title, an image, and descriptive text.
 */
@Composable
fun InterceptionScreen(onBackClick: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Interception") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                modifier = Modifier.statusBarsPadding()
            )
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.secondary,
                            MaterialTheme.colorScheme.primary
                        )
                    )
                )
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Our Interception Technology",
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))

            Image(
                painter = painterResource(id = R.drawable.booms_in_action),
                contentDescription = "Litterboom in a river",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(MaterialTheme.shapes.medium),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "At the core of our mission is our innovative river interception technology. We deploy passively collecting, 100% recyclable plastic pipes known as 'litter booms' across rivers to catch and collect floating plastic pollution.",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.9f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "This preventative approach is crucial. By capturing waste in our river systems, we stop it from ever reaching the ocean, preventing further harm to marine ecosystems.",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.9f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
/**
 * A screen that displays information about The Litterboom Project's educational initiatives.
 * It features a title, image, and text explaining their approach to education.
 */
@Composable
fun EducationScreen(onBackClick: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Education") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                modifier = Modifier.statusBarsPadding()
            )
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.secondary,
                            MaterialTheme.colorScheme.primary
                        )
                    )
                )
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Empowering Through Education",
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))

            Image(
                painter = painterResource(id = R.drawable.innovation),
                contentDescription = "Community education session",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(MaterialTheme.shapes.medium),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "We believe that lasting change comes from knowledge. Our education programs aim to inspire a new generation of environmental stewards by teaching the youth how to protect their ocean heritage.",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.9f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Our approach focuses on three key areas: educating the public with facts, empowering the youth, and guiding businesses on balancing profit with sustainability. We provide an 'Education Toolkit' for teachers to help engage learners on the subject of plastic, its challenges, and what we can do to solve the problem together.",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.9f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
/**
 * A screen dedicated to explaining The Litterboom Project's innovation and circular economy efforts.
 * It includes an image and text about the 'Wastepreneur Programme'.
 */
@Composable
fun InnovationScreen(onBackClick: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Innovation") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                modifier = Modifier.statusBarsPadding()
            )
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.secondary,
                            MaterialTheme.colorScheme.primary
                        )
                    )
                )
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Innovation & The Circular Economy",
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))

            Image(
                painter = painterResource(id = R.drawable.waste_audit),
                contentDescription = "Upcycled plastic products",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(MaterialTheme.shapes.medium),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "We believe that simply collecting waste isn't enough. True innovation lies in creating value from the pollution we recover. Our Innovation Hub is where this transformation happens.",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.9f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Through our 'Wastepreneur Programme', we train and equip local entrepreneurs to turn river plastic into new, durable products. By creating an economic incentive for materials that would otherwise have no recycling value, we empower communities and help build a sustainable circular economy from the ground up.",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.9f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
/**
 * A screen that tells the story of The Litterboom Project's origin.
 * It features an image of the founder and text describing the project's history.
 */
@Composable
fun OurStoryScreen(onBackClick: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Our Story") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                modifier = Modifier.statusBarsPadding()
            )
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.secondary,
                            MaterialTheme.colorScheme.primary
                        )
                    )
                )
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "A Mission Born from Passion",
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))

            Image(
                painter = painterResource(id = R.drawable.cameron_service),
                contentDescription = "The Litterboom Project Team",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(450.dp)
                    .clip(MaterialTheme.shapes.medium),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "The Litterboom Project was initiated in 2017 by our founder, Cameron Service. As a passionate surfer and trail builder, he was constantly confronted with the devastating amount of plastic pollution flowing down our rivers into the ocean.",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.9f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "He realised that while beach clean-ups were important, a more effective, preventative solution was needed. This led to a simple yet powerful idea: targeting the river systems to intercept the waste upstream. What began as a pilot project with a small team has now grown into a nationwide movement, preventing millions of kilograms of plastic from ever reaching the sea.",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.9f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
/**
 * A screen introducing the key members of The Litterboom Project team.
 * It displays a list of team members with their names, roles, and a short biography.
 */
@Composable
fun TheTeamScreen(onBackClick: () -> Unit) {
    // Data class to hold team member information
    data class TeamMember(val name: String, val role: String, val bio: String)

    val teamMembers = listOf(
        TeamMember("Cameron Service", "CEO & Founder", "Cameron's passion for the ocean and outdoors led him to create a project to tackle the plastic pollution crisis at its source: our rivers."),
        TeamMember("Rudi Clark", "KZN Director", "Rudi manages daily operations and leads the Innovation Hub in KwaZulu-Natal, connecting people and ideas to find lasting solutions."),
        TeamMember("Megan Swart", "Cape Town Project Manager", "Megan's passion for protecting rivers and coastlines drives her work, inspiring others to join the fight against plastic pollution."),
        TeamMember("Casey Pratt", "Media Manager", "Through social media and photography, Casey shares The Litterboom Project's story to raise awareness and inspire action."),
        TeamMember("Jihaad Jacobs", "Cape Town Ops Manager", "Jihaad leads teams, coordinates logistics, and oversees events, using his experience in waste management to protect the environment.")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("The Team") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                modifier = Modifier.statusBarsPadding()
            )
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.secondary,
                            MaterialTheme.colorScheme.primary
                        )
                    )
                ),
            contentPadding = PaddingValues(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Text(
                    text = "Meet Our Dedicated Team",
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            items(teamMembers) { member ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.2f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(member.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
                        Text(member.role, style = MaterialTheme.typography.titleMedium, color = Color.White.copy(alpha = 0.9f))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(member.bio, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.8f))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
/**
 * A screen providing contact information for The Litterboom Project.
 * It lists general enquiries email and office addresses for Durban and Cape Town.
 */
@Composable
fun ContactScreen(onBackClick: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Contact Us") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                modifier = Modifier.statusBarsPadding()
            )
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.secondary,
                            MaterialTheme.colorScheme.primary
                        )
                    )
                )
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Get In Touch",
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))

            // General Enquiries Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("General Enquiries", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Email, contentDescription = "Email", tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("info@thelitterboomproject.com", style = MaterialTheme.typography.bodyLarge, color = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Durban Office Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Durban Office (KZN)", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = "Address", tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("100 Gallan Road, Umbogintwini, 4126", style = MaterialTheme.typography.bodyLarge, color = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Cape Town Office Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Cape Town Office", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = "Address", tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("3rd Floor, Tifco Building, 280 Voortrekker Road, Maitland", style = MaterialTheme.typography.bodyLarge, color = Color.White)
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
/**
 * The main screen for the Admin Panel, providing a central hub for administrative tasks.
 * It features a top app bar with a menu button and a grid of icon buttons for navigation.
 * @param onMenuClick A lambda to open the navigation drawer.
 * @param navigateTo A lambda function that takes a screen route string to navigate to other admin screens.
 */
@Composable
fun AdminPanelScreen(onMenuClick: () -> Unit, navigateTo: (String) -> Unit) {

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Admin Panel",
                        style = MaterialTheme.typography.headlineLarge,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(Icons.Default.Menu, "Menu", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                ),
                modifier = Modifier.statusBarsPadding()
            )
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(brush = Brush.verticalGradient(colors = listOf(MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.primary)))
                .padding(paddingValues)
        ) {
            AdminMenu(
                onAddUserClick = { navigateTo("Add User") },
                onCreateEventClick = { navigateTo("Create Event") },
                onEventListClick = { navigateTo("Event List") },
                onManageCategoriesClick = { navigateTo("Manage Categories") },
                onManageFieldsClick = { navigateTo("Manage Fields") },
                onEventLogsClick = { navigateTo("Event Logs") },
                onBagLogClick = { navigateTo("Bag Logs") }
            )
        }
    }
}

/**
 * A composable that displays a grid of icon buttons for the Admin Panel.
 * Each button navigates to a specific administrative function.
 * @param onAddUserClick Lambda for navigating to the Add User screen.
 * @param onCreateEventClick Lambda for navigating to the Create Event screen.
 * @param onEventListClick Lambda for navigating to the Event List screen.
 * @param onManageCategoriesClick Lambda for navigating to the Manage Categories screen.
 * @param onManageFieldsClick Lambda for navigating to the Manage Fields screen.
 * @param onEventLogsClick Lambda for navigating to the Event Logs screen.
 * @param onBagLogClick Lambda for navigating to the Bag Logs screen.
 */
@Composable
fun AdminMenu(onAddUserClick: () -> Unit, onCreateEventClick: () -> Unit, onEventListClick: () -> Unit, onManageCategoriesClick: () -> Unit, onManageFieldsClick: () -> Unit, onEventLogsClick: () -> Unit, onBagLogClick: () -> Unit) {

    val menuItems = listOf(
        "Create Event" to Icons.Default.Event,
        "Event List" to Icons.AutoMirrored.Filled.ViewList,
        "Waste Data" to Icons.Default.Assessment,
        "Bag Logs" to Icons.Default.Inventory,
        "Manage Categories" to Icons.Default.Category,
        "Manage Fields" to Icons.Default.Settings,
        "Add User" to Icons.Default.PersonAdd
    )

    // A grid layout for the admin menu items for a clean and responsive UI.
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(menuItems) { (title, icon) ->
            // Determine which click handler to pass
            val onClick = when (title) {
                "Add User" -> onAddUserClick
                "Create Event" -> onCreateEventClick
                "Event List" -> onEventListClick
                "Manage Categories" -> onManageCategoriesClick
                "Manage Fields" -> onManageFieldsClick
                "Waste Data" -> onEventLogsClick
                "Bag Logs" -> onBagLogClick
                else -> { -> }
            }
            AdminIconButton(title, icon, onClick)
        }
    }
}

/**
 * A screen for administrators to view bag logs. It's a two-step process:
 * first, select an event, then view the detailed bag logs for that event.
 * @param onBackClick Lambda to navigate back from the event selection view.
 */
@Composable
fun AdminBagLogScreen(onBackClick: () -> Unit) {
    var selectedEvent by remember { mutableStateOf<Event?>(null) }

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
        Crossfade(targetState = selectedEvent, label = "BagLogCrossfade") { event ->
            if (event == null) {
                EventSelectionForBagLogs(
                    onBackClick = onBackClick,
                    onEventSelected = { selectedEvent = it }
                )
            } else {
                BagLogDetailScreen(
                    event = event,
                    onBack = { selectedEvent = null }
                )
            }
        }
    }
}

/**
 * A private composable responsible for displaying a list of events for an admin to select from.
 * Once an event is selected, it triggers the [onEventSelected] callback.
 * @param onBackClick Lambda to navigate back to the previous screen.
 * @param onEventSelected Callback invoked with the selected [Event].
 */
@Composable
private fun EventSelectionForBagLogs(onBackClick: () -> Unit, onEventSelected: (Event) -> Unit) {
    val context = LocalContext.current
    var events by remember { mutableStateOf<List<Event>>(emptyList()) }

    LaunchedEffect(Unit) {
        // Admins able to see all events
        events = AppDatabase.getDatabase(context).eventDao().getAllEvents()
    }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp).statusBarsPadding().navigationBarsPadding()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text("Select Event to View Bag Logs", style = MaterialTheme.typography.headlineLarge, color = Color.White)
        }
        Spacer(modifier = Modifier.height(24.dp))

        if (events.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No events found.", color = Color.White)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(events) { event ->
                    Button(
                        onClick = { onEventSelected(event) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(event.name, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Text("${SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(event.date))} - ${event.location}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                        }
                    }
                }
            }
        }
    }
}

/**
 * A private composable that displays the detailed bag log information for a specific [Event].
 * It shows a list of all logged bags with their weights and provides a summary of total bags and weight.
 * @param event The [Event] for which to display bag logs.
 * @param onBack Callback to return to the event selection screen.
 */
@Composable
private fun BagLogDetailScreen(event: Event, onBack: () -> Unit) {
    val context = LocalContext.current
    var bags by remember { mutableStateOf<List<Bag>>(emptyList()) }
    val db = remember { AppDatabase.getDatabase(context) }

    LaunchedEffect(event) {
        // Fetch bags for the specific event from the local database.
        bags = db.bagDao().getBagsByEvent(event.id)
    }

    // Calculate totals
    val totalBags = bags.size
    val totalWeight = bags.sumOf { it.weight }
    val totalWeightFormatted = DecimalFormat("#,##0.00").format(totalWeight)

    Column(modifier = Modifier.fillMaxSize().padding(16.dp).statusBarsPadding().navigationBarsPadding()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White) }
            Spacer(modifier = Modifier.width(16.dp))
            Text("Bag Logs for ${event.name}", style = MaterialTheme.typography.headlineMedium, color = Color.White)
        }
        Spacer(modifier = Modifier.height(16.dp))

        if (bags.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No bags have been logged for this event yet.", color = Color.White)
            }
        } else {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(bags.sortedBy { it.bagNumber }) { bag -> // Sort by bag number
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.2f))) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Bag ${bag.bagNumber}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "${DecimalFormat("#,##0.00").format(bag.weight)} kg",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.White
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Totals Summary Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Total Bags: $totalBags",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Total Weight: $totalWeightFormatted kg",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

/**
 * A screen for administrators to view detailed waste data logs for events.
 * Similar to [AdminBagLogScreen], it first requires selecting an event.
 * @param onBackClick Lambda to navigate back from the event selection view.
 */
@Composable
fun EventLogsScreen(onBackClick: () -> Unit) {
    var selectedEvent by remember { mutableStateOf<Event?>(null) }

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
        Crossfade(targetState = selectedEvent, label = "EventLogCrossfade") { event ->
            if (event == null) {
                EventSelectionForLogs(
                    onBackClick = onBackClick,
                    onEventSelected = { selectedEvent = it }
                )
            } else {
                LoggedWasteDetailScreen(
                    event = event,
                    onBack = { selectedEvent = null }
                )
            }
        }
    }
}

/**
 * A private composable that allows an admin to select an event to view its waste logs.
 * It fetches and displays all available events.
 * @param onBackClick Lambda to navigate back.
 * @param onEventSelected Callback invoked with the selected [Event].
 */
@Composable
private fun EventSelectionForLogs(onBackClick: () -> Unit, onEventSelected: (Event) -> Unit) {
    val context = LocalContext.current
    var events by remember { mutableStateOf<List<Event>>(emptyList()) }

    LaunchedEffect(Unit) {
        events = AppDatabase.getDatabase(context).eventDao().getAllEvents()
    }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp).statusBarsPadding().navigationBarsPadding()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text("Select Event to View Logs", style = MaterialTheme.typography.headlineLarge, color = Color.White)
        }
        Spacer(modifier = Modifier.height(24.dp))

        if (events.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No events found.", color = Color.White)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(events) { event ->
                    Button(
                        onClick = { onEventSelected(event) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(event.name, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Text("${SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(event.date))} - ${event.location}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                        }
                    }
                }
            }
        }
    }
}

/**
 * A screen displaying the detailed logged waste items for a selected event.
 * It shows a list of all waste items, who logged them, and their details.
 * It also includes a feature to export all the event data to an XLSX (Excel) file.
 * @param event The event whose logs are to be displayed.
 * @param onBack Callback to return to the event selection screen.
 */
@Suppress("ExperimentalMaterial3Api")
@Composable
private fun LoggedWasteDetailScreen(event: Event, onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val db = remember { AppDatabase.getDatabase(context) }
    var loggedItems by remember { mutableStateOf<List<Pair<LoggedWaste, User?>>>(emptyList()) }
    var bagItems by remember { mutableStateOf<List<Bag>>(emptyList()) }

    // ActivityResultLauncher for saving the .XLSX file
    val fileSaverLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    ) { uri ->
        uri?.let { saveUri ->
            scope.launch {
                try {
                    // Pre-calculate all totals
                    val totalBags = bagItems.size
                    val totalBagWeight = bagItems.sumOf { it.weight }
                    val groupedByCategory = loggedItems.groupBy { it.first.category }
                    var overallLoggedWeight = 0.0
                    var overallTotalPieces = 0.0
                    val categoryTotals = mutableMapOf<String, Pair<Double, Double>>() // Category -> Pair(Pieces, Weight)
                    val weightKey = "Weight (kg)"
                    val piecesKey = "Pieces"

                    // Calculate totals needed for the summary sheet
                    groupedByCategory.forEach { (mainCategory, itemsInCategory) ->
                        var categoryTotalWeight = 0.0
                        var categoryTotalPieces = 0.0
                        itemsInCategory.forEach { (waste, _) ->
                            try {
                                val detailsJson = JSONObject(waste.details)
                                detailsJson.keys().forEach { key ->
                                    val value = detailsJson.optString(key, "")
                                    val numericValue = value.toDoubleOrNull()
                                    if (numericValue != null) {
                                        when (key) {
                                            weightKey -> categoryTotalWeight += numericValue
                                            piecesKey -> categoryTotalPieces += numericValue
                                        }
                                    }
                                }
                            } catch (_: Exception) { /* ignore bad json */ }
                        }
                        overallLoggedWeight += categoryTotalWeight
                        overallTotalPieces += categoryTotalPieces
                        categoryTotals[mainCategory] = Pair(categoryTotalPieces, categoryTotalWeight)
                    }
                    val weightVariance = totalBagWeight - overallLoggedWeight
                    val estimatePerBag = if (totalBags > 0) overallTotalPieces / totalBags else 0.0

                    // Create a blank Excel Workbook
                    val workbook = XSSFWorkbook()

                    // Create helper styles
                    val boldFont: XSSFFont = workbook.createFont()
                    boldFont.bold = true

                    val headerStyle: XSSFCellStyle = workbook.createCellStyle()
                    headerStyle.fillForegroundColor = IndexedColors.GREY_25_PERCENT.index
                    headerStyle.fillPattern = FillPatternType.SOLID_FOREGROUND
                    headerStyle.setFont(boldFont)

                    val boldStyle: XSSFCellStyle = workbook.createCellStyle()
                    boldStyle.setFont(boldFont)

                    // Create event summary sheet
                    val summarySheet = workbook.createSheet("Event Summary")
                    var summaryRowIndex = 0

                    // Event Details
                    summarySheet.createRow(summaryRowIndex++).apply {
                        createCell(0).apply { setCellValue("Event Name:"); setCellStyle(boldStyle) }
                        createCell(1).setCellValue(event.name)
                    }
                    summarySheet.createRow(summaryRowIndex++).apply {
                        createCell(0).apply { setCellValue("Event Date:"); setCellStyle(boldStyle) }
                        createCell(1).setCellValue(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(event.date)))
                    }
                    summarySheet.createRow(summaryRowIndex++).apply {
                        createCell(0).apply { setCellValue("Event Location:"); setCellStyle(boldStyle) }
                        createCell(1).setCellValue(event.location)
                    }
                    summaryRowIndex++ // Blank row

                    // Category Breakdown Section
                    summarySheet.createRow(summaryRowIndex++).apply {
                        createCell(0).apply { setCellValue("Category Totals:"); setCellStyle(headerStyle) }
                        createCell(1).apply { setCellValue("Total Pieces"); setCellStyle(headerStyle) }
                        createCell(2).apply { setCellValue("Total Weight (kg)"); setCellStyle(headerStyle) }
                    }
                    categoryTotals.toSortedMap().forEach { (categoryName, totals) -> // Sort categories alphabetically
                        val (pieces, weight) = totals
                        summarySheet.createRow(summaryRowIndex++).apply {
                            createCell(0).setCellValue(categoryName)
                            createCell(1).setCellValue(pieces)
                            createCell(2).setCellValue(weight)
                        }
                    }
                    summaryRowIndex++ // Blank row

                    // Overall Calculated Summary
                    summarySheet.createRow(summaryRowIndex++).apply {
                        createCell(0).apply { setCellValue("Overall Summary:"); setCellStyle(headerStyle) }
                    }
                    summarySheet.createRow(summaryRowIndex++).apply {
                        createCell(0).apply { setCellValue("Original Weight (kg):"); setCellStyle(boldStyle) }
                        createCell(1).setCellValue(totalBagWeight)
                    }
                    summarySheet.createRow(summaryRowIndex++).apply {
                        createCell(0).apply { setCellValue("Weight Variance (kg):"); setCellStyle(boldStyle) }
                        createCell(1).setCellValue(weightVariance)
                    }
                    summarySheet.createRow(summaryRowIndex++).apply {
                        createCell(0).apply { setCellValue("Estimate Pieces Per Bag:"); setCellStyle(boldStyle) }
                        createCell(1).setCellValue(estimatePerBag)
                    }

                    // Build the Bag Summary Sheet
                    val bagSheet = workbook.createSheet("Bag Summary")
                    var currentBagRowIndex = 0

                    // Create Bag Header Row
                    var headerRow = bagSheet.createRow(currentBagRowIndex++)
                    headerRow.createCell(0).apply {
                        setCellValue("Bag Number")
                        setCellStyle(headerStyle)
                    }
                    headerRow.createCell(1).apply {
                        setCellValue("Weight (kg)")
                        setCellStyle(headerStyle)
                    }

                    // Populate Bag Data
                    val sortedBags = bagItems.sortedBy { it.bagNumber }
                    sortedBags.forEach { bag ->
                        val dataRow = bagSheet.createRow(currentBagRowIndex++)
                        dataRow.createCell(0).setCellValue(bag.bagNumber.toDouble())
                        dataRow.createCell(1).setCellValue(bag.weight)
                    }

                    // Add Bag Totals
                    currentBagRowIndex += 1 // Blank row
                    val totalRow1 = bagSheet.createRow(currentBagRowIndex++)
                    totalRow1.createCell(0).apply {
                        setCellValue("Total Bags:")
                        setCellStyle(boldStyle)
                    }
                    totalRow1.createCell(1).setCellValue(totalBags.toDouble())

                    val totalRow2 = bagSheet.createRow(currentBagRowIndex)
                    totalRow2.createCell(0).apply {
                        setCellValue("Total Weight (kg):")
                        setCellStyle(boldStyle)
                    }
                    totalRow2.createCell(1).setCellValue(totalBagWeight)


                    // Build a Sheet for Each Waste Category
                    groupedByCategory.forEach { (mainCategory, itemsInCategory) ->
                        val safeSheetName = mainCategory.replace(Regex("[:\\\\/?*\\[\\]]"), " ")
                        val sheet = workbook.createSheet(safeSheetName)
                        var currentWasteRowIndex = 0

                        // Find unique dynamic fields
                        val detailKeysForThisCategory = categoryTotals[mainCategory]?.let { totals ->
                            // Get keys specific to this category from loggedItems
                            itemsInCategory.flatMap { (waste, _) ->
                                try { JSONObject(waste.details).keys().asSequence().toList() }
                                catch (_: Exception) { emptyList() }
                            }.toSet()
                        } ?: emptySet() // Fallback if category somehow wasn't in totals map
                        val sortedDetailKeys = detailKeysForThisCategory.sorted()


                        // Define standard keys and ensure they come first
                        val standardKeys = listOfNotNull(
                            piecesKey.takeIf { detailKeysForThisCategory.contains(it) },
                            weightKey.takeIf { detailKeysForThisCategory.contains(it) }
                        )
                        val otherKeys = sortedDetailKeys.filter { it != weightKey && it != piecesKey }
                        val headers = listOf("Sub-Category") + standardKeys + otherKeys

                        // Create the Header Row
                        headerRow = sheet.createRow(currentWasteRowIndex++)
                        headers.forEachIndexed { index, header ->
                            headerRow.createCell(index).apply {
                                setCellValue(header)
                                setCellStyle(headerStyle)
                            }
                        }

                        // Populate Data Rows
                        itemsInCategory.forEach { (waste, _) ->
                            val dataRow = sheet.createRow(currentWasteRowIndex++)
                            dataRow.createCell(0).setCellValue(waste.subCategory)

                            val detailsJson = try { JSONObject(waste.details) } catch (e: Exception) { JSONObject() }

                            headers.drop(1).forEachIndexed { headerIndex, key ->
                                val value = detailsJson.optString(key, "")
                                val numericValue = value.toDoubleOrNull()
                                val cell = dataRow.createCell(headerIndex + 1)

                                if (numericValue != null) {
                                    cell.setCellValue(numericValue)
                                } else {
                                    cell.setCellValue(value)
                                }
                            }
                        }

                        // Add Category Total Rows using pre-calculated
                        val (categoryTotalPieces, categoryTotalWeight) = categoryTotals[mainCategory] ?: Pair(0.0, 0.0)
                        currentWasteRowIndex += 1 // Blank row

                        val weightColIndex = headers.indexOf(weightKey)
                        val piecesColIndex = headers.indexOf(piecesKey)
                        val firstTotalCol = listOf(piecesColIndex, weightColIndex).filter { it != -1 }.minOrNull() ?: 0
                        val labelColIndex = if (firstTotalCol > 0) firstTotalCol - 1 else 0

                        if (piecesColIndex != -1) {
                            val totalPiecesRow = sheet.createRow(currentWasteRowIndex++)
                            totalPiecesRow.createCell(labelColIndex).apply {
                                setCellValue("Total Pieces:")
                                setCellStyle(boldStyle)
                            }
                            totalPiecesRow.createCell(piecesColIndex).apply {
                                setCellValue(categoryTotalPieces)
                                setCellStyle(boldStyle)
                            }
                        }

                        if (weightColIndex != -1) {
                            val totalWeightRow = sheet.createRow(currentWasteRowIndex)
                            totalWeightRow.createCell(labelColIndex).apply {
                                setCellValue("Total Weight (kg):")
                                setCellStyle(boldStyle)
                            }
                            totalWeightRow.createCell(weightColIndex).apply {
                                setCellValue(categoryTotalWeight)
                                setCellStyle(boldStyle)
                            }
                        }
                    } // End loop for categories


                    //  Write the file
                    context.contentResolver.openOutputStream(saveUri)?.use { outputStream ->
                        workbook.write(outputStream)
                    }
                    workbook.close()

                    Toast.makeText(context, "Export successful!", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(context, "Error exporting file: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    LaunchedEffect(event) {
        val wasteItems = db.loggedWasteDao().getWasteForEvent(event.id)
        val userWastePairs = wasteItems.map { waste ->
            val user = db.userDao().getUserById(waste.userId) // Uses Int ID
            Pair(waste, user)
        }
        loggedItems = userWastePairs
        bagItems = db.bagDao().getBagsByEvent(event.id)
    }

    // The UI layout is unchanged
    Column(modifier = Modifier.fillMaxSize().padding(16.dp).statusBarsPadding().navigationBarsPadding()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White) }
            Spacer(modifier = Modifier.width(16.dp))
            Text("Logs for ${event.name}", style = MaterialTheme.typography.headlineMedium, color = Color.White, modifier = Modifier.weight(1f))

            IconButton(onClick = {
                val simpleDate = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date(event.date))
                val fileName = "${event.name.replace(" ", "_")}_${simpleDate}_export.xlsx"
                fileSaverLauncher.launch(fileName)
            }) {
                Icon(Icons.Default.Share, "Export Data", tint = Color.White)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        if (loggedItems.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No waste has been logged for this event yet.", color = Color.White)
            }
        } else {
            LazyColumn {
                items(loggedItems) { (waste, user) ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.2f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("${waste.category} > ${waste.subCategory}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                            Text("Logged by: ${user?.username ?: "Unknown"}", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.7f))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = try {
                                    val detailsJson = JSONObject(waste.details)
                                    detailsJson.keys().asSequence().joinToString("\n") { key ->
                                        "$key: ${detailsJson.getString(key)}"
                                    }
                                } catch (_: Exception) { "No details" },
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
/**
 * A styled square icon button used in the Admin Panel menu.
 * @param text The text label displayed below the icon.
 * @param icon The vector icon to be displayed.
 * @param onClick The lambda function to be executed when the button is clicked.
 */
@Composable
fun AdminIconButton(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        // Make the button a square that fills its cell
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f),
        // Slightly softer corners
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp, hoveredElevation = 8.dp),
        // A translucent frosted container and white text
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.2f),
            contentColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                icon,
                contentDescription = text,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
/**
 * An admin screen for adding new users to the system.
 * It provides fields for username, password, and role, and lists existing users.
 * @param onBackClick Lambda to navigate back to the admin panel.
 */
@Composable
fun AddUserScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("User") }
    var roleMenuExpanded by remember { mutableStateOf(false) }
    var successMessage by remember { mutableStateOf("") }
    var users by remember { mutableStateOf(listOf<User>()) }

    fun refreshUsers() {
        scope.launch { users = AppDatabase.getDatabase(context).userDao().getAllUsers() }
    }

    LaunchedEffect(Unit) { refreshUsers() }

    Box(
        modifier = Modifier.fillMaxSize().background(
            brush = Brush.verticalGradient(
                colors = listOf(
                    MaterialTheme.colorScheme.secondary,
                    MaterialTheme.colorScheme.primary
                )
            )
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp).statusBarsPadding()
                .navigationBarsPadding()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        "Back",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    "Add User",
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    containerColor = Color.White.copy(alpha = 0.9f)
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    containerColor = Color.White.copy(alpha = 0.9f)
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            ExposedDropdownMenuBox(
                expanded = roleMenuExpanded,
                onExpandedChange = { roleMenuExpanded = !roleMenuExpanded }) {
                OutlinedTextField(
                    value = selectedRole,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Role") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = roleMenuExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        containerColor = Color.White.copy(alpha = 0.9f)
                    )
                )
                ExposedDropdownMenu(
                    expanded = roleMenuExpanded,
                    onDismissRequest = { roleMenuExpanded = false }) {
                    DropdownMenuItem(
                        text = { Text("User") },
                        onClick = { selectedRole = "User"; roleMenuExpanded = false })
                    DropdownMenuItem(
                        text = { Text("Admin") },
                        onClick = { selectedRole = "Admin"; roleMenuExpanded = false })
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    scope.launch {
                        if (username.isNotBlank() && password.isNotBlank()) {
                            try {
                                AppDatabase.getDatabase(context).userDao().insertUser(
                                    User(
                                        username = username,
                                        password = password,
                                        role = selectedRole
                                    )
                                )
                                username = ""
                                password = ""
                                successMessage = "User added successfully!"
                                refreshUsers()
                            } catch (e: retrofit2.HttpException) {
                                successMessage = if (e.code() == 400) "Failed to add user: A user with this email already exists." else "Failed to add user: ${e.message()}"
                            } catch (e: Exception) {
                                successMessage = "Failed to add user: ${e.message}"
                            }
                        } else {
                            successMessage = "Please fill all fields."
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Text(
                    "Add User",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
            if (successMessage.isNotEmpty()) {
                Text(
                    text = successMessage,
                    color = Color.White,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text(
                "Current Users:",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))

           // LazyColumn of Cards
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp) // Add space between cards
            ) {
                items(users) { user ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White.copy(alpha = 0.2f),
                            contentColor = Color.White
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // User Info
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = user.username,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = user.role,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }

                            // Added the Switch
                            var isChecked by remember(user.isActive) { mutableStateOf(user.isActive) }
                            val isAdmin = user.role == "Admin"

                            Switch(
                                checked = if (isAdmin) true else isChecked,
                                onCheckedChange = { newValue ->
                                    if (!isAdmin) {
                                        scope.launch {
                                            try {
                                                val updatedUser = ApiClient.apiService.toggleUserActive(user.id)
                                                isChecked = updatedUser.isActive
                                                refreshUsers()
                                                Toast.makeText(context, "User ${if (newValue) "enabled" else "disabled"}", Toast.LENGTH_SHORT).show()
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "Failed to update user", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }
                                },
                                enabled = !isAdmin,
                                colors = SwitchDefaults.colors(
                                    checkedTrackColor = MaterialTheme.colorScheme.primary,
                                    uncheckedTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                                    disabledCheckedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                    disabledUncheckedTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * An extension function for [String] to capitalize the first letter of each word.
 * @return A new string with each word capitalized.
 */
fun String.capitalizeWords(): String = this.split(" ")
    .joinToString(" ") { it.replaceFirstChar { char -> if (char.isLowerCase()) char.titlecase(Locale.getDefault()) else char.toString() } }

@OptIn(ExperimentalMaterial3Api::class)
/**
 * A screen for administrators to manage waste categories and sub-categories.
 * Allows creating new main categories and viewing/managing existing ones.
 * Admins can also archive/unarchive categories.
 * @param onBackClick A lambda function to navigate back to the previous screen.
 */
@Composable
fun ManageCategoriesScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val db = AppDatabase.getDatabase(context)

    var categories by remember { mutableStateOf<List<WasteCategory>>(emptyList()) }
    var allFields by remember { mutableStateOf<List<LoggingField>>(emptyList()) }
    var newCategoryName by remember { mutableStateOf("") }
    var expandedCategoryId by remember { mutableStateOf<Int?>(null) }

    fun refreshAll() {
        scope.launch {
            categories = db.wasteDao().getAllCategories() // Gets all active and inactive
            allFields = db.wasteDao().getAllLoggingFields() // Gets all active and inactive
        }
    }

    LaunchedEffect(Unit) { refreshAll() }

    Box(modifier = Modifier.fillMaxSize().background(brush = Brush.verticalGradient(colors = listOf(MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.primary)))) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp).statusBarsPadding().navigationBarsPadding()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White) }
                Spacer(modifier = Modifier.width(16.dp))
                Text("Manage Categories", style = MaterialTheme.typography.headlineLarge, color = Color.White)
            }
            Spacer(modifier = Modifier.height(16.dp))

            Text("Add New Main Category", style = MaterialTheme.typography.titleMedium, color = Color.White)
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = newCategoryName,
                    onValueChange = { newCategoryName = it },
                    label = { Text("Category Name") },
                    modifier = Modifier.weight(1f),
                    colors = TextFieldDefaults.outlinedTextFieldColors(containerColor = Color.White.copy(alpha = 0.9f))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = {
                    scope.launch {
                        val trimmedName = newCategoryName.trim()
                        val trimmedLower = trimmedName.lowercase()
                        if (trimmedName.isNotBlank()) {
                            val existingCategories = db.wasteDao().getAllCategories()
                            if (existingCategories.none { it.name.lowercase() == trimmedLower }) {
                                db.wasteDao().insertCategory(WasteCategory(name = trimmedName.capitalizeWords()))
                                newCategoryName = ""
                                refreshAll()
                                Toast.makeText(context, "Category Added", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Category '$trimmedName' already exists", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }) { Text("Add") }
            }

            Divider(modifier = Modifier.padding(vertical = 24.dp), color = Color.White.copy(alpha = 0.5f))

            Text("Existing Main Categories", style = MaterialTheme.typography.titleMedium, color = Color.White)
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(categories) { category ->
                    CategoryItem(
                        category = category,
                        allFields = allFields,
                        isExpanded = expandedCategoryId == category.id,
                        onExpand = { expandedCategoryId = if (expandedCategoryId == category.id) null else category.id },
                        onStatusChange = {
                            scope.launch {
                                db.wasteDao().updateCategory(category.copy(isActive = it))
                                refreshAll()
                            }
                        }
                    )
                }
            }
        }
    }
}

/**
 * A composable that represents a single item in the list of main waste categories on the [ManageCategoriesScreen].
 * It displays the category name, a toggle for its active status, and can be expanded to show sub-categories.
 * @param category The [WasteCategory] to display.
 * @param allFields A list of all available [LoggingField]s to be used by sub-categories.
 * @param isExpanded Whether the item is currently expanded to show details.
 * @param onExpand A lambda to toggle the expanded state.
 * @param onStatusChange A lambda to handle changes to the category's active status.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryItem(
    category: WasteCategory,
    allFields: List<LoggingField>,
    isExpanded: Boolean,
    onExpand: () -> Unit,
    onStatusChange: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val db = AppDatabase.getDatabase(context)

    var subCategories by remember { mutableStateOf<List<WasteSubCategory>>(emptyList()) }
    var newSubCategoryName by remember { mutableStateOf("") }
    val textColor = if (category.isActive) Color.White else Color.Gray

    fun refreshSubCategories() {
        scope.launch {
            subCategories = db.wasteDao().getSubCategoriesForCategory(category.id)
        }
    }

    LaunchedEffect(isExpanded) {
        if (isExpanded) {
            refreshSubCategories()
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f)),
        onClick = onExpand
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(category.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = textColor)
                    if (!category.isActive) {
                        Text("(Archived)", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                }
                Switch(checked = category.isActive, onCheckedChange = onStatusChange)
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(16.dp))
                Text("Sub-Categories:", style = MaterialTheme.typography.titleMedium, color = Color.White.copy(alpha = 0.8f))



                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = newSubCategoryName,
                        onValueChange = { newSubCategoryName = it },
                        label = { Text("New Sub-Category Name") },
                        modifier = Modifier.weight(1f),
                        colors = TextFieldDefaults.outlinedTextFieldColors(containerColor = Color.White.copy(alpha = 0.9f))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = {
                        scope.launch {
                            val trimmedName = newSubCategoryName.trim()
                            val trimmedLower = trimmedName.lowercase()
                            if (trimmedName.isNotBlank()) {
                                val existingSubs = db.wasteDao().getSubCategoriesForCategory(category.id)
                                if (existingSubs.none { it.name.lowercase() == trimmedLower }) {
                                    db.wasteDao().insertSubCategory(WasteSubCategory(name = trimmedName.capitalizeWords(), categoryId = category.id))
                                    newSubCategoryName = ""
                                    refreshSubCategories()
                                    Toast.makeText(context, "Sub-Category Added", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "This sub-category already exists", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }) { Text("Add") }
                }
                subCategories.forEach { subCategory ->
                    SubCategoryItem(
                        subCategory = subCategory,
                        allFields = allFields.filter { it.isActive }, // Only show active fields
                        onStatusChange = {
                            scope.launch {
                                db.wasteDao().updateSubCategory(subCategory.copy(isActive = it))
                                refreshSubCategories()
                            }
                        }
                    )
                }
            }
        }
    }
}

/**
 * A composable representing a single sub-category item within an expanded [CategoryItem].
 * It allows for managing the sub-category's status, assigning logging fields to it, and viewing assigned fields.
 * @param subCategory The [WasteSubCategory] data to display.
 * @param allFields A list of active [LoggingField]s that can be assigned.
 * @param onStatusChange A lambda to handle changes in the sub-category's active status.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubCategoryItem(
    subCategory: WasteSubCategory,
    allFields: List<LoggingField>,
    onStatusChange: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val db = AppDatabase.getDatabase(context)

    var assignedFields by remember { mutableStateOf<List<LoggingField>>(emptyList()) }
    var selectedField by remember { mutableStateOf<LoggingField?>(null) }
    var fieldMenuExpanded by remember { mutableStateOf(false) }
    val textColor = if (subCategory.isActive) Color.White else Color.Gray

    fun refreshAssignedFields() {
        scope.launch {
            assignedFields = db.wasteDao().getFieldsForSubCategory(subCategory.id)
        }
    }

    LaunchedEffect(Unit) { refreshAssignedFields() }

    Card(
        modifier = Modifier.fillMaxWidth().padding(start = 16.dp, top = 8.dp, bottom = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(subCategory.name, style = MaterialTheme.typography.titleMedium, color = textColor)
                    if (!subCategory.isActive) {
                        Text("(Archived)", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                }
                Switch(checked = subCategory.isActive, onCheckedChange = onStatusChange)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("Assigned Fields:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.8f))

            if (assignedFields.isEmpty()) {
                Text("No fields assigned.", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.6f))
            } else {
                assignedFields.forEach { field ->
                    Text("• ${field.fieldName}", style = MaterialTheme.typography.bodyMedium, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            ExposedDropdownMenuBox(expanded = fieldMenuExpanded, onExpandedChange = { fieldMenuExpanded = !fieldMenuExpanded }) {
                OutlinedTextField(
                    value = selectedField?.fieldName ?: "Select Field to Assign",
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = fieldMenuExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    colors = TextFieldDefaults.outlinedTextFieldColors(containerColor = Color.White.copy(alpha = 0.9f))
                )
                ExposedDropdownMenu(expanded = fieldMenuExpanded, onDismissRequest = { fieldMenuExpanded = false }) {
                    allFields.forEach { field ->
                        DropdownMenuItem(text = { Text(field.fieldName) }, onClick = { selectedField = field; fieldMenuExpanded = false })
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = {
                scope.launch {
                    if (selectedField != null) {
                        val isAssigned = db.wasteDao().isFieldAssignedToSubCategory(subCategory.id, selectedField!!.id) > 0
                        if (!isAssigned) {
                            db.wasteDao().assignFieldToSubCategory(SubCategoryField(subCategoryId = subCategory.id, fieldId = selectedField!!.id))
                            refreshAssignedFields()
                            Toast.makeText(context, "Field Assigned", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Field already assigned", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(context, "Please select a field", Toast.LENGTH_SHORT).show()
                    }
                }
            }) {
                Text("Assign Field")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
/**
 * An admin screen for managing the types of fields that can be logged for waste items (e.g., "Colour", "Brand").
 * It allows creating new fields and archiving/unarchiving existing ones.
 * @param onBackClick A lambda function to navigate back.
 */
@Composable
fun ManageFieldsScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val db = AppDatabase.getDatabase(context)

    var fields by remember { mutableStateOf<List<LoggingField>>(emptyList()) }
    var newFieldName by remember { mutableStateOf("") }

    fun refreshFields() {
        scope.launch {
            fields = db.wasteDao().getAllLoggingFields()
        }
    }

    LaunchedEffect(Unit) { refreshFields() }

    Box(modifier = Modifier.fillMaxSize().background(brush = Brush.verticalGradient(colors = listOf(MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.primary)))) {
        Column(modifier = Modifier.fillMaxSize().padding(24.dp).statusBarsPadding().navigationBarsPadding()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White) }
                Spacer(modifier = Modifier.width(16.dp))
                Text("Manage Field Types", style = MaterialTheme.typography.headlineLarge, color = Color.White)
            }
            Spacer(modifier = Modifier.height(24.dp))

            Text("Create New Field Type", style = MaterialTheme.typography.titleMedium, color = Color.White)
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = newFieldName,
                    onValueChange = { newFieldName = it },
                    label = { Text("Field Name (e.g., Colour)") },
                    modifier = Modifier.weight(1f),
                    colors = TextFieldDefaults.outlinedTextFieldColors(containerColor = Color.White.copy(alpha = 0.9f))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = {
                    scope.launch {
                        val trimmedName = newFieldName.trim()
                        val trimmedLower = trimmedName.lowercase()
                        if (trimmedName.isNotBlank()) {
                            val existingFields = db.wasteDao().getAllLoggingFields()
                            if (existingFields.none { it.fieldName.lowercase() == trimmedLower }) {
                                db.wasteDao().insertLoggingField(LoggingField(fieldName = trimmedName.capitalizeWords()))
                                newFieldName = ""
                                refreshFields()
                                Toast.makeText(context, "Field Created", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Field '$trimmedName' already exists", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }) { Text("Create") }
            }

            Divider(modifier = Modifier.padding(vertical = 24.dp), color = Color.White.copy(alpha = 0.5f))

            Text("Existing Field Types", style = MaterialTheme.typography.titleMedium, color = Color.White)
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(fields) { field ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = field.fieldName,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = if (field.isActive) Color.White else Color.Gray
                                )
                                if (!field.isActive) {
                                    Text("(Archived)", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                }
                            }
                            Switch(
                                checked = field.isActive,
                                onCheckedChange = {
                                    scope.launch {
                                        db.wasteDao().updateField(field.copy(isActive = it))
                                        refreshFields()
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
/**
 * The main screen of the application, which combines the home page content with a swipeable
 * bottom sheet for logging in.
 * @param loggedIn A boolean indicating if the user is currently logged in.
 * @param onLoginChange A callback that is invoked when the login status changes.
 * @param onMenuClick A lambda to open the navigation drawer.
 * @param navigateTo A lambda to handle navigation to other screens after a successful login.
 */
@Composable
fun LoginScreenWithSwipeableSheet(loggedIn: Boolean, onLoginChange: (Boolean) -> Unit, onMenuClick: () -> Unit,navigateTo: (String) -> Unit) {
    val sheetState = rememberStandardBottomSheetState(
        initialValue = SheetValue.PartiallyExpanded,
        skipHiddenState = true
    )
    val scaffoldState = rememberBottomSheetScaffoldState(bottomSheetState = sheetState)
    val scope = rememberCoroutineScope()
    val isSheetExpanding = sheetState.targetValue == SheetValue.Expanded
    val isSheetFullyExpanded = sheetState.currentValue == SheetValue.Expanded


    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 90.dp,
        sheetShape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp),
        sheetShadowElevation = 8.dp,
        sheetDragHandle = null,
        sheetContent = {
            LoginSheetContent(
                isExpanded = isSheetFullyExpanded,
                loggedIn = loggedIn,
                onLoginClick = {
                    scope.launch { sheetState.expand() }},
                onLoginSuccess = { onLoginChange(true) },
                navigateTo = navigateTo

            )
        },
        topBar = {

            CenterAlignedTopAppBar(
                title = {
                    Image(
                        painter = painterResource(id = R.drawable.litterboom_logo__2_),
                        contentDescription = "Home Logo",
                        modifier = Modifier.height(40.dp)
                    )
                },

                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu", tint = MaterialTheme.colorScheme.onBackground)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
                modifier = Modifier.statusBarsPadding()
            )
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.primary)
                    )
                )
        ) {
            AnimatedVisibility(visible = !isSheetExpanding, enter = fadeIn(), exit = fadeOut()) {
                CollapsedStateContent()
            }
            AnimatedVisibility(visible = isSheetExpanding, enter = fadeIn(), exit = fadeOut()) {
                ExpandedStateContent()
            }
        }
    }
}

/**
 * The content of the modal navigation drawer.
 * It displays the Litterboom header, user login status, and a list of navigation items.
 * @param currentScreen The route of the currently displayed screen.
 * @param navItems The list of navigation item labels to display.
 * @param isAdmin A boolean indicating if the current user has admin privileges.
 * @param onItemClick A callback that is invoked when a navigation item is clicked.
 */
@Composable
fun AppDrawerContent(
    currentScreen: String,
    navItems: List<String>,
    isAdmin: Boolean,
    onItemClick: (String) -> Unit
) {
    val actionItems = setOf("Admin Panel", "Event Selection", "Logout")

    val iconMap = mapOf(
        "Source to Sea" to Icons.Default.Home,
        "Interception" to Icons.Default.Waves,
        "Education" to Icons.Default.School,
        "Innovation" to Icons.Default.WbIncandescent,
        "Our Story" to Icons.Default.Info,
        "The Team" to Icons.Default.People,
        "Contact" to Icons.Default.Email,
        "Admin Panel" to Icons.Default.AdminPanelSettings,
        "Event Selection" to Icons.Default.ListAlt,
        "Logout" to Icons.AutoMirrored.Filled.ExitToApp
    )

    ModalDrawerSheet(
        modifier = Modifier.width(280.dp),
        drawerContainerColor = MaterialTheme.colorScheme.primary
    ) {
        // header for the drawer
        Text(
            text = "Litterboom",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 28.dp, vertical = 20.dp)
        )
        if (CurrentUserManager.currentUser != null) {
            Text(
                text = "Logged in as ${CurrentUserManager.currentUser?.role}",
                color = Color.White.copy(alpha = 0.8f),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 28.dp)
            )
        }
        Spacer(Modifier.height(16.dp))

        navItems.forEach { item ->
            val isSelected = item == currentScreen

            // Check if the item is an action item
            if (item in actionItems) {
                // styling for action buttons
                NavigationDrawerItem(
                    label = {
                        Text(
                            item,
                            style = MaterialTheme.typography.bodyLarge,
                            // Make text bold to stand out
                            fontWeight = FontWeight.Bold
                        )
                    },
                    selected = isSelected,
                    onClick = { onItemClick(item) },
                    // Provide an icon
                    icon = {
                        Icon(
                            imageVector = iconMap[item]!!,
                            contentDescription = item,
                            // Dim the icon colour slightly
                            tint = Color.White.copy(alpha = 0.9f)
                        )
                    },
                    modifier = Modifier
                        .padding(NavigationDrawerItemDefaults.ItemPadding)
                        // Add padding to make it look like a distinct button
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    colors = NavigationDrawerItemDefaults.colors(
                        // Use a brighter colour for the button itself
                        selectedContainerColor = MaterialTheme.colorScheme.secondary,
                        unselectedContainerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f),
                        selectedTextColor = Color.White,
                        unselectedTextColor = Color.White,
                        selectedIconColor = Color.White,
                        unselectedIconColor = Color.White.copy(alpha = 0.9f)
                    ),
                    // Add rounded corners
                    shape = RoundedCornerShape(12.dp)
                )

                // Add a divider after Event Selection
                if (item == "Event Selection") {
                    Divider(color = Color.White.copy(alpha = 0.2f), modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                }
            } else {
                NavigationDrawerItem(
                    label = { Text(item, style = MaterialTheme.typography.bodyLarge) },
                    selected = isSelected,
                    onClick = { onItemClick(item) },
                    icon = {
                        iconMap[item]?.let {
                            Icon(imageVector = it, contentDescription = item)
                        }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedTextColor = Color.White,
                        selectedIconColor = Color.White,
                        // Make selected item darker to contrast with action buttons
                        selectedContainerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f),
                        unselectedTextColor = Color.White.copy(alpha = 0.9f),
                        unselectedIconColor = Color.White.copy(alpha = 0.9f),
                        unselectedContainerColor = Color.Transparent
                    )
                )
            }
        }
    }
}

/**
 * The content displayed inside the swipeable bottom sheet on the main screen.
 * It handles both the collapsed "Login" prompt and the expanded login form.
 * @param isExpanded Whether the sheet is fully expanded.
 * @param loggedIn A boolean indicating if a user is currently logged in.
 * @param onLoginClick A lambda to handle the action of expanding the sheet.
 * @param onLoginSuccess A callback invoked upon successful login.
 * @param navigateTo A lambda for navigating to another screen post-login.
 */
@Composable
fun LoginSheetContent(isExpanded: Boolean, loggedIn: Boolean, onLoginClick: () -> Unit, onLoginSuccess: () -> Unit,navigateTo: (String) -> Unit) {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(false) }
    var loginMessage by remember { mutableStateOf("") }

    // Reset fields when the user logs out.
    LaunchedEffect(loggedIn) {
        if (!loggedIn) {
            username = ""
            password = ""
            loginMessage = ""
            rememberMe = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp)
            .padding(top = 12.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Color.LightGray)
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Determine the header text based on login state.
        val headerText = if (CurrentUserManager.isLoggedIn()) "Logged In" else "Login"

        if (!isExpanded) {
            Text(
                headerText,
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.pointerInput(Unit) {
                    detectTapGestures(onTap = { onLoginClick() })
                }
            )
        } else {
            if (!loggedIn) {
                Text(
                    "Login",
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        val image =
                            if (isPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                            Icon(image, "Toggle password visibility")
                        }
                    },
                    textStyle = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(checked = rememberMe, onCheckedChange = { rememberMe = it })
                    Text(
                        "Remember Me",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        scope.launch {
                            if (username.isNotBlank() && password.isNotBlank()) {
                                try {
                                    val response = ApiClient.apiService.login(User(username = username, password = password, role = "", id = 0))
                                    val auth = FirebaseAuth.getInstance()
                                    val result = auth.signInWithCustomToken(response.token).await()
                                    val token = result.user?.getIdToken(true)?.await()?.token
                                    if (token != null) {
                                        SessionManager.saveToken(context, token)
                                        Log.d("LOGIN", "ID Token saved: ${token.take(100)}...")
                                    } else {
                                        Log.e("LOGIN", "Failed to get ID token! User: ${result.user?.uid}")
                                    }

                                    val fullUserResponse = ApiClient.apiService.getUserById(response.userId)
                                    if (!fullUserResponse.isActive) {
                                        loginMessage = "Your account has been disabled. Contact admin."
                                        return@launch
                                    }

                                    val user = User(
                                        id = response.userId,
                                        username = username,
                                        password = "",
                                        role = response.role,
                                        isActive = fullUserResponse.isActive
                                    )

                                    loginMessage = "Login successful as ${response.role}!"
                                    if (rememberMe) {
                                        SessionManager.saveUserSession(context, user)
                                    }
                                    CurrentUserManager.login(user)
                                    onLoginSuccess()
                                    if (response.role == "Admin") {
                                        navigateTo("Admin Panel")
                                    } else {
                                        val intent = Intent(context, EventSelectionActivity::class.java)
                                        context.startActivity(intent)
                                    }
                                } catch (e: Exception) {
                                    val errorMessage = when (e) {
                                        is retrofit2.HttpException -> {
                                            try {
                                                val errorBody = e.response()?.errorBody()?.string()
                                                val json = JSONObject(errorBody ?: "{}")
                                                json.optString("message", "Login failed")
                                            } catch (ex: Exception) {
                                                "Login failed"
                                            }
                                        }
                                        else -> "No internet connection"
                                    }

                                    loginMessage = when {
                                        errorMessage.contains("Incorrect username", ignoreCase = true) ->
                                            "Incorrect username or password"
                                        errorMessage.contains("internet", ignoreCase = true) ->
                                            "No internet connection"
                                        else ->
                                            errorMessage
                                    }

                                    Log.e("Login", "Login failed", e)
                                }
                            } else {
                                loginMessage = "Enter username and password."
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("LOGIN", style = MaterialTheme.typography.labelLarge)
                }
                Spacer(modifier = Modifier.height(16.dp))


                if (loginMessage.isNotEmpty()) {
                    Text(
                        text = loginMessage,
                        color = if (loginMessage.startsWith("Invalid")) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else { // UI to show when the user is already logged in.
                Text(
                    "You are logged in.",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
/**
 * An admin screen for creating new events.
 * Provides fields for event name, date (via a date picker), and location (via Google Places Autocomplete).
 * @param onEventCreated A callback invoked with the newly created event data.
 * @param onBackClick A lambda to navigate back to the admin panel.
 */
@Composable
fun CreateEventScreen(onEventCreated: (Event) -> Unit, onBackClick: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var eventName by remember { mutableStateOf("") }

    var eventDate by remember { mutableStateOf(Calendar.getInstance().time) }
    val calendar = Calendar.getInstance()
    calendar.time = eventDate
    val datePickerDialog = android.app.DatePickerDialog(context, { _, year, month, dayOfMonth ->
        calendar.set(year, month, dayOfMonth)
        eventDate = calendar.time
    }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))

    var eventLocation by remember { mutableStateOf("") }
    val placeFields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG)
    val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, placeFields)
        .setCountries(listOf("ZA"))
        .build(context)
    val autocompleteLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        when (result.resultCode) {
            Activity.RESULT_OK -> {
                val place = result.data?.let { Autocomplete.getPlaceFromIntent(it) }
                // Update state with the result
                eventLocation = place?.address ?: ""
            }
            AutocompleteActivity.RESULT_ERROR -> {
                val status = result.data?.let { Autocomplete.getStatusFromIntent(it) }
                Toast.makeText(context, "Error: ${status?.statusMessage}", Toast.LENGTH_SHORT).show()
            }
            Activity.RESULT_CANCELED -> {
                // User canceled the operation
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(brush = Brush.verticalGradient(colors = listOf(MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.primary)))) {
        Column(modifier = Modifier.fillMaxSize().padding(24.dp).statusBarsPadding().navigationBarsPadding()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White) }
                Spacer(modifier = Modifier.width(16.dp))
                Text("Create an Event", style = MaterialTheme.typography.headlineLarge, color = Color.White)
            }
            Spacer(modifier = Modifier.height(32.dp))
            OutlinedTextField(value = eventName, onValueChange = { eventName = it }, label = { Text("Event Name") }, modifier = Modifier.fillMaxWidth(), colors = TextFieldDefaults.outlinedTextFieldColors(containerColor = Color.White.copy(alpha = 0.9f)))
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = eventLocation,
                onValueChange = { }, // Read-only
                label = { Text("Event Location") },
                placeholder = { Text("Click to search location") },
                modifier = Modifier
                    .fillMaxWidth()
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = {
                            // Launch the autocomplete activity
                            autocompleteLauncher.launch(intent)
                        })
                    },
                readOnly = true,
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    containerColor = Color.White.copy(alpha = 0.9f),
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledLabelColor = MaterialTheme.colorScheme.onSurface,
                    disabledPlaceholderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    disabledTrailingIconColor = MaterialTheme.colorScheme.onSurface
                ),
                enabled = false, // Use disabled state to make it look non-editable
                trailingIcon = {
                    Icon(Icons.Default.LocationOn, "Search Location")
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { datePickerDialog.show() }, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Text("Date: ${android.text.format.DateFormat.format("yyyy-MM-dd", eventDate)}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = {
                    scope.launch {
                        if (eventName.isNotBlank() && eventLocation.isNotBlank()) {
                            AppDatabase.getDatabase(context).eventDao().insertEvent(Event(name = eventName, date = eventDate.time, location = eventLocation))
                            Toast.makeText(context, "Event created!", Toast.LENGTH_SHORT).show()
                            onEventCreated(Event(name = eventName, date = eventDate.time, location = eventLocation))
                        } else {
                            Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Text("Create Event", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
        }
    }
}

/**
 * An admin screen that lists all created events.
 * It allows filtering events by a date range and provides a toggle to open or close each event.
 * @param onBackClick A lambda to navigate back to the admin panel.
 */
@Composable
fun EventListScreen(onBackClick: () -> Unit) {

    val context = LocalContext.current
    var events by remember { mutableStateOf(listOf<Event>()) }
    val scope = rememberCoroutineScope()
    var startDate by remember { mutableStateOf<Date?>(null) }
    var endDate by remember { mutableStateOf<Date?>(null) }
    val db = AppDatabase.getDatabase(context)

    fun refreshEvents() {
        scope.launch {
            events = AppDatabase.getDatabase(context).eventDao().getAllEvents()
        }
    }

    LaunchedEffect(Unit) { refreshEvents() }
    fun showDatePicker(isStartDate: Boolean) {
        val calendar = Calendar.getInstance()
        android.app.DatePickerDialog(
            context,
            { _, year, month, day ->
                val cal = Calendar.getInstance().apply { set(year, month, day) }
                if (isStartDate) startDate = cal.time else endDate = cal.time
                scope.launch {
                    val allEvents = db.eventDao().getAllEvents()
                    events = allEvents.filter { event ->
                        val afterStartDate = startDate?.let { event.date >= it.time } != false
                        val beforeEndDate = endDate?.let { event.date <= it.time } != false
                        afterStartDate && beforeEndDate
                    }
                }
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    Box(
        modifier = Modifier.fillMaxSize().background(
            brush = Brush.verticalGradient(
                colors = listOf(
                    MaterialTheme.colorScheme.secondary,
                    MaterialTheme.colorScheme.primary
                )
            )
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp).statusBarsPadding()
                .navigationBarsPadding()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        "Back",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    "Event List",
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { showDatePicker(true) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Text(startDate?.let {
                        android.text.format.DateFormat.format("yyyy-MM-dd", it).toString()
                    } ?: "Start Date", color = MaterialTheme.colorScheme.primary)
                }
                Button(
                    onClick = { showDatePicker(false) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Text(endDate?.let {
                        android.text.format.DateFormat.format("yyyy-MM-dd", it).toString()
                    } ?: "End Date", color = MaterialTheme.colorScheme.primary)
                }
                Button(
                    onClick = {
                        startDate = null
                        endDate = null
                        scope.launch { events = db.eventDao().getAllEvents() }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Text("Clear", color = MaterialTheme.colorScheme.primary)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            LazyColumn {
                items(events) { event ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.2f))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    event.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White
                                )
                                Text(
                                    "${
                                        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(
                                            Date(event.date)
                                        )
                                    } - ${event.location}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                                Text(
                                    text = if (event.isOpen) "Status: Open" else "Status: Closed",
                                    fontWeight = FontWeight.Bold,
                                    color = if (event.isOpen) Color(0xFF4CAF50) else Color(
                                        0xFFF44336
                                    ) // Green for open, Red for closed
                                )
                            }
                            // Switch to toggle the event status
                            Switch(
                                checked = event.isOpen,
                                onCheckedChange = { isOpen ->
                                    scope.launch {
                                        val updatedEvent = event.copy(isOpen = isOpen)
                                        AppDatabase.getDatabase(context).eventDao()
                                            .updateEvent(updatedEvent)
                                        refreshEvents() // Refresh the list to show the change
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * A composable that displays a piece of text with a clickable link to the Litterboom Project website.
 * Uses [AnnotatedString] to create a hyperlink within the text.
 * @param modifier Modifier for this composable.
 */
@Suppress("DEPRECATION")
@Composable
fun ClickableWebsiteText(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val url = "https://www.thelitterboomproject.com/"

    val annotatedString = buildAnnotatedString {
        withStyle(style = SpanStyle(color = Color.White, fontSize = 16.sp)) {
            append("If you wish to support or contact us, please visit our website ")
        }
        pushStringAnnotation(tag = "URL", annotation = url)
        withStyle(style = SpanStyle(color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)) {
            append("HERE")
        }
        pop()
    }

    ClickableText(
        text = annotatedString,
        style = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
        modifier = modifier,
        onClick = { offset ->

            annotatedString.getStringAnnotations(tag = "URL", start = offset, end = offset)
                .firstOrNull()?.let {

                    val intent = Intent(Intent.ACTION_VIEW, it.item.toUri())
                    context.startActivity(intent)
                }
        }
    )
}

/**
 * The background content displayed on the main screen when the login sheet is collapsed.
 * It shows promotional text and a collage of images.
 */
@Composable
fun CollapsedStateContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(90.dp))
        Text(
            text = "Thank you for supporting the cause!",
            style = MaterialTheme.typography.headlineLarge,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, shape = RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Text(
                text = "Join the movement to clean our rivers and oceans",
                style = MaterialTheme.typography.headlineLarge,
                textAlign = TextAlign.Center,
                lineHeight = 32.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp),
            contentAlignment = Alignment.Center
        ) {
            // Image 1 (Back Left)
            Image(
                painter = painterResource(id = R.drawable.river_cleanup),
                contentDescription = "Community cleanup",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(180.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .align(Alignment.CenterStart)

            )
            // Image 2 (Top Right)
            Image(
                painter = painterResource(id = R.drawable.plastic_bottles),
                contentDescription = "River cleanup",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(160.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .align(Alignment.TopEnd)

            )
            // Image 3 (Bottom Center)
            Image(
                painter = painterResource(id = R.drawable.litterboom_employee),
                contentDescription = "Collected plastic waste",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(150.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .align(Alignment.BottomCenter)
                    .offset(y = (5).dp)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        ClickableWebsiteText(modifier = Modifier.padding(bottom = 120.dp))
    }
}

/**
 * The background content displayed on the main screen when the login sheet is expanded.
 * This version has slightly different text content.
 */
@Composable
fun ExpandedStateContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(90.dp))
        Text(
            text = "Thank you for supporting the cause!",
            style = MaterialTheme.typography.headlineLarge,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Regretfully,\nThis app is for Litterboom employees only",
            color = Color.White,
            textAlign = TextAlign.Center,
            lineHeight = 26.sp,
            style = MaterialTheme.typography.headlineLarge.copy(fontSize = 20.sp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp),
            contentAlignment = Alignment.Center
        ) {
            // Image 1 (Back Left)
            Image(
                painter = painterResource(id = R.drawable.river_cleanup),
                contentDescription = "Community cleanup",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(180.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .align(Alignment.CenterStart)

            )
            // Image 2 (Top Right)
            Image(
                painter = painterResource(id = R.drawable.plastic_bottles),
                contentDescription = "River cleanup",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(160.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .align(Alignment.TopEnd)

            )
            // Image 3 (Bottom Center)
            Image(
                painter = painterResource(id = R.drawable.litterboom_employee),
                contentDescription = "Collected plastic waste",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(150.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .align(Alignment.BottomCenter)
                    .offset(y = (5).dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        ClickableWebsiteText()
    }
}


/**
 * A preview composable for visualizing the [AppWithNavDrawer] component in Android Studio.
 */
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LoginScreenPreview() {
    LitterboomTheme {
        AppWithNavDrawer()
    }
}
