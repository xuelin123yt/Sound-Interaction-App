package com.soundinteractionapp.screens.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
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

@OptIn(ExperimentalMaterial3Api::class) // 👈 需要加入這個註解來使用 TopAppBar 的 ScrollBehavior
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

    // ✅ 設定 ScrollBehavior：設定為 enterAlways，往下滑時隱藏，往上滑時顯示
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    LaunchedEffect(Unit) {
        viewModel.loadAllLeaderboards()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFFF8F5FF)
        ) {
            // ✅ 使用 Scaffold 來管理 TopBar 和內容的捲動連動
            Scaffold(
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(scrollBehavior.nestedScrollConnection), // 👈 綁定捲動行為
                topBar = {
                    // ✅ 將原本的紫色 Box 區塊改寫為 TopAppBar
                    // 這裡使用 CenterAlignedTopAppBar 讓標題置中
                    CenterAlignedTopAppBar(
                        title = {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(bottom = 8.dp) // 增加一點底部間距
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.EmojiEvents,
                                    contentDescription = null,
                                    tint = Color(0xFFFFD700),
                                    modifier = Modifier.size(32.dp) // 稍微縮小一點圖示以適應 TopBar
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
                        // 自定義背景顏色與漸層
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = Color.Transparent, // 設為透明以便顯示下方的漸層
                            scrolledContainerColor = Color.Transparent
                        ),
                        modifier = Modifier
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xFF673AB7), Color(0xFF512DA8))
                                )
                            )
                            .statusBarsPadding(), // 避開狀態列
                        scrollBehavior = scrollBehavior // 👈 連接行為
                    )
                }
            ) { innerPadding ->
                // === 內容區塊 ===
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding) // 👈 這是關鍵，Scaffold 會自動計算剩下的空間
                ) {
                    // === 2. 分頁標籤 (Tabs) ===
                    // Tabs 放在這裡，當紫色 TopBar 收起時，Tabs 會往上頂並停留在頂部
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

                    // === 3. 內容區 (HorizontalPager) ===
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

// ... EmptyStateDisplay, LeaderboardList, LeaderboardRowItem 維持不變 ...
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
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
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
                    .background(Color.Gray),
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