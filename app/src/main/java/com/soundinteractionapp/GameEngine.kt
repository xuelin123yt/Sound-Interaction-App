package com.soundinteractionapp

object GameEngine {
    init {
        System.loadLibrary("voicegame")
    }

    external fun initGame()
    external fun updateGame(): Float

    // ★ 改成這個：觸控跳躍
    external fun flap()

    external fun getGameState(): FloatArray
    external fun getObstacleData(): FloatArray
}