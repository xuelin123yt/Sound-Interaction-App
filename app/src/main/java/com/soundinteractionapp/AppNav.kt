package com.soundinteractionapp

// 定義應用程式的主要模式，用於導航
sealed class Screen(val route: String) {
    object Welcome : Screen("welcome")
    object Relax : Screen("relax")
    object Game : Screen("game")
    object FreePlay : Screen("freeplay")

    // 互動畫面路由
    object CatInteraction : Screen("interaction/cat")
    object PianoInteraction : Screen("interaction/piano")
    object DogInteraction : Screen("interaction/dog")
    object BirdInteraction : Screen("interaction/bird")
    object DrumInteraction : Screen("interaction/drum")
    object WaveInteraction : Screen("interaction/wave")
    object BellInteraction : Screen("interaction/bell") // <-- [新增] 鈴鐺路由

    // 四個獨立的遊戲關卡路由
    object GameLevel1 : Screen("game/level1")
    object GameLevel2 : Screen("game/level2")
    object GameLevel3 : Screen("game/level3")
    object GameLevel4 : Screen("game/level4")
}