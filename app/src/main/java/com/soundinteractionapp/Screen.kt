package com.soundinteractionapp

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Auth : Screen("auth")
    object Login : Screen("login")
    object Register : Screen("register")
    object GameHome : Screen("game_home")
    object Welcome : Screen("welcome")
    object Profile : Screen("profile")
    object Settings : Screen("settings")
    object FreePlay : Screen("freeplay")
    object Relax : Screen("relax")
    object Game : Screen("game")

    object CatInteraction : Screen("interaction/cat")
    object PianoInteraction : Screen("interaction/piano")
    object DogInteraction : Screen("interaction/dog")
    object BirdInteraction : Screen("interaction/bird")
    object DrumInteraction : Screen("interaction/drum")
    object BellInteraction : Screen("interaction/bell")
    object OceanInteraction : Screen("interaction/ocean")
    object RainInteraction : Screen("interaction/rain")
    object WindInteraction : Screen("interaction/wind")

    object GameLevel1 : Screen("game/level1")
    object GameLevel2 : Screen("game/level2")
    object GameLevel3 : Screen("game/level3")
    object GameLevel4 : Screen("game/level4")

    object GameMode : Screen("game_mode")

    // ✅ 新增排行榜
    object Leaderboard : Screen("leaderboard")
}