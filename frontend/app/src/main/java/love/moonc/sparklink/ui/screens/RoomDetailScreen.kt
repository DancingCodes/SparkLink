package love.moonc.sparklink.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.SubcomposeAsyncImage

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
    val detail = viewModel.roomDetail
    var showExitConfirmDialog by remember { mutableStateOf(false) }
    var isMicOn by remember { mutableStateOf(false) } // 默认下麦状态

    // 麦克风权限请求
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.joinRtcChannel(agoraToken, channelName, agoraUid)
        } else {
            viewModel.errorMessage = "未获得麦克风权限，无法发言"
        }
    }

    // 初始化加载
    LaunchedEffect(roomId) {
        viewModel.fetchRoomInfo(roomId)
        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }

    // 退出逻辑监听
    LaunchedEffect(viewModel.isDissolved, viewModel.isLeft) {
        if (viewModel.isDissolved || viewModel.isLeft) {
            navController.popBackStack()
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(viewModel.errorMessage) {
        viewModel.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(detail?.room?.title ?: "语音房", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { showExitConfirmDialog = true }) {
                        Icon(Icons.Default.Close, contentDescription = "退出")
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                actions = {
                    Spacer(Modifier.weight(1f))
                    // 麦克风控制按钮
                    FloatingActionButton(
                        onClick = {
                            isMicOn = !isMicOn
                            viewModel.toggleMic(isMicOn)
                        },
                        containerColor = if (isMicOn) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                        elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation()
                    ) {
                        Icon(
                            imageVector = if (isMicOn) Icons.Default.Mic else Icons.Default.MicOff,
                            contentDescription = "麦克风开关",
                            tint = if (isMicOn) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(Modifier.weight(1f))
                }
            )
        }
    ) { padding ->
        // 退出弹窗
        if (showExitConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showExitConfirmDialog = false },
                title = { Text(if (viewModel.isOwner) "解散房间" else "退出房间") },
                text = { Text(if (viewModel.isOwner) "确定要解散并关闭此房间吗？" else "确定要退出此房间吗？") },
                confirmButton = {
                    TextButton(onClick = {
                        if (viewModel.isOwner) viewModel.dissolveRoom(roomId) else viewModel.leaveRoom(roomId)
                        showExitConfirmDialog = false
                    }) {
                        Text("确定", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showExitConfirmDialog = false }) { Text("取消") }
                }
            )
        }

        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (viewModel.isLoading && detail == null) {
                // ✅ 修正：使用 Modifier.align(Alignment.Center)
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(4),
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(24.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(8) { index ->
                            val member = detail?.members?.getOrNull(index)
                            VoiceSeat(
                                name = member?.user?.name ?: "待入座",
                                avatarUrl = member?.user?.avatar,
                                isOwner = member?.role == 1,
                                onClick = { /* TODO: 点击空位可以触发上麦 */ }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VoiceSeat(name: String, avatarUrl: String?, isOwner: Boolean, onClick: () -> Unit = {}) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(IntrinsicSize.Min)
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center
        ) {
            if (!avatarUrl.isNullOrEmpty()) {
                SubcomposeAsyncImage(
                    model = avatarUrl,
                    contentDescription = name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                    loading = { CircularProgressIndicator(modifier = Modifier.padding(16.dp), strokeWidth = 2.dp) },
                    error = { Icon(Icons.Default.AccountCircle, null, modifier = Modifier.size(40.dp)) }
                )
            } else {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.5f)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = if (isOwner) "👑 $name" else name,
            style = MaterialTheme.typography.labelMedium,
            color = if (isOwner) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            fontWeight = if (isOwner) FontWeight.Bold else FontWeight.Normal
        )
    }
}