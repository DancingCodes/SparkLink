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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import love.moonc.sparklink.data.remote.NetworkModule
import love.moonc.sparklink.data.remote.exception.ApiException
import love.moonc.sparklink.data.remote.model.RegisterRequest
import love.moonc.sparklink.ui.viewmodel.RegisterViewModel
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

@Composable
fun RegisterScreen(
    navController: NavController,
    regViewModel: RegisterViewModel = viewModel()
) {
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
                    uploadedAvatarUrl = uploadImageAction(it, context)
                    Toast.makeText(context, "头像上传成功", Toast.LENGTH_SHORT).show()
                } catch (e: ApiException) {
                    Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                } catch (_: Exception) {
                    Toast.makeText(context, "上传失败", Toast.LENGTH_SHORT).show()
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
            .padding(horizontal = 30.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(60.dp))

        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(Color.LightGray.copy(alpha = 0.3f))
                .clickable(enabled = !isUploading && !regViewModel.isRegistering) {
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

            if (isUploading) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(30.dp))
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        OutlinedTextField(
            value = nickname,
            onValueChange = { nickname = it },
            label = { Text("昵称") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            enabled = !regViewModel.isRegistering
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("性别：", modifier = Modifier.padding(end = 16.dp))
            listOf("男", "女").forEach { text ->
                RadioButton(
                    selected = (gender == text),
                    onClick = { gender = text },
                    enabled = !regViewModel.isRegistering
                )
                Text(text, modifier = Modifier.clickable { if(!regViewModel.isRegistering) gender = text })
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
            enabled = !regViewModel.isRegistering
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("设置密码") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            enabled = !regViewModel.isRegistering
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
                        Toast.makeText(context, "注册成功，欢迎！", Toast.LENGTH_SHORT).show()
                        navController.popBackStack()
                    },
                    onError = { msg ->
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    }
                )
            },
            enabled = !isUploading && !regViewModel.isRegistering && phone.isNotBlank() && password.isNotBlank() && nickname.isNotBlank(),
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(28.dp)
        ) {
            if (regViewModel.isRegistering) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text("完成注册")
            }
        }

        TextButton(onClick = { navController.popBackStack() }, enabled = !regViewModel.isRegistering) {
            Text("已有账号？去登录")
        }
    }
}

// 图片上传辅助函数 (保持不变)
private suspend fun uploadImageAction(
    uri: Uri,
    context: android.content.Context
): String {
    return withContext(Dispatchers.IO) {
        val inputStream = context.contentResolver.openInputStream(uri)
        val file = File(context.cacheDir, "temp_${System.currentTimeMillis()}.jpg")
        val outputStream = FileOutputStream(file)
        inputStream?.use { input -> outputStream.use { output -> input.copyTo(output) } }

        val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

        val response = NetworkModule.Api.uploadFile(body)
        response.data.fileUuid
    }
}