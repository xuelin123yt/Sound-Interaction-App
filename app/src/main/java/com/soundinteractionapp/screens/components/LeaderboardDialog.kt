package com.soundinteractionapp.screens.components

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
import com.soundinteractionapp.R
import com.soundinteractionapp.data.LeaderboardItem
import com.soundinteractionapp.data.LeaderboardViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardDialog(
    viewModel: LeaderboardViewModel,
    onDismiss: () -> Unit
) {
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

    // ✅ 不使用 Dialog，直接用 Box 覆蓋整個畫面
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F5FF))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 頂部紫色區域
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
                // 返回按鈕
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "返回",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // 中央內容
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

            // Tabs
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
                        onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
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

            // 列表區域
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(Color(0xFFF4F4F4))
            ) {
                if (isLoading) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF673AB7))
                    }
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

@Composable
fun EmptyStateDisplay() {
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
            "目前還沒有人上榜\n趕快去挑戰成為第一名吧！",
            color = Color.Gray,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
    }
}

@Composable
fun LeaderboardList(list: List<LeaderboardItem>) {
    LazyColumn(
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp, start = 16.dp, end = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(list) { item ->
            LeaderboardRowItem(item)
        }
    }
}

@Composable
fun LeaderboardRowItem(item: LeaderboardItem) {
    val rankColor = when (item.rank) {
        1 -> Color(0xFFFFD700)
        2 -> Color(0xFFC0C0C0)
        3 -> Color(0xFFCD7F32)
        else -> Color.Transparent
    }

    val rankTextColor = if (item.rank <= 3) Color.White else Color(0xFF666666)
    val rankTextWeight = if (item.rank <= 3) FontWeight.Bold else FontWeight.Normal

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (item.rank <= 3) rankColor else Color(0xFFEEEEEE)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${item.rank}",
                    color = rankTextColor,
                    fontWeight = rankTextWeight,
                    fontSize = 16.sp
                )
            }

            Spacer(Modifier.width(16.dp))

            Image(
                painter = painterResource(id = item.avatarResId),
                contentDescription = null,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .border(width = 2.dp, color = Color(0xFF673AB7), shape = CircleShape)
                    .background(Color.White),
                contentScale = ContentScale.Crop
            )

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF333333),
                    maxLines = 1
                )
            }

            Text(
                text = "${item.score}",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF673AB7)
            )
        }
    }
}