package com.soundinteractionapp

object GameEngine {
    init {
        System.loadLibrary("voicegame")
    }

    // 初始化遊戲
    external fun initGame()

    // 更新遊戲邏輯 (回傳鳥的高度)
    external fun updateGame(): Float

    // ★ 關鍵修復：補回這個函數，紅字就會消失了！
    // 用來把麥克風的錄音數據傳給 C++
    external fun processAudio(audioData: ShortArray, size: Int)

    // 觸控跳躍 (保留著也沒關係，C++端有對應函數)
    external fun flap()

    // 獲取分數與時間
    external fun getGameState(): FloatArray

    // 獲取障礙物位置
    external fun getObstacleData(): FloatArray

    // 預留的音高接口 (目前沒用到但留著避免報錯)
    external fun sendPitchData(pitch: Float)
}