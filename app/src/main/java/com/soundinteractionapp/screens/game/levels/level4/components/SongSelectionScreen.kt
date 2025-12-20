package com.soundinteractionapp.screens.game.levels.level4.components

import android.media.MediaPlayer
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.soundinteractionapp.R
import com.soundinteractionapp.data.ScoreEntry
import com.soundinteractionapp.screens.game.levels.level4.Level4UnlockSystem
import com.soundinteractionapp.screens.game.levels.level4.beatmaps.Beatmap
import com.soundinteractionapp.screens.game.levels.level4.logic.PreviewAudioManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * 歌曲選擇畫面 - 整合解鎖系統
 */
@Composable
fun SongSelectionScreen(
    beatmaps: List<Beatmap>,
    isGuest: Boolean,
    scoreEntry: ScoreEntry,
    onSongSelected: (Int) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current

    val previewAudioManager = remember { PreviewAudioManager(context) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> previewAudioManager.pause()
                Lifecycle.Event.ON_RESUME -> previewAudioManager.resume()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    var scrollSoundPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var previousSelectedIndex by remember { mutableIntStateOf(0) }
    var lastSoundPlayTime by remember { mutableLongStateOf(0L) }

    val selectedIndex by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val viewportCenter = layoutInfo.viewportStartOffset + layoutInfo.viewportSize.height / 2

            layoutInfo.visibleItemsInfo
                .minByOrNull { item ->
                    abs((item.offset + item.size / 2) - viewportCenter)
                }?.index ?: 0
        }
    }

    LaunchedEffect(selectedIndex) {
        if (selectedIndex != previousSelectedIndex && beatmaps.isNotEmpty()) {
            val currentTime = System.currentTimeMillis()
            val indexDiff = abs(selectedIndex - previousSelectedIndex)

            if (indexDiff > 1) {
                val start = minOf(previousSelectedIndex, selectedIndex)
                val end = maxOf(previousSelectedIndex, selectedIndex)

                for (i in start + 1..end) {
                    if (i < beatmaps.size) {
                        val delayTime = (i - start - 1) * 30L
                        delay(delayTime)

                        try {
                            val player = MediaPlayer.create(context, R.raw.options4)
                            player?.start()
                            player?.setOnCompletionListener { it.release() }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            } else {
                if (currentTime - lastSoundPlayTime > 50) {
                    try {
                        scrollSoundPlayer?.release()
                        scrollSoundPlayer = MediaPlayer.create(context, R.raw.options4)
                        scrollSoundPlayer?.start()
                        lastSoundPlayTime = currentTime
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            val selectedBeatmap = beatmaps.getOrNull(selectedIndex)
            selectedBeatmap?.let { beatmap ->
                previewAudioManager.playPreview(
                    audioResId = beatmap.audioResId,
                    startTime = beatmap.previewStartTime,
                    beatmapId = beatmap.id
                )
            }

            previousSelectedIndex = selectedIndex
        }
    }

    LaunchedEffect(Unit) {
        delay(300)
        beatmaps.firstOrNull()?.let { beatmap ->
            previewAudioManager.playPreview(
                audioResId = beatmap.audioResId,
                startTime = beatmap.previewStartTime,
                beatmapId = beatmap.id
            )
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            scrollSoundPlayer?.release()
            previewAudioManager.release()
        }
    }

    val selectedBeatmap = beatmaps.getOrNull(selectedIndex)

    // ✅ 檢查當前選中歌曲是否解鎖
    val isUnlocked = selectedBeatmap?.let { beatmap ->
        Level4UnlockSystem.isSongUnlocked(beatmap.id, scoreEntry, isGuest)
    } ?: false

    Box(modifier = Modifier.fillMaxSize()) {
        // 背景圖片（模糊化）
        if (selectedBeatmap != null) {
            Image(
                painter = painterResource(id = selectedBeatmap.backgroundImageResId),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .blur(20.dp),
                contentScale = ContentScale.Crop,
                alpha = 0.3f
            )
        }

        Row(modifier = Modifier.fillMaxSize()) {
            // 左側 - 歌曲列表
            Box(
                modifier = Modifier
                    .weight(0.45f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                BoxWithConstraints {
                    val screenHeight = constraints.maxHeight
                    val itemHeight = 82
                    val padding = (screenHeight - itemHeight) / 2

                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            vertical = with(LocalContext.current.resources.displayMetrics) {
                                padding / density
                            }.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        itemsIndexed(beatmaps) { index, beatmap ->
                            val isSelected = index == selectedIndex
                            val isSongUnlocked = Level4UnlockSystem.isSongUnlocked(
                                beatmap.id, scoreEntry, isGuest
                            )

                            SongCard(
                                beatmap = beatmap,
                                isSelected = isSelected,
                                isLocked = !isSongUnlocked,
                                onSelect = {
                                    if (!isSelected) {
                                        coroutineScope.launch {
                                            listState.animateScrollToItem(index)
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }

            // 右側 - 斜切梯形圖片顯示區
            Box(
                modifier = Modifier
                    .weight(0.55f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                if (selectedBeatmap != null) {
                    TrapezoidImageDisplay(
                        beatmap = selectedBeatmap,
                        isLocked = !isUnlocked,
                        unlockHintText = Level4UnlockSystem.getUnlockHintText(
                            selectedBeatmap.id,
                            scoreEntry
                        )
                    )
                }
            }
        }

        // 左上角返回按鈕
        IconButton(
            onClick = {
                previewAudioManager.stopImmediately()
                onBack()
            },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .size(48.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "返回",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }

        // 右上角玩法說明按鈕
        GameInstructionsButton(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        )

        // 右下角操作按鈕
        GameActionButtons(
            onStartGame = {
                previewAudioManager.stopImmediately()
                selectedBeatmap?.let { onSongSelected(it.id) }
            },
            onShowExample = {
                // TODO: 實現遊玩範例邏輯
            },
            isGameStartEnabled = selectedBeatmap != null && isUnlocked,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .fillMaxWidth(0.45f)
        )
    }
}