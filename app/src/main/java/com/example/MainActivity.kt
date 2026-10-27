package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.example.ui.screens.*
import com.example.ui.components.SnowhiteBrandIcon
import com.example.ui.theme.SnowhiteTheme
import com.example.viewmodel.SnowhiteViewModel
import kotlinx.coroutines.launch

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

fun NavHostController.safePopBackStack() {
    if (this.previousBackStackEntry != null) {
        this.popBackStack()
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SnowhiteTheme {
                SnowhiteMainApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SnowhiteMainApp() {
    val context = LocalContext.current
    val viewModel: SnowhiteViewModel = viewModel()
    val navController = rememberNavController()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "home"

    val cartItems by viewModel.cartItems.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val isSwMember by viewModel.isSwMember.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val notificationsList by viewModel.notificationsList.collectAsState()

    val unreadNotificationCount = remember(notificationsList) { notificationsList.count { !it.isRead } }
    var showNotificationDialog by remember { mutableStateOf(false) }

    val totalCartQty = remember(cartItems) { cartItems.sumOf { it.quantity } }
    val grandTotal = viewModel.calculateGrandTotal()

    LaunchedEffect(currentUser) {
        viewModel.startBackgroundWatcher(context)
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val permissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
            onResult = { /* granted or denied */ }
        )
        LaunchedEffect(Unit) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    if (currentRoute == "home") {
        BackHandler(enabled = drawerState.isOpen) {
            scope.launch { drawerState.close() }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = currentRoute == "home" || drawerState.isOpen,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = Color.White,
                modifier = Modifier.width(300.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0061E0))
                        .padding(24.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            SnowhiteBrandIcon(logoUrl = settings.logoUrl, size = 72.dp)
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "Snowhite",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Laundry & Dry Cleaning • Karachi",
                                    fontSize = 12.sp,
                                    color = Color(0xFFE0EFFE)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        if (currentUser != null) {
                            Column(
                                modifier = Modifier.clickable {
                                    scope.launch { drawerState.close() }
                                    navController.navigate("profile") { launchSingleTop = true }
                                }
                            ) {
                                Text(
                                    text = "Logged in as:",
                                    fontSize = 11.sp,
                                    color = Color(0xFFBAE6FD)
                                )
                                Text(
                                    text = currentUser?.name ?: "Customer",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                if (isSwMember) {
                                    Surface(
                                        color = Color(0xFFFEF3C7),
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.padding(top = 4.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Star,
                                                contentDescription = null,
                                                tint = Color(0xFFD97706),
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "SW GOLD MEMBER ACTIVE",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFB45309)
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            Button(
                                onClick = {
                                    scope.launch { drawerState.close() }
                                    navController.navigate("auth")
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Log In / Register", color = Color(0xFF0061E0), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = null, tint = Color(0xFF0061E0)) },
                    label = { Text("Home", fontWeight = FontWeight.SemiBold) },
                    selected = currentRoute == "home",
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate("home") {
                            popUpTo("home") { inclusive = false }
                            launchSingleTop = true
                        }
                    }
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Category, contentDescription = null, tint = Color(0xFF0061E0)) },
                    label = { Text("Departments", fontWeight = FontWeight.SemiBold) },
                    selected = currentRoute == "departments",
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate("departments") { launchSingleTop = true }
                    }
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.LocalShipping, contentDescription = null, tint = Color(0xFF0061E0)) },
                    label = { Text("My Orders & Tracking", fontWeight = FontWeight.SemiBold) },
                    selected = currentRoute == "orders",
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate("orders") { launchSingleTop = true }
                    }
                )

                if (currentUser != null) {
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF0061E0)) },
                        label = { Text("My Profile & Settings", fontWeight = FontWeight.SemiBold) },
                        selected = currentRoute == "profile",
                        onClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate("profile") { launchSingleTop = true }
                        }
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                Text(
                    text = "HELP & SUPPORT",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF94A3B8),
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp)
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Phone, contentDescription = null, tint = Color(0xFF16A34A)) },
                    label = { Text("WhatsApp Support (03111110500)", fontSize = 13.sp) },
                    selected = false,
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/923111110500"))
                        context.startActivity(intent)
                    }
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Email, contentDescription = null, tint = Color(0xFF0061E0)) },
                    label = { Text("cs@snowhite.com.pk", fontSize = 13.sp) },
                    selected = false,
                    onClick = {
                        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:cs@snowhite.com.pk"))
                        context.startActivity(intent)
                    }
                )

                if (currentUser != null) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Logout, contentDescription = null, tint = Color(0xFFDC2626)) },
                        label = { Text("Logout", fontWeight = FontWeight.SemiBold, color = Color(0xFFDC2626)) },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            viewModel.logout()
                            navController.navigate("home") {
                                popUpTo("home") { inclusive = false }
                                launchSingleTop = true
                            }
                        }
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable {
                                navController.navigate("home") {
                                    popUpTo("home") { inclusive = false }
                                    launchSingleTop = true
                                }
                            }
                        ) {
                            SnowhiteBrandIcon(logoUrl = settings.logoUrl, size = 32.dp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Snowhite",
                                fontSize = 19.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF0061E0),
                                maxLines = 1,
                                softWrap = false,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = { scope.launch { drawerState.open() } }
                        ) {
                            Icon(Icons.Default.Menu, contentDescription = "Open Drawer", tint = Color(0xFF0061E0))
                        }
                    },
                    actions = {
                        if (isSwMember) {
                            Surface(
                                color = Color(0xFFFEF3C7),
                                shape = RoundedCornerShape(10.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEAB308)),
                                modifier = Modifier
                                    .padding(end = 2.dp)
                                    .clickable { navController.navigate("membership") }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = "SW Gold Member",
                                        tint = Color(0xFFD97706),
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = "★ GOLD",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFB45309)
                                    )
                                }
                            }
                        }
                        IconButton(onClick = { showNotificationDialog = true }) {
                            BadgedBox(
                                badge = {
                                    if (unreadNotificationCount > 0) {
                                        Badge(containerColor = Color(0xFFDC2626), contentColor = Color.White) {
                                            Text("$unreadNotificationCount")
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "Notifications",
                                    tint = Color(0xFF0061E0)
                                )
                            }
                        }

                        IconButton(onClick = { navController.navigate("checkout") }) {
                            BadgedBox(
                                badge = {
                                    if (totalCartQty > 0) {
                                        Badge(containerColor = Color(0xFF0061E0), contentColor = Color.White) {
                                            Text("$totalCartQty")
                                        }
                                    }
                                }
                            ) {
                                Icon(Icons.Default.ShoppingCart, contentDescription = "Cart", tint = Color(0xFF0061E0))
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.White
                    )
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = Color.White,
                    tonalElevation = 8.dp
                ) {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("Home") },
                        selected = currentRoute == "home",
                        onClick = {
                            navController.navigate("home") {
                                popUpTo("home") { inclusive = false }
                                launchSingleTop = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF0061E0),
                            selectedTextColor = Color(0xFF0061E0),
                            indicatorColor = Color(0xFFF0F7FF)
                        )
                    )

                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Category, contentDescription = "Departments") },
                        label = { Text("Departments") },
                        selected = currentRoute == "departments",
                        onClick = {
                            if (currentRoute != "departments") {
                                navController.navigate("departments") {
                                    launchSingleTop = true
                                }
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF0061E0),
                            selectedTextColor = Color(0xFF0061E0),
                            indicatorColor = Color(0xFFF0F7FF)
                        )
                    )

                    NavigationBarItem(
                        icon = { Icon(Icons.Default.LocalShipping, contentDescription = "Tracking") },
                        label = { Text("Tracking") },
                        selected = currentRoute == "orders",
                        onClick = {
                            if (currentRoute != "orders") {
                                navController.navigate("orders") {
                                    launchSingleTop = true
                                }
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF0061E0),
                            selectedTextColor = Color(0xFF0061E0),
                            indicatorColor = Color(0xFFF0F7FF)
                        )
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                NavHost(
                    navController = navController,
                    startDestination = "home"
                ) {
                    composable("home") {
                        HomeScreen(
                            viewModel = viewModel,
                            onNavigateToDepartments = { navController.navigate("departments") },
                            onNavigateToOrders = { navController.navigate("orders") },
                            onNavigateToMembership = { navController.navigate("membership") }
                        )
                    }
                    composable("membership") {
                        MembershipScreen(
                            viewModel = viewModel,
                            onBackClick = { navController.safePopBackStack() },
                            onNavigateToDepartments = { navController.navigate("departments") },
                            onNavigateToRegister = { navController.navigate("membership_registration") }
                        )
                    }
                    composable("membership_registration") {
                        MembershipRegistrationScreen(
                            viewModel = viewModel,
                            onNavigateBack = { navController.safePopBackStack() }
                        )
                    }
                    composable("register") {
                        AuthScreen(
                            viewModel = viewModel,
                            initialRegisterMode = true,
                            onAuthSuccess = {
                                navController.navigate("home") {
                                    popUpTo("home") { inclusive = false }
                                    launchSingleTop = true
                                }
                            }
                        )
                    }

                    composable("profile") {
                        ProfileScreen(
                            viewModel = viewModel,
                            onBackClick = { navController.safePopBackStack() }
                        )
                    }

                    composable("departments") {
                        DepartmentsScreen(
                            viewModel = viewModel,
                            onSelectLaundry = { navController.navigate("products/laundry") },
                            onSelectRetail = { navController.navigate("products/retail") }
                        )
                    }

                    composable("products/{department}") { backStackEntry ->
                        val dept = backStackEntry.arguments?.getString("department") ?: "all"
                        ProductListScreen(
                            departmentType = dept,
                            viewModel = viewModel,
                            onNavigateToCart = { navController.navigate("checkout") }
                        )
                    }

                    composable("checkout") {
                        CheckoutScreen(
                            viewModel = viewModel,
                            onNavigateToAuth = { navController.navigate("auth") },
                            onOrderPlacedSuccess = {
                                navController.navigate("orders") {
                                    popUpTo("home") { inclusive = false }
                                }
                            }
                        )
                    }

                    composable("orders") {
                        OrdersTrackingScreen(
                            viewModel = viewModel,
                            onNavigateToAuth = { navController.navigate("auth") }
                        )
                    }

                    composable("auth") {
                        AuthScreen(
                            viewModel = viewModel,
                            onAuthSuccess = {
                                navController.navigate("home") {
                                    popUpTo("home") { inclusive = false }
                                    launchSingleTop = true
                                }
                            }
                        )
                    }
                }

                AnimatedVisibility(
                    visible = totalCartQty > 0 && currentRoute != "checkout",
                    enter = slideInVertically(initialOffsetY = { it }),
                    exit = slideOutVertically(targetOffsetY = { it }),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .clickable {
                                if (currentUser == null) {
                                    navController.navigate("auth")
                                } else {
                                    navController.navigate("checkout")
                                }
                            },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = Color(0xFF0061E0),
                                    shape = CircleShape
                                ) {
                                    Text(
                                        text = "$totalCartQty",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Rs. ${grandTotal.toInt()} PKR",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "View Bag & Checkout",
                                        fontSize = 11.sp,
                                        color = Color(0xFF94A3B8)
                                    )
                                }
                            }

                            Text(
                                text = "CONTINUE",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF38BDF8)
                            )
                        }
                    }
                }
            }
        }
    }

    if (showNotificationDialog) {
        NotificationCenterDialog(
            notifications = notificationsList,
            onDismiss = { showNotificationDialog = false },
            onMarkAllRead = { viewModel.markAllNotificationsAsRead() },
            onTestSound = { viewModel.testNotificationSound(context) },
            onNotificationClick = { notification ->
                viewModel.markNotificationAsRead(notification.id)
                showNotificationDialog = false
                if (notification.orderId != null) {
                    if (notification.type == "chat") {
                        viewModel.openChatForOrder(notification.orderId)
                        viewModel.clearUnreadChatForOrder(notification.orderId)
                    }
                    navController.navigate("orders")
                } else {
                    navController.navigate("orders")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationCenterDialog(
    notifications: List<com.example.network.models.AppNotification>,
    onDismiss: () -> Unit,
    onMarkAllRead: () -> Unit,
    onTestSound: () -> Unit,
    onNotificationClick: (com.example.network.models.AppNotification) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        tint = Color(0xFF0061E0),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Notifications & Updates",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF0F172A)
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF64748B))
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onMarkAllRead,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("Mark All as Read", fontSize = 11.sp, color = Color(0xFF0061E0))
                    }

                    Button(
                        onClick = onTestSound,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0061E0)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Icon(Icons.Default.VolumeUp, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Test Sound", fontSize = 11.sp, color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (notifications.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No notifications yet.\nOrder updates and rider messages will appear here in real-time.",
                            fontSize = 13.sp,
                            color = Color(0xFF64748B),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                } else {
                    androidx.compose.foundation.lazy.LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 360.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(notifications.size) { index ->
                            val item = notifications[index]
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onNotificationClick(item) },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (!item.isRead) Color(0xFFF0F7FF) else Color(0xFFF8FAFC)
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (!item.isRead) Color(0xFFBAE6FD) else Color(0xFFE2E8F0)
                                )
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Surface(
                                                color = when (item.type) {
                                                    "chat" -> Color(0xFFDCFCE7)
                                                    "test" -> Color(0xFFFEF3C7)
                                                    else -> Color(0xFFE0EFFE)
                                                },
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = item.type.uppercase(),
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = when (item.type) {
                                                        "chat" -> Color(0xFF15803D)
                                                        "test" -> Color(0xFFB45309)
                                                        else -> Color(0xFF0369A1)
                                                    },
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = item.title,
                                                fontSize = 13.sp,
                                                fontWeight = if (!item.isRead) FontWeight.Bold else FontWeight.SemiBold,
                                                color = Color(0xFF0F172A),
                                                maxLines = 1
                                            )
                                        }
                                        Text(
                                            text = item.timestamp,
                                            fontSize = 10.sp,
                                            color = Color(0xFF94A3B8)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = item.body,
                                        fontSize = 12.sp,
                                        color = Color(0xFF334155)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("CLOSE", color = Color(0xFF0061E0), fontWeight = FontWeight.Bold)
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(20.dp)
    )
}
