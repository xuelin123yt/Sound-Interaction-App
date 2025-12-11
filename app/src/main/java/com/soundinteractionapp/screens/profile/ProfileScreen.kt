package com.soundinteractionapp.screens.profile

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.soundinteractionapp.R
import com.soundinteractionapp.data.ProfileViewModel
import com.soundinteractionapp.screens.profile.components.*
import com.soundinteractionapp.screens.profile.dialogs.*
import com.soundinteractionapp.screens.profile.models.AchievementProvider

/**
 * 個人資料主畫面
 * 顯示用戶資訊、成就展示和相關操作
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit,
    onAccountDeleted: () -> Unit = {},
    profileViewModel: ProfileViewModel = viewModel()
) {
    val context = LocalContext.current

    // 載入用戶資料
    LaunchedEffect(Unit) {
        profileViewModel.loadUserProfile()
    }

    // 狀態收集
    val userProfile by profileViewModel.userProfile.collectAsState()
    val isAnonymous by profileViewModel.isAnonymous.collectAsState()
    val isLoading by profileViewModel.isLoading.collectAsState()

    // 對話框顯示狀態
    var showEditDialog by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showAvatarPicker by remember { mutableStateOf(false) }

    // 預設頭像列表
    val defaultAvatars = remember {
        listOf(
            R.drawable.avatar_01, R.drawable.avatar_02, R.drawable.avatar_03, R.drawable.avatar_04,
            R.drawable.avatar_05, R.drawable.avatar_06, R.drawable.avatar_07, R.drawable.avatar_08,
            R.drawable.avatar_09, R.drawable.avatar_10, R.drawable.avatar_11, R.drawable.avatar_12,
            R.drawable.avatar_13, R.drawable.avatar_14, R.drawable.avatar_15, R.drawable.avatar_16,
            R.drawable.avatar_17, R.drawable.avatar_18, R.drawable.avatar_19, R.drawable.avatar_20,
            R.drawable.avatar_21, R.drawable.avatar_22, R.drawable.avatar_23, R.drawable.avatar_24
        )
    }

    // 獲取成就資料
    val achievements = remember { AchievementProvider.getAllAchievements() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("個人資料", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF673AB7),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF8F5FF))
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 訪客模式提示
            if (isAnonymous) {
                AnonymousWarning()
                Spacer(Modifier.height(24.dp))
            }

            // 頭像區域
            AvatarSection(
                displayName = userProfile.displayName,
                account = userProfile.account,
                photoUrl = userProfile.photoUrl,
                isAnonymous = isAnonymous,
                isLoading = isLoading,
                defaultAvatars = defaultAvatars,
                onAvatarClick = { showAvatarPicker = true }
            )

            Spacer(Modifier.height(32.dp))

            // 使用者資訊卡片
            UserInfoCard(
                account = userProfile.account,
                displayName = userProfile.displayName,
                bio = userProfile.bio,
                createdAt = userProfile.createdAt,
                isAnonymous = isAnonymous,
                onEditName = { showEditDialog = true },
                onEditBio = { showAboutDialog = true },
                onChangePassword = { showPasswordDialog = true },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(24.dp))

            // 成就展示
            AchievementDisplay(
                achievements = achievements,
                modifier = Modifier.fillMaxWidth()
            )

            // 刪除帳號按鈕（僅非訪客顯示）
            if (!isAnonymous) {
                Spacer(Modifier.height(32.dp))

                DeleteAccountButton(
                    onClick = { showDeleteDialog = true }
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }

    // 所有對話框
    ProfileDialogs(
        showAvatarPicker = showAvatarPicker,
        showEditDialog = showEditDialog,
        showAboutDialog = showAboutDialog,
        showPasswordDialog = showPasswordDialog,
        showDeleteDialog = showDeleteDialog,
        defaultAvatars = defaultAvatars,
        userProfile = userProfile,
        profileViewModel = profileViewModel,
        onDismissAvatarPicker = { showAvatarPicker = false },
        onDismissEditDialog = { showEditDialog = false },
        onDismissAboutDialog = { showAboutDialog = false },
        onDismissPasswordDialog = { showPasswordDialog = false },
        onDismissDeleteDialog = { showDeleteDialog = false },
        onAccountDeleted = onAccountDeleted,
        context = context
    )
}

/**
 * 訪客模式警告提示
 */
@Composable
private fun AnonymousWarning() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Info,
                contentDescription = null,
                tint = Color(0xFFFF9800),
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(12.dp))
            Text(
                "您目前以訪客身分登入，無法修改個人資料",
                fontSize = 14.sp,
                color = Color(0xFFE65100)
            )
        }
    }
}

/**
 * 刪除帳號按鈕
 */
@Composable
private fun DeleteAccountButton(
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = Color(0xFFE57373)
        ),
        border = BorderStroke(1.dp, Color(0xFFE57373)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Icon(
            Icons.Default.DeleteForever,
            contentDescription = "刪除帳號",
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text("刪除帳號", fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

/**
 * 所有對話框的統一管理
 */
@Composable
private fun ProfileDialogs(
    showAvatarPicker: Boolean,
    showEditDialog: Boolean,
    showAboutDialog: Boolean,
    showPasswordDialog: Boolean,
    showDeleteDialog: Boolean,
    defaultAvatars: List<Int>,
    userProfile: com.soundinteractionapp.data.UserProfile,
    profileViewModel: ProfileViewModel,
    onDismissAvatarPicker: () -> Unit,
    onDismissEditDialog: () -> Unit,
    onDismissAboutDialog: () -> Unit,
    onDismissPasswordDialog: () -> Unit,
    onDismissDeleteDialog: () -> Unit,
    onAccountDeleted: () -> Unit,
    context: android.content.Context
) {
    // 頭像選擇器
    if (showAvatarPicker) {
        AvatarSelectorDialog(
            avatars = defaultAvatars,
            currentAvatarResId = userProfile.photoUrl.toIntOrNull(),
            onDismiss = onDismissAvatarPicker,
            onSelect = { selectedResId ->
                profileViewModel.updateAvatar(selectedResId.toString())
                onDismissAvatarPicker()
                Toast.makeText(context, "頭像已更新", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // 暱稱編輯
    if (showEditDialog) {
        NameEditorDialog(
            currentName = userProfile.displayName,
            onDismiss = onDismissEditDialog,
            onConfirm = { newName ->
                profileViewModel.updateDisplayName(newName)
                onDismissEditDialog()
                Toast.makeText(context, "暱稱已更新", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // 關於我編輯
    if (showAboutDialog) {
        BioEditorDialog(
            currentBio = userProfile.bio,
            onDismiss = onDismissAboutDialog,
            onConfirm = { newBio ->
                profileViewModel.updateBio(newBio)
                onDismissAboutDialog()
                Toast.makeText(context, "已更新關於我", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // 密碼變更
    if (showPasswordDialog) {
        PasswordChangerDialog(
            onDismiss = onDismissPasswordDialog,
            onConfirm = { oldPassword, newPassword ->
                profileViewModel.changePassword(oldPassword, newPassword) { success, error ->
                    if (success) {
                        onDismissPasswordDialog()
                        Toast.makeText(context, "密碼已成功變更", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, error ?: "變更失敗", Toast.LENGTH_LONG).show()
                    }
                }
            }
        )
    }

    // 帳號刪除
    if (showDeleteDialog) {
        AccountDeleterDialog(
            onDismiss = onDismissDeleteDialog,
            onConfirm = { password ->
                profileViewModel.deleteAccount(password) { success, error ->
                    if (success) {
                        onDismissDeleteDialog()
                        Toast.makeText(
                            context,
                            "帳號已刪除，即將返回登入畫面",
                            Toast.LENGTH_SHORT
                        ).show()

                        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                            onAccountDeleted()
                        }, 500)
                    } else {
                        Toast.makeText(context, error ?: "刪除失敗", Toast.LENGTH_LONG).show()
                    }
                }
            }
        )
    }
}