package love.moonc.sparklink.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import love.moonc.sparklink.data.remote.NetworkModule
import love.moonc.sparklink.data.remote.exception.ApiException
import love.moonc.sparklink.data.remote.model.request.RegisterRequest
import love.moonc.sparklink.data.remote.uploadImage

@Composable
fun RegisterScreen(navController: NavController) {
    val regViewModel: RegisterViewModel = viewModel()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var nickname by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("男") }

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var uploadedAvatarUrl by remember { mutableStateOf("") }
    var isUploading by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            scope.launch {
                isUploading = true
                try {
                    uploadedAvatarUrl = NetworkModule.Api.uploadImage(context, uri, "avatar")
                    Toast.makeText(context, "头像上传成功", Toast.LENGTH_SHORT).show()
                } catch (e: ApiException) {
                    Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(context, "图片处理失败", Toast.LENGTH_SHORT).show()
                } finally {
                    isUploading = false
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 30.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(60.dp))

        // --- 头像上传区域 ---
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable(enabled = !isUploading && !regViewModel.isRegistering) {
                    launcher.launch("image/*")
                },
            contentAlignment = Alignment.Center
        ) {
            if (selectedImageUri != null) {
                AsyncImage(
                    model = selectedImageUri,
                    contentDescription = "Avatar Preview",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(Icons.Default.CameraAlt, contentDescription = "Add Photo", tint = Color.Gray)
            }

            if (isUploading) {
                // 上传时的进度条覆盖层
                Box(
                    modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(30.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        OutlinedTextField(
            value = nickname,
            onValueChange = { nickname = it },
            label = { Text("昵称") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            enabled = !regViewModel.isRegistering,
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // --- 性别选择器 ---
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("性别：", style = MaterialTheme.typography.bodyLarge)
            listOf("男", "女").forEach { text ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable(enabled = !regViewModel.isRegistering) { gender = text }
                ) {
                    RadioButton(
                        selected = (gender == text),
                        onClick = { gender = text },
                        enabled = !regViewModel.isRegistering
                    )
                    Text(text)
                }
                Spacer(modifier = Modifier.width(16.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("手机号") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            enabled = !regViewModel.isRegistering,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("设置密码") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            enabled = !regViewModel.isRegistering,
            // 优化：指定为密码键盘类型
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = {
                regViewModel.register(
                    request = RegisterRequest(
                        phone = phone,
                        password = password,
                        name = nickname,
                        sex = gender,
                        avatar = uploadedAvatarUrl
                    ),
                    onSuccess = {
                        Toast.makeText(context, "注册成功，欢迎加入！", Toast.LENGTH_SHORT).show()
                        navController.popBackStack()
                    }
                )
            },
            enabled = !isUploading && !regViewModel.isRegistering &&
                    phone.length == 11 && password.isNotBlank() && nickname.isNotBlank(),
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(28.dp)
        ) {
            if (regViewModel.isRegistering) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
            } else {
                Text("完成注册", style = MaterialTheme.typography.titleMedium)
            }
        }

        TextButton(
            onClick = { navController.popBackStack() },
            enabled = !regViewModel.isRegistering
        ) {
            Text("已有账号？去登录")
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}
