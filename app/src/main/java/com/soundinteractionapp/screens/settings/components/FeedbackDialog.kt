package com.soundinteractionapp.screens.settings.components

import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Title
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun FeedbackDialog(
    isLoggedIn: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (title: String, description: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()
    val coroutine = rememberCoroutineScope()

    Dialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.88f)
                    .fillMaxHeight(0.80f),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFAFAFA)
                ),
                elevation = CardDefaults.cardElevation(12.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {

                    // 標題漸層區塊
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFF667eea),
                                        Color(0xFF764ba2)
                                    )
                                )
                            )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "💬 意見回饋",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )

                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "關閉",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    // 可捲動內容區
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight()
                            .verticalScroll(scrollState)
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(18.dp)
                    ) {

                        if (!isLoggedIn) {
                            // 未登入顯示卡片
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFFFFF8E1)
                                ),
                                elevation = CardDefaults.cardElevation(0.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text("🔒", fontSize = 40.sp)
                                    Text(
                                        text = "需要登入",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFF57C00)
                                    )
                                    Text(
                                        text = "此功能僅供登入用戶使用\n請先登入您的帳號以繼續",
                                        fontSize = 13.sp,
                                        color = Color(0xFF5D4037),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                        lineHeight = 20.sp
                                    )
                                }
                            }

                            // 我知道了按鈕
                            Button(
                                onClick = onDismiss,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF667eea)
                                ),
                                elevation = ButtonDefaults.buttonElevation(3.dp)
                            ) {
                                Text(
                                    "我知道了",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else {

                            // 標題輸入區
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        Icons.Outlined.Title,
                                        contentDescription = null,
                                        tint = Color(0xFF667eea),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        "標題",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF333333)
                                    )
                                }

                                OutlinedTextField(
                                    value = title,
                                    onValueChange = {
                                        title = it
                                        showError = false
                                    },
                                    placeholder = {
                                        Text(
                                            "簡短描述您的問題或建議",
                                            fontSize = 13.sp,
                                            color = Color(0xFFBDBDBD)
                                        )
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(60.dp),
                                    singleLine = true,
                                    enabled = !isSubmitting,
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Text,
                                        imeAction = ImeAction.Next
                                    ),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFF667eea),
                                        unfocusedBorderColor = Color(0xFFE0E0E0),
                                        focusedContainerColor = Color.White,
                                        unfocusedContainerColor = Color.White,
                                        cursorColor = Color(0xFF667eea)
                                    ),
                                    shape = RoundedCornerShape(14.dp),
                                    textStyle = LocalTextStyle.current.copy(fontSize = 14.sp)
                                )
                            }

                            // 內容輸入框
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        Icons.Outlined.Description,
                                        contentDescription = null,
                                        tint = Color(0xFF667eea),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        "詳細內容",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF333333)
                                    )
                                }

                                OutlinedTextField(
                                    value = description,
                                    onValueChange = {
                                        description = it
                                        showError = false
                                    },
                                    placeholder = {
                                        Text(
                                            "請詳細說明您的想法或遇到的問題...",
                                            fontSize = 13.sp,
                                            color = Color(0xFFBDBDBD)
                                        )
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp),
                                    maxLines = 10,
                                    enabled = !isSubmitting,
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Text,
                                        imeAction = ImeAction.Default
                                    ),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFF667eea),
                                        unfocusedBorderColor = Color(0xFFE0E0E0),
                                        focusedContainerColor = Color.White,
                                        unfocusedContainerColor = Color.White,
                                        cursorColor = Color(0xFF667eea)
                                    ),
                                    shape = RoundedCornerShape(14.dp),
                                    textStyle = LocalTextStyle.current.copy(fontSize = 14.sp)
                                )
                            }

                            // ⚠️ 錯誤提示 + 動畫
                            AnimatedVisibility(
                                visible = showError,
                                enter = fadeIn() + expandVertically(),
                                exit = fadeOut() + shrinkVertically()
                            ) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color(0xFFFFEBEE)
                                    ),
                                    elevation = CardDefaults.cardElevation(0.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Text("⚠️", fontSize = 18.sp)
                                        Text(
                                            text = "請填寫標題和內容",
                                            fontSize = 13.sp,
                                            color = Color(0xFFC62828),
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }

                            // 捲動動畫:錯誤時滑動到最底部
                            LaunchedEffect(showError) {
                                if (showError) {
                                    scrollState.animateScrollTo(
                                        scrollState.maxValue,
                                        animationSpec = tween(
                                            durationMillis = 450,
                                            easing = FastOutSlowInEasing
                                        )
                                    )
                                }
                            }

                            // 按鈕
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedButton(
                                    onClick = onDismiss,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(52.dp),
                                    enabled = !isSubmitting,
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = Color(0xFF667eea)
                                    ),
                                    border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.5.dp)
                                ) {
                                    Text(
                                        "取消",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                Button(
                                    onClick = {
                                        if (title.isBlank() || description.isBlank()) {
                                            showError = true
                                        } else {
                                            isSubmitting = true
                                            onSubmit(title.trim(), description.trim())
                                            showSuccess = true

                                            coroutine.launch {
                                                delay(1500)
                                                onDismiss()
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(52.dp),
                                    enabled = !isSubmitting,
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF667eea)
                                    ),
                                    elevation = ButtonDefaults.buttonElevation(3.dp)
                                ) {
                                    if (isSubmitting) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(18.dp),
                                            color = Color.White,
                                            strokeWidth = 2.dp
                                        )
                                        Spacer(Modifier.width(10.dp))
                                        Text(
                                            "送出中...",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    } else {
                                        Text(
                                            "送出",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 成功提示層
            AnimatedVisibility(
                visible = showSuccess,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                Card(
                    modifier = Modifier
                        .padding(40.dp)
                        .size(200.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(24.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(androidx.compose.foundation.shape.CircleShape)
                                .background(Color(0xFFE8F5E9)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(48.dp)
                            )
                        }

                        Spacer(Modifier.height(20.dp))

                        Text(
                            "送出成功",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32)
                        )

                        Spacer(Modifier.height(8.dp))

                        Text(
                            "感謝您的回饋",
                            fontSize = 14.sp,
                            color = Color(0xFF666666)
                        )
                    }
                }
            }
        }
    }
}