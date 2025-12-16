package com.soundinteractionapp.screens.game.levels.level4.logic

import android.content.Context
import android.content.SharedPreferences
import android.media.AudioManager
import android.os.Build

/**
 * 音訊延遲管理器
 * 負責檢測和補償不同音訊輸出設備的延遲
 *
 * ✅ 此文件不需要修改，已經包含完整的延遲補償邏輯
 */
object AudioOffsetManager {

    private const val PREFS_NAME = "audio_offset_prefs"
    private const val KEY_GLOBAL_OFFSET = "global_offset"
    private const val KEY_SPEAKER_OFFSET = "speaker_offset"
    private const val KEY_BLUETOOTH_OFFSET = "bluetooth_offset"
    private const val KEY_WIRED_OFFSET = "wired_offset"

    // 預設延遲值（毫秒）
    private const val DEFAULT_SPEAKER_OFFSET = -50  // 喇叭通常有 50ms 延遲
    private const val DEFAULT_BLUETOOTH_OFFSET = 150  // 藍牙通常有 150-200ms 延遲
    private const val DEFAULT_WIRED_OFFSET = 0  // 有線耳機延遲最小

    private var prefs: SharedPreferences? = null
    private var audioManager: AudioManager? = null

    /**
     * 初始化音訊延遲管理器
     */
    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    /**
     * 獲取當前音訊輸出設備類型
     */
    enum class AudioOutputDevice {
        SPEAKER,
        BLUETOOTH,
        WIRED_HEADSET,
        UNKNOWN
    }

    /**
     * 檢測當前使用的音訊輸出設備
     */
    fun getCurrentAudioDevice(): AudioOutputDevice {
        audioManager?.let { am ->
            return when {
                am.isBluetoothA2dpOn || am.isBluetoothScoOn -> AudioOutputDevice.BLUETOOTH
                am.isWiredHeadsetOn -> AudioOutputDevice.WIRED_HEADSET
                am.isSpeakerphoneOn || (!am.isBluetoothA2dpOn && !am.isWiredHeadsetOn) ->
                    AudioOutputDevice.SPEAKER
                else -> AudioOutputDevice.UNKNOWN
            }
        }
        return AudioOutputDevice.UNKNOWN
    }

    /**
     * 獲取當前設備的音訊延遲補償值（毫秒）
     */
    fun getCurrentOffset(): Int {
        val device = getCurrentAudioDevice()
        val globalOffset = getGlobalOffset()
        val deviceOffset = getDeviceOffset(device)

        return globalOffset + deviceOffset
    }

    /**
     * 獲取全局偏移（用戶自定義校準）
     */
    fun getGlobalOffset(): Int {
        return prefs?.getInt(KEY_GLOBAL_OFFSET, 0) ?: 0
    }

    /**
     * 設置全局偏移
     */
    fun setGlobalOffset(offset: Int) {
        prefs?.edit()?.putInt(KEY_GLOBAL_OFFSET, offset)?.apply()
    }

    /**
     * 獲取特定設備的偏移值
     */
    private fun getDeviceOffset(device: AudioOutputDevice): Int {
        return when (device) {
            AudioOutputDevice.SPEAKER ->
                prefs?.getInt(KEY_SPEAKER_OFFSET, DEFAULT_SPEAKER_OFFSET) ?: DEFAULT_SPEAKER_OFFSET
            AudioOutputDevice.BLUETOOTH ->
                prefs?.getInt(KEY_BLUETOOTH_OFFSET, DEFAULT_BLUETOOTH_OFFSET) ?: DEFAULT_BLUETOOTH_OFFSET
            AudioOutputDevice.WIRED_HEADSET ->
                prefs?.getInt(KEY_WIRED_OFFSET, DEFAULT_WIRED_OFFSET) ?: DEFAULT_WIRED_OFFSET
            AudioOutputDevice.UNKNOWN -> 0
        }
    }

    /**
     * 設置特定設備的偏移值
     */
    fun setDeviceOffset(device: AudioOutputDevice, offset: Int) {
        val key = when (device) {
            AudioOutputDevice.SPEAKER -> KEY_SPEAKER_OFFSET
            AudioOutputDevice.BLUETOOTH -> KEY_BLUETOOTH_OFFSET
            AudioOutputDevice.WIRED_HEADSET -> KEY_WIRED_OFFSET
            AudioOutputDevice.UNKNOWN -> return
        }
        prefs?.edit()?.putInt(key, offset)?.apply()
    }

    /**
     * 獲取系統音訊延遲（Android 6.0+）
     */
    fun getSystemAudioLatency(context: Context): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                val property = audioManager.getProperty(AudioManager.PROPERTY_OUTPUT_FRAMES_PER_BUFFER)
                val framesPerBuffer = property?.toIntOrNull() ?: 0

                val sampleRate = audioManager.getProperty(AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE)
                    ?.toIntOrNull() ?: 44100

                // 計算延遲: (緩衝區大小 / 採樣率) * 1000
                if (framesPerBuffer > 0) {
                    ((framesPerBuffer.toFloat() / sampleRate) * 1000).toInt()
                } else {
                    0
                }
            } catch (e: Exception) {
                0
            }
        } else {
            0
        }
    }

    /**
     * 重置所有偏移設定為預設值
     */
    fun resetToDefaults() {
        prefs?.edit()?.apply {
            putInt(KEY_GLOBAL_OFFSET, 0)
            putInt(KEY_SPEAKER_OFFSET, DEFAULT_SPEAKER_OFFSET)
            putInt(KEY_BLUETOOTH_OFFSET, DEFAULT_BLUETOOTH_OFFSET)
            putInt(KEY_WIRED_OFFSET, DEFAULT_WIRED_OFFSET)
            apply()
        }
    }

    /**
     * 獲取所有設備的偏移資訊（用於顯示）
     */
    data class OffsetInfo(
        val globalOffset: Int,
        val speakerOffset: Int,
        val bluetoothOffset: Int,
        val wiredOffset: Int,
        val currentDevice: AudioOutputDevice,
        val currentTotalOffset: Int
    )

    fun getOffsetInfo(): OffsetInfo {
        val currentDevice = getCurrentAudioDevice()
        return OffsetInfo(
            globalOffset = getGlobalOffset(),
            speakerOffset = getDeviceOffset(AudioOutputDevice.SPEAKER),
            bluetoothOffset = getDeviceOffset(AudioOutputDevice.BLUETOOTH),
            wiredOffset = getDeviceOffset(AudioOutputDevice.WIRED_HEADSET),
            currentDevice = currentDevice,
            currentTotalOffset = getCurrentOffset()
        )
    }
}