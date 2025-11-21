package com.soundinteractionapp

// 定義應用程式的主要模式，用於導航
sealed class Screen(val route: String) {
    object Welcome : Screen("welcome")
    object Relax : Screen("relax")
    object Game : Screen("game")
    object FreePlay : Screen("freeplay")
    // 四個獨立的遊戲關卡路由
    object GameLevel1 : Screen("game/level1") // 跟著拍拍手
    object GameLevel2 : Screen("game/level2") // 找出小動物
    object GameLevel3 : Screen("game/level3") // 音階高低
    object GameLevel4 : Screen("game/level4") // 創作小樂曲
}