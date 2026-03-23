package love.moonc.sparklink

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import love.moonc.sparklink.data.events.AppEvent
import love.moonc.sparklink.data.events.AppEventBus
import love.moonc.sparklink.data.local.UserPreferences
import love.moonc.sparklink.ui.navigation.*
import love.moonc.sparklink.ui.screens.*
import love.moonc.sparklink.ui.theme.SparklinkTheme
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.toRoute

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            AppEventBus.events.collect { event ->
                when (event) {
                    is AppEvent.ShowToast -> {
                        Toast.makeText(this@MainActivity, event.message, Toast.LENGTH_SHORT).show()
                    }
                    is AppEvent.Logout -> {
                        UserPreferences.getInstance().clear()
                    }
                }
            }
        }

        setContent {
            SparklinkTheme {
                var isLoggedIn by remember { mutableStateOf<Boolean?>(null) }

                LaunchedEffect(Unit) {
                    isLoggedIn = !UserPreferences.getInstance().token.first().isNullOrBlank()
                }

                when (isLoggedIn) {
                    null -> SplashScreen()
                    else -> MainContent(isLoggedIn = isLoggedIn!!)
                }
            }
        }
    }
}

@Composable
fun MainContent(isLoggedIn: Boolean) {
    val navController = rememberNavController()

    LaunchedEffect(Unit) {
        AppEventBus.events.collect { event ->
            if (event is AppEvent.Logout) {
                navController.navigate(LoginRoute) {
                    popUpTo(0) { inclusive = true }
                }
            }
        }
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val showBottomBar = currentDestination?.let { dest ->
        dest.hasRoute<HomeRoute>() || dest.hasRoute<MessagesRoute>() || dest.hasRoute<ProfileRoute>()
    } ?: false

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        val selected = currentDestination.hierarchy.any {
                            it.hasRoute(item.route::class)
                        }

                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = null) },
                            label = { Text(item.title) },
                            selected = selected,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = if (isLoggedIn) HomeRoute else LoginRoute,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable<LoginRoute> { LoginScreen(navController) }
            composable<RegisterRoute> { RegisterScreen(navController) }
            composable<HomeRoute> { HomeScreen(navController) }
            composable<MessagesRoute> { MessageScreen() }
            composable<ProfileRoute> { ProfileScreen(navController) }
            composable<UserUpdateRoute> { UserUpdateScreen(navController) }
            composable<CreateRoomRoute> { CreateRoomScreen(navController) }

            // 房间详情
            composable<RoomDetailRoute> { backStackEntry ->
                val args = backStackEntry.toRoute<RoomDetailRoute>()
                RoomDetailScreen(
                    navController = navController,
                    roomId = args.roomId,
                    agoraToken = args.agoraToken,
                    agoraUid = args.agoraUid,
                    channelName = args.channelName
                )
            }
        }
    }
}

@Composable
fun SplashScreen() {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
    }
}