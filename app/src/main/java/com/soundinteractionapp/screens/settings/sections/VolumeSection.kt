package com.soundinteractionapp.screens.settings.sections

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.soundinteractionapp.SoundManager
import com.soundinteractionapp.screens.settings.components.HorizontalVolumeControl

@Composable
fun VolumeSection(
    soundManager: SoundManager,
    masterVolume: Float,
    musicVolume: Float,
    sfxVolume: Float,
    isMasterMuted: Boolean,
    isMusicMuted: Boolean,
    isSfxMuted: Boolean,
    onMasterVolumeChange: (Float) -> Unit,
    onMusicVolumeChange: (Float) -> Unit,
    onSfxVolumeChange: (Float) -> Unit,
    onMasterMuteToggle: () -> Unit,
    onMusicMuteToggle: () -> Unit,
    onSfxMuteToggle: () -> Unit,
    onResetVolumes: () -> Unit
) {
    Column {
        // 主音量設定
        HorizontalVolumeControl(
            title = "主音量",
            icon = if (isMasterMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
            iconColor = Color(0xFF673AB7),
            volume = masterVolume,
            isMuted = isMasterMuted,
            onVolumeChange = onMasterVolumeChange,
            onMuteToggle = {
                onMasterMuteToggle()
                soundManager.playSFX("settings")
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 音樂音量設定
        HorizontalVolumeControl(
            title = "音樂音量",
            icon = if (isMusicMuted) Icons.Default.MusicOff else Icons.Default.MusicNote,
            iconColor = Color(0xFF4FC3F7),
            volume = musicVolume,
            isMuted = isMusicMuted,
            onVolumeChange = onMusicVolumeChange,
            onMuteToggle = {
                onMusicMuteToggle()
                soundManager.playSFX("settings")
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 音效音量設定
        HorizontalVolumeControl(
            title = "音效音量",
            icon = if (isSfxMuted) Icons.Default.NotificationsOff else Icons.Default.Notifications,
            iconColor = Color(0xFFFF9800),
            volume = sfxVolume,
            isMuted = isSfxMuted,
            onVolumeChange = onSfxVolumeChange,
            onMuteToggle = {
                onSfxMuteToggle()
                soundManager.playSFX("settings")
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 重置按鈕
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        soundManager.playSFX("settings")
                        onResetVolumes()
                    }
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = "重置",
                    tint = Color(0xFF673AB7),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "重置為預設值",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF673AB7)
                )
            }
        }
    }
}