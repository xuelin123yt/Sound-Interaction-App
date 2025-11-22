package com.soundinteractionapp

sealed class Screen(val route: String) {
    object Welcome : Screen("welcome")
    object FreePlay : Screen("freeplay")
    object Relax : Screen("relax")
    object Game : Screen("game")

    // 自由探索 - 9個互動畫面
    object CatInteraction : Screen("interaction/cat")
    object DogInteraction : Screen("interaction/dog")
    object BirdInteraction : Screen("interaction/bird")
    object PianoInteraction : Screen("interaction/piano")
    object DrumInteraction : Screen("interaction/drum")
    object BellInteraction : Screen("interaction/bell")
    object RainInteraction : Screen("interaction/rain")
    object OceanInteraction : Screen("interaction/ocean")
    object WindInteraction : Screen("interaction/wind")

    // 遊戲訓練 - 4個關卡
    object GameLevel1 : Screen("game/level1")
    object GameLevel2 : Screen("game/level2")
    object GameLevel3 : Screen("game/level3")
    object GameLevel4 : Screen("game/level4")
}