package com.soundinteractionapp

import android.content.Context
import android.media.MediaPlayer

/**
 * 負責管理應用程式中所有聲音資源和播放的類別。
 * 這是實現「音效系統」的核心技術。
 */
class SoundManager(private val context: Context) {
    // 儲存 MediaPlayer 實例，用於控制播放
    private var mediaPlayer: MediaPlayer? = null

    /**
     * 播放指定的聲音資源。
     * @param resId 聲音資源的 R.raw ID (例如 R.raw.cat_sound)
     */
    fun playSound(resId: Int) {
        // 先釋放舊的 MediaPlayer 實例，確保不會洩漏資源
        release()

        try {
            // 創建新的 MediaPlayer 實例並載入資源
            mediaPlayer = MediaPlayer.create(context, resId)

            // 設置完成監聽器，播放結束後自動釋放
            mediaPlayer?.setOnCompletionListener { mp ->
                mp.release()
                mediaPlayer = null
            }

            mediaPlayer?.start()
        } catch (e: Exception) {
            // 實際開發中應該在這裡記錄錯誤日誌
            println("Error playing sound: ${e.message}")
        }
    }

    /**
     * 停止並釋放當前正在播放的聲音資源。
     * 這是防止內存洩漏的關鍵步驟。
     */
    fun release() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}