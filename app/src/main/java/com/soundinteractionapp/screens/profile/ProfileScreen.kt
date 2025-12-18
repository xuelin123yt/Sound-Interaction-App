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
import com.soundinteractionapp.SoundManager
import com.soundinteractionapp.data.AuthViewModel
import com.soundinteractionapp.data.ProfileViewModel
import com.soundinteractionapp.data.RankingViewModel
import com.soundinteractionapp.screens.profile.components.*
import com.soundinteractionapp.screens.profile.dialogs.*
import com.soundinteractionapp.screens.profile.models.AchievementProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit = {},
    onNavigateToLogin: () -> Unit = {},
    soundManager: SoundManager,
    authViewModel: AuthViewModel = viewModel(),
    profileViewModel: ProfileViewModel = viewModel(),
    rankingViewModel: RankingViewModel = viewModel()
) {
    val context = LocalContext.current

    // ✅ 防抖狀態
    var isNavigating by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        profileViewModel.loadUserProfile()
    }

    val userProfile by profileViewModel.userProfile.collectAsState()
    val isAnonymous by profileViewModel.isAnonymous.collectAsState()
    val isLoading by profileViewModel.isLoading.collectAsState()

    var showEditDialog by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showAvatarPicker by remember { mutableStateOf(false) }

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

    val achievements = remember { AchievementProvider.getAllAchievements() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("個人資料", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (isNavigating) return@IconButton
                            isNavigating = true
                            soundManager.playSFX("cancel")
                            onNavigateBack()
                        },
                        enabled = !isNavigating
                    ) {
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
            if (isAnonymous) {
                AnonymousWarning()
                Spacer(Modifier.height(24.dp))
            }

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

            if (!isAnonymous) {
                ScoreBoardSection(rankingViewModel)
                Spacer(Modifier.height(24.dp))
            }

            AchievementDisplay(
                achievements = achievements,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(32.dp))

            if (!isAnonymous) {
                DeleteAccountButton(
                    onClick = { showDeleteDialog = true }
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }

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
        onAccountDeleted = onLogout,
        context = context
    )
}

@Composable
fun ScoreBoardSection(rankingViewModel: RankingViewModel) {
    val scores by rankingViewModel.scores.collectAsState()

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.EmojiEvents, null, tint = Color(0xFFFFC107))
                Spacer(Modifier.width(8.dp))
                Text(
                    "歷史最高紀錄",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333)
                )
            }

            Divider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = Color(0xFFEEEEEE)
            )

            Text(
                "🎵 關卡 1: 跟著按",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray
            )
            Spacer(Modifier.height(8.dp))
            ScoreRowItem("簡單", scores.level1Easy, Color(0xFF81C784))
            ScoreRowItem("普通", scores.level1Normal, Color(0xFF4FC3F7))
            ScoreRowItem("困難", scores.level1Hard, Color(0xFFFF8A65))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp, horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "總分",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333)
                )
                Text(
                    "${scores.level1Total} 分",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF673AB7)
                )
            }

            Spacer(Modifier.height(12.dp))

            Text(
                "🎹 關卡 2: 鋼琴節奏",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray
            )
            Spacer(Modifier.height(8.dp))
            ScoreRowItem("簡單 (天空之城)", scores.level2Easy, Color(0xFF4CAF50))
            ScoreRowItem("普通 (龍貓)", scores.level2Normal, Color(0xFF2196F3))
            ScoreRowItem("困難 (Maria)", scores.level2Hard, Color(0xFFE53935))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp, horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "總分",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333)
                )
                Text(
                    "${scores.level2Total} 分",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF673AB7)
                )
            }

            Spacer(Modifier.height(12.dp))

            Text(
                "🎤 關卡 3: 音高控制",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray
            )
            Spacer(Modifier.height(8.dp))
            ScoreRowItem("最高分", scores.level3Score, Color(0xFFE91E63))

            Spacer(Modifier.height(12.dp))

            Text(
                "🎵 關卡 4: 音遊模式",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray
            )
            Spacer(Modifier.height(8.dp))
            ScoreRowItem("哆啦A夢 主題曲", scores.level4Osu01, Color(0xFF9C27B0))
            ScoreRowItem("神魔之塔 主題曲（夜）", scores.level4Osu02, Color(0xFF673AB7))
            ScoreRowItem("能看見海的街道", scores.level4Osu03, Color(0xFF3F51B5))
            ScoreRowItem("伴隨著你 (純音樂版)", scores.level4Osu04, Color(0xFF2196F3))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp, horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "總分",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333)
                )
                Text(
                    "${scores.level4Total} 分",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF673AB7)
                )
            }
        }
    }
}

@Composable
fun ScoreRowItem(label: String, score: Int, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 15.sp, color = Color(0xFF555555))
        Text(
            "$score 分",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = if (score > 0) color else Color.LightGray
        )
    }
}

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
                "您目前以訪客身分登入,無法修改個人資料",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFFE65100)
            )
        }
    }
}

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

    if (showDeleteDialog) {
        AccountDeleterDialog(
            onDismiss = onDismissDeleteDialog,
            onConfirm = { password ->
                profileViewModel.deleteAccount(password) { success, error ->
                    if (success) {
                        onDismissDeleteDialog()
                        Toast.makeText(context, "帳號已刪除", Toast.LENGTH_SHORT).show()
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