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
import com.soundinteractionapp.data.RankingRepository
import com.soundinteractionapp.data.RankingViewModel
import com.soundinteractionapp.data.AuthViewModel
import com.soundinteractionapp.data.ProfileViewModel

class MainActivity : ComponentActivity() {

    private lateinit var soundManager: SoundManager
    private val rankingRepository = RankingRepository()
    private var isInGameLevel by mutableStateOf(false)

    // ViewModels
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
                        Screen.GameLevel1.route, Screen.GameLevel2.route, Screen.GameLevel3.route, Screen.GameLevel4.route -> true
                        else -> false
                    }
                    when (currentRoute) {
                        Screen.Splash.route, Screen.GameLevel1.route, Screen.GameLevel2.route, Screen.GameLevel3.route, Screen.GameLevel4.route -> soundManager.stopBgm()
                        else -> soundManager.playBgm(R.raw.bgm)
                    }
                }

                NavHost(navController = navController, startDestination = Screen.Splash.route) {

                    composable(Screen.Splash.route) { SplashScreen(navController) }

                    // ★ 修正 1：參數名稱改為 onLogout (解決紅線)
                    composable(Screen.Welcome.route) {
                        WelcomeScreen(
                            soundManager = soundManager,
                            onNavigateToFreePlay = { navController.navigate(Screen.FreePlay.route) },
                            onNavigateToRelax = { navController.navigate(Screen.Relax.route) },
                            onNavigateToGame = { navController.navigate(Screen.Game.route) },
                            onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
                            onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                            onLogout = { finish() }, // 這裡對應 WelcomeScreen 的 onLogout 參數
                            authViewModel = authViewModel
                        )
                    }

                    // ★ 修正 2：如果這裡紅線，是因為 ProfileScreen 檔案沒更新
                    composable(Screen.Profile.route) {
                        ProfileScreen(
                            onNavigateBack = { navController.popBackStack() },
                            onAccountDeleted = {
                                // 帳號刪除後的處理，導回 Welcome
                                navController.navigate(Screen.Welcome.route) {
                                    popUpTo(0) { inclusive = true }
                                }
                            },
                            authViewModel = authViewModel, // 這裡要改，您原本可能是自己初始化的
                            profileViewModel = profileViewModel, // 從 MainActivity 傳入
                            rankingViewModel = rankingViewModel // ★ 記得傳入這個！
                        )
                    }

                    // ★ 注意：移除了 composable("login")，因為不需要了

                    // 其他頁面保持不變
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
                    composable(Screen.GameLevel1.route) { Level1FollowBeatScreen({ navController.popBackStack() }, soundManager, rankingViewModel) }
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

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (event?.repeatCount != 0) return super.onKeyDown(keyCode, event)
        val isVolumeKey = keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN
        if (isVolumeKey && !isInGameLevel) return super.onKeyDown(keyCode, event)
        when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP, KeyEvent.KEYCODE_VOLUME_DOWN, KeyEvent.KEYCODE_ENTER,
            KeyEvent.KEYCODE_HEADSETHOOK, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, KeyEvent.KEYCODE_CAMERA,
            KeyEvent.KEYCODE_DPAD_CENTER -> { GameInputManager.triggerBeat(); return true }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onPause() { super.onPause(); if (::soundManager.isInitialized) soundManager.pauseAllAudio() }
    override fun onResume() { super.onResume(); if (::soundManager.isInitialized) soundManager.resumeAllAudio() }
    override fun onDestroy() { super.onDestroy(); if (::soundManager.isInitialized) soundManager.release() }
    private fun hideSystemUI() {
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
    }
    override fun onWindowFocusChanged(hasFocus: Boolean) { super.onWindowFocusChanged(hasFocus); if (hasFocus) hideSystemUI() }
}