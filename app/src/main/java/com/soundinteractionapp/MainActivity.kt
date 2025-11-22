package com.soundinteractionapp

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.soundinteractionapp.ui.theme.SoundInteractionAppTheme
import com.soundinteractionapp.screens.WelcomeScreenContent
import com.soundinteractionapp.screens.freeplay.FreePlayScreenContent
import com.soundinteractionapp.screens.freeplay.interactions.*
import com.soundinteractionapp.screens.game.GameModeScreenContent
import com.soundinteractionapp.screens.game.levels.*
import com.soundinteractionapp.screens.relax.RelaxScreenContent

class MainActivity : ComponentActivity() {

    private lateinit var soundManager: SoundManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        soundManager = SoundManager(this)

        setContent {
            SoundInteractionAppTheme {
                val navController = rememberNavController()

                DisposableEffect(Unit) {
                    onDispose { soundManager.release() }
                }

                NavHost(
                    navController = navController,
                    startDestination = Screen.Welcome.route
                ) {
                    // 歡迎畫面
                    composable(Screen.Welcome.route) {
                        WelcomeScreenContent(
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

                    // 自由探索模式
                    composable(Screen.FreePlay.route) {
                        FreePlayScreenContent(
                            onNavigateBack = { navController.popBackStack() },
                            soundManager = soundManager,
                            onNavigateToInteraction = { route ->
                                navController.navigate(route)
                            }
                        )
                    }

                    // 放鬆模式
                    composable(Screen.Relax.route) {
                        RelaxScreenContent(
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }

                    // ========================================
                    // 9 個互動畫面
                    // ========================================

                    // 貓
                    composable(Screen.CatInteraction.route) {
                        CatInteractionScreen(
                            onNavigateBack = { navController.popBackStack() },
                            soundManager = soundManager
                        )
                    }

                    // 狗
                    composable(Screen.DogInteraction.route) {
                        DogInteractionScreen(
                            onNavigateBack = { navController.popBackStack() },
                            soundManager = soundManager
                        )
                    }

                    // 鳥
                    composable(Screen.BirdInteraction.route) {
                        BirdInteractionScreen(
                            onNavigateBack = { navController.popBackStack() },
                            soundManager = soundManager
                        )
                    }

                    // 鋼琴
                    composable(Screen.PianoInteraction.route) {
                        PianoInteractionScreen(
                            onNavigateBack = { navController.popBackStack() },
                            soundManager = soundManager
                        )
                    }

                    // 鼓
                    composable(Screen.DrumInteraction.route) {
                        DrumInteractionScreen(
                            onNavigateBack = { navController.popBackStack() },
                            soundManager = soundManager
                        )
                    }

                    // 鈴鐺
                    composable(Screen.BellInteraction.route) {
                        BellInteractionScreen(
                            onNavigateBack = { navController.popBackStack() },
                            soundManager = soundManager
                        )
                    }

                    // 雨
                    composable(Screen.RainInteraction.route) {
                        RainInteractionScreen(
                            onNavigateBack = { navController.popBackStack() },
                            soundManager = soundManager
                        )
                    }

                    // 海浪
                    composable(Screen.OceanInteraction.route) {
                        OceanInteractionScreen(
                            onNavigateBack = { navController.popBackStack() },
                            soundManager = soundManager
                        )
                    }

                    // 風
                    composable(Screen.WindInteraction.route) {
                        WindInteractionScreen(
                            onNavigateBack = { navController.popBackStack() },
                            soundManager = soundManager
                        )
                    }

                    // ========================================
                    // 遊戲訓練模式
                    // ========================================

                    composable(Screen.Game.route) {
                        GameModeScreenContent(
                            onNavigateBack = { navController.popBackStack() },
                            onNavigateToLevel = { route ->
                                navController.navigate(route)
                            }
                        )
                    }

                    // ========================================
                    // 4 個關卡
                    // ========================================

                    composable(Screen.GameLevel1.route) {
                        Level1Screen(
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }

                    composable(Screen.GameLevel2.route) {
                        Level2Screen(
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }

                    composable(Screen.GameLevel3.route) {
                        Level3Screen(
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }

                    composable(Screen.GameLevel4.route) {
                        Level4Screen(
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::soundManager.isInitialized) {
            soundManager.release()
        }
    }
}