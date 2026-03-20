package love.moonc.sparklink.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.serialization.Serializable
import kotlin.reflect.KClass

// --- 无参数页面用 object ---
@Serializable object LoginRoute
@Serializable object RegisterRoute
@Serializable object HomeRoute
@Serializable object MessagesRoute
@Serializable object ProfileRoute
@Serializable object UserUpdateRoute
@Serializable object CreateRoomRoute

@Serializable
data class RoomDetailRoute(
    val roomId: Long,
    val agoraToken: String,
    val agoraUid: Int,
    val channelName: String
)

data class BottomNavItem<T : Any>(
    val title: String,
    val icon: ImageVector,
    val route: T,           // 用于 navigate(route)
    val routeClass: KClass<T> // 用于 hasRoute<T>() 判断选中状态
)

val bottomNavItems = listOf(
    BottomNavItem("首页", Icons.Default.Home, HomeRoute, HomeRoute::class),
    BottomNavItem("消息", Icons.AutoMirrored.Filled.Message, MessagesRoute, MessagesRoute::class),
    BottomNavItem("我的", Icons.Default.Person, ProfileRoute, ProfileRoute::class)
)