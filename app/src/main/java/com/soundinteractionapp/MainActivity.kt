package com.soundinteractionapp

import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.soundinteractionapp.ui.theme.SoundInteractionAppTheme
import com.soundinteractionapp.utils.GameInputManager

// --- Screens Imports ---
import com.soundinteractionapp.screens.SplashScreen
import com.soundinteractionapp.screens.WelcomeScreen
import com.soundinteractionapp.screens.SettingScreen
import com.soundinteractionapp.screens.profile.ProfileScreen
import com.soundinteractionapp.components.FreePlayScreenContent
import com.soundinteractionapp.screens.freeplay.interactions.*
import com.soundinteractionapp.screens.relax.RelaxScreenContent
import com.soundinteractionapp.screens.relax.ambiences.OceanInteractionScreen
import com.soundinteractionapp.screens.relax.ambiences.RainInteractionScreen
import com.soundinteractionapp.screens.relax.ambiences.WindInteractionScreen
import com.soundinteractionapp.screens.game.GameModeScreenContent
import com.soundinteractionapp.screens.game.levels.Level1FollowBeatScreen
import com.soundinteractionapp.screens.game.levels.Level2FindAnimalScreen
import com.soundinteractionapp.screens.game.levels.Level3PitchScreen
import com.soundinteractionapp.screens.game.levels.Level4CompositionScreen
import com.soundinteractionapp.data.RankingRepository
import com.soundinteractionapp.data.RankingViewModel

class MainActivity : ComponentActivity() {

    private lateinit var soundManager: SoundManager
    private val rankingRepository = RankingRepository()

    // ★修改 1: 新增變數用來追蹤是否在遊戲關卡中
    private var isInGameLevel by mutableStateOf(false)

    private val rankingViewModel by viewModels<RankingViewModel> {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(RankingViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return RankingViewModel(rankingRepository) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        hideSystemUI()
        window.decorView.setOnSystemUiVisibilityChangeListener { visibility ->
            if (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0) {
                hideSystemUI()
            }
        }

        soundManager = SoundManager(this)

        setContent {
            SoundInteractionAppTheme {
                val navController = rememberNavController()

                DisposableEffect(Unit) {
                    onDispose { soundManager.release() }
                }

                // 監聽當前路由
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                // ★修改 2: 根據路由判斷是否需要攔截按鍵 (是否為遊戲關卡)
                LaunchedEffect(currentRoute) {
                    isInGameLevel = when (currentRoute) {
                        Screen.GameLevel1.route,
                        Screen.GameLevel2.route,
                        Screen.GameLevel3.route,
                        Screen.GameLevel4.route -> true
                        else -> false
                    }

                    // 原有的 BGM 控制邏輯保持不變
                    when (currentRoute) {
                        Screen.Splash.route -> soundManager.stopBgm()
                        Screen.GameLevel1.route,
                        Screen.GameLevel2.route,
                        Screen.GameLevel3.route,
                        Screen.GameLevel4.route -> soundManager.stopBgm()
                        Screen.Settings.route -> soundManager.playBgm(R.raw.bgm)
                        Screen.FreePlay.route,
                        Screen.Relax.route,
                        Screen.Game.route,
                        Screen.Profile.route -> soundManager.playBgm(R.raw.bgm)
                        Screen.CatInteraction.route,
                        Screen.PianoInteraction.route,
                        Screen.DogInteraction.route,
                        Screen.BirdInteraction.route,
                        Screen.DrumInteraction.route,
                        Screen.BellInteraction.route,
                        Screen.OceanInteraction.route,
                        Screen.RainInteraction.route,
                        Screen.WindInteraction.route -> soundManager.playBgm(R.raw.bgm)
                    }
                }

                NavHost(navController = navController, startDestination = Screen.Splash.route) {
                    composable(Screen.Splash.route) { SplashScreen(navController) }
                    composable(Screen.Welcome.route) {
                        WelcomeScreen(soundManager,
                            { navController.navigate(Screen.FreePlay.route) },
                            { navController.navigate(Screen.Relax.route) },
                            { navController.navigate(Screen.Game.route) },
                            { navController.navigate(Screen.Profile.route) },
                            { navController.navigate(Screen.Settings.route) },
                            { navController.navigate(Screen.Splash.route) { popUpTo(0) { inclusive = true } } }
                        )
                    }
                    composable(Screen.Profile.route) { ProfileScreen({ navController.popBackStack() }, { navController.navigate(Screen.Splash.route) { popUpTo(0) { inclusive = true } } }) }
                    composable(Screen.Settings.route) { SettingScreen(soundManager, { navController.popBackStack() }) }
                    composable(Screen.FreePlay.route) {
                        FreePlayScreenContent(
                            { navController.popBackStack() }, soundManager,
                            { navController.navigate(Screen.CatInteraction.route) },
                            { navController.navigate(Screen.PianoInteraction.route) },
                            { navController.navigate(Screen.DogInteraction.route) },
                            { navController.navigate(Screen.BirdInteraction.route) },
                            { navController.navigate(Screen.DrumInteraction.route) },
                            { navController.navigate(Screen.BellInteraction.route) }
                        )
                    }
                    composable(Screen.Relax.route) {
                        RelaxScreenContent(
                            { navController.popBackStack() }, soundManager,
                            { navController.navigate(Screen.OceanInteraction.route) },
                            { navController.navigate(Screen.RainInteraction.route) },
                            { navController.navigate(Screen.WindInteraction.route) }
                        )
                    }
                    composable(Screen.Game.route) {
                        GameModeScreenContent({ navController.popBackStack() }, { route -> navController.navigate(route) }, rankingViewModel)
                    }
                    composable(Screen.GameLevel1.route) {
                        Level1FollowBeatScreen({ navController.popBackStack() }, soundManager, rankingViewModel)
                    }
                    composable(Screen.GameLevel2.route) { Level2FindAnimalScreen({ navController.popBackStack() }) }
                    composable(Screen.GameLevel3.route) { Level3PitchScreen({ navController.popBackStack() }) }
                    composable(Screen.GameLevel4.route) { Level4CompositionScreen({ navController.popBackStack() }) }
                    composable(Screen.CatInteraction.route) { CatInteractionScreen({ navController.popBackStack() }, soundManager) }
                    composable(Screen.PianoInteraction.route) { PianoInteractionScreen({ navController.popBackStack() }, soundManager) }
                    composable(Screen.DogInteraction.route) { DogInteractionScreen({ navController.popBackStack() }, soundManager) }
                    composable(Screen.BirdInteraction.route) { BirdInteractionScreen({ navController.popBackStack() }, soundManager) }
                    composable(Screen.DrumInteraction.route) { DrumInteractionScreen({ navController.popBackStack() }, soundManager) }
                    composable(Screen.BellInteraction.route) { BellInteractionScreen({ navController.popBackStack() }, soundManager) }
                    composable(Screen.OceanInteraction.route) { OceanInteractionScreen({ navController.popBackStack() }, soundManager) }
                    composable(Screen.RainInteraction.route) { RainInteractionScreen({ navController.popBackStack() }, soundManager) }
                    composable(Screen.WindInteraction.route) { WindInteractionScreen({ navController.popBackStack() }, soundManager) }
                }
            }
        }
    }

    // --- 實體按鍵監聽 ---
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (event?.repeatCount != 0) {
            return super.onKeyDown(keyCode, event)
        }

        // ★修改 3: 加入邏輯判斷
        // 如果是音量鍵，且目前「不在」遊戲關卡中 -> 執行 super.onKeyDown (允許系統調整音量)
        // 如果是音量鍵，且目前「在」遊戲關卡中 -> 攔截並觸發打擊

        val isVolumeKey = keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN

        if (isVolumeKey && !isInGameLevel) {
            // 如果不在遊戲中，讓系統處理音量鍵 (這樣你就可以調整音量了)
            return super.onKeyDown(keyCode, event)
        }

        when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP,
            KeyEvent.KEYCODE_VOLUME_DOWN,
            KeyEvent.KEYCODE_ENTER,
            KeyEvent.KEYCODE_HEADSETHOOK,
            KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE,
            KeyEvent.KEYCODE_CAMERA,
            KeyEvent.KEYCODE_DPAD_CENTER -> {
                // 這裡只會在 isInGameLevel 為 true 時，或者按的是非音量鍵時執行
                GameInputManager.triggerBeat()
                return true // 阻止系統處理 (例如阻止跳出音量條)
            }
        }

        return super.onKeyDown(keyCode, event)
    }

    // ✅ 新增：當 App 進入後台時暫停音樂
    override fun onPause() {
        super.onPause()
        if (::soundManager.isInitialized) {
            soundManager.pauseAllAudio()
        }
    }

    // ✅ 新增：當 App 回到前台時恢復音樂
    override fun onResume() {
        super.onResume()
        if (::soundManager.isInitialized) {
            soundManager.resumeAllAudio()
        }
    }

    // ✅ 修改：移除 onStop，讓音樂在後台保持暫停狀態
    // 只在 onDestroy 時才完全釋放資源

    private fun hideSystemUI() {
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                )
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemUI()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::soundManager.isInitialized) soundManager.release()
    }
}