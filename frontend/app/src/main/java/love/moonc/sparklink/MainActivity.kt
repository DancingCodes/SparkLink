package love.moonc.sparklink

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import kotlinx.coroutines.flow.first
import love.moonc.sparklink.data.events.AppEvent
import love.moonc.sparklink.data.events.AppEventBus
import love.moonc.sparklink.data.local.UserPreferences
import love.moonc.sparklink.data.remote.NetworkModule
import love.moonc.sparklink.ui.navigation.Screen
import love.moonc.sparklink.ui.navigation.bottomNavItems
import love.moonc.sparklink.ui.screens.*
import love.moonc.sparklink.ui.theme.SparklinkTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        NetworkModule.init(this)
        val userPrefs = UserPreferences(this)

        setContent {
            SparklinkTheme {
                var token by remember { mutableStateOf<Boolean?>(null) }
                LaunchedEffect(Unit) {
                    token = !userPrefs.Token.first().isNullOrBlank()
                }

                when (token) {
                    null -> SplashScreen()
                    else -> {
                        MainContent(isLoggedIn = token!!, userPrefs = userPrefs)
                    }
                }
            }
        }
    }
}

@Composable
fun MainContent(isLoggedIn: Boolean, userPrefs: UserPreferences) {
    val navController = rememberNavController()

    LaunchedEffect(Unit) {
        AppEventBus.events.collect { event ->
            if (event is AppEvent.Logout) {
                userPrefs.clear()
                navController.navigate(Screen.Login.route) {
                    popUpTo(0)
                }
            }
        }
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in listOf(
        Screen.TabScreen.Home.route,
        Screen.TabScreen.Messages.route,
        Screen.TabScreen.Profile.route
    )

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    val currentDestination = navBackStackEntry?.destination
                    bottomNavItems.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = null) },
                            label = { Text(screen.title) },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
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
            startDestination = if (isLoggedIn) Screen.TabScreen.Home.route else Screen.Login.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Login.route) { LoginScreen(navController) }
            composable(Screen.Register.route) { RegisterScreen(navController) }
            composable(Screen.TabScreen.Home.route) { HomeScreen(navController) }
            composable(Screen.TabScreen.Messages.route) { MessageScreen() }
            composable(Screen.TabScreen.Profile.route) { ProfileScreen(navController) }
            composable(Screen.CreateRoom.route) { CreateRoomScreen(navController) }
            composable(Screen.RoomDetail.route) { RoomDetailScreen(navController) }
        }
    }
}

@Composable
fun SplashScreen() {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {}
}