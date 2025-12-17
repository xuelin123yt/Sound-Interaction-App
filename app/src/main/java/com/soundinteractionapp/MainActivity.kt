package com.soundinteractionapp

import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels // 確保有這個 import
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.soundinteractionapp.ui.theme.SoundInteractionAppTheme
import com.soundinteractionapp.utils.GameInputManager

// Screens
import com.soundinteractionapp.screens.*
import com.soundinteractionapp.screens.profile.ProfileScreen
import com.soundinteractionapp.components.FreePlayScreenContent
import com.soundinteractionapp.screens.freeplay.interactions.*
import com.soundinteractionapp.screens.relax.RelaxScreenContent
import com.soundinteractionapp.screens.relax.ambiences.*
import com.soundinteractionapp.screens.game.GameModeScreenContent
import com.soundinteractionapp.screens.game.levels.*

// Data
// RankingRepository 已經在 ViewModel 內部處理，這裡不需要 import 了 (除非其他地方用到)
import com.soundinteractionapp.data.RankingViewModel
import com.soundinteractionapp.data.AuthViewModel
import com.soundinteractionapp.data.ProfileViewModel
import com.soundinteractionapp.screens.game.levels.level1.Level1FollowBeatScreen
import com.soundinteractionapp.screens.game.levels.level2.Level2FollowBeatScreen
import com.soundinteractionapp.screens.game.levels.level4.Level4Screen
import com.soundinteractionapp.screens.settings.SettingScreen

class MainActivity : ComponentActivity() {

    private lateinit var soundManager: SoundManager

    // ❌ 移除這行：不再需要從 Activity 建立 Repository
    // private val rankingRepository = RankingRepository()

    private var isInGameLevel by mutableStateOf(false)

    // ✅ 修正：因為 ViewModel 沒有參數，直接這樣寫即可
    private val rankingViewModel by viewModels<RankingViewModel>()

    private val authViewModel by viewModels<AuthViewModel>()
    private val profileViewModel by viewModels<ProfileViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideSystemUI()
        window.decorView.setOnSystemUiVisibilityChangeListener { visibility ->
            if (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0) hideSystemUI()
        }
        soundManager = SoundManager(this)

        setContent {
            SoundInteractionAppTheme {
                val navController = rememberNavController()
                DisposableEffect(Unit) { onDispose { soundManager.release() } }

                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                LaunchedEffect(currentRoute) {
                    isInGameLevel = when (currentRoute) {
                        Screen.GameLevel1.route, Screen.GameLevel2.route,
                        Screen.GameLevel3.route, Screen.GameLevel4.route -> true
                        else -> false
                    }
                    when (currentRoute) {
                        Screen.Splash.route, Screen.GameLevel1.route,
                        Screen.GameLevel2.route, Screen.GameLevel3.route,
                        Screen.GameLevel4.route -> soundManager.stopBgm()
                        else -> soundManager.playBgm(R.raw.bgm)
                    }
                }

                NavHost(navController = navController, startDestination = Screen.Splash.route) {

                    composable(Screen.Splash.route) {
                        SplashScreen(navController)
                    }

                    composable(Screen.Welcome.route) {
                        WelcomeScreen(
                            soundManager = soundManager,
                            onNavigateToFreePlay = {
                                navController.navigate(Screen.FreePlay.route)
                            },
                            onNavigateToRelax = {
                                navController.navigate(Screen.Relax.route)
                            },
                            onNavigateToGame = {
                                navController.navigate(Screen.Game.route)
                            },
                            onNavigateToProfile = {
                                navController.navigate(Screen.Profile.route)
                            },
                            onNavigateToSettings = {
                                navController.navigate(Screen.Settings.route)
                            },
                            onLogout = {
                                soundManager.stopBgm()
                                authViewModel.signOut()
                                navController.navigate(Screen.Splash.route) {
                                    popUpTo(0) { inclusive = true }
                                }
                            },
                            authViewModel = authViewModel
                        )
                    }

                    composable(Screen.Profile.route) {
                        ProfileScreen(
                            onNavigateBack = {
                                navController.popBackStack()
                            },
                            onLogout = {
                                soundManager.stopBgm()
                                authViewModel.signOut()
                                navController.navigate(Screen.Splash.route) {
                                    popUpTo(0) { inclusive = true }
                                }
                            },
                            authViewModel = authViewModel,
                            profileViewModel = profileViewModel,
                            rankingViewModel = rankingViewModel,
                            onNavigateToLogin = {
                                navController.navigate(Screen.Welcome.route) {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        )
                    }

                    composable(Screen.Settings.route) {
                        SettingScreen(
                            soundManager = soundManager,
                            onNavigateBack = { navController.popBackStack() },
                            isLoggedIn = authViewModel.isLoggedIn() && !authViewModel.isAnonymous()
                        )
                    }

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
                        GameModeScreenContent(
                            { navController.popBackStack() },
                            { route -> navController.navigate(route) },
                            rankingViewModel
                        )
                    }

                    composable(Screen.GameLevel1.route) {
                        Level1FollowBeatScreen(
                            { navController.popBackStack() },
                            soundManager,
                            rankingViewModel
                        )
                    }
                    composable(Screen.GameLevel2.route) {
                        Level2FollowBeatScreen(
                            onNavigateBack = { navController.popBackStack() },
                            soundManager = soundManager,
                            rankingViewModel = rankingViewModel
                        )
                    }
                    composable(Screen.GameLevel3.route) {
                        Level3PitchScreen(
                            onNavigateBack = { navController.popBackStack() },
                            rankingViewModel = rankingViewModel
                        )
                    }
                    composable(Screen.GameLevel4.route) {
                        Level4Screen(navController)
                    }

                    // Interactions...
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

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (event?.repeatCount != 0) return super.onKeyDown(keyCode, event)
        val isVolumeKey = keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN
        if (isVolumeKey && !isInGameLevel) return super.onKeyDown(keyCode, event)

        when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP,
            KeyEvent.KEYCODE_VOLUME_DOWN,
            KeyEvent.KEYCODE_ENTER,
            KeyEvent.KEYCODE_HEADSETHOOK,
            KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE,
            KeyEvent.KEYCODE_CAMERA,
            KeyEvent.KEYCODE_DPAD_CENTER -> {
                GameInputManager.triggerBeat()
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onPause() {
        super.onPause()
        if (::soundManager.isInitialized) soundManager.pauseAllAudio()
    }

    override fun onResume() {
        super.onResume()
        if (::soundManager.isInitialized) soundManager.resumeAllAudio()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::soundManager.isInitialized) soundManager.release()
    }

    private fun hideSystemUI() {
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        or View.SYSTEM_UI_FLAG_FULLSCREEN          // ← 這個隱藏狀態列
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION     // ← 這個隱藏導航列
                        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                )
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemUI()
    }
}