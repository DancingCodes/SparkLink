package love.moonc.sparklink.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage // 替换为 Subcompose 以支持自定义加载状态
import kotlinx.coroutines.launch
import love.moonc.sparklink.data.events.AppEvent
import love.moonc.sparklink.data.events.AppEventBus
import love.moonc.sparklink.data.local.UserPreferences

@Composable
fun ProfileScreen() {
    val scope = rememberCoroutineScope()
    val userPrefs = UserPreferences.getInstance()
    // 实时监听 DataStore 中的用户信息
    val user by userPrefs.userData.collectAsState(initial = null)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(60.dp))

        // ✨ 动态头像组件
        SubcomposeAsyncImage(
            model = user?.avatar,
            contentDescription = "用户头像",
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant), // 加载前的底色
            contentScale = ContentScale.Crop,
            // 🔄 加载中的状态
            loading = {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(30.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            // ❌ 加载失败（或 avatar 为 null）的状态
            error = {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "加载失败",
                    modifier = Modifier.fillMaxSize(),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = user?.name ?: "未登录",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = user?.phone ?: "手机号未绑定",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // 这里的 Spacer 负责把按钮顶到底部
        Spacer(modifier = Modifier.weight(1f))

        // 🚪 退出登录按钮
        Button(
            onClick = {
                scope.launch {
                    AppEventBus.emit(AppEvent.Logout)
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.error
            ),
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .height(55.dp)
        ) {
            Text(text = "退出登录", style = MaterialTheme.typography.titleMedium)
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}