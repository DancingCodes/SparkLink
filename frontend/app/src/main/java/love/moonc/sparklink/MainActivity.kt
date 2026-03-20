package love.moonc.sparklink

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import androidx.navigation.toRoute
import kotlinx.coroutines.flow.first
import love.moonc.sparklink.data.events.AppEvent
import love.moonc.sparklink.data.events.AppEventBus
import love.moonc.sparklink.data.local.UserPreferences
import love.moonc.sparklink.ui.navigation.*
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

    // 登录/登出事件监听
    LaunchedEffect(Unit) {
        AppEventBus.events.collect { event ->
            if (event is AppEvent.Logout) {
                userPrefs.clear()
                navController.navigate(LoginRoute) {
                    popUpTo(0) { inclusive = true }
                }
            }
        }
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // ✅ 判断是否显示底部导航栏：判断当前目的地是否在 Tab 列表里
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
            // ✅ 起始页面改为 Route 对象
            startDestination = if (isLoggedIn) HomeRoute else LoginRoute,
            modifier = Modifier.padding(innerPadding)
        ) {
            // 所有路由注册全部改为类型安全模式
            composable<LoginRoute> { LoginScreen(navController) }
            composable<RegisterRoute> { RegisterScreen(navController) }
            composable<HomeRoute> { HomeScreen(navController) }
            composable<MessagesRoute> { MessageScreen() }
            composable<ProfileRoute> { ProfileScreen(navController) }
            composable<UserUpdateRoute> { UserUpdateScreen(navController) }
            composable<CreateRoomRoute> { CreateRoomScreen(navController) }

            // 核心：房间详情页
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
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {}
}