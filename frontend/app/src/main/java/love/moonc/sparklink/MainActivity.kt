package love.moonc.sparklink

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import kotlinx.coroutines.flow.first
import love.moonc.sparklink.data.local.UserPreferences
import love.moonc.sparklink.ui.navigation.Screen
import love.moonc.sparklink.ui.navigation.bottomNavItems
import love.moonc.sparklink.ui.screens.*
import love.moonc.sparklink.ui.theme.SparklinkTheme
import androidx.compose.foundation.layout.fillMaxSize

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 初始化存储工具
        val userPrefs = UserPreferences(this)

        setContent {
            SparklinkTheme {
                // 1. 定义登录状态：null = 检查中, true = 已登录, false = 未登录
                var isLoggedIn by remember { mutableStateOf<Boolean?>(null) }

                // 2. 启动时执行一次检查
                LaunchedEffect(Unit) {
                    // 读取本地存储的值，如果没有读取到则默认为 false
                    isLoggedIn = userPrefs.isLoggedIn.first()
                }

                // 3. 根据状态决定渲染什么
                when (val loggedIn = isLoggedIn) {
                    null -> {
                        // 还在读取 localStorage，显示空背景或 Splash 避免闪烁
                        SplashScreen()
                    }
                    else -> {
                        // 读取完成，正式进入 App 导航
                        MainContent(loggedIn)
                    }
                }
            }
        }
    }
}

@Composable
fun MainContent(startLoggedIn: Boolean) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // 判断当前页面是否属于底部 Tab 栏
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
            // 根据本地存储的状态决定起始页
            startDestination = if (startLoggedIn) Screen.TabScreen.Home.route else Screen.Login.route,
            modifier = Modifier.padding(innerPadding),
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
    // 纯白色背景，防止加载时的黑白闪烁
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        // 这里可以留空，或者放一个简单的进度条
    }
}