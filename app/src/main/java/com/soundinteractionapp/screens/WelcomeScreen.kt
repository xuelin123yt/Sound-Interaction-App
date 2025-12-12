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
                    label = { Text("帳號(英數混合,至少4字元)") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.AccountCircle,
                            null,
                            tint = Color(0xFF673AB7)
                        )
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 密碼輸入框
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it; errorMessage = null },
                    label = { Text("密碼(至少6個字元)") },
                    leadingIcon = {
                        Icon(Icons.Default.Lock, null, tint = Color(0xFF673AB7))
                    },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                if (passwordVisible) Icons.Default.Visibility
                                else Icons.Default.VisibilityOff,
                                null
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible)
                        VisualTransformation.None
                    else
                        PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 確認密碼輸入框
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it; errorMessage = null },
                    label = { Text("確認密碼") },
                    leadingIcon = {
                        Icon(Icons.Default.Lock, null, tint = Color(0xFF673AB7))
                    },
                    trailingIcon = {
                        IconButton(onClick = { confirmVisible = !confirmVisible }) {
                            Icon(
                                if (confirmVisible) Icons.Default.Visibility
                                else Icons.Default.VisibilityOff,
                                null
                            )
                        }
                    },
                    visualTransformation = if (confirmVisible)
                        VisualTransformation.None
                    else
                        PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
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
                        enabled = !isLoading
                    ) {
                        Text("取消")
                    }
                    Button(
                        onClick = {
                            when {
                                account.isBlank() -> errorMessage = "請輸入帳號"
                                account.length < 4 -> errorMessage = "帳號至少需要4個字元"
                                !account.any { it.isLetter() } -> errorMessage = "帳號需包含英文字母"
                                !account.any { it.isDigit() } -> errorMessage = "帳號需包含數字"
                                !account.all { it.isLetterOrDigit() } -> errorMessage = "帳號只能包含英文和數字"
                                password.isBlank() -> errorMessage = "請輸入密碼"
                                password.length < 6 -> errorMessage = "密碼至少需要6個字元"
                                password != confirmPassword -> errorMessage = "兩次輸入的密碼不一致"
                                else -> {
                                    isLoading = true
                                    errorMessage = null
                                    authViewModel.signUp(account, password) { success, err ->
                                        isLoading = false
                                        if (!success) {
                                            errorMessage = err ?: "註冊失敗,請稍後再試"
                                        } else {
                                            onDismiss()
                                        }
                                    }
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF673AB7)
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
                            Text("註冊", fontWeight = FontWeight.Bold)
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
            modifier = Modifier.widthIn(max = 320.dp)
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
                    label = { Text("帳號") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.AccountCircle,
                            null,
                            tint = Color(0xFF673AB7)
                        )
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 密碼輸入框
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it; errorMessage = null },
                    label = { Text("密碼") },
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
                    modifier = Modifier.fillMaxWidth()
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
                        enabled = !isLoading
                    ) {
                        Text("取消")
                    }
                    Button(
                        onClick = loginAction,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF673AB7)
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
                            Text("登入", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}