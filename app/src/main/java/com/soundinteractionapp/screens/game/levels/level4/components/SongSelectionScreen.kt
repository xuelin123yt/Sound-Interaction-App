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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.soundinteractionapp.R
import com.soundinteractionapp.screens.game.levels.level4.beatmaps.Beatmap
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * 歌曲選擇畫面 - 帶模糊背景和返回按鈕
 */
@Composable
fun SongSelectionScreen(
    beatmaps: List<Beatmap>,
    onSongSelected: (Int) -> Unit,
    onBack: () -> Unit  // 新增返回回調
) {
    val context = LocalContext.current
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

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

    // 監聽選中索引變化並播放音效
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

            previousSelectedIndex = selectedIndex
        }
    }

    // 清理音效播放器
    DisposableEffect(Unit) {
        onDispose {
            scrollSoundPlayer?.release()
        }
    }

    // 獲取當前選中的歌曲
    val selectedBeatmap = beatmaps.getOrNull(selectedIndex)

    Box(modifier = Modifier.fillMaxSize()) {
        // 背景圖片（模糊化）- 使用 backgroundImageResId
        if (selectedBeatmap != null) {
            Image(
                painter = painterResource(id = selectedBeatmap.backgroundImageResId),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .blur(20.dp),  // 模糊半徑 20dp
                contentScale = ContentScale.Crop,
                alpha = 0.3f  // 降低透明度避免干擾前景
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
                                onClick = {
                                    if (isSelected) {
                                        onSongSelected(beatmap.id)
                                    } else {
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
            onClick = onBack,
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
    }
}