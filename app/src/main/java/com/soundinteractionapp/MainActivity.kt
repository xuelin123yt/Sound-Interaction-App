package com.soundinteractionapp

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.soundinteractionapp.ui.theme.SoundInteractionAppTheme
import androidx.compose.ui.platform.LocalContext // 預覽時用於 SoundManager
import com.soundinteractionapp.Screen

// 確保所有 AppComponents 和 GameComponents 中的 Composable 函數都被導入
import com.soundinteractionapp.WelcomeScreenContent
import com.soundinteractionapp.AppModeButton
import com.soundinteractionapp.FreePlayScreenContent
import com.soundinteractionapp.GameModeScreenContent
import com.soundinteractionapp.Level1FollowBeatScreen
import com.soundinteractionapp.Level2FindAnimalScreen
import com.soundinteractionapp.Level3PitchHighLowScreen
import com.soundinteractionapp.Level4ComposeTuneScreen



class MainActivity : ComponentActivity() {
    // 實例化 SoundManager，並將 Activity Context 傳遞給它
    private val soundManager = SoundManager(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 設定全螢幕模式
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        setContent {
            SoundInteractionAppTheme {
                // 【導航控制器】
                val navController = rememberNavController()

                // 【NavHost 負責畫面切換】
                NavHost(navController = navController, startDestination = Screen.Welcome.route) {

                    // 1. 歡迎畫面
                    composable(Screen.Welcome.route) {
                        WelcomeScreen(
                            onNavigateToFreePlay = {
                                navController.navigate(Screen.FreePlay.route)
                            },
                            onNavigateToRelax = {
                                navController.navigate(Screen.Relax.route)
                            },
                            onNavigateToGame = {
                                navController.navigate(Screen.Game.route)
                            }
                        )
                    }

                    // 2. 自由探索模式畫面
                    composable(Screen.FreePlay.route) {
                        FreePlayScreen(
                            onNavigateBack = {
                                navController.popBackStack()
                            },
                            soundManager = soundManager // 傳遞 SoundManager 實例
                        )
                    }

                    // 3. 放鬆模式畫面
                    composable(Screen.Relax.route) {
                        RelaxScreen(
                            onNavigateBack = {
                                navController.popBackStack()
                            }
                        )
                    }

                    // 4. 遊戲訓練模式 (關卡選擇) 畫面
                    composable(Screen.Game.route) {
                        GameModeScreen(
                            onNavigateBack = {
                                navController.popBackStack()
                            },
                            onNavigateToLevel = { route ->
                                navController.navigate(route)
                            }
                        )
                    }

                    // 新增：四個獨立的遊戲關卡畫面 (傳遞 SoundManager)
                    composable(Screen.GameLevel1.route) {
                        Level1FollowBeatScreen(onNavigateBack = { navController.popBackStack() }, soundManager = soundManager)
                    }
                    composable(Screen.GameLevel2.route) {
                        Level2FindAnimalScreen(onNavigateBack = { navController.popBackStack() }, soundManager = soundManager)
                    }
                    composable(Screen.GameLevel3.route) {
                        Level3PitchHighLowScreen(onNavigateBack = { navController.popBackStack() }, soundManager = soundManager)
                    }
                    composable(Screen.GameLevel4.route) {
                        Level4ComposeTuneScreen(onNavigateBack = { navController.popBackStack() }, soundManager = soundManager)
                    }
                }
            }
        }
    }

    // 確保在 Activity 銷毀時釋放 MediaPlayer 資源
    override fun onDestroy() {
        super.onDestroy()
        soundManager.release()
    }
}

// =======================================================
// 主畫面 Composable 骨架 (從 AppComponents/GameComponents 引入內容)
// =======================================================

@Composable
fun WelcomeScreen(
    onNavigateToFreePlay: () -> Unit,
    onNavigateToRelax: () -> Unit,
    onNavigateToGame: () -> Unit
) {
    // 這裡直接調用 AppComponents 裡的內容
    WelcomeScreenContent(
        onNavigateToFreePlay = onNavigateToFreePlay,
        onNavigateToRelax = onNavigateToRelax,
        onNavigateToGame = onNavigateToGame
    )
}

@Composable
fun FreePlayScreen(onNavigateBack: () -> Unit, soundManager: SoundManager) {
    // 這裡直接調用 GameComponents 裡的內容
    FreePlayScreenContent(onNavigateBack = onNavigateBack, soundManager = soundManager)
}

@Composable
fun GameModeScreen(onNavigateBack: () -> Unit, onNavigateToLevel: (String) -> Unit) {
    // 這裡直接調用 GameComponents 裡的內容
    GameModeScreenContent(onNavigateBack = onNavigateBack, onNavigateToLevel = onNavigateToLevel)
}

@Composable
fun RelaxScreen(onNavigateBack: () -> Unit) {
    // 這裡使用簡單的 Box 佈局作為 Placeholder
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("放鬆模式（開發中）", style = MaterialTheme.typography.displayLarge)
        Button(onClick = onNavigateBack, modifier = Modifier.align(Alignment.BottomCenter).padding(32.dp)) {
            Text("返回")
        }
    }
}

// 預覽功能 (Preview)
@Preview(showBackground = true, widthDp = 800, heightDp = 480)
@Composable
fun WelcomeScreenPreview() {
    SoundInteractionAppTheme {
        WelcomeScreen(
            onNavigateToFreePlay = {},
            onNavigateToRelax = {},
            onNavigateToGame = {}
        )
    }
}
@Preview(showBackground = true, widthDp = 800, heightDp = 480)
@Composable
fun FreePlayScreenPreview() {
    SoundInteractionAppTheme {
        // 預覽時傳遞一個假的 SoundManager 實例
        FreePlayScreen(onNavigateBack = {}, soundManager = SoundManager(LocalContext.current))
    }
}
@Preview(showBackground = true, widthDp = 800, heightDp = 480)
@Composable
fun GameModeScreenPreview() {
    SoundInteractionAppTheme {
        GameModeScreen(onNavigateBack = {}, onNavigateToLevel = {})
    }
}


