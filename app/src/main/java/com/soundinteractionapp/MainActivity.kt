package com.soundinteractionapp

import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.*
import androidx.lifecycle.Lifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.soundinteractionapp.ui.theme.SoundInteractionAppTheme
import com.soundinteractionapp.utils.GameInputManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

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
import com.soundinteractionapp.data.RankingViewModel
import com.soundinteractionapp.data.AuthViewModel
import com.soundinteractionapp.data.ProfileViewModel
import com.soundinteractionapp.data.LeaderboardViewModel
import com.soundinteractionapp.screens.game.levels.level1.Level1FollowBeatScreen
import com.soundinteractionapp.screens.game.levels.level2.Level2FollowBeatScreen
import com.soundinteractionapp.screens.game.levels.level4.Level4Screen
import com.soundinteractionapp.screens.settings.SettingScreen

private const val TAG = "MainActivity"

class MainActivity : ComponentActivity() {

    private lateinit var soundManager: SoundManager
    private var isInGameLevel by mutableStateOf(false)

    private val rankingViewModel by viewModels<RankingViewModel>()
    private val authViewModel by viewModels<AuthViewModel>()
    private val profileViewModel by viewModels<ProfileViewModel>()
    private val leaderboardViewModel by viewModels<LeaderboardViewModel>()

    // 🔥 新增：導航鎖（防止快速點擊）
    private val mainScope = CoroutineScope(Dispatchers.Main + Job())
    private var navigationJob: Job? = null
    private var isNavigating = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate called")

        hideSystemUI()
        window.decorView.setOnSystemUiVisibilityChangeListener { visibility ->
            if (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0) hideSystemUI()
        }
        soundManager = SoundManager(this)

        setContent {
            SoundInteractionAppTheme {
                val navController = rememberNavController()

                DisposableEffect(Unit) {
                    Log.d(TAG, "DisposableEffect: Setup")
                    onDispose {
                        Log.d(TAG, "DisposableEffect: Dispose")
                        soundManager.release()
                    }
                }

                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                // 🔥 監控路由變化
                LaunchedEffect(currentRoute) {
                    Log.d(TAG, "Current Route: $currentRoute")

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

                // 🔥 返回鍵處理（完全重寫）
                DisposableEffect(currentRoute) {
                    Log.d(TAG, "Setting up BackPressedCallback for route: $currentRoute")

                    val callback = object : androidx.activity.OnBackPressedCallback(true) {
                        override fun handleOnBackPressed() {
                            // 🔥 如果正在導航，直接忽略
                            if (isNavigating.value) {
                                Log.d(TAG, "BLOCKED: Navigation in progress")
                                return
                            }

                            Log.d(TAG, "=== BACK PRESSED ===")
                            Log.d(TAG, "Current Route: $currentRoute")

                            when (currentRoute) {
                                Screen.Welcome.route -> {
                                    Log.d(TAG, "ACTION: Finish app")
                                    soundManager.playSFX("cancel")
                                    finish()
                                }
                                Screen.Splash.route -> {
                                    Log.d(TAG, "ACTION: Blocked (Splash screen)")
                                }
                                else -> {
                                    Log.d(TAG, "ACTION: PopBackStack")
                                    soundManager.playSFX("cancel")

                                    // 🔥 取消之前的導航任務，啟動新任務
                                    navigationJob?.cancel()
                                    navigationJob = mainScope.launch {
                                        try {
                                            isNavigating.value = true

                                            val currentEntry = navController.currentBackStackEntry
                                            if (currentEntry != null) {
                                                val state = currentEntry.lifecycle.currentState
                                                Log.d(TAG, "Current entry lifecycle state: $state")

                                                if (state.isAtLeast(Lifecycle.State.STARTED)) {
                                                    Log.d(TAG, "Executing popBackStack()")
                                                    val result = navController.popBackStack()
                                                    Log.d(TAG, "PopBackStack result: $result")

                                                    if (!result) {
                                                        Log.e(TAG, "PopBackStack FAILED! Possible empty stack")
                                                    }
                                                } else {
                                                    Log.e(TAG, "BLOCKED: Entry not in valid state ($state)")
                                                }
                                            } else {
                                                Log.e(TAG, "ERROR: currentBackStackEntry is null!")
                                            }

                                            // 🔥 延遲 300ms 後解鎖，確保導航動畫完成
                                            delay(300)
                                        } catch (e: Exception) {
                                            Log.e(TAG, "EXCEPTION in popBackStack", e)
                                            e.printStackTrace()
                                        } finally {
                                            isNavigating.value = false
                                        }
                                    }
                                }
                            }
                            Log.d(TAG, "===================")
                        }
                    }

                    onBackPressedDispatcher.addCallback(callback)

                    onDispose {
                        Log.d(TAG, "Removing BackPressedCallback for route: $currentRoute")
                        callback.remove()
                    }
                }

                NavHost(
                    navController = navController,
                    startDestination = Screen.Splash.route
                ) {

                    composable(Screen.Splash.route) {
                        Log.d(TAG, "Composing: Splash")
                        SplashScreen(navController)
                    }

                    composable(Screen.Welcome.route) {
                        Log.d(TAG, "Composing: Welcome")
                        WelcomeScreen(
                            soundManager = soundManager,
                            onNavigateToFreePlay = {
                                if (!isNavigating.value) {
                                    Log.d(TAG, "Navigate: Welcome -> FreePlay")
                                    navController.navigate(Screen.FreePlay.route)
                                }
                            },
                            onNavigateToRelax = {
                                if (!isNavigating.value) {
                                    Log.d(TAG, "Navigate: Welcome -> Relax")
                                    navController.navigate(Screen.Relax.route)
                                }
                            },
                            onNavigateToGame = {
                                if (!isNavigating.value) {
                                    Log.d(TAG, "Navigate: Welcome -> Game")
                                    navController.navigate(Screen.Game.route)
                                }
                            },
                            onNavigateToProfile = {
                                if (!isNavigating.value) {
                                    Log.d(TAG, "Navigate: Welcome -> Profile")
                                    navController.navigate(Screen.Profile.route)
                                }
                            },
                            onNavigateToSettings = {
                                if (!isNavigating.value) {
                                    Log.d(TAG, "Navigate: Welcome -> Settings")
                                    navController.navigate(Screen.Settings.route)
                                }
                            },
                            onNavigateToLeaderboard = {
                                if (!isNavigating.value) {
                                    Log.d(TAG, "Navigate: Welcome -> Leaderboard")
                                    navController.navigate(Screen.Leaderboard.route)
                                }
                            },
                            onLogout = {
                                Log.d(TAG, "Logout triggered")
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
                        Log.d(TAG, "Composing: Profile")
                        ProfileScreen(
                            onNavigateBack = {
                                if (!isNavigating.value) {
                                    Log.d(TAG, "Profile: Navigate Back")
                                    navController.popBackStack()
                                }
                            },
                            onLogout = {
                                Log.d(TAG, "Profile: Logout")
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
                        Log.d(TAG, "Composing: Settings")
                        SettingScreen(
                            soundManager = soundManager,
                            onNavigateBack = {
                                if (!isNavigating.value) {
                                    Log.d(TAG, "Settings: Navigate Back")
                                    navController.popBackStack()
                                }
                            },
                            isLoggedIn = authViewModel.isLoggedIn() && !authViewModel.isAnonymous()
                        )
                    }

                    composable(Screen.Leaderboard.route) {
                        Log.d(TAG, "Composing: Leaderboard")
                        LeaderboardScreen(
                            navController = navController,
                            viewModel = leaderboardViewModel
                        )
                    }

                    composable(Screen.FreePlay.route) {
                        Log.d(TAG, "Composing: FreePlay")
                        FreePlayScreenContent(
                            { if (!isNavigating.value) navController.popBackStack() },
                            soundManager,
                            { navController.navigate(Screen.CatInteraction.route) },
                            { navController.navigate(Screen.PianoInteraction.route) },
                            { navController.navigate(Screen.DogInteraction.route) },
                            { navController.navigate(Screen.BirdInteraction.route) },
                            { navController.navigate(Screen.DrumInteraction.route) },
                            { navController.navigate(Screen.BellInteraction.route) }
                        )
                    }

                    composable(Screen.Relax.route) {
                        Log.d(TAG, "Composing: Relax")
                        RelaxScreenContent(
                            { if (!isNavigating.value) navController.popBackStack() },
                            soundManager,
                            { navController.navigate(Screen.OceanInteraction.route) },
                            { navController.navigate(Screen.RainInteraction.route) },
                            { navController.navigate(Screen.WindInteraction.route) }
                        )
                    }

                    composable(Screen.Game.route) {
                        Log.d(TAG, "Composing: Game")
                        GameModeScreenContent(
                            {
                                if (!isNavigating.value) {
                                    Log.d(TAG, "Game: Navigate Back")
                                    navController.popBackStack()
                                }
                            },
                            { route ->
                                Log.d(TAG, "Game: Navigate to $route")
                                navController.navigate(route)
                            },
                            rankingViewModel
                        )
                    }

                    composable(Screen.GameLevel1.route) {
                        Log.d(TAG, "Composing: GameLevel1")
                        Level1FollowBeatScreen(
                            { if (!isNavigating.value) navController.popBackStack() },
                            soundManager,
                            rankingViewModel
                        )
                    }

                    composable(Screen.GameLevel2.route) {
                        Log.d(TAG, "Composing: GameLevel2")
                        Level2FollowBeatScreen(
                            onNavigateBack = { if (!isNavigating.value) navController.popBackStack() },
                            soundManager = soundManager,
                            rankingViewModel = rankingViewModel
                        )
                    }

                    composable(Screen.GameLevel3.route) {
                        Log.d(TAG, "Composing: GameLevel3")
                        Level3PitchScreen(
                            onNavigateBack = { if (!isNavigating.value) navController.popBackStack() },
                            rankingViewModel = rankingViewModel
                        )
                    }

                    composable(Screen.GameLevel4.route) {
                        Log.d(TAG, "Composing: GameLevel4")
                        Level4Screen(
                            onNavigateBack = {
                                if (!isNavigating.value) {
                                    Log.d(TAG, "Level4: Navigate Back")
                                    navController.popBackStack()
                                }
                            },
                            soundManager = soundManager
                        )
                    }

                    // Interactions
                    composable(Screen.CatInteraction.route) {
                        CatInteractionScreen({ if (!isNavigating.value) navController.popBackStack() }, soundManager)
                    }
                    composable(Screen.PianoInteraction.route) {
                        PianoInteractionScreen({ if (!isNavigating.value) navController.popBackStack() }, soundManager)
                    }
                    composable(Screen.DogInteraction.route) {
                        DogInteractionScreen({ if (!isNavigating.value) navController.popBackStack() }, soundManager)
                    }
                    composable(Screen.BirdInteraction.route) {
                        BirdInteractionScreen({ if (!isNavigating.value) navController.popBackStack() }, soundManager)
                    }
                    composable(Screen.DrumInteraction.route) {
                        DrumInteractionScreen({ if (!isNavigating.value) navController.popBackStack() }, soundManager)
                    }
                    composable(Screen.BellInteraction.route) {
                        BellInteractionScreen({ if (!isNavigating.value) navController.popBackStack() }, soundManager)
                    }

                    composable(Screen.OceanInteraction.route) {
                        OceanInteractionScreen({ if (!isNavigating.value) navController.popBackStack() }, soundManager)
                    }
                    composable(Screen.RainInteraction.route) {
                        RainInteractionScreen({ if (!isNavigating.value) navController.popBackStack() }, soundManager)
                    }
                    composable(Screen.WindInteraction.route) {
                        WindInteractionScreen({ if (!isNavigating.value) navController.popBackStack() }, soundManager)
                    }
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
        Log.d(TAG, "onPause")
        if (::soundManager.isInitialized) soundManager.pauseAllAudio()
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume")
        if (::soundManager.isInitialized) soundManager.resumeAllAudio()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")
        navigationJob?.cancel()
        if (::soundManager.isInitialized) soundManager.release()
    }

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
}