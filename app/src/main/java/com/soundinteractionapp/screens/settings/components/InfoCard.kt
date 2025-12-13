package com.soundinteractionapp.screens.settings.components

import com.soundinteractionapp.BuildConfig
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.ceil

data class CommitInfo(
    val message: String,
    val shortHash: String,
    val date: String,
    val authorName: String,
    val authorAvatarUrl: String
)

// 全局快取，避免重複請求
private object CommitCache {
    var commits: List<CommitInfo>? = null
    var cacheTime: Long = 0
    private const val CACHE_DURATION = 5 * 60 * 1000L // 5分鐘快取

    fun isValid(): Boolean {
        return commits != null && (System.currentTimeMillis() - cacheTime) < CACHE_DURATION
    }

    fun set(newCommits: List<CommitInfo>) {
        commits = newCommits
        cacheTime = System.currentTimeMillis()
    }

    fun clear() {
        commits = null
        cacheTime = 0
    }

    fun getRemainingCacheTimeMillis(): Long {
        if (!isValid()) return 0
        return CACHE_DURATION - (System.currentTimeMillis() - cacheTime)
    }

    fun getRemainingCacheTime(): String {
        val remaining = getRemainingCacheTimeMillis()
        if (remaining <= 0) return "快取已過期"
        val minutes = remaining / 60000
        val seconds = (remaining % 60000) / 1000
        return "快取剩餘: ${minutes}分${seconds}秒"
    }
}

@Composable
fun InfoCard(
    title: String,
    icon: ImageVector,
    iconColor: Color,
    showHistoryButton: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    var showCommitDialog by remember { mutableStateOf(false) }
    var allCommits by remember { mutableStateOf<List<CommitInfo>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var currentPage by remember { mutableIntStateOf(1) }

    val scope = rememberCoroutineScope()

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
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(iconColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333),
                    modifier = Modifier.weight(1f)
                )

                if (showHistoryButton) {
                    IconButton(
                        onClick = {
                            showCommitDialog = true
                            // 檢查快取是否有效
                            if (CommitCache.isValid()) {
                                allCommits = CommitCache.commits!!
                                errorMessage = null
                                currentPage = 1
                            } else if (!isLoading) {
                                // 快取無效，重新載入
                                isLoading = true
                                errorMessage = null
                                scope.launch {
                                    try {
                                        allCommits = fetchAllCommits()
                                        currentPage = 1
                                    } catch (e: Exception) {
                                        errorMessage = e.message ?: "未知錯誤"
                                    } finally {
                                        isLoading = false
                                    }
                                }
                            }
                        }
                    ) {
                        Icon(
                            Icons.Default.History,
                            contentDescription = "查看提交記錄",
                            tint = iconColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            content()
        }
    }

    if (showCommitDialog) {
        CommitHistoryDialog(
            allCommits = allCommits,
            isLoading = isLoading,
            errorMessage = errorMessage,
            currentPage = currentPage,
            onDismiss = {
                showCommitDialog = false
            },
            onPageChange = { page ->
                currentPage = page
            },
            onRetry = {
                isLoading = true
                errorMessage = null
                CommitCache.clear() // 清除快取
                scope.launch {
                    try {
                        allCommits = fetchAllCommits()
                        currentPage = 1
                    } catch (e: Exception) {
                        errorMessage = e.message ?: "未知錯誤"
                    } finally {
                        isLoading = false
                    }
                }
            }
        )
    }
}

@Composable
private fun CommitHistoryDialog(
    allCommits: List<CommitInfo>,
    isLoading: Boolean,
    errorMessage: String?,
    currentPage: Int,
    onDismiss: () -> Unit,
    onPageChange: (Int) -> Unit,
    onRetry: () -> Unit
) {
    val perPage = 10
    val totalPages = if (allCommits.isEmpty()) 0 else ceil(allCommits.size.toFloat() / perPage).toInt()
    val displayCommits = allCommits.chunked(perPage).getOrNull(currentPage - 1) ?: emptyList()

    // ✅ 即時更新快取剩餘時間
    var cacheTimeText by remember { mutableStateOf(CommitCache.getRemainingCacheTime()) }

    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(1000) // 每秒更新
            cacheTimeText = CommitCache.getRemainingCacheTime()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.88f),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // 標題欄
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "提交記錄",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF333333)
                        )
                        if (totalPages > 0) {
                            Text(
                                text = "第 $currentPage 頁，共 $totalPages 頁 (${allCommits.size} 筆)",
                                fontSize = 12.sp,
                                color = Color(0xFF666666)
                            )
                        }
                        // 顯示快取狀態
                        if (CommitCache.isValid() && !isLoading) {
                            Text(
                                text = cacheTimeText,
                                fontSize = 10.sp,
                                color = Color(0xFF4CAF50)
                            )
                        }
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "關閉",
                            tint = Color(0xFF666666),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Divider(color = Color(0xFFEEEEEE), thickness = 1.dp)

                // 內容區域
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    when {
                        isLoading -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    CircularProgressIndicator(
                                        color = Color(0xFF4CAF50),
                                        modifier = Modifier.size(40.dp),
                                        strokeWidth = 4.dp
                                    )
                                    Text(
                                        text = "正在載入提交記錄...",
                                        color = Color(0xFF666666),
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                        errorMessage != null -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = Color(0xFFE53935),
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Text(
                                        text = errorMessage,
                                        color = Color(0xFFE53935),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Button(
                                        onClick = onRetry,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF4CAF50)
                                        )
                                    ) {
                                        Icon(
                                            Icons.Default.Refresh,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("重試")
                                    }
                                }
                            }
                        }
                        allCommits.isEmpty() -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "沒有提交記錄",
                                    color = Color(0xFF666666),
                                    fontSize = 14.sp
                                )
                            }
                        }
                        else -> {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(displayCommits) { commit ->
                                    CommitItem(commit)
                                }
                            }
                        }
                    }
                }

                // 分頁控制
                if (totalPages > 1 && !isLoading) {
                    Divider(color = Color(0xFFEEEEEE), thickness = 1.dp)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 上一頁按鈕
                        IconButton(
                            onClick = { onPageChange(currentPage - 1) },
                            enabled = currentPage > 1,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "上一頁",
                                tint = if (currentPage > 1) Color(0xFF4CAF50) else Color(0xFFCCCCCC),
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        // 頁碼顯示
                        Text(
                            text = "$currentPage / $totalPages",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF333333)
                        )

                        // 下一頁按鈕
                        IconButton(
                            onClick = { onPageChange(currentPage + 1) },
                            enabled = currentPage < totalPages,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                Icons.Default.ArrowForward,
                                contentDescription = "下一頁",
                                tint = if (currentPage < totalPages) Color(0xFF4CAF50) else Color(0xFFCCCCCC),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CommitItem(commit: CommitInfo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF5F5F5)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 作者頭像
            AsyncImage(
                model = commit.authorAvatarUrl,
                contentDescription = "作者頭像",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE0E0E0))
            )

            // 提交資訊
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // 作者名稱
                Text(
                    text = commit.authorName,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF666666)
                )

                Spacer(modifier = Modifier.height(4.dp))

                // 提交訊息
                Text(
                    text = commit.message,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF333333),
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Hash 和日期
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = commit.shortHash,
                        fontSize = 11.sp,
                        color = Color(0xFF4CAF50),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = commit.date,
                        fontSize = 11.sp,
                        color = Color(0xFF666666)
                    )
                }
            }
        }
    }
}

private suspend fun fetchAllCommits(): List<CommitInfo> = withContext(Dispatchers.IO) {
    // 檢查快取
    if (CommitCache.isValid()) {
        return@withContext CommitCache.commits!!
    }

    val allCommits = mutableListOf<CommitInfo>()
    var page = 1
    val perPage = 100

    val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
    inputFormat.timeZone = TimeZone.getTimeZone("UTC")
    val outputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

    try {
        while (page <= 3) { // 減少到3頁，避免過多請求
            val urlString = "https://api.github.com/repos/xuelin123yt/Sound-Interaction-App/commits?sha=master&page=$page&per_page=$perPage"

            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection

            try {
                connection.connectTimeout = 10000
                connection.readTimeout = 10000
                connection.setRequestProperty("Accept", "application/vnd.github.v3+json")
                connection.setRequestProperty("User-Agent", "SoundInteractionApp")

                // ✅ 如果有 Token，就使用認證（提升到每小時 5000 次）
                if (BuildConfig.GITHUB_TOKEN.isNotEmpty()) {
                    connection.setRequestProperty("Authorization", "Bearer ${BuildConfig.GITHUB_TOKEN}")
                }

                val responseCode = connection.responseCode

                when (responseCode) {
                    200 -> {
                        // 成功
                        val response = connection.inputStream.bufferedReader().use { it.readText() }
                        val jsonArray = JSONArray(response)

                        if (jsonArray.length() == 0) break

                        for (i in 0 until jsonArray.length()) {
                            try {
                                val commitObj = jsonArray.getJSONObject(i)
                                val commit = commitObj.getJSONObject("commit")
                                val author = commit.getJSONObject("author")

                                val authorData = commitObj.optJSONObject("author")
                                val authorName = author.optString("name", "Unknown")
                                val authorAvatarUrl = authorData?.optString("avatar_url", "") ?: ""

                                val message = commit.optString("message", "No message")
                                val hash = commitObj.optString("sha", "").take(7)
                                val dateStr = author.optString("date", "")

                                val date = try {
                                    if (dateStr.isNotEmpty()) {
                                        val parsedDate = inputFormat.parse(dateStr)
                                        outputFormat.format(parsedDate ?: Date())
                                    } else {
                                        "Unknown date"
                                    }
                                } catch (e: Exception) {
                                    dateStr.take(10)
                                }

                                allCommits.add(CommitInfo(message, hash, date, authorName, authorAvatarUrl))
                            } catch (e: Exception) {
                                continue
                            }
                        }

                        if (jsonArray.length() < perPage) break
                    }
                    403 -> {
                        // GitHub API 請求次數限制
                        val errorResponse = try {
                            connection.errorStream?.bufferedReader()?.use { it.readText() }
                        } catch (e: Exception) {
                            null
                        }

                        val rateLimitReset = connection.getHeaderField("X-RateLimit-Reset")
                        val resetTime = if (rateLimitReset != null) {
                            try {
                                val resetDate = Date(rateLimitReset.toLong() * 1000)
                                val formatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                                formatter.format(resetDate)
                            } catch (e: Exception) {
                                "未知"
                            }
                        } else {
                            "未知"
                        }

                        throw Exception("請求次數已達上限\n\n" +
                                "GitHub 伺服器的請求限制已達上限\n" +
                                "限制將在 $resetTime 重置\n\n" +
                                "📌 請稍後再試\n" +
                                "系統會自動快取資料 5 分鐘")
                    }
                    404 -> {
                        throw Exception("找不到儲存庫 (404)\n請確認儲存庫名稱是否正確")
                    }
                    500, 502, 503, 504 -> {
                        throw Exception("GitHub 服務暫時無法使用 ($responseCode)\n請稍後再試")
                    }
                    else -> {
                        val errorResponse = try {
                            connection.errorStream?.bufferedReader()?.use { it.readText() }
                        } catch (e: Exception) {
                            null
                        }
                        throw Exception("HTTP 錯誤: $responseCode\n${errorResponse ?: "未知錯誤"}")
                    }
                }
            } finally {
                connection.disconnect()
            }

            page++
        }
    } catch (e: java.net.SocketTimeoutException) {
        throw Exception("網路連線逾時\n請檢查網路連線")
    } catch (e: java.net.UnknownHostException) {
        throw Exception("無法連線到 GitHub\n請檢查網路連線")
    } catch (e: Exception) {
        if (e.message?.contains("403") == true || e.message?.contains("GitHub API") == true) {
            throw e // 保留原始的 403 錯誤訊息
        }
        throw Exception("載入失敗: ${e.message}")
    }

    if (allCommits.isEmpty()) {
        throw Exception("沒有找到任何提交記錄")
    }

    // 儲存到快取
    CommitCache.set(allCommits)

    allCommits
}

@Composable
fun InfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color(0xFF666666)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF333333)
        )
    }
}