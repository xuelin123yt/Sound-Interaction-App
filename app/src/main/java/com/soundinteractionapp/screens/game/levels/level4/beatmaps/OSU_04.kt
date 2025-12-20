package com.soundinteractionapp.screens.game.levels.level4.beatmaps

import com.soundinteractionapp.R
import com.soundinteractionapp.screens.game.levels.level4.CurveType
import com.soundinteractionapp.screens.game.levels.level4.Note
import com.soundinteractionapp.screens.game.levels.level4.NoteType
import com.soundinteractionapp.screens.game.levels.level4.models.TimingPoint

object OSU_04 : Beatmap {

    // ⚠️ TODO: 請更新此 ID 為正確的 Beatmap ID
    override val id = 4  // ⚠️ 請檢查並修改
    override val title = "打上花火"
    override val description = "DAOKO x Kenshi Yonezu"

    // ⚠️ TODO: 請確保音頻文件已添加到 res/raw/ 目錄
    // 預期文件名: uchiage_hanabi.mp3 (原始: audio.mp3)
    // ⚠️ 請檢查 R.raw.uchiage_hanabi 是否存在，否則請修改為正確的資源名稱
    override val audioResId = R.raw.osu_04  // ⚠️ 請檢查此資源是否存在

    // TODO: 請更新為實際的封面和背景圖片資源
    override val coverImageResId = R.drawable.osu_04  // 預設圖片
    override val backgroundImageResId = R.drawable.osu_04  // 預設圖片

    override val sliderMultiplier = 0.999999999999999f
    override val preempt = 1440L
    override val fadeIn = 960L
    override val hitWindowPerfect = 110L
    override val hitWindowGreat = 180L
    override val hitWindowGood = 250L
    override val quickTapGrace = 500L
    override val offset = 0L
    override val audioLeadIn = 0L

    override val timingPoints = listOf(
        TimingPoint(67, 625.0, 40, false),
        TimingPoint(60067, -100.0, 70, true),
        TimingPoint(70067, -100.0, 70, true),
        TimingPoint(78817, -100.0, 50, true)
    )

    // 音符數據 (共 69 個)
    override val notes = listOf(
        Note(256f, 344f, 67, NoteType.SLIDER, 1629, CurveType.PERFECT, listOf(348f to 344f, 284f to 252f), 1, 250f, 1),
        Note(152f, 312f, 2567, NoteType.SLIDER, 4129, CurveType.PERFECT, listOf(60f to 312f, 124f to 220f), 1, 250f, 1),
        Note(252f, 292f, 5067, NoteType.SLIDER, 6629, CurveType.BEZIER, listOf(376f to 324f, 376f to 256f, 500f to 296f), 1, 250f, 2),
        Note(504f, 144f, 7567, NoteType.SLIDER, 9129, CurveType.BEZIER, listOf(380f to 112f, 380f to 180f, 256f to 140f), 1, 250f, 2),
        Note(144f, 40f, 10067, NoteType.SLIDER, 12567, CurveType.PERFECT, listOf(44f to 40f, 16f to 124f), 2, 200f, 3),
        Note(256f, 208f, 13817, NoteType.CIRCLE, comboColor = 3),
        Note(368f, 40f, 15067, NoteType.SLIDER, 16317, CurveType.PERFECT, listOf(468f to 40f, 496f to 124f), 1, 200f, 4),
        Note(452f, 212f, 16942, NoteType.CIRCLE, comboColor = 4),
        Note(356f, 188f, 17567, NoteType.SLIDER, 18817, CurveType.LINEAR, listOf(152f to 212f), 1, 200f, 4),
        Note(64f, 180f, 19442, NoteType.CIRCLE, comboColor = 4),
        Note(100f, 88f, 20067, NoteType.SLIDER, 20692, CurveType.LINEAR, listOf(200f to 104f), 1, 99.9999999999999f, 5),
        Note(284f, 160f, 21317, NoteType.SLIDER, 21942, CurveType.LINEAR, listOf(384f to 144f), 1, 99.9999999999999f, 5),
        Note(448f, 68f, 22567, NoteType.SLIDER, 23817, CurveType.PERFECT, listOf(476f to 112f, 480f to 160f), 2, 99.9999999999999f, 5),
        Note(348f, 52f, 24442, NoteType.CIRCLE, comboColor = 5),
        Note(288f, 132f, 25067, NoteType.SLIDER, 25692, CurveType.LINEAR, listOf(188f to 124f), 1, 99.9999999999999f, 6),
        Note(100f, 80f, 26317, NoteType.CIRCLE, comboColor = 6),
        Note(32f, 152f, 26942, NoteType.CIRCLE, comboColor = 6),
        Note(68f, 244f, 27567, NoteType.SLIDER, 28817, CurveType.BEZIER, listOf(56f to 284f, 56f to 284f, 64f to 308f, 64f to 308f, 56f to 344f), 2, 99.9999999999999f, 6),
        Note(168f, 224f, 29442, NoteType.CIRCLE, comboColor = 6),
        Note(228f, 144f, 30067, NoteType.SLIDER, 30692, CurveType.LINEAR, listOf(216f to 44f), 1, 99.9999999999999f, 7),
        Note(312f, 68f, 31317, NoteType.SLIDER, 31942, CurveType.LINEAR, listOf(324f to 168f), 1, 99.9999999999999f, 7),
        Note(384f, 248f, 32567, NoteType.CIRCLE, comboColor = 7),
        Note(336f, 336f, 33192, NoteType.CIRCLE, comboColor = 7),
        Note(240f, 304f, 33817, NoteType.SLIDER, 34442, CurveType.PERFECT, listOf(188f to 304f, 140f to 324f), 1, 99.9999999999999f, 7),
        Note(72f, 252f, 35067, NoteType.SLIDER, 35692, CurveType.PERFECT, listOf(98f to 206f, 104f to 155f), 1, 99.9999999999999f, 8),
        Note(72f, 64f, 36317, NoteType.CIRCLE, comboColor = 8),
        Note(168f, 40f, 36942, NoteType.CIRCLE, comboColor = 8),
        Note(250f, 40f, 37567, NoteType.SLIDER, 38192, CurveType.LINEAR, listOf(350f to 48f), 1, 99.9999999999999f, 8),
        Note(356f, 96f, 38817, NoteType.CIRCLE, comboColor = 8),
        Note(330f, 150f, 39442, NoteType.SLIDER, 39754, CurveType.LINEAR, listOf(330f to 200f), 1, 50f, 8),
        Note(316f, 280f, 40067, NoteType.SLIDER, 40692, CurveType.PERFECT, listOf(268f to 264f, 216f to 264f), 1, 99.9999999999999f, 9),
        Note(140f, 324f, 41317, NoteType.SLIDER, 41942, CurveType.LINEAR, listOf(148f to 380f), 2, 50f, 9),
        Note(124f, 224f, 42567, NoteType.CIRCLE, comboColor = 9),
        Note(32f, 188f, 43192, NoteType.CIRCLE, comboColor = 9),
        Note(112f, 128f, 43817, NoteType.SLIDER, 44129, CurveType.LINEAR, listOf(164f to 132f), 1, 50f, 9),
        Note(212f, 136f, 44442, NoteType.CIRCLE, comboColor = 9),
        Note(292f, 76f, 45067, NoteType.SLIDER, 45692, CurveType.PERFECT, listOf(344f to 84f, 384f to 112f), 1, 99.9999999999999f, 0),
        Note(444f, 188f, 46317, NoteType.SLIDER, 46942, CurveType.LINEAR, listOf(496f to 184f), 2, 50f, 0),
        Note(356f, 240f, 47567, NoteType.SLIDER, 48192, CurveType.LINEAR, listOf(368f to 344f), 1, 99.9999999999999f, 0),
        Note(272f, 368f, 48817, NoteType.SLIDER, 49129, CurveType.LINEAR, listOf(264f to 312f), 1, 50f, 0),
        Note(256f, 268f, 49442, NoteType.CIRCLE, comboColor = 0),
        Note(172f, 212f, 50067, NoteType.CIRCLE, comboColor = 1),
        Note(96f, 280f, 50692, NoteType.CIRCLE, comboColor = 1),
        Note(16f, 224f, 51317, NoteType.SLIDER, 51629, CurveType.LINEAR, listOf(8f to 174f), 1, 50f, 1),
        Note(0f, 124f, 51942, NoteType.CIRCLE, comboColor = 1),
        Note(80f, 64f, 52567, NoteType.SLIDER, 53192, CurveType.PERFECT, listOf(128f to 80f, 180f to 80f), 1, 99.9999999999999f, 1),
        Note(268f, 36f, 53817, NoteType.SLIDER, 54442, CurveType.PERFECT, listOf(317f to 36f, 365f to 52f), 1, 99.9999999999999f, 1),
        Note(256f, 340f, 59442, NoteType.SLIDER, 59754, CurveType.LINEAR, listOf(256f to 288f), 1, 50f, 2),
        Note(220f, 256f, 60067, NoteType.SLIDER, 61317, CurveType.PERFECT, listOf(172f to 272f, 120f to 272f), 2, 99.9999999999999f, 3),
        Note(276f, 172f, 61942, NoteType.CIRCLE, comboColor = 3),
        Note(368f, 208f, 62567, NoteType.CIRCLE, comboColor = 3),
        Note(424f, 124f, 63192, NoteType.CIRCLE, comboColor = 3),
        Note(364f, 44f, 63817, NoteType.SLIDER, 64442, CurveType.LINEAR, listOf(260f to 44f), 1, 99.9999999999999f, 3),
        Note(193f, 114f, 65067, NoteType.SLIDER, 66317, CurveType.LINEAR, listOf(89f to 114f), 2, 99.9999999999999f, 4),
        Note(263f, 184f, 66942, NoteType.CIRCLE, comboColor = 4),
        Note(344f, 244f, 67567, NoteType.SLIDER, 68192, CurveType.LINEAR, listOf(444f to 244f), 1, 99.9999999999999f, 4),
        Note(444f, 344f, 68817, NoteType.CIRCLE, comboColor = 4),
        Note(344f, 344f, 69442, NoteType.SLIDER, 69754, CurveType.LINEAR, listOf(288f to 344f), 1, 50f, 4),
        Note(252f, 316f, 70067, NoteType.SLIDER, 71317, CurveType.PERFECT, listOf(200f to 316f, 156f to 336f), 2, 99.9999999999999f, 5),
        Note(316f, 240f, 71942, NoteType.CIRCLE, comboColor = 5),
        Note(386f, 310f, 72567, NoteType.CIRCLE, comboColor = 5),
        Note(456f, 239f, 73192, NoteType.CIRCLE, comboColor = 5),
        Note(385f, 168f, 73817, NoteType.SLIDER, 74442, CurveType.LINEAR, listOf(384f to 68f), 1, 99.9999999999999f, 5),
        Note(288f, 36f, 75067, NoteType.SLIDER, 75692, CurveType.LINEAR, listOf(188f to 48f), 1, 99.9999999999999f, 6),
        Note(88f, 60f, 76317, NoteType.CIRCLE, comboColor = 6),
        Note(56f, 156f, 76942, NoteType.CIRCLE, comboColor = 6),
        Note(156f, 140f, 77567, NoteType.SLIDER, 78192, CurveType.LINEAR, listOf(256f to 128f), 1, 99.9999999999999f, 6),
        Note(336f, 188f, 78817, NoteType.SLIDER, 79442, CurveType.LINEAR, listOf(388f to 180f), 2, 50f, 6),
        Note(256f, 248f, 80067, NoteType.CIRCLE, comboColor = 7)
    )

    override fun getBPM() = 60000.0 / timingPoints.first().beatLength
}