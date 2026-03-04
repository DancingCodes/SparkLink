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
import love.moonc.sparklink.ui.navigation.Screen
import love.moonc.sparklink.ui.navigation.bottomNavItems
import love.moonc.sparklink.ui.screens.*
import love.moonc.sparklink.ui.theme.SparklinkTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SparklinkTheme {
                val userPrefs = UserPreferences.getInstance()
                var isLoggedIn by remember { mutableStateOf<Boolean?>(null) }
                LaunchedEffect(Unit) {
                    isLoggedIn = !userPrefs.token.first().isNullOrBlank()
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
    val userPrefs = UserPreferences.getInstance()

    LaunchedEffect(Unit) {
        AppEventBus.events.collect { event ->
            if (event is AppEvent.Logout) {
                userPrefs.clear()
                navController.navigate(Screen.Login.route) {
                    popUpTo(0) { inclusive = true }
                }
            }
        }
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // 判断是否显示底部导航栏
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
            // 根据登录状态决定起始页面
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
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        // 这里可以加个加载动画或 Logo
    }
}