package com.soundinteractionapp.screens.game.levels.level4.beatmaps

import com.soundinteractionapp.R
import com.soundinteractionapp.screens.game.levels.level4.Note
import com.soundinteractionapp.screens.game.levels.level4.NoteType
import com.soundinteractionapp.screens.game.levels.level4.CurveType
import com.soundinteractionapp.screens.game.levels.level4.models.TimingPoint

object OSU_02 : Beatmap {

    // ⚠️ TODO: 請更新此 ID 為正確的 Beatmap ID
    override val id = 2  // ⚠️ 請檢查並修改
    override val title = "神魔之塔 主題曲（夜）"
    override val description = "Madhead"

    // ⚠️ TODO: 請確保音頻文件已添加到 res/raw/ 目錄
    // 預期文件名: tower_of_saviors_bgm_main_theme_nightold_version.mp3 (原始: [1.0]Tower of saviors BGM 01.Tower of saviors.mp3)
    // ⚠️ 請檢查 R.raw.tower_of_saviors_bgm_main_theme_nightold_version 是否存在，否則請修改為正確的資源名稱
    override val audioResId = R.raw.osu_02  // ⚠️ 請檢查此資源是否存在

    // TODO: 請更新為實際的封面和背景圖片資源
    override val coverImageResId = R.drawable.osu_02  // 預設圖片
    override val backgroundImageResId = R.drawable.osu_02  // 預設圖片

    override val sliderMultiplier = 1.4f
    override val preempt = 1560L
    override val fadeIn = 1040L
    override val hitWindowPerfect = 110L
    override val hitWindowGreat = 180L
    override val hitWindowGood = 250L
    override val quickTapGrace = 500L
    override val offset = 0L
    override val audioLeadIn = 0L

    override val timingPoints = listOf(
        TimingPoint(471, 600.017130079724, 100, false),
        TimingPoint(20871, 600.017130079724, 100, true),
        TimingPoint(40071, 600.017130079724, 100, false)
    )

    // 音符數據 (共 51 個)
    override val notes = listOf(
        Note(40f, 192f, 170, NoteType.CIRCLE, comboColor = 1),
        Note(128f, 192f, 471, NoteType.CIRCLE, comboColor = 1),
        Note(216f, 192f, 1071, NoteType.CIRCLE, comboColor = 1),
        Note(304f, 192f, 1671, NoteType.SLIDER, 2271, CurveType.LINEAR, listOf(448f to 192f, 448f to 192f), 1, 140f, 1),
        Note(400f, 120f, 2871, NoteType.CIRCLE, comboColor = 1),
        Note(304f, 104f, 3471, NoteType.CIRCLE, comboColor = 1),
        Note(208f, 80f, 4071, NoteType.SLIDER, 4671, CurveType.LINEAR, listOf(64f to 80f, 64f to 80f), 1, 140f, 1),
        Note(120f, 192f, 5271, NoteType.CIRCLE, comboColor = 2),
        Note(192f, 272f, 5871, NoteType.CIRCLE, comboColor = 2),
        Note(256f, 328f, 6471, NoteType.CIRCLE, comboColor = 2),
        Note(256f, 232f, 7071, NoteType.CIRCLE, comboColor = 2),
        Note(160f, 192f, 7671, NoteType.CIRCLE, comboColor = 3),
        Note(256f, 192f, 8871, NoteType.CIRCLE, comboColor = 3),
        Note(344f, 192f, 9471, NoteType.CIRCLE, comboColor = 0),
        Note(160f, 56f, 10071, NoteType.CIRCLE, comboColor = 0),
        Note(160f, 192f, 10671, NoteType.CIRCLE, comboColor = 0),
        Note(260f, 96f, 11271, NoteType.SLIDER, 11871, CurveType.LINEAR, listOf(360f to 96f), 1, 140f, 0),
        Note(304f, 192f, 12471, NoteType.SLIDER, 13071, CurveType.LINEAR, listOf(152f to 192f), 1, 140f, 0),
        Note(200f, 288f, 13671, NoteType.SLIDER, 14271, CurveType.LINEAR, listOf(312f to 288f, 304f to 288f), 1, 140f, 0),
        Note(136f, 192f, 14871, NoteType.CIRCLE, comboColor = 1),
        Note(256f, 192f, 15471, NoteType.CIRCLE, comboColor = 1),
        Note(256f, 72f, 16071, NoteType.SLIDER, 16671, CurveType.BEZIER, listOf(376f to 192f, 376f to 192f), 1, 140f, 1),
        Note(376f, 192f, 17271, NoteType.CIRCLE, comboColor = 1),
        Note(256f, 192f, 17871, NoteType.CIRCLE, comboColor = 1),
        Note(128f, 200f, 18471, NoteType.SLIDER, 19671, CurveType.PERFECT, listOf(200f to 120f, 160f to 216f), 1, 280f, 1),
        Note(96f, 264f, 20271, NoteType.CIRCLE, comboColor = 2),
        Note(112f, 152f, 20871, NoteType.SLIDER, 21471, CurveType.LINEAR, listOf(216f to 56f), 1, 140f, 2),
        Note(296f, 56f, 22071, NoteType.SLIDER, 22671, CurveType.LINEAR, listOf(410f to 159f), 1, 140f, 2),
        Note(400f, 232f, 23271, NoteType.SLIDER, 23871, CurveType.LINEAR, listOf(296f to 328f), 1, 140f, 2),
        Note(216f, 328f, 24471, NoteType.SLIDER, 25071, CurveType.LINEAR, listOf(104f to 224f), 1, 140f, 2),
        Note(250f, 192f, 25671, NoteType.SLIDER, 26271, CurveType.LINEAR, listOf(280f to 92f), 1, 140f, 3),
        Note(160f, 120f, 26871, NoteType.SLIDER, 27471, CurveType.LINEAR, listOf(160f to 280f), 1, 140f, 3),
        Note(352f, 280f, 28071, NoteType.SLIDER, 28671, CurveType.LINEAR, listOf(352f to 120f), 1, 140f, 3),
        Note(336f, 80f, 29271, NoteType.CIRCLE, comboColor = 3),
        Note(256f, 192f, 30471, NoteType.SLIDER, 31671, CurveType.LINEAR, listOf(256f to 48f), 2, 140f, 0),
        Note(360f, 184f, 32271, NoteType.CIRCLE, comboColor = 0),
        Note(392f, 296f, 32871, NoteType.SLIDER, 33471, CurveType.LINEAR, listOf(232f to 296f), 1, 140f, 0),
        Note(144f, 256f, 34071, NoteType.CIRCLE, comboColor = 1),
        Note(104f, 152f, 34671, NoteType.CIRCLE, comboColor = 1),
        Note(152f, 40f, 35271, NoteType.CIRCLE, comboColor = 2),
        Note(288f, 64f, 35871, NoteType.CIRCLE, comboColor = 2),
        Note(408f, 136f, 36471, NoteType.SLIDER, 37071, CurveType.LINEAR, listOf(288f to 240f), 1, 140f, 2),
        Note(240f, 184f, 37671, NoteType.SLIDER, 39471, CurveType.BEZIER, listOf(120f to 152f, 24f to 216f, 88f to 368f, 224f to 360f, 224f to 352f, 280f to 280f), 1, 420f, 2),
        Note(344f, 216f, 40071, NoteType.CIRCLE, comboColor = 3),
        Note(392f, 304f, 40671, NoteType.CIRCLE, comboColor = 3),
        Note(456f, 248f, 41271, NoteType.CIRCLE, comboColor = 3),
        Note(328f, 152f, 42471, NoteType.CIRCLE, comboColor = 0),
        Note(256f, 112f, 43071, NoteType.CIRCLE, comboColor = 0),
        Note(256f, 300f, 43671, NoteType.CIRCLE, comboColor = 0),
        Note(160f, 192f, 44571, NoteType.CIRCLE, comboColor = 1),
        Note(256f, 192f, 44871, NoteType.CIRCLE, comboColor = 1)
    )

    override fun getBPM() = 60000.0 / timingPoints.first().beatLength
}