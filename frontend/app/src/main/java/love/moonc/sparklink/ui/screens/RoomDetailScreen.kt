package love.moonc.sparklink.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import love.moonc.sparklink.data.rtc.AgoraManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomDetailScreen(
    navController: NavController,
    roomId: Long,
    agoraToken: String,
    agoraUid: Int,
    channelName: String
) {
    val viewModel: RoomDetailViewModel = viewModel()

    // 1. 麦克风权限与声网自动加入逻辑
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        AgoraManager.joinChannel(agoraToken, channelName, agoraUid)
        if (isGranted) {
            AgoraManager.setMicEnabled(true) // 默认直接开麦
        }
    }

    // 2. 页面启动：执行初始化
    LaunchedEffect(roomId) {
        viewModel.enterRoom(roomId)
        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(viewModel.roomDetail?.room?.title ?: "正在放映") },
                actions = {
                    TextButton(onClick = {
                        viewModel.exitRoom(roomId) {
                            navController.popBackStack()
                        }
                    }) {
                        Text("退出房间", color = Color.Red)
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // 固定展示 8 个麦位
                items(8) { index ->
                    val member = viewModel.occupants.getOrNull(index)
                    // ✅ 直接调用下方定义的 VoiceSeat
                    VoiceSeat(
                        name = member?.user?.name ?: "待入座",
                        avatarUrl = member?.user?.avatar,
                        isOwner = member?.role == 1
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Text(
                    "默认已为您开启麦克风，点击右上角退出",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }
        }
    }
}

/**
 * ✅ 核心组件：麦位展示
 * 包含了头像加载、默认占位图以及房主标识
 */
@Composable
fun VoiceSeat(name: String, avatarUrl: String?, isOwner: Boolean) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(IntrinsicSize.Min)
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center
        ) {
            if (!avatarUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = avatarUrl,
                    contentDescription = name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.4f)
                )
            }

            // 如果是房主，在头像下面或者旁边加个小皇冠/标识
            if (isOwner) {
                Surface(
                    color = Color(0xFFFFD700), // 金色
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.align(Alignment.BottomCenter).offset(y = 4.dp)
                ) {
                    Text(
                        "OWNER",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                        modifier = Modifier.padding(horizontal = 4.dp),
                        color = Color.Black
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = name,
            style = MaterialTheme.typography.labelMedium,
            color = if (isOwner) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            fontWeight = if (isOwner) FontWeight.Bold else FontWeight.Normal
        )
    }
}