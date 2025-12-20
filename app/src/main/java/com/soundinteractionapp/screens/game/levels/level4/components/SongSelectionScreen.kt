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
import com.soundinteractionapp.screens.game.levels.level4.beatmaps.Beatmap
import com.soundinteractionapp.screens.game.levels.level4.logic.PreviewAudioManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * 歌曲選擇畫面 - 帶模糊背景、返回按鈕和試聽音樂
 */
@Composable
fun SongSelectionScreen(
    beatmaps: List<Beatmap>,
    onSongSelected: (Int) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current

    // ✅ 試聽音樂管理器
    val previewAudioManager = remember { PreviewAudioManager(context) }

    // ✅ 監聽生命週期：暫停/恢復試聽音樂
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    previewAudioManager.pause()
                }
                Lifecycle.Event.ON_RESUME -> {
                    previewAudioManager.resume()
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // 滾動音效播放器
    var scrollSoundPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var previousSelectedIndex by remember { mutableIntStateOf(0) }
    var lastSoundPlayTime by remember { mutableLongStateOf(0L) }

    // 根據滾動位置計算選中的索引
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

    // ✅ 監聽選中索引變化：播放滾動音效 + 試聽音樂
    LaunchedEffect(selectedIndex) {
        if (selectedIndex != previousSelectedIndex && beatmaps.isNotEmpty()) {
            val currentTime = System.currentTimeMillis()
            val indexDiff = abs(selectedIndex - previousSelectedIndex)

            // 播放滾動音效
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

            // ✅ 播放新選中歌曲的試聽音樂
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

    // ✅ 初始化時播放第一首歌的試聽
    LaunchedEffect(Unit) {
        delay(300)  // 稍微延遲，等待畫面載入完成
        beatmaps.firstOrNull()?.let { beatmap ->
            previewAudioManager.playPreview(
                audioResId = beatmap.audioResId,
                startTime = beatmap.previewStartTime,
                beatmapId = beatmap.id
            )
        }
    }

    // ✅ 清理資源
    DisposableEffect(Unit) {
        onDispose {
            scrollSoundPlayer?.release()
            previewAudioManager.release()
        }
    }

    // 獲取當前選中的歌曲
    val selectedBeatmap = beatmaps.getOrNull(selectedIndex)

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

        // 前景內容
        Row(
            modifier = Modifier.fillMaxSize()
        ) {
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

                            SongCard(
                                beatmap = beatmap,
                                isSelected = isSelected,
                                onSelect = {
                                    // 只負責滾動到該項目
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
                    TrapezoidImageDisplay(beatmap = selectedBeatmap)
                }
            }
        }

        // 左上角返回按鈕
        IconButton(
            onClick = {
                // ✅ 返回時停止試聽音樂
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
                // ✅ 開始遊戲時停止試聽音樂
                previewAudioManager.stopImmediately()
                selectedBeatmap?.let { onSongSelected(it.id) }
            },
            onShowExample = {
                // TODO: 實現遊玩範例邏輯
            },
            isGameStartEnabled = selectedBeatmap != null,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .fillMaxWidth(0.45f)
        )
    }
}