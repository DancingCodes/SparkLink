package love.moonc.sparklink.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import love.moonc.sparklink.data.events.AppEvent
import love.moonc.sparklink.data.events.AppEventBus
import love.moonc.sparklink.data.remote.NetworkModule
import love.moonc.sparklink.data.remote.uploadImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserUpdateScreen(navController: NavController) {
    val viewModel: UserUpdateViewModel = viewModel()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var isUploading by remember { mutableStateOf(false) }
    var showCloseAccountDialog by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            scope.launch {
                isUploading = true
                try {
                    val url = NetworkModule.Api.uploadImage(context, uri, "avatar")
                    viewModel.avatar = url
                    Toast.makeText(context, "头像上传成功", Toast.LENGTH_SHORT).show()
                } catch (_: Exception) {
                    Toast.makeText(context, "上传失败", Toast.LENGTH_SHORT).show()
                } finally {
                    isUploading = false
                }
            }
        }
    }

    // 状态监听：资料更新成功
    LaunchedEffect(viewModel.updateSuccess) {
        if (viewModel.updateSuccess) {
            Toast.makeText(context, "资料已更新", Toast.LENGTH_SHORT).show()
            navController.popBackStack()
        }
    }

    // 状态监听：注销成功
    LaunchedEffect(viewModel.isAccountClosed) {
        if (viewModel.isAccountClosed) {
            Toast.makeText(context, "账号已注销", Toast.LENGTH_LONG).show()
            AppEventBus.emit(AppEvent.Logout) // 触发 MainActivity 的登出清理逻辑
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("修改个人资料", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // 头像上传区域
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable(enabled = !isUploading) { launcher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (viewModel.avatar.isNotEmpty()) {
                    AsyncImage(
                        model = viewModel.avatar,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(Icons.Default.CameraAlt, null, tint = Color.Gray)
                }

                if (isUploading) {
                    Box(
                        modifier = Modifier.fillMaxSize().background(Color.Black.copy(0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(Modifier.size(30.dp), color = Color.White, strokeWidth = 2.dp)
                    }
                }
            }

            TextButton(onClick = { launcher.launch("image/*") }) {
                Text("更换头像")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 输入表单
            OutlinedTextField(
                value = viewModel.name,
                onValueChange = { viewModel.name = it },
                label = { Text("昵称") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(start = 4.dp)
            ) {
                Text("性别：", style = MaterialTheme.typography.bodyLarge)
                RadioButton(selected = viewModel.sex == "男", onClick = { viewModel.sex = "男" })
                Text("男")
                Spacer(modifier = Modifier.width(16.dp))
                RadioButton(selected = viewModel.sex == "女", onClick = { viewModel.sex = "女" })
                Text("女")
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = viewModel.password,
                onValueChange = { viewModel.password = it },
                label = { Text("新密码 (留空表示不修改)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 保存按钮
            Button(
                onClick = { viewModel.updateUserInfo() },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = !viewModel.isLoading && !isUploading,
                shape = RoundedCornerShape(28.dp)
            ) {
                if (viewModel.isLoading) {
                    CircularProgressIndicator(Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text("保存更新", style = MaterialTheme.typography.titleMedium)
                }
            }

            // 注销按钮
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(
                onClick = { showCloseAccountDialog = true },
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Text("注销账号")
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // 注销二次确认弹窗
        if (showCloseAccountDialog) {
            AlertDialog(
                onDismissRequest = { showCloseAccountDialog = false },
                title = { Text("注销账号") },
                text = { Text("注销后您的账号信息将无法找回。确定要注销吗？") },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.closeAccount()
                        showCloseAccountDialog = false
                    }) {
                        Text("确定注销", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCloseAccountDialog = false }) { Text("取消") }
                }
            )
        }
    }
}
