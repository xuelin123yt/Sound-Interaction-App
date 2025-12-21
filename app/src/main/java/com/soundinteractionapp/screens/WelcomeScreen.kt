package com.soundinteractionapp.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.soundinteractionapp.R
import com.soundinteractionapp.SoundManager
import com.soundinteractionapp.data.AuthState
import com.soundinteractionapp.data.AuthViewModel
import com.soundinteractionapp.data.RankingViewModel
import kotlinx.coroutines.launch

@Composable
fun WelcomeScreen(
    soundManager: SoundManager,
    onNavigateToFreePlay: () -> Unit,
    onNavigateToRelax: () -> Unit,
    onNavigateToGame: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToLeaderboard: () -> Unit, // ✅ 新增此參數
    onLogout: () -> Unit,
    authViewModel: AuthViewModel = viewModel(),
    rankingViewModel: RankingViewModel = viewModel()
) {
    val authState by authViewModel.authState.collectAsState()
    var showRegisterDialog by remember { mutableStateOf(false) }
    var showLoginDialog by remember { mutableStateOf(false) }

    // ✅ 根據登入狀態控制 BGM（移除 reloadScores，Repository 會自動處理）
    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Authenticated -> {
                // 已登入，播放 BGM（分數會由 Repository 自動同步）
                soundManager.playBgm(R.raw.bgm)
            }
            else -> {
                // 未登入，停止 BGM
                soundManager.stopBgm()
            }
        }
    }

    when (authState) {
        is AuthState.Loading -> {
            // 載入中畫面
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF8F5FF)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        color = Color(0xFF673AB7),
                        strokeWidth = 4.dp,
                        modifier = Modifier.size(60.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "載入中...",
                        fontSize = 18.sp,
                        color = Color(0xFF673AB7),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        is AuthState.Authenticated -> {
            // 已登入，顯示遊戲主畫面
            GameHomeScreen(
                soundManager = soundManager,
                onNavigateToFreePlay = onNavigateToFreePlay,
                onNavigateToRelax = onNavigateToRelax,
                onNavigateToGame = onNavigateToGame,
                onNavigateToProfile = onNavigateToProfile,
                onNavigateToSettings = onNavigateToSettings,
                onNavigateToLeaderboard = onNavigateToLeaderboard, // ✅ 傳遞參數
                onLogout = onLogout
            )
        }

        else -> {
            // 未登入，顯示登入畫面
            LoginScreen(
                onLoginClick = { showLoginDialog = true },
                onRegisterClick = { showRegisterDialog = true },
                onGuestLoginClick = {
                    authViewModel.signInAnonymously { _, _ -> }
                }
            )
        }
    }

    // 註冊對話框
    if (showRegisterDialog) {
        RegisterDialog(
            onDismiss = {
                showRegisterDialog = false
                authViewModel.resetAuthState()
            },
            authViewModel = authViewModel
        )
    }

    // 登入對話框
    if (showLoginDialog) {
        LoginDialog(
            onDismiss = {
                showLoginDialog = false
                authViewModel.resetAuthState()
            },
            authViewModel = authViewModel
        )
    }
}

// =====================================================
// 📝 註冊對話框
// =====================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterDialog(
    onDismiss: () -> Unit,
    authViewModel: AuthViewModel
) {
    var account by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    // ✅ 添加注册逻辑
    val registerAction = remember {
        {
            when {
                account.isBlank() -> {
                    errorMessage = "請輸入帳號"
                }
                account.length < 4 -> {
                    errorMessage = "帳號至少需要 4 個字元"
                }
                !account.matches(Regex("^[a-zA-Z0-9]+$")) -> {
                    errorMessage = "帳號只能包含英文字母和數字"
                }
                password.isBlank() -> {
                    errorMessage = "請輸入密碼"
                }
                password.length < 6 -> {
                    errorMessage = "密碼至少需要 6 個字元"
                }
                confirmPassword.isBlank() -> {
                    errorMessage = "請確認密碼"
                }
                password != confirmPassword -> {
                    errorMessage = "兩次密碼輸入不一致"
                }
                else -> {
                    isLoading = true
                    errorMessage = null

                    authViewModel.signUp(account, password) { success, error ->
                        isLoading = false
                        if (!success) {
                            // ✅ 所有错误提示都不带前缀
                            errorMessage = when {
                                error?.contains("email-already-in-use", true) == true ->
                                    "此帳號已被註冊"
                                error?.contains("weak-password", true) == true ->
                                    "密碼強度不足"
                                error?.contains("network", true) == true ->
                                    "網路異常，請檢查連線"
                                error?.contains("invalid-email", true) == true ->
                                    "帳號格式錯誤"
                                else -> error ?: "註冊失敗，請稍後再試"  // ✅ 移除前缀
                            }
                        } else {
                            onDismiss()
                        }
                    }
                }
            }
        }
    }

    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            tonalElevation = 8.dp,
            modifier = Modifier.widthIn(max = 320.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 20.dp, vertical = 24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "註冊新帳號",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF673AB7)
                )
                Spacer(modifier = Modifier.height(20.dp))

                // 帳號輸入框
                OutlinedTextField(
                    value = account,
                    onValueChange = { account = it; errorMessage = null },
                    label = { Text("帳號(英數混合,至少4字元)", color = Color.Black) },
                    leadingIcon = {
                        Icon(
                            Icons.Default.AccountCircle,
                            null,
                            tint = Color(0xFF673AB7)
                        )
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        focusedLabelColor = Color.Black,
                        unfocusedLabelColor = Color.Black
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 密碼輸入框
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it; errorMessage = null },
                    label = { Text("密碼(至少6個字元)", color = Color.Black) },
                    leadingIcon = {
                        Icon(Icons.Default.Lock, null, tint = Color(0xFF673AB7))
                    },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                if (passwordVisible) Icons.Default.Visibility
                                else Icons.Default.VisibilityOff,
                                null,
                                tint = Color(0xFF673AB7)
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible)
                        VisualTransformation.None
                    else
                        PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        focusedLabelColor = Color.Black,
                        unfocusedLabelColor = Color.Black
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 確認密碼輸入框
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it; errorMessage = null },
                    label = { Text("確認密碼", color = Color.Black) },
                    leadingIcon = {
                        Icon(Icons.Default.Lock, null, tint = Color(0xFF673AB7))
                    },
                    trailingIcon = {
                        IconButton(onClick = { confirmVisible = !confirmVisible }) {
                            Icon(
                                if (confirmVisible) Icons.Default.Visibility
                                else Icons.Default.VisibilityOff,
                                null,
                                tint = Color(0xFF673AB7)
                            )
                        }
                    },
                    visualTransformation = if (confirmVisible)
                        VisualTransformation.None
                    else
                        PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done  // ✅ 添加完成动作
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { if (!isLoading) registerAction() }  // ✅ 按 Enter 触发注册
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        focusedLabelColor = Color.Black,
                        unfocusedLabelColor = Color.Black
                    )
                )

                // 錯誤訊息顯示區域
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 50.dp, max = 80.dp)  // ✅ 增加最小高度和最大高度
                        .padding(vertical = 8.dp)
                ) {
                    errorMessage?.let {
                        Text(
                            text = it,
                            color = Color(0xFFE91E63),
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp,  // ✅ 添加行高，让多行文字更易读
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.Center)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 按鈕區域
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        enabled = !isLoading,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.Black
                        )
                    ) {
                        Text("取消", color = Color.Black)
                    }

                    // ✅ 修复：添加注册逻辑
                    Button(
                        onClick = registerAction,  // ✅ 这里改成调用 registerAction
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF673AB7),
                            contentColor = Color.White
                        ),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(18.dp)
                            )
                        } else {
                            Text("註冊", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

// =====================================================
// 🔐 登入對話框
// =====================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginDialog(
    onDismiss: () -> Unit,
    authViewModel: AuthViewModel
) {
    var account by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val loginAction = remember {
        {
            when {
                account.isBlank() -> errorMessage = "請輸入帳號"
                password.isBlank() -> errorMessage = "請輸入密碼"
                else -> {
                    isLoading = true
                    errorMessage = null
                    authViewModel.signIn(account, password) { success, error ->
                        isLoading = false
                        if (!success) {
                            errorMessage = when {
                                error?.contains("wrong-password", true) == true -> "密碼錯誤"
                                error?.contains("user-not-found", true) == true -> "此帳號尚未註冊"
                                error?.contains("invalid-credential", true) == true -> "帳號或密碼錯誤"
                                error?.contains("network", true) == true -> "網路異常,請檢查連線"
                                else -> "登入失敗,請再試一次"
                            }
                        } else {
                            onDismiss()
                        }
                    }
                }
            }
        }
    }

    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            tonalElevation = 8.dp,
            modifier = Modifier.widthIn(max = 360.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 20.dp, vertical = 24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "登入帳號",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF673AB7)
                )
                Spacer(modifier = Modifier.height(20.dp))

// 帳號輸入框
                OutlinedTextField(
                    value = account,
                    onValueChange = { account = it; errorMessage = null },
                    label = { Text("帳號", color = Color.Black) },  // ✅ 加這行
                    leadingIcon = {
                        Icon(
                            Icons.Default.AccountCircle,
                            null,
                            tint = Color(0xFF673AB7)
                        )
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(  // ✅ 加入整個 colors 區塊
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        focusedLabelColor = Color.Black,
                        unfocusedLabelColor = Color.Black
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

// 密碼輸入框
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it; errorMessage = null },
                    label = { Text("密碼", color = Color.Black) },  // ✅ 加這行
                    leadingIcon = {
                        Icon(Icons.Default.Lock, null, tint = Color(0xFF673AB7))
                    },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                if (passwordVisible) Icons.Default.Visibility
                                else Icons.Default.VisibilityOff,
                                null,
                                tint = Color(0xFF673AB7)
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible)
                        VisualTransformation.None
                    else
                        PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { if (!isLoading) loginAction() }
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(  // ✅ 加入整個 colors 區塊
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        focusedLabelColor = Color.Black,
                        unfocusedLabelColor = Color.Black
                    )
                )

                // 錯誤訊息顯示區域
                Box(modifier = Modifier.height(40.dp)) {
                    errorMessage?.let {
                        Text(
                            text = it,
                            color = Color(0xFFE91E63),
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.Center)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

// 按鈕區域
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        enabled = !isLoading,
                        colors = ButtonDefaults.outlinedButtonColors(  // ✅ 加這行
                            contentColor = Color.Black
                        )
                    ) {
                        Text("取消", color = Color.Black)  // ✅ 加 color
                    }
                    Button(
                        onClick = loginAction,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF673AB7),
                            contentColor = Color.White  // ✅ 加這行
                        ),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(18.dp)
                            )
                        } else {
                            Text("登入", fontWeight = FontWeight.Bold, color = Color.White)  // ✅ 加 color
                        }
                    }
                }
            }
        }
    }
}