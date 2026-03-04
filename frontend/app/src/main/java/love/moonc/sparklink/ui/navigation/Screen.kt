package love.moonc.sparklink.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String) {
    sealed class TabScreen(
        route: String,
        val title: String,
        val icon: ImageVector
    ) : Screen(route) {
        object Home : TabScreen("home", "首页", Icons.Default.Home)
        object Messages : TabScreen("messages", "消息", Icons.Default.MailOutline)
        object Profile : TabScreen("profile", "我的", Icons.Default.Person)
    }

    object Login : Screen("login")
    object Register : Screen("register")
    object CreateRoom : Screen("create_room")
    object RoomDetail : Screen("room_detail")
}

val bottomNavItems = listOf(
    Screen.TabScreen.Home,
    Screen.TabScreen.Messages,
    Screen.TabScreen.Profile
)