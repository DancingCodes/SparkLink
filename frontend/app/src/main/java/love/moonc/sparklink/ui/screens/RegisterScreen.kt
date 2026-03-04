package love.moonc.sparklink.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import love.moonc.sparklink.data.local.UserPreferences
import love.moonc.sparklink.data.remote.NetworkModule
import love.moonc.sparklink.data.model.RegisterRequest
import love.moonc.sparklink.ui.navigation.Screen
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

@Composable
fun RegisterScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val userPrefs = remember { UserPreferences(context) }

    // 表单输入状态
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var nickname by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("男") }

    // 头像上传相关状态
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) } // 本地预览Uri
    var uploadedAvatarUrl by remember { mutableStateOf("") }       // 上传成功后后端返回的URL
    var isUploading by remember { mutableStateOf(false) }          // 是否正在上传图片
    var isRegistering by remember { mutableStateOf(false) }        // 是否正在提交注册

    // 图片选择器启动器
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            // 选中图片后立即自动上传
            scope.launch {
                isUploading = true
                val url = uploadImageAction(it, context, userPrefs)
                if (url != null) {
                    uploadedAvatarUrl = url
                    Toast.makeText(context, "头像上传成功", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "头像上传失败", Toast.LENGTH_SHORT).show()
                }
                isUploading = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 30.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(60.dp))

        Text(
            text = "创建新账号",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(30.dp))

        // --- 1. 头像上传区域 ---
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(Color.LightGray.copy(alpha = 0.3f))
                .clickable(enabled = !isUploading && !isRegistering) {
                    launcher.launch("image/*")
                },
            contentAlignment = Alignment.Center
        ) {
            if (selectedImageUri != null) {
                AsyncImage(
                    model = selectedImageUri,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color.Gray)
            }

            // 上传中的加载动画遮罩
            if (isUploading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(30.dp))
                }
            }
        }
        Text(
            text = if (isUploading) "图片上传中..." else "上传头像",
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(top = 8.dp)
        )

        Spacer(modifier = Modifier.height(30.dp))

        // --- 2. 基础资料输入 ---
        OutlinedTextField(
            value = nickname,
            onValueChange = { nickname = it },
            label = { Text("昵称") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            enabled = !isRegistering
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 性别选择
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("性别：", modifier = Modifier.padding(end = 16.dp))
            listOf("男", "女").forEach { text ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { gender = text }) {
                    RadioButton(
                        selected = (gender == text),
                        onClick = { gender = text },
                        enabled = !isRegistering
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
            singleLine = true,
            enabled = !isRegistering
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("设置密码") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            enabled = !isRegistering
        )

        Spacer(modifier = Modifier.height(40.dp))

        // --- 3. 提交按钮 ---
        Button(
            onClick = {
                scope.launch {
                    isRegistering = true
                    try {
                        val regRequest = RegisterRequest(
                            phone = phone,
                            password = password,
                            name = nickname,
                            sex = gender,
                            avatar = uploadedAvatarUrl
                        )

                        val apiService = NetworkModule.getApiService(userPrefs)
                        val response = apiService.register(regRequest)

                        if (response.code == 200) {
                            // 注册成功：保存状态并跳转
                            userPrefs.saveLoginStatus(true)
                            Toast.makeText(context, "注册成功，欢迎！", Toast.LENGTH_SHORT).show()
                            navController.navigate(Screen.TabScreen.Home.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        } else {
                            Toast.makeText(context, "注册失败: ${response.msg}", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(context, "连接服务器失败: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                    } finally {
                        isRegistering = false
                    }
                }
            },
            // 校验逻辑：不在上传中、不在注册中、资料不为空
            enabled = !isUploading && !isRegistering && phone.isNotBlank() && password.isNotBlank() && nickname.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(28.dp)
        ) {
            if (isRegistering) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text("完成注册并登录")
            }
        }

        TextButton(
            onClick = { navController.popBackStack() },
            enabled = !isRegistering
        ) {
            Text("已有账号？去登录")
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}


/**
 * 辅助函数：处理图片文件转换与上传
 */
private suspend fun uploadImageAction(
    uri: Uri,
    context: android.content.Context,
    userPrefs: UserPreferences
): String? {
    return withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            // 建议文件名稍微区分一下，避免缓存冲突
            val file = File(context.cacheDir, "temp_avatar_${System.currentTimeMillis()}.jpg")
            val outputStream = FileOutputStream(file)

            inputStream?.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }

            val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

            val apiService = NetworkModule.getApiService(userPrefs)

            // 执行请求
            val response = apiService.uploadFile(body)

            if (response.code == 200) {
                response.data.fileUuid
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}