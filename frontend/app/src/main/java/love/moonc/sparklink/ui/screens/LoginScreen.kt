package love.moonc.sparklink.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import love.moonc.sparklink.data.local.UserPreferences
import love.moonc.sparklink.ui.navigation.Screen
import love.moonc.sparklink.ui.viewmodel.LoginViewModel

@Composable
fun LoginScreen(navController: NavController) {
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val userPrefs = remember { UserPreferences(context) }

    // ✨ 大厂标准：手动创建一个简单的 ViewModel 实例（不带注入框架时的做法）
    // 或者如果你之后用了 Hilt，直接 val loginViewModel: LoginViewModel = viewModel() 即可
    val loginViewModel = remember { LoginViewModel(userPrefs) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 30.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Spacer(modifier = Modifier.height(100.dp))

        Text(
            text = "SparkLink",
            style = MaterialTheme.typography.displaySmall.copy(
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )
        )
        Text(
            text = "在星空下遇见，陪你看世界",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            modifier = Modifier.padding(top = 8.dp)
        )

        Spacer(modifier = Modifier.height(60.dp))

        // --- 输入框：禁用状态绑定 isLoggingIn ---
        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("手机号") },
            placeholder = { Text("请输入手机号") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            enabled = !loginViewModel.isLoggingIn,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("密码") },
            placeholder = { Text("请输入密码") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            enabled = !loginViewModel.isLoggingIn,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = image, contentDescription = null)
                }
            }
        )

        Spacer(modifier = Modifier.height(40.dp))

        // --- 登录按钮：逻辑全部交给 ViewModel ---
        Button(
            onClick = {
                loginViewModel.login(
                    phone = phone,
                    pass = password,
                    onSuccess = {
                        Toast.makeText(context, "欢迎回来！", Toast.LENGTH_SHORT).show()
                        // 登录成功后跳转到主页，并销毁登录页
                        navController.navigate(Screen.TabScreen.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onError = { msg ->
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    }
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
            // 按钮逻辑：Loading 时禁用，且格式校验通过
            enabled = !loginViewModel.isLoggingIn && phone.length == 11 && password.isNotBlank()
        ) {
            if (loginViewModel.isLoggingIn) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text("立即进入", style = MaterialTheme.typography.titleMedium)
            }
        }

        TextButton(
            onClick = { navController.navigate(Screen.Register.route) },
            modifier = Modifier.align(Alignment.CenterHorizontally),
            enabled = !loginViewModel.isLoggingIn
        ) {
            Text("新用户？点击注册", color = MaterialTheme.colorScheme.primary)
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 40.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "登录即代表同意《用户协议》与《隐私政策》",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )
        }
    }
}