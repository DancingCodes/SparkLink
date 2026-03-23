package love.moonc.sparklink.ui.screens

import android.net.Uri
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
import love.moonc.sparklink.ui.navigation.CreateRoomRoute
import love.moonc.sparklink.ui.navigation.RoomDetailRoute

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRoomScreen(navController: NavController) {
    val context = LocalContext.current
    val viewModel: CreateRoomViewModel = viewModel()

    var roomName by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            viewModel.uploadCover(context, it)
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
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clickable(enabled = !viewModel.isUploading && !viewModel.isCreating) {
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

                    if (viewModel.isUploading) {
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
                    viewModel.createAndEnterRoom(
                        title = roomName,
                        onSuccess = { roomId, enterData ->
                            navController.navigate(
                                RoomDetailRoute(
                                    roomId = roomId,
                                    agoraToken = enterData.agoraToken,
                                    agoraUid = enterData.agoraUid,
                                    channelName = enterData.channelName
                                )
                            ) {
                                popUpTo<CreateRoomRoute> { inclusive = true }
                            }
                        }
                    )
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(25.dp),
                enabled = roomName.isNotBlank() && !viewModel.isCreating && !viewModel.isUploading
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