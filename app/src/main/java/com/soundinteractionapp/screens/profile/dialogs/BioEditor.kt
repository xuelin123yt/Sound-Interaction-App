package com.soundinteractionapp.screens.profile.dialogs

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

/**
 * 關於我編輯對話框
 * 用於修改使用者的個人簡介
 */
@Composable
fun BioEditorDialog(
    currentBio: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var bio by remember { mutableStateOf(currentBio) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("編輯關於我") },
        text = {
            OutlinedTextField(
                value = bio,
                onValueChange = { bio = it },
                label = { Text("關於我(可留空)") },
                maxLines = 5,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(onClick = { onConfirm(bio.trim()) }) {
                Text("確定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}