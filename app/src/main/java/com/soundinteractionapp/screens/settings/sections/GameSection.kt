package com.soundinteractionapp.screens.settings.sections

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.soundinteractionapp.SoundManager
import com.soundinteractionapp.data.SoundSettingsViewModel
import com.soundinteractionapp.screens.settings.components.GameModeCategory
import com.soundinteractionapp.screens.settings.components.LevelCategory
import com.soundinteractionapp.screens.settings.components.HorizontalVolumeControl
import com.soundinteractionapp.utils.VolumeKeys
import kotlin.math.roundToInt

@Composable
fun GameSection(
    soundSettingsViewModel: SoundSettingsViewModel,
    soundManager: SoundManager
) {
    val soundSettings by soundSettingsViewModel.soundSettings.collectAsState()

    var expandedMode by remember { mutableStateOf<Int?>(null) }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // 模式一：自由探索
        GameModeCategory(
            title = "🎵 模式一：自由探索",
            expanded = expandedMode == 1,
            onToggle = {
                soundManager.playSFX("options4")  // ✅ 播放音效
                expandedMode = if (expandedMode == 1) null else 1
            }
        ) {
            FreePlaySoundControls(
                settings = soundSettings.freePlay,
                viewModel = soundSettingsViewModel
            )
        }

        // 模式二：放鬆時光
        GameModeCategory(
            title = "🌊 模式二：放鬆時光",
            expanded = expandedMode == 2,
            onToggle = {
                soundManager.playSFX("options4")  // ✅ 播放音效
                expandedMode = if (expandedMode == 2) null else 2
            }
        ) {
            RelaxSoundControls(
                settings = soundSettings.relax,
                viewModel = soundSettingsViewModel
            )
        }

        // 模式三：音樂遊戲
        GameModeCategory(
            title = "🎮 模式三：音樂遊戲",
            expanded = expandedMode == 3,
            onToggle = {
                soundManager.playSFX("options4")  // ✅ 播放音效
                expandedMode = if (expandedMode == 3) null else 3
            }
        ) {
            GameModeSoundControls(
                settings = soundSettings.game,
                viewModel = soundSettingsViewModel,
                soundManager = soundManager
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

// ========== 模式一音效控制 ==========
@Composable
fun FreePlaySoundControls(
    settings: com.soundinteractionapp.data.FreePlaySounds,
    viewModel: SoundSettingsViewModel
) {
    Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)) {
        Text(
            "貓咪音效",  // ✅ 移除 emoji
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF555555),
            modifier = Modifier.padding(bottom = 12.dp, start = 16.dp)
        )

        HorizontalVolumeControl(
            title = "貓咪 1",
            icon = Icons.Default.Pets,
            iconColor = Color(0xFFFF9800),
            volume = settings.cat1Volume,
            isMuted = false,
            onVolumeChange = { viewModel.updateCat1Volume(it) },
            onMuteToggle = { }
        )

        Spacer(modifier = Modifier.height(12.dp))

        HorizontalVolumeControl(
            title = "貓咪 2",
            icon = Icons.Default.Pets,
            iconColor = Color(0xFFFF9800),
            volume = settings.cat2Volume,
            isMuted = false,
            onVolumeChange = { viewModel.updateCat2Volume(it) },
            onMuteToggle = { }
        )

        Spacer(modifier = Modifier.height(12.dp))

        HorizontalVolumeControl(
            title = "貓咪 3",
            icon = Icons.Default.Pets,
            iconColor = Color(0xFFFF9800),
            volume = settings.cat3Volume,
            isMuted = false,
            onVolumeChange = { viewModel.updateCat3Volume(it) },
            onMuteToggle = { }
        )

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "狗狗音效",  // ✅ 移除 emoji
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF555555),
            modifier = Modifier.padding(bottom = 12.dp, start = 16.dp)
        )

        HorizontalVolumeControl(
            title = "狗狗 1",
            icon = Icons.Default.Favorite,
            iconColor = Color(0xFFFFA726),
            volume = settings.dog1Volume,
            isMuted = false,
            onVolumeChange = { viewModel.updateDog1Volume(it) },
            onMuteToggle = { }
        )

        Spacer(modifier = Modifier.height(12.dp))

        HorizontalVolumeControl(
            title = "狗狗 2",
            icon = Icons.Default.Favorite,
            iconColor = Color(0xFFFF7043),
            volume = settings.dog2Volume,
            isMuted = false,
            onVolumeChange = { viewModel.updateDog2Volume(it) },
            onMuteToggle = { }
        )

        Spacer(modifier = Modifier.height(12.dp))

        HorizontalVolumeControl(
            title = "狗狗 3",
            icon = Icons.Default.Favorite,
            iconColor = Color(0xFFBF360C),
            volume = settings.dog3Volume,
            isMuted = false,
            onVolumeChange = { viewModel.updateDog3Volume(it) },
            onMuteToggle = { }
        )

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "其他音效",  // ✅ 移除 emoji
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF555555),
            modifier = Modifier.padding(bottom = 12.dp, start = 16.dp)
        )

        HorizontalVolumeControl(
            title = "鳥兒",
            icon = Icons.Default.FlightTakeoff,
            iconColor = Color(0xFF4FC3F7),
            volume = settings.birdVolume,
            isMuted = false,
            onVolumeChange = { viewModel.updateBirdVolume(it) },
            onMuteToggle = { }
        )

        Spacer(modifier = Modifier.height(12.dp))

        HorizontalVolumeControl(
            title = "鋼琴",
            icon = Icons.Default.Piano,
            iconColor = Color(0xFF9C27B0),
            volume = settings.pianoVolume,
            isMuted = false,
            onVolumeChange = { viewModel.updatePianoVolume(it) },
            onMuteToggle = { }
        )

        Spacer(modifier = Modifier.height(12.dp))

        HorizontalVolumeControl(
            title = "爵士鼓",
            icon = Icons.Default.Album,
            iconColor = Color(0xFFF44336),
            volume = settings.drumVolume,
            isMuted = false,
            onVolumeChange = { viewModel.updateDrumVolume(it) },
            onMuteToggle = { }
        )

        Spacer(modifier = Modifier.height(12.dp))

        HorizontalVolumeControl(
            title = "鈴鐺",
            icon = Icons.Default.Notifications,
            iconColor = Color(0xFFFFD700),
            volume = settings.bellVolume,
            isMuted = false,
            onVolumeChange = { viewModel.updateBellVolume(it) },
            onMuteToggle = { }
        )
    }
}

// ========== 模式二音效控制 ==========
@Composable
fun RelaxSoundControls(
    settings: com.soundinteractionapp.data.RelaxSounds,
    viewModel: SoundSettingsViewModel
) {
    Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)) {
        HorizontalVolumeControl(
            title = "雨聲",  // ✅ 移除 emoji
            icon = Icons.Default.Cloud,
            iconColor = Color(0xFF2196F3),
            volume = settings.rainVolume,
            isMuted = false,
            onVolumeChange = { viewModel.updateRainVolume(it) },
            onMuteToggle = { }
        )

        Spacer(modifier = Modifier.height(12.dp))

        HorizontalVolumeControl(
            title = "海浪",  // ✅ 移除 emoji
            icon = Icons.Default.Waves,
            iconColor = Color(0xFF00BCD4),
            volume = settings.oceanVolume,
            isMuted = false,
            onVolumeChange = { viewModel.updateOceanVolume(it) },
            onMuteToggle = { }
        )

        Spacer(modifier = Modifier.height(12.dp))

        HorizontalVolumeControl(
            title = "微風",  // ✅ 移除 emoji
            icon = Icons.Default.Air,
            iconColor = Color(0xFF4CAF50),
            volume = settings.windVolume,
            isMuted = false,
            onVolumeChange = { viewModel.updateWindVolume(it) },
            onMuteToggle = { }
        )
    }
}

// ========== 模式三音效控制 ==========
@Composable
fun GameModeSoundControls(
    settings: com.soundinteractionapp.data.GameSounds,
    viewModel: SoundSettingsViewModel,
    soundManager: SoundManager
) {
    var expandedLevel by remember { mutableStateOf<Int?>(null) }

    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        // 關卡一
        LevelCategory(
            title = "關卡一：料理鼠王",
            expanded = expandedLevel == 1,
            onToggle = {
                soundManager.playSFX("options4")  // ✅ 播放音效
                expandedLevel = if (expandedLevel == 1) null else 1
            }
        ) {
            Level1SoundControls(settings.level1, viewModel)
        }

        // 關卡二
        LevelCategory(
            title = "關卡二：鋼琴演奏",
            expanded = expandedLevel == 2,
            onToggle = {
                soundManager.playSFX("options4")  // ✅ 播放音效
                expandedLevel = if (expandedLevel == 2) null else 2
            }
        ) {
            Level2SoundControls(settings.level2, viewModel)
        }

        // 關卡三
        LevelCategory(
            title = "關卡三：聲控飛行",
            expanded = expandedLevel == 3,
            onToggle = {
                soundManager.playSFX("options4")  // ✅ 播放音效
                expandedLevel = if (expandedLevel == 3) null else 3
            }
        ) {
            Level3SoundControls(settings.level3, viewModel)
        }

        // 關卡四
        LevelCategory(
            title = "關卡四：音樂節奏",
            expanded = expandedLevel == 4,
            onToggle = {
                soundManager.playSFX("options4")  // ✅ 播放音效
                expandedLevel = if (expandedLevel == 4) null else 4
            }
        ) {
            Level4SoundControls(settings.level4, viewModel)
        }
    }
}

// ========== 關卡一音效控制 ==========
@Composable
fun Level1SoundControls(
    settings: com.soundinteractionapp.data.Level1Sounds,
    viewModel: SoundSettingsViewModel
) {
    Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp)) {
        Text(
            "背景音樂",  // ✅ 移除 emoji
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF666666),
            modifier = Modifier.padding(bottom = 12.dp, start = 16.dp)
        )

        HorizontalVolumeControl(
            title = "簡單難度",
            icon = Icons.Default.MusicNote,
            iconColor = Color(0xFF4CAF50),
            volume = settings.easyMusicVolume,
            isMuted = false,
            onVolumeChange = { viewModel.updateLevel1EasyMusic(it) },
            onMuteToggle = { }
        )

        Spacer(modifier = Modifier.height(12.dp))

        HorizontalVolumeControl(
            title = "普通難度",
            icon = Icons.Default.MusicNote,
            iconColor = Color(0xFFFF9800),
            volume = settings.mediumMusicVolume,
            isMuted = false,
            onVolumeChange = { viewModel.updateLevel1MediumMusic(it) },
            onMuteToggle = { }
        )

        Spacer(modifier = Modifier.height(12.dp))

        HorizontalVolumeControl(
            title = "困難難度",
            icon = Icons.Default.MusicNote,
            iconColor = Color(0xFFF44336),
            volume = settings.hardMusicVolume,
            isMuted = false,
            onVolumeChange = { viewModel.updateLevel1HardMusic(it) },
            onMuteToggle = { }
        )

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = Color(0xFFDDDDDD), thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "音效",  // ✅ 移除 emoji
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF666666),
            modifier = Modifier.padding(bottom = 12.dp, start = 16.dp)
        )

        HorizontalVolumeControl(
            title = "打擊音效",
            icon = Icons.Default.Notifications,
            iconColor = Color(0xFF673AB7),
            volume = settings.hitSoundVolume,
            isMuted = false,
            onVolumeChange = { viewModel.updateLevel1HitSound(it) },
            onMuteToggle = { }
        )
    }
}

// ========== 關卡二音效控制 ==========
@Composable
fun Level2SoundControls(
    settings: com.soundinteractionapp.data.Level2Sounds,
    viewModel: SoundSettingsViewModel
) {
    Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp)) {
        Text(
            "背景音樂",  // ✅ 移除 emoji
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF666666),
            modifier = Modifier.padding(bottom = 12.dp, start = 16.dp)
        )

        HorizontalVolumeControl(
            title = "簡單難度",
            icon = Icons.Default.MusicNote,
            iconColor = Color(0xFF4CAF50),
            volume = settings.easyMusicVolume,
            isMuted = false,
            onVolumeChange = { viewModel.updateLevel2EasyMusic(it) },
            onMuteToggle = { }
        )

        Spacer(modifier = Modifier.height(12.dp))

        HorizontalVolumeControl(
            title = "普通難度",
            icon = Icons.Default.MusicNote,
            iconColor = Color(0xFFFF9800),
            volume = settings.mediumMusicVolume,
            isMuted = false,
            onVolumeChange = { viewModel.updateLevel2MediumMusic(it) },
            onMuteToggle = { }
        )

        Spacer(modifier = Modifier.height(12.dp))

        HorizontalVolumeControl(
            title = "困難難度",
            icon = Icons.Default.MusicNote,
            iconColor = Color(0xFFF44336),
            volume = settings.hardMusicVolume,
            isMuted = false,
            onVolumeChange = { viewModel.updateLevel2HardMusic(it) },
            onMuteToggle = { }
        )

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = Color(0xFFDDDDDD), thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "音效",  // ✅ 移除 emoji
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF666666),
            modifier = Modifier.padding(bottom = 12.dp, start = 16.dp)
        )

        HorizontalVolumeControl(
            title = "打擊音效",
            icon = Icons.Default.Notifications,
            iconColor = Color(0xFF673AB7),
            volume = settings.hitSoundVolume,
            isMuted = false,
            onVolumeChange = { viewModel.updateLevel2HitSound(it) },
            onMuteToggle = { }
        )
    }
}

// ========== 關卡三音效控制 ==========
@Composable
fun Level3SoundControls(
    settings: com.soundinteractionapp.data.Level3Sounds,
    viewModel: SoundSettingsViewModel
) {
    Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp)) {
        HorizontalVolumeControl(
            title = "背景音樂",  // ✅ 移除 emoji
            icon = Icons.Default.MusicNote,
            iconColor = Color(0xFF4FC3F7),
            volume = settings.musicVolume,
            isMuted = false,
            onVolumeChange = { viewModel.updateLevel3Music(it) },
            onMuteToggle = { }
        )

        Spacer(modifier = Modifier.height(12.dp))

        HorizontalVolumeControl(
            title = "音效",  // ✅ 移除 emoji
            icon = Icons.Default.Notifications,
            iconColor = Color(0xFFFF9800),
            volume = settings.effectVolume,
            isMuted = false,
            onVolumeChange = { viewModel.updateLevel3Effect(it) },
            onMuteToggle = { }
        )
    }
}

// ========== 關卡四音效控制 ==========
@Composable
fun Level4SoundControls(
    settings: com.soundinteractionapp.data.Level4Sounds,
    viewModel: SoundSettingsViewModel
) {
    Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp)) {
        Text(
            "音樂音量（試聽 & 遊戲）",  // ✅ 移除 emoji
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF666666),
            modifier = Modifier.padding(bottom = 8.dp, start = 16.dp)
        )

        Text(
            "※ 以下音量同時影響選歌試聽和遊戲內播放",
            fontSize = 11.sp,
            color = Color(0xFF888888),
            modifier = Modifier.padding(bottom = 12.dp, start = 16.dp)
        )

        HorizontalVolumeControl(
            title = "哆啦A夢",
            icon = Icons.Default.MusicNote,
            iconColor = Color(0xFF4CAF50),
            volume = settings.song1Volume,
            isMuted = false,
            onVolumeChange = { viewModel.updateLevel4Song1(it) },
            onMuteToggle = { }
        )

        Spacer(modifier = Modifier.height(12.dp))

        HorizontalVolumeControl(
            title = "神魔之塔",
            icon = Icons.Default.MusicNote,
            iconColor = Color(0xFF2196F3),
            volume = settings.song2Volume,
            isMuted = false,
            onVolumeChange = { viewModel.updateLevel4Song2(it) },
            onMuteToggle = { }
        )

        Spacer(modifier = Modifier.height(12.dp))

        HorizontalVolumeControl(
            title = "Ib 記憶",
            icon = Icons.Default.MusicNote,
            iconColor = Color(0xFFFF9800),
            volume = settings.song3Volume,
            isMuted = false,
            onVolumeChange = { viewModel.updateLevel4Song3(it) },
            onMuteToggle = { }
        )

        Spacer(modifier = Modifier.height(12.dp))

        HorizontalVolumeControl(
            title = "打上花火",
            icon = Icons.Default.MusicNote,
            iconColor = Color(0xFFFF5722),
            volume = settings.song4Volume,
            isMuted = false,
            onVolumeChange = { viewModel.updateLevel4Song4(it) },
            onMuteToggle = { }
        )

        Spacer(modifier = Modifier.height(12.dp))

        HorizontalVolumeControl(
            title = "海的街道",
            icon = Icons.Default.MusicNote,
            iconColor = Color(0xFFF44336),
            volume = settings.song5Volume,
            isMuted = false,
            onVolumeChange = { viewModel.updateLevel4Song5(it) },
            onMuteToggle = { }
        )

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = Color(0xFFDDDDDD), thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "音效",  // ✅ 移除 emoji
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF666666),
            modifier = Modifier.padding(bottom = 12.dp, start = 16.dp)
        )

        HorizontalVolumeControl(
            title = "打擊音效",
            icon = Icons.Default.Check,
            iconColor = Color(0xFF4CAF50),
            volume = settings.hitSoundVolume,
            isMuted = false,
            onVolumeChange = { viewModel.updateLevel4HitSound(it) },
            onMuteToggle = { }
        )

        Spacer(modifier = Modifier.height(12.dp))

        HorizontalVolumeControl(
            title = "MISS 音效",
            icon = Icons.Default.Close,
            iconColor = Color(0xFFF44336),
            volume = settings.missSoundVolume,
            isMuted = false,
            onVolumeChange = { viewModel.updateLevel4MissSound(it) },
            onMuteToggle = { }
        )
    }
}