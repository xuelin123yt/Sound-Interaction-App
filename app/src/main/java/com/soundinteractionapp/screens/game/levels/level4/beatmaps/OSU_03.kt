package com.soundinteractionapp.screens.game.levels.level4.beatmaps

import com.soundinteractionapp.R
import com.soundinteractionapp.screens.game.levels.level4.Note
import com.soundinteractionapp.screens.game.levels.level4.NoteType
import com.soundinteractionapp.screens.game.levels.level4.CurveType
import com.soundinteractionapp.screens.game.levels.level4.models.TimingPoint

object OSU_03 : Beatmap {

    // ⚠️ TODO: 請更新此 ID 為正確的 Beatmap ID
    override val id = 3  // ⚠️ 請檢查並修改
    override val title = "Ib 記憶"
    override val description = "EX Industry"

    // ⚠️ TODO: 請確保音頻文件已添加到 res/raw/ 目錄
    // 預期文件名: memory.mp3 (原始: Ib_Memory.mp3)
    // ⚠️ 請檢查 R.raw.memory 是否存在，否則請修改為正確的資源名稱
    override val audioResId = R.raw.osu_03  // ⚠️ 請檢查此資源是否存在

    // TODO: 請更新為實際的封面和背景圖片資源
    override val coverImageResId = R.drawable.osu_03  // 預設圖片
    override val backgroundImageResId = R.drawable.osu_03  // 預設圖片

    override val sliderMultiplier = 1f
    override val preempt = 1440L
    override val fadeIn = 960L
    override val hitWindowPerfect = 110L
    override val hitWindowGreat = 180L
    override val hitWindowGood = 250L
    override val quickTapGrace = 500L
    override val offset = 0L
    override val audioLeadIn = 1000L

    override val timingPoints = listOf(
        TimingPoint(855, 536.193029490617, 30, false),
        TimingPoint(16940, 534.75935828877, 30, false),
        TimingPoint(24961, 560.747663551402, 30, false),
        TimingPoint(28318, 541.27198917456, 50, false),
        TimingPoint(38060, 548.446069469835, 50, false),
        TimingPoint(41350, 541.27198917456, 70, false),
        TimingPoint(64083, 545.454545454546, 70, false),
        TimingPoint(65730, 550.45871559633, 70, false),
        TimingPoint(69115, 548.446069469835, 50, false)
    )

    // 音符數據 (共 70 個)
    override val notes = listOf(
        Note(80f, 60f, 855, NoteType.CIRCLE, comboColor = 0),
        Note(132f, 144f, 1391, NoteType.CIRCLE, comboColor = 0),
        Note(232f, 144f, 1927, NoteType.CIRCLE, comboColor = 0),
        Note(284f, 60f, 2463, NoteType.SLIDER, 3535, CurveType.PERFECT, listOf(384f to 60f, 460f to 124f), 1, 200f, 0),
        Note(496f, 212f, 4072, NoteType.SLIDER, 4608, CurveType.PERFECT, listOf(496f to 261f, 469f to 302f), 1, 100f, 1),
        Note(372f, 324f, 5144, NoteType.CIRCLE, comboColor = 1),
        Note(272f, 304f, 5680, NoteType.SLIDER, 6752, CurveType.PERFECT, listOf(168f to 304f, 92f to 240f), 1, 200f, 1),
        Note(36f, 164f, 7289, NoteType.SLIDER, 7825, CurveType.LINEAR, listOf(112f to 100f), 1, 100f, 2),
        Note(204f, 140f, 8361, NoteType.CIRCLE, comboColor = 2),
        Note(192f, 40f, 8897, NoteType.SLIDER, 9969, CurveType.PERFECT, listOf(292f to 40f, 372f to 96f), 1, 200f, 2),
        Note(440f, 168f, 10506, NoteType.CIRCLE, comboColor = 3),
        Note(460f, 268f, 11042, NoteType.SLIDER, 12114, CurveType.BEZIER, listOf(364f to 268f, 364f to 212f, 264f to 212f), 1, 200f, 3),
        Note(192f, 272f, 12651, NoteType.CIRCLE, comboColor = 3),
        Note(116f, 208f, 13187, NoteType.CIRCLE, comboColor = 3),
        Note(192f, 144f, 13723, NoteType.SLIDER, 14795, CurveType.PERFECT, listOf(292f to 144f, 368f to 84f), 1, 200f, 4),
        Note(452f, 140f, 15332, NoteType.CIRCLE, comboColor = 4),
        Note(452f, 240f, 15868, NoteType.SLIDER, 16404, CurveType.PERFECT, listOf(400f to 240f, 360f to 268f), 1, 100f, 4),
        Note(280f, 324f, 16940, NoteType.CIRCLE, comboColor = 5),
        Note(188f, 364f, 17474, NoteType.SLIDER, 18010, CurveType.PERFECT, listOf(147f to 337f, 98f to 338f), 1, 100f, 5),
        Note(4f, 300f, 18544, NoteType.CIRCLE, comboColor = 5),
        Note(16f, 200f, 19079, NoteType.SLIDER, 20151, CurveType.PERFECT, listOf(16f to 100f, 84f to 28f), 1, 200f, 6),
        Note(180f, 44f, 20683, NoteType.CIRCLE, comboColor = 6),
        Note(256f, 108f, 21218, NoteType.CIRCLE, comboColor = 6),
        Note(332f, 44f, 21752, NoteType.SLIDER, 22824, CurveType.PERFECT, listOf(424f to 84f, 424f to 176f), 1, 200f, 6),
        Note(376f, 260f, 23357, NoteType.CIRCLE, comboColor = 0),
        Note(306f, 330f, 23891, NoteType.CIRCLE, comboColor = 0),
        Note(206f, 330f, 24426, NoteType.CIRCLE, comboColor = 0),
        Note(136f, 260f, 24961, NoteType.SLIDER, 25497, CurveType.LINEAR, listOf(72f to 184f), 1, 100f, 0),
        Note(164f, 144f, 26082, NoteType.SLIDER, 26618, CurveType.LINEAR, listOf(100f to 68f), 1, 100f, 0),
        Note(296f, 44f, 27764, NoteType.CIRCLE, comboColor = 1),
        Note(388f, 88f, 28318, NoteType.SLIDER, 28854, CurveType.PERFECT, listOf(440f to 88f, 480f to 112f), 1, 100f, 1),
        Note(464f, 212f, 29400, NoteType.CIRCLE, comboColor = 1),
        Note(376f, 260f, 29941, NoteType.SLIDER, 31013, CurveType.PERFECT, listOf(276f to 260f, 200f to 324f), 1, 200f, 1),
        Note(108f, 284f, 31565, NoteType.SLIDER, 32101, CurveType.LINEAR, listOf(28f to 216f), 1, 100f, 2),
        Note(20f, 120f, 32648, NoteType.CIRCLE, comboColor = 2),
        Note(92f, 52f, 33189, NoteType.SLIDER, 34261, CurveType.PERFECT, listOf(192f to 52f, 256f to 124f), 1, 200f, 2),
        Note(352f, 100f, 34813, NoteType.SLIDER, 35349, CurveType.LINEAR, listOf(396f to 200f), 1, 100f, 3),
        Note(424f, 284f, 35895, NoteType.CIRCLE, comboColor = 3),
        Note(324f, 264f, 36437, NoteType.SLIDER, 37509, CurveType.PERFECT, listOf(224f to 264f, 152f to 328f), 1, 200f, 3),
        Note(52f, 324f, 38060, NoteType.CIRCLE, comboColor = 4),
        Note(0f, 236f, 38608, NoteType.CIRCLE, comboColor = 4),
        Note(52f, 152f, 39156, NoteType.CIRCLE, comboColor = 4),
        Note(152f, 152f, 39705, NoteType.SLIDER, 40777, CurveType.PERFECT, listOf(252f to 152f, 320f to 80f), 1, 200f, 4),
        Note(408f, 20f, 41350, NoteType.SLIDER, 41886, CurveType.LINEAR, listOf(472f to 96f), 1, 100f, 5),
        Note(444f, 192f, 42432, NoteType.CIRCLE, comboColor = 5),
        Note(472f, 288f, 42973, NoteType.SLIDER, 44045, CurveType.PERFECT, listOf(384f to 348f, 288f to 348f), 1, 200f, 5),
        Note(220f, 280f, 44597, NoteType.SLIDER, 45133, CurveType.PERFECT, listOf(168f to 280f, 128f to 256f), 1, 100f, 6),
        Note(80f, 168f, 45680, NoteType.CIRCLE, comboColor = 6),
        Note(60f, 68f, 46221, NoteType.SLIDER, 47293, CurveType.PERFECT, listOf(164f to 68f, 252f to 20f), 1, 200f, 6),
        Note(348f, 24f, 47845, NoteType.SLIDER, 48381, CurveType.LINEAR, listOf(432f to 84f), 1, 100f, 0),
        Note(508f, 140f, 48927, NoteType.CIRCLE, comboColor = 0),
        Note(436f, 208f, 49469, NoteType.SLIDER, 50541, CurveType.LINEAR, listOf(296f to 356f), 1, 200f, 0),
        Note(220f, 292f, 51092, NoteType.CIRCLE, comboColor = 1),
        Note(140f, 352f, 51634, NoteType.SLIDER, 52170, CurveType.PERFECT, listOf(88f to 352f, 48f to 324f), 1, 100f, 1),
        Note(48f, 224f, 52716, NoteType.SLIDER, 53788, CurveType.PERFECT, listOf(48f to 124f, 112f to 48f), 1, 200f, 1),
        Note(208f, 48f, 54340, NoteType.SLIDER, 54876, CurveType.LINEAR, listOf(312f to 48f), 1, 100f, 2),
        Note(408f, 48f, 55423, NoteType.CIRCLE, comboColor = 2),
        Note(476f, 120f, 55964, NoteType.SLIDER, 57036, CurveType.BEZIER, listOf(476f to 224f, 476f to 224f, 420f to 304f), 1, 200f, 2),
        Note(356f, 228f, 57588, NoteType.SLIDER, 58124, CurveType.LINEAR, listOf(296f to 320f), 1, 100f, 3),
        Note(200f, 332f, 58670, NoteType.CIRCLE, comboColor = 3),
        Note(100f, 312f, 59211, NoteType.SLIDER, 60283, CurveType.PERFECT, listOf(24f to 236f, 24f to 144f), 1, 200f, 3),
        Note(68f, 60f, 60835, NoteType.SLIDER, 61371, CurveType.PERFECT, listOf(120f to 60f, 160f to 36f), 1, 100f, 4),
        Note(256f, 60f, 61918, NoteType.CIRCLE, comboColor = 4),
        Note(352f, 36f, 62459, NoteType.SLIDER, 63531, CurveType.PERFECT, listOf(452f to 36f, 508f to 112f), 1, 200f, 4),
        Note(452f, 192f, 64083, NoteType.CIRCLE, comboColor = 5),
        Note(444f, 292f, 64628, NoteType.SLIDER, 65164, CurveType.PERFECT, listOf(392f to 292f, 352f to 320f), 1, 100f, 5),
        Note(256f, 360f, 65719, NoteType.CIRCLE, comboColor = 5),
        Note(158f, 318f, 66280, NoteType.SLIDER, 66816, CurveType.PERFECT, listOf(117f to 291f, 68f to 292f), 1, 100f, 5),
        Note(60f, 192f, 67381, NoteType.CIRCLE, comboColor = 5),
        Note(256f, 192f, 67656, NoteType.SPINNER, 69115, comboColor = 6)
    )

    override fun getBPM() = 60000.0 / timingPoints.first().beatLength
}