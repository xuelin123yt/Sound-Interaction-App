package com.soundinteractionapp

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.soundinteractionapp.ui.theme.SoundInteractionAppTheme
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.soundinteractionapp.components.FreePlayScreenContent

// 導入所有畫面 Composable

import com.soundinteractionapp.screens.freeplay.interactions.CatInteractionScreen
import com.soundinteractionapp.screens.freeplay.interactions.OceanInteractionScreen
import com.soundinteractionapp.screens.freeplay.interactions.BellInteractionScreen // <-- [新增]
import com.soundinteractionapp.screens.WelcomeScreenContent
import com.soundinteractionapp.screens.game.GameModeScreenContent
import com.soundinteractionapp.screens.game.Level1FollowBeatScreen
import com.soundinteractionapp.screens.game.Level2FindAnimalScreen
import com.soundinteractionapp.screens.game.Level3PitchScreen
import com.soundinteractionapp.screens.game.Level4CompositionScreen


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
                    onDispose {
                        soundManager.release()
                    }
                }

                NavHost(navController = navController, startDestination = Screen.Welcome.route) {

                    // 1. 歡迎畫面
                    composable(Screen.Welcome.route) {
                        WelcomeScreenContent(
                            onNavigateToFreePlay = { navController.navigate(Screen.FreePlay.route) },
                            onNavigateToRelax = { navController.navigate(Screen.Relax.route) },
                            onNavigateToGame = { navController.navigate(Screen.Game.route) }
                        )
                    }

                    // 2. 自由探索模式畫面
                    composable(Screen.FreePlay.route) {
                        FreePlayScreenContent(
                            onNavigateBack = { navController.popBackStack() },
                            soundManager = soundManager,
                            onNavigateToCatInteraction = { navController.navigate(Screen.CatInteraction.route) },
                            onNavigateToPianoInteraction = { navController.navigate(Screen.PianoInteraction.route) },
                            onNavigateToDogInteraction = { navController.navigate(Screen.DogInteraction.route) },
                            onNavigateToBirdInteraction = { navController.navigate(Screen.BirdInteraction.route) },
                            onNavigateToDrumInteraction = { navController.navigate(Screen.DrumInteraction.route) },
                            onNavigateToOceanInteraction = { navController.navigate(Screen.OceanInteraction.route) },
                            onNavigateToBellInteraction = { // <-- [新增]
                                navController.navigate(Screen.BellInteraction.route)
                            }
                        )
                    }

                    // 3. 放鬆模式 (海浪)
                    composable(Screen.Relax.route) {
                        OceanInteractionScreen(
                            onNavigateBack = { navController.popBackStack() },
                            soundManager = soundManager
                        )
                    }
                    composable(Screen.OceanInteraction.route) {
                        OceanInteractionScreen(
                            onNavigateBack = { navController.popBackStack() },
                            soundManager = soundManager
                        )
                    }

                    // 4. 遊戲訓練模式
                    composable(Screen.Game.route) {
                        GameModeScreenContent(
                            onNavigateBack = { navController.popBackStack() },
                            onNavigateToLevel = { route -> navController.navigate(route) }
                        )
                    }

                    // 互動子畫面
                    composable(Screen.CatInteraction.route) {
                        CatInteractionScreen(onNavigateBack = { navController.popBackStack() }, soundManager = soundManager)
                    }
                    composable(Screen.PianoInteraction.route) {
                        PianoInteractionScreen(onNavigateBack = { navController.popBackStack() }, soundManager = soundManager)
                    }
                    composable(Screen.DogInteraction.route) {
                        DogInteractionScreen(onNavigateBack = { navController.popBackStack() }, soundManager = soundManager)
                    }
                    composable(Screen.BirdInteraction.route) {
                        BirdInteractionScreen(onNavigateBack = { navController.popBackStack() }, soundManager = soundManager)
                    }
                    composable(Screen.DrumInteraction.route) {
                        DrumInteractionScreen(onNavigateBack = { navController.popBackStack() }, soundManager = soundManager)
                    }
                    composable(Screen.BellInteraction.route) { // <-- [新增]
                        BellInteractionScreen(onNavigateBack = { navController.popBackStack() }, soundManager = soundManager)
                    }

                    // 遊戲關卡
                    composable(Screen.GameLevel1.route) { Level1FollowBeatScreen(onNavigateBack = { navController.popBackStack() }) }
                    composable(Screen.GameLevel2.route) { Level2FindAnimalScreen(onNavigateBack = { navController.popBackStack() }) }
                    composable(Screen.GameLevel3.route) { Level3PitchScreen(onNavigateBack = { navController.popBackStack() }) }
                    composable(Screen.GameLevel4.route) { Level4CompositionScreen(onNavigateBack = { navController.popBackStack() }) }
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