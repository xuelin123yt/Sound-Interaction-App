package com.soundinteractionapp.screens.profile.dialogs

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 帳號刪除對話框
 * 用於確認並執行帳號刪除操作
 */
@Composable
fun AccountDeleterDialog(
    onDismiss: () -> Unit,
    onConfirm: (password: String) -> Unit
) {
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFFE57373),
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    "刪除帳號",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE57373),
                    fontSize = 18.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 警告卡片
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFEBEE)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            "⚠️ 此操作無法復原",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFC62828)
                        )
                        Text(
                            "以下資料將被永久刪除：",
                            fontSize = 12.sp,
                            color = Color(0xFF666666)
                        )
                        Text(
                            "• 個人資料",
                            fontSize = 11.sp,
                            color = Color(0xFF666666)
                        )
                        Text(
                            "• 遊戲進度",
                            fontSize = 11.sp,
                            color = Color(0xFF666666)
                        )
                        Text(
                            "• 所有成就",
                            fontSize = 11.sp,
                            color = Color(0xFF666666)
                        )
                    }
                }

                // 密碼輸入框
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("輸入密碼確認", fontSize = 14.sp) },
                    placeholder = { Text("請輸入您的密碼", fontSize = 13.sp) },
                    visualTransformation = if (passwordVisible)
                        VisualTransformation.None
                    else
                        PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                if (passwordVisible)
                                    Icons.Default.Visibility
                                else
                                    Icons.Default.VisibilityOff,
                                contentDescription = if (passwordVisible)
                                    "隱藏密碼"
                                else
                                    "顯示密碼",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFE57373),
                        focusedLabelColor = Color(0xFFE57373)
                    ),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    when {
                        password.isBlank() -> {
                            Toast.makeText(
                                context,
                                "請輸入密碼",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        password.length < 6 -> {
                            Toast.makeText(
                                context,
                                "密碼長度不正確",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        else -> onConfirm(password)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE57373)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    "確認刪除",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, Color(0xFF9E9E9E))
            ) {
                Text(
                    "取消",
                    fontSize = 14.sp,
                    color = Color(0xFF666666)
                )
            }
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = Color.White
    )
}