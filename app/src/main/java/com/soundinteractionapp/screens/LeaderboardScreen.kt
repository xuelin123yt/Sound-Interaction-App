package com.soundinteractionapp.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.soundinteractionapp.R
import com.soundinteractionapp.SoundManager
import com.soundinteractionapp.data.LeaderboardViewModel
import com.soundinteractionapp.data.LeaderboardItem
import kotlinx.coroutines.launch

// =====================================================
// 🏆 排行榜畫面 (完整版)
// =====================================================
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LeaderboardScreen(
    navController: NavController,
    soundManager: SoundManager,
    viewModel: LeaderboardViewModel = viewModel()
) {
    var isNavigating by remember { mutableStateOf(false) }

    val tabs = listOf("總排行榜", "關卡一", "關卡二", "關卡三", "關卡四")
    val pagerState = rememberPagerState(pageCount = { 5 })
    val scope = rememberCoroutineScope()

    val totalList by viewModel.totalRank.collectAsState()
    val level1List by viewModel.level1Rank.collectAsState()
    val level2List by viewModel.level2Rank.collectAsState()
    val level3List by viewModel.level3Rank.collectAsState()
    val level4List by viewModel.level4Rank.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadAllLeaderboards()
    }

    Scaffold(
        containerColor = Color(0xFFF8F5FF)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF673AB7), Color(0xFF512DA8))
                        )
                    )
                    .padding(vertical = 12.dp)
            ) {
                IconButton(
                    onClick = {
                        if (isNavigating) return@IconButton
                        isNavigating = true
                        soundManager.playSFX("cancel")
                        navController.navigateUp()
                    },
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 4.dp),
                    enabled = !isNavigating
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "返回",
                        tint = if (!isNavigating) Color.White else Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.size(24.dp)
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = 56.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.EmojiEvents,
                        contentDescription = null,
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "榮譽排行榜",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            TabRow(
                selectedTabIndex = pagerState.currentPage,
                containerColor = Color.White,
                contentColor = Color(0xFF673AB7),
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                        color = Color(0xFF673AB7),
                        height = 3.dp
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = {
                            soundManager.playSFX("options")
                            scope.launch { pagerState.animateScrollToPage(index) }
                        },
                        text = {
                            Text(
                                title,
                                fontWeight = if (pagerState.currentPage == index) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 15.sp
                            )
                        },
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(Color(0xFFF4F4F4))
            ) {
                if (isLoading) {
                    LoadingState()
                } else {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize()
                    ) { page ->
                        val currentList = when (page) {
                            0 -> totalList
                            1 -> level1List
                            2 -> level2List
                            3 -> level3List
                            4 -> level4List
                            else -> emptyList()
                        }

                        if (currentList.isEmpty()) {
                            EmptyStateDisplay()
                        } else {
                            LeaderboardList(currentList)
                        }
                    }
                }
            }
        }
    }
}

// =====================================================
// ⏳ 載入中狀態
// =====================================================
@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                color = Color(0xFF673AB7),
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "載入排行榜中...",
                color = Color.Gray,
                fontSize = 16.sp
            )
        }
    }
}

// =====================================================
// 📭 空白狀態
// =====================================================
@Composable
private fun EmptyStateDisplay() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(id = R.drawable.music_01),
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = Color.LightGray
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "目前還沒有人上榜\n趕快去挑戰成為第一名吧!",
            color = Color.Gray,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp,
            fontSize = 16.sp
        )
    }
}

// =====================================================
// 📋 排行榜列表
// =====================================================
@Composable
private fun LeaderboardList(list: List<LeaderboardItem>) {
    LazyColumn(
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp, start = 16.dp, end = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(list) { item ->
            LeaderboardRowItem(item)
        }
    }
}

// =====================================================
// 🎖️ 排行榜項目卡片
// =====================================================
@Composable
private fun LeaderboardRowItem(item: LeaderboardItem) {
    val rankColor = when (item.rank) {
        1 -> Color(0xFFFFD700) // 金色
        2 -> Color(0xFFC0C0C0) // 銀色
        3 -> Color(0xFFCD7F32) // 銅色
        else -> Color.Transparent
    }

    val rankTextColor = if (item.rank <= 3) Color.White else Color(0xFF666666)
    val rankTextWeight = if (item.rank <= 3) FontWeight.Bold else FontWeight.Normal

    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color.White // ✅ 所有卡片都是白色背景
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp // ✅ 統一陰影高度
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 排名圓圈
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (item.rank <= 3) rankColor else Color(0xFFEEEEEE)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${item.rank}",
                    color = rankTextColor,
                    fontWeight = rankTextWeight,
                    fontSize = 18.sp
                )
            }

            Spacer(Modifier.width(16.dp))

            // 頭像
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(
                        width = 2.dp,
                        color = if (item.rank <= 3) rankColor else Color(0xFF673AB7),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = item.avatarResId),
                    contentDescription = "頭像",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(Modifier.width(16.dp))

            // 使用者名稱
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF333333),
                    maxLines = 1
                )

                // 前三名顯示獎牌
                if (item.rank <= 3) {
                    Text(
                        text = when (item.rank) {
                            1 -> "🏆 冠軍"
                            2 -> "🥈 亞軍"
                            3 -> "🥉 季軍"
                            else -> ""
                        },
                        fontSize = 12.sp,
                        color = rankColor
                    )
                }
            }

            // 分數
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "${item.score}",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (item.rank <= 3) rankColor else Color(0xFF673AB7)
                )
                Text(
                    text = "分",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
}