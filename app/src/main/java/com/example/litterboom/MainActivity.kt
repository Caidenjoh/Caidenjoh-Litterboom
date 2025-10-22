package com.example.litterboom

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import com.example.litterboom.data.CurrentUserManager
import com.example.litterboom.data.Event
import com.example.litterboom.data.LoggedWaste
import com.example.litterboom.data.LoggingField
import com.example.litterboom.data.SessionManager
import com.example.litterboom.data.SubCategoryField
import com.example.litterboom.data.User
import com.example.litterboom.data.WasteCategory
import com.example.litterboom.data.WasteSubCategory
import com.example.litterboom.ui.EventSelectionActivity
import com.example.litterboom.ui.theme.LitterboomTheme
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        AppDatabase.getDatabase(this)

        setContent {
            LitterboomTheme {
                AppWithNavDrawer()
            }
        }
    }
}

@Composable
fun AppWithNavDrawer() {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var currentScreen by remember { mutableStateOf("Source to Sea") }
    var loggedIn by rememberSaveable { mutableStateOf(false) }
    val isAdmin = CurrentUserManager.isAdmin()
    val context = LocalContext.current
    val navItems = remember(loggedIn, isAdmin) {
        listOf("Source to Sea", "Interception", "Education", "Innovation", "Our Story", "The Team", "Contact").plus(
            if (loggedIn && isAdmin) listOf("Admin Panel") else emptyList()
        ).plus(if (loggedIn) listOf("Event Selection", "Logout") else emptyList())
    }

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
                        "Logout" -> {
                            SessionManager.clearSession(context)
                            CurrentUserManager.logout()
                            loggedIn = false
                            currentScreen = "Source to Sea"
                        }
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
        Crossfade(targetState = currentScreen, label = "ScreenCrossfade") { screen ->
            when (screen) {
                "Source to Sea" -> LoginScreenWithSwipeableSheet(loggedIn, { loggedIn = it }, { scope.launch { drawerState.open() } })
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
                "Event Logs" -> EventLogsScreen(onBackClick = { currentScreen = "Admin Panel" })
                else -> {
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
@Composable
fun InterceptionScreen(onBackClick: () -> Unit) { //interception info page
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
@Composable
fun EducationScreen(onBackClick: () -> Unit) { //education screen
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


@Composable
fun AdminPanelScreen(onMenuClick: () -> Unit, navigateTo: (String) -> Unit) {
    Box(modifier = Modifier.fillMaxSize().background(brush = Brush.verticalGradient(colors = listOf(MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.primary)))) {
        Column(modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
            Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onMenuClick) { Icon(Icons.Default.Menu, "Menu", tint = Color.White) }
                Spacer(modifier = Modifier.width(16.dp))
                Text("Admin Panel", style = MaterialTheme.typography.headlineLarge, color = Color.White)
            }
            AdminMenu(
                onAddUserClick = { navigateTo("Add User") },
                onCreateEventClick = { navigateTo("Create Event") },
                onEventListClick = { navigateTo("Event List") },
                onManageCategoriesClick = { navigateTo("Manage Categories") },
                onManageFieldsClick = { navigateTo("Manage Fields") },
                onEventLogsClick = { navigateTo("Event Logs") }
            )
        }
    }
}

@Composable
fun AdminMenu(onAddUserClick: () -> Unit, onCreateEventClick: () -> Unit, onEventListClick: () -> Unit, onManageCategoriesClick: () -> Unit, onManageFieldsClick: () -> Unit, onEventLogsClick: () -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                AdminIconButton("Add User", Icons.Default.PersonAdd, onAddUserClick)
                AdminIconButton("Create Event", Icons.Default.Event, onCreateEventClick)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                AdminIconButton("Event List", Icons.AutoMirrored.Filled.ViewList, onEventListClick)
                AdminIconButton("Manage Categories", Icons.Default.Category, onManageCategoriesClick)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ){
                AdminIconButton("Manage Fields", Icons.Default.Settings, onManageFieldsClick)
                Spacer(modifier = Modifier.width(16.dp))
                AdminIconButton("Event Data", Icons.Default.Assessment, onClick = onEventLogsClick)
            }
        }
    }
}

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

@Composable
private fun LoggedWasteDetailScreen(event: Event, onBack: () -> Unit) {
    val context = LocalContext.current
    var loggedItems by remember { mutableStateOf<List<Pair<LoggedWaste, User?>>>(emptyList()) }

    LaunchedEffect(event) {
        val db = AppDatabase.getDatabase(context)
        val wasteItems = db.loggedWasteDao().getWasteForEvent(event.id)
        val userWastePairs = wasteItems.map { waste ->
            val user = db.userDao().getUserById(waste.userId)
            Pair(waste, user)
        }
        loggedItems = userWastePairs
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp).statusBarsPadding().navigationBarsPadding()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White) }
            Spacer(modifier = Modifier.width(16.dp))
            Text("Logs for ${event.name}", style = MaterialTheme.typography.headlineMedium, color = Color.White)
        }
        Spacer(modifier = Modifier.height(16.dp))

        if (loggedItems.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No waste has been logged for this event yet.", color = Color.White)
            }
        } else {
            LazyColumn {
                items(loggedItems) { (waste, user) ->
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.2f))) {
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
@Composable
fun AdminIconButton(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) { //Icons and cards for admin functionality buttons
    Card(
        onClick = onClick,
        modifier = Modifier.size(150.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = text, modifier = Modifier.size(48.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
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
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(users) { user ->
                    Text(
                        text = "${user.id}: ${user.username} (${user.role})",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    Divider(color = Color.White.copy(alpha = 0.3f))
                }
            }
        }
    }
}

// Helper function to capitalise words
fun String.capitalizeWords(): String = this.split(" ")
    .joinToString(" ") { it.replaceFirstChar { char -> if (char.isLowerCase()) char.titlecase(Locale.getDefault()) else char.toString() } }

@OptIn(ExperimentalMaterial3Api::class)
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
                        if (trimmedName.isNotBlank()) {
                            if (db.wasteDao().getCategoryByName(trimmedName) == null) {
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

            Text("Existing Categories", style = MaterialTheme.typography.titleMedium, color = Color.White)
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

// Helper for the Category List
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
                            if (trimmedName.isNotBlank()) {
                                if (db.wasteDao().getSubCategoryByName(trimmedName, category.id) == null) {
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
            }
        }
    }
}

// Helper for the Category Item
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
                        if (trimmedName.isNotBlank()) {
                            if (db.wasteDao().getFieldByName(trimmedName) == null) {
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

/*@Composable
fun AdminControlPanelScreen(onMenuClick: () -> Unit, onItemClick: (String) -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("User") }
    var roleMenuExpanded by remember { mutableStateOf(false) }
    var successMessage by remember { mutableStateOf("") }
    var users by remember { mutableStateOf(listOf<User>()) }

    LaunchedEffect(Unit) {
        val db = AppDatabase.getDatabase(context)
        users = db.userDao().getAllUsers()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onMenuClick) {
                Icon(Icons.Default.Menu, contentDescription = "Menu")
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text("Admin Panel", style = MaterialTheme.typography.headlineLarge)
        }

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Role: ", style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.width(8.dp))
            Box {
                Button(onClick = { roleMenuExpanded = true }) {
                    Text(selectedRole)
                }
                DropdownMenu(
                    expanded = roleMenuExpanded,
                    onDismissRequest = { roleMenuExpanded = false }
                ) {
                    listOf("User", "Admin").forEach { role ->
                        DropdownMenuItem(
                            text = { Text(role) },
                            onClick = {
                                selectedRole = role
                                roleMenuExpanded = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                scope.launch {
                    if (username.isNotBlank() && password.isNotBlank()) {
                        val db = AppDatabase.getDatabase(context)
                        db.userDao().insertUser(
                            User(
                                username = username,
                                password = password,
                                role = selectedRole
                            )
                        )
                        username = ""
                        password = ""
                        selectedRole = "User"
                        successMessage = "User added successfully!"

                        users = db.userDao().getAllUsers()
                    } else {
                        successMessage = "Please enter both username and password."
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Add User")
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (successMessage.isNotEmpty()) {
            Text(
                text = successMessage,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyLarge
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("Current Users:", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            items(users) { user ->
                Text(
                    text = "${user.id}: ${user.username} (${user.role})",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                Divider(color = Color.Gray.copy(alpha = 0.3f))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { onItemClick("Create Event") },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Create Event")
        }

    }
}*/

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreenWithSwipeableSheet(loggedIn: Boolean, onLoginChange: (Boolean) -> Unit, onMenuClick: () -> Unit) { //swipeable sheet for login page
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
                onLoginSuccess = { onLoginChange(true) }

            )
        },
        topBar = { //top bar for app

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

@Composable
fun AppDrawerContent(
    currentScreen: String,
    navItems: List<String>,
    isAdmin: Boolean,
    onItemClick: (String) -> Unit
) {
    ModalDrawerSheet(
        modifier = Modifier.width(280.dp),
        drawerContainerColor = MaterialTheme.colorScheme.primary
    ) {
        Spacer(Modifier.height(16.dp))
        if (CurrentUserManager.currentUser != null) {
            Text(
                text = "Logged in as ${CurrentUserManager.currentUser?.role}",
                color = Color.White,
                modifier = Modifier.padding(16.dp)
            )
        }
        navItems.forEach { item ->
            val isSelected = item == currentScreen
            NavigationDrawerItem(
                label = { Text(item, style = MaterialTheme.typography.bodyLarge) },
                selected = isSelected,
                onClick = { onItemClick(item) },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                colors = NavigationDrawerItemDefaults.colors(
                    selectedTextColor = Color.White,
                    selectedContainerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f),
                    unselectedTextColor = Color.White,
                    unselectedContainerColor = Color.Transparent
                )
            )
        }
    }
}

@Composable
fun LoginSheetContent(isExpanded: Boolean, loggedIn: Boolean, onLoginClick: () -> Unit, onLoginSuccess: () -> Unit) { //login sheet content

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(false) }
    var loginMessage by remember { mutableStateOf("") }

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
                                val db = AppDatabase.getDatabase(context)
                                val user = db.userDao().getUser(username, password)
                                if (user != null) {
                                    loginMessage = "Login successful as ${user.role}!"
                                    if (rememberMe) {
                                        SessionManager.saveUserSession(context, user)
                                    }
                                    CurrentUserManager.login(user)
                                    onLoginSuccess()
                                    val intent = Intent(context, EventSelectionActivity::class.java)
                                    context.startActivity(intent)
                                } else {
                                    loginMessage = "Invalid username or password."

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
            } else { // show when the user is already logged in
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
@Composable
fun CreateEventScreen(onEventCreated: (Event) -> Unit, onBackClick: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var eventName by remember { mutableStateOf("") }
    var eventDate by remember { mutableStateOf(Calendar.getInstance().time) }
    var eventLocation by remember { mutableStateOf("") }
    val calendar = Calendar.getInstance()
    calendar.time = eventDate
    val datePickerDialog = android.app.DatePickerDialog(context, { _, year, month, dayOfMonth ->
        calendar.set(year, month, dayOfMonth)
        eventDate = calendar.time
    }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
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
            OutlinedTextField(value = eventLocation, onValueChange = { eventLocation = it }, label = { Text("Event Location") }, modifier = Modifier.fillMaxWidth(), colors = TextFieldDefaults.outlinedTextFieldColors(containerColor = Color.White.copy(alpha = 0.9f)))
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

@Suppress("DEPRECATION")
@Composable
fun ClickableWebsiteText(modifier: Modifier = Modifier) { //clickable text for redirect to website
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

@Composable
fun CollapsedStateContent() { //background content when login is collapsed
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

@Composable
fun ExpandedStateContent() { //background content when login is expanded
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

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LoginScreenPreview() {
    LitterboomTheme {
        AppWithNavDrawer()
    }
}