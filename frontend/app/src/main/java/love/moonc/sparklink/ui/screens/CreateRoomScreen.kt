package love.moonc.sparklink.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import love.moonc.sparklink.ui.navigation.Screen
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRoomScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val viewModel: CreateRoomViewModel = viewModel()

    var roomName by remember { mutableStateOf("") }

    // --- 图片上传相关状态 ---
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var uploadedCoverUrl by remember { mutableStateOf("") }
    var isUploading by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            scope.launch {
                isUploading = true
                try {
                    uploadedCoverUrl = uploadImageAction(it, context)
                    Toast.makeText(context, "封面上传成功", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "图片处理失败: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                } finally {
                    isUploading = false
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("创建放映厅", style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // --- 封面上传卡片 ---
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clickable(enabled = !isUploading && !viewModel.isCreating) {
                        launcher.launch("image/*")
                    },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    if (selectedImageUri != null) {
                        AsyncImage(
                            model = selectedImageUri,
                            contentDescription = "Cover Preview",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(40.dp), tint = Color.Gray)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("点击上传封面图片", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    if (isUploading) {
                        Box(
                            modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Color.White)
                        }
                    }
                }
            }

            OutlinedTextField(
                value = roomName,
                onValueChange = { roomName = it },
                label = { Text("房间名称") },
                placeholder = { Text("起个好听的名字吸引小伙伴吧") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                enabled = !viewModel.isCreating,
                singleLine = true
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    viewModel.createRoom(
                        title = roomName,
                        cover = uploadedCoverUrl, // 提交上传后得到的 URL/UUID
                        onSuccess = { room ->
                            Toast.makeText(context, "开房成功！", Toast.LENGTH_SHORT).show()
                            navController.navigate("${Screen.RoomDetail.route}/${room.id}") {
                                popUpTo(Screen.CreateRoom.route) { inclusive = true }
                            }
                        },
                        onError = { msg ->
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        }
                    )
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(25.dp),
                enabled = roomName.isNotBlank() && !viewModel.isCreating && !isUploading
            ) {
                if (viewModel.isCreating) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text("立即开房", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

/**
 * 复用上传逻辑
 */
private suspend fun uploadImageAction(
    uri: Uri,
    context: android.content.Context
): String {
    return withContext(Dispatchers.IO) {
        val inputStream = context.contentResolver.openInputStream(uri) ?: throw Exception("无法读取图片")
        val file = File(context.cacheDir, "room_cover_${System.currentTimeMillis()}.jpg")

        FileOutputStream(file).use { output ->
            inputStream.use { input -> input.copyTo(output) }
        }

        val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

        val response = NetworkModule.Api.uploadFile(body)
        response.data.fileUrl
    }
}