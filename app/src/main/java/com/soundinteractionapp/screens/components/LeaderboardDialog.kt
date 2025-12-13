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
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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
    val tabs = listOf("總排行榜", "關卡一", "關卡二", "關卡三")
    val pagerState = rememberPagerState(pageCount = { 4 })
    val scope = rememberCoroutineScope()

    // 收集資料流
    val totalList by viewModel.totalRank.collectAsState()
    val level1List by viewModel.level1Rank.collectAsState()
    val level2List by viewModel.level2Rank.collectAsState()
    val level3List by viewModel.level3Rank.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // 啟動時載入資料
    LaunchedEffect(Unit) {
        viewModel.loadAllLeaderboards()
    }

    // ✅ 設定 ScrollBehavior：enterAlways 代表往下滑列表時標題隱藏，往上滑時標題顯示
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false, // 全寬度
            decorFitsSystemWindows = false   // 延伸至狀態列
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFFF8F5FF) // 淡紫色背景
        ) {
            // ✅ 使用 Scaffold 來管理 TopBar 的滑動行為
            Scaffold(
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(scrollBehavior.nestedScrollConnection), // 綁定捲動事件
                topBar = {
                    // ✅ 將紫色區塊改為 TopAppBar
                    CenterAlignedTopAppBar(
                        title = {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(bottom = 8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.EmojiEvents,
                                    contentDescription = null,
                                    tint = Color(0xFFFFD700),
                                    modifier = Modifier.size(32.dp)
                                )
                                Text(
                                    text = "榮譽排行榜",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = onDismiss) {
                                Icon(
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = "返回",
                                    tint = Color.White
                                )
                            }
                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = Color.Transparent, // 設為透明以顯示漸層
                            scrolledContainerColor = Color.Transparent
                        ),
                        modifier = Modifier
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xFF673AB7), Color(0xFF512DA8))
                                )
                            )
                            .statusBarsPadding(), // 避開狀態列
                        scrollBehavior = scrollBehavior // 連接捲動行為
                    )
                }
            ) { innerPadding ->
                // === 內容區塊 ===
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding) // Scaffold 會自動計算需要的 padding
                ) {
                    // === Tabs (不會被隱藏，會停留在頂部) ===
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

                    // === 列表內容區 ===
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
    }
}

// 空狀態顯示
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
    // 前三名的特殊顏色
    val rankColor = when (item.rank) {
        1 -> Color(0xFFFFD700) // 金
        2 -> Color(0xFFC0C0C0) // 銀
        3 -> Color(0xFFCD7F32) // 銅
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
            // 名次圈圈
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

            // 頭像：使用資料中的 ID (預設為 user.png)
            Image(
                painter = painterResource(id = item.avatarResId),
                contentDescription = null,
                modifier = Modifier
                    .size(48.dp) // 設定大小
                    .clip(CircleShape) // 先裁切成圓形
                    // ✅ 新增這行：加入 2dp 寬的紫色圓形邊框
                    // 建議放在 clip 之後，background 之前
                    .border(width = 2.dp, color = Color(0xFF673AB7), shape = CircleShape)
                    .background(Color.White), // 背景色 (上次修改的)
                contentScale = ContentScale.Crop
            )

            Spacer(Modifier.width(16.dp))

            // 名字與分數
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