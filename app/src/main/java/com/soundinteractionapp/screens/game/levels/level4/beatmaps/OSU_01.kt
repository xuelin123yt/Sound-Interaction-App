package com.soundinteractionapp.screens.game.levels.level4.beatmaps

import com.soundinteractionapp.R
import com.soundinteractionapp.screens.game.levels.level4.Note
import com.soundinteractionapp.screens.game.levels.level4.NoteType
import com.soundinteractionapp.screens.game.levels.level4.CurveType
import com.soundinteractionapp.screens.game.levels.level4.models.TimingPoint

object OSU_01 : Beatmap {

    override val id = 1
    override val title = "哆啦A夢主題曲"
    override val description = "Yamano Satoko"

    // ✅ 請確保音頻文件已添加到 res/raw/ 目錄
    // 預期文件名: doraemon_no_uta.mp3
    override val audioResId = R.raw.osu_01  // 原始文件: 02.A.mp3

    override val coverImageResId = R.drawable.osu_01
    override val backgroundImageResId = R.drawable.osu_01

    override val sliderMultiplier = 1f
    override val preempt = 1440L
    override val fadeIn = 960L
    override val hitWindowPerfect = 110L
    override val hitWindowGreat = 180L
    override val hitWindowGood = 250L
    override val quickTapGrace = 500L
    override val offset = 0L
    override val audioLeadIn = 0L

    override val timingPoints = listOf(
        TimingPoint(569, 497.293892402178, 50, false),
        TimingPoint(36374, -100.0, 50, true),
        TimingPoint(41347, -100.0, 40, true),
        TimingPoint(41844, -100.0, 30, true),
        TimingPoint(42175, -100.0, 20, true),
        TimingPoint(42341, -100.0, 50, true),
        TimingPoint(48143, -100.0, 30, true),
        TimingPoint(48474, -100.0, 50, true),
        TimingPoint(88092, -100.0, 50, true),
        TimingPoint(93065, -100.0, 40, true),
        TimingPoint(93562, -100.0, 30, true),
        TimingPoint(93894, -100.0, 20, true),
        TimingPoint(94060, -100.0, 50, true),
        TimingPoint(99862, -100.0, 30, true),
        TimingPoint(100193, -100.0, 50, true),
        TimingPoint(139811, -100.0, 50, true),
        TimingPoint(145281, -100.0, 40, true),
        TimingPoint(145778, -100.0, 30, true),
        TimingPoint(146110, -100.0, 20, true),
        TimingPoint(146276, -100.0, 50, true),
        TimingPoint(155724, -100.0, 50, true)
    )

    // 音符數據 (共 120 個)
    override val notes = listOf(
        Note(76f, 92f, 569, NoteType.SLIDER, 1563, CurveType.LINEAR, listOf(176f to 92f), 2, 100f, 0),
        Note(124f, 180f, 2060, NoteType.CIRCLE, comboColor = 0),
        Note(35f, 232f, 2558, NoteType.SLIDER, 4049, CurveType.PERFECT, listOf(123f to 332f, 213f to 234f), 1, 300f, 0),
        Note(228f, 135f, 4547, NoteType.SLIDER, 6038, CurveType.PERFECT, listOf(360f to 112f, 378f to 245f), 1, 300f, 1),
        Note(280f, 220f, 6536, NoteType.CIRCLE, comboColor = 1),
        Note(192f, 264f, 7033, NoteType.SLIDER, 8027, CurveType.PERFECT, listOf(222f to 302f, 268f to 319f), 2, 100f, 1),
        Note(279f, 219f, 8525, NoteType.SLIDER, 9022, CurveType.PERFECT, listOf(249f to 181f, 203f to 164f), 1, 100f, 2),
        Note(103f, 187f, 9520, NoteType.SLIDER, 10017, CurveType.PERFECT, listOf(57f to 171f, 27f to 132f), 1, 100f, 2),
        Note(116f, 87f, 10514, NoteType.SLIDER, 11011, CurveType.PERFECT, listOf(164f to 99f, 198f to 133f), 1, 100f, 2),
        Note(239f, 228f, 11509, NoteType.SLIDER, 12006, CurveType.PERFECT, listOf(278f to 196f, 295f to 150f), 1, 100f, 2),
        Note(256f, 192f, 12504, NoteType.SPINNER, 14493, comboColor = 3),
        Note(160f, 192f, 15487, NoteType.CIRCLE, comboColor = 0),
        Note(260f, 192f, 15984, NoteType.CIRCLE, comboColor = 0),
        Note(347f, 139f, 16482, NoteType.SLIDER, 16979, CurveType.PERFECT, listOf(363f to 186f, 351f to 234f), 1, 100f, 1),
        Note(431f, 292f, 17476, NoteType.SLIDER, 17973, CurveType.PERFECT, listOf(399f to 328f, 350f to 337f), 1, 100f, 1),
        Note(251f, 330f, 18471, NoteType.SLIDER, 19962, CurveType.BEZIER, listOf(220f to 200f, 60f to 200f, 83f to 100f), 1, 300f, 1),
        Note(159f, 36f, 20460, NoteType.SLIDER, 20957, CurveType.PERFECT, listOf(205f to 17f, 252f to 31f), 1, 100f, 2),
        Note(211f, 123f, 21455, NoteType.SLIDER, 21952, CurveType.PERFECT, listOf(258f to 140f, 304f to 122f), 1, 100f, 2),
        Note(399f, 156f, 22449, NoteType.SLIDER, 24438, CurveType.PERFECT, listOf(259f to 275f, 113f to 162f), 1, 400f, 2),
        Note(140f, 75f, 26428, NoteType.SLIDER, 27422, CurveType.PERFECT, listOf(189f to 75f, 229f to 102f), 2, 100f, 3),
        Note(160f, 176f, 27920, NoteType.CIRCLE, comboColor = 3),
        Note(64f, 151f, 28417, NoteType.SLIDER, 29411, CurveType.PERFECT, listOf(64f to 201f, 88f to 244f), 2, 100f, 3),
        Note(160f, 176f, 29909, NoteType.CIRCLE, comboColor = 3),
        Note(184f, 272f, 30406, NoteType.SLIDER, 34384, CurveType.PERFECT, listOf(373f to 267f, 454f to 95f), 2, 400f, 0),
        Note(72f, 64f, 36374, NoteType.SLIDER, 36871, CurveType.LINEAR, listOf(170f to 79f), 1, 100f, 1),
        Note(268f, 108f, 37368, NoteType.SLIDER, 37865, CurveType.LINEAR, listOf(356f to 154f), 1, 100f, 1),
        Note(436f, 212f, 38363, NoteType.CIRCLE, comboColor = 2),
        Note(352f, 268f, 38860, NoteType.CIRCLE, comboColor = 2),
        Note(252f, 268f, 39357, NoteType.CIRCLE, comboColor = 2),
        Note(256f, 192f, 40352, NoteType.SPINNER, 42341, comboColor = 3),
        Note(236f, 44f, 44330, NoteType.CIRCLE, comboColor = 0),
        Note(136f, 52f, 44828, NoteType.CIRCLE, comboColor = 0),
        Note(72f, 132f, 45325, NoteType.CIRCLE, comboColor = 0),
        Note(155f, 311f, 46320, NoteType.SLIDER, 47811, CurveType.PERFECT, listOf(294f to 319f, 345f to 188f), 1, 300f, 1),
        Note(284f, 108f, 48309, NoteType.CIRCLE, comboColor = 1),
        Note(375f, 71f, 48806, NoteType.SLIDER, 49303, CurveType.PERFECT, listOf(422f to 87f, 453f to 125f), 1, 100f, 2),
        Note(432f, 220f, 49801, NoteType.CIRCLE, comboColor = 2),
        Note(352f, 280f, 50298, NoteType.CIRCLE, comboColor = 3),
        Note(260f, 314f, 50795, NoteType.SLIDER, 51789, CurveType.PERFECT, listOf(210f to 316f, 166f to 292f), 2, 100f, 3),
        Note(184f, 224f, 56265, NoteType.SLIDER, 56762, CurveType.LINEAR, listOf(259f to 158f), 1, 100f, 0),
        Note(364f, 156f, 57260, NoteType.SLIDER, 57757, CurveType.LINEAR, listOf(438f to 222f), 1, 100f, 0),
        Note(424f, 320f, 58255, NoteType.SLIDER, 59249, CurveType.LINEAR, listOf(224f to 320f), 1, 200f, 0),
        Note(72f, 188f, 60244, NoteType.SLIDER, 60741, CurveType.LINEAR, listOf(72f to 88f), 1, 100f, 1),
        Note(168f, 52f, 61238, NoteType.SLIDER, 61735, CurveType.LINEAR, listOf(268f to 52f), 1, 100f, 1),
        Note(328f, 132f, 62233, NoteType.SLIDER, 62730, CurveType.LINEAR, listOf(328f to 232f), 1, 100f, 1),
        Note(232f, 256f, 63228, NoteType.SLIDER, 63725, CurveType.LINEAR, listOf(132f to 256f), 1, 100f, 1),
        Note(48f, 204f, 68200, NoteType.SLIDER, 69691, CurveType.BEZIER, listOf(232f to 204f, 192f to 104f, 156f to 204f, 333f to 203f), 1, 300f, 2),
        Note(336f, 304f, 70190, NoteType.SLIDER, 71681, CurveType.BEZIER, listOf(152f to 304f, 192f to 400f, 228f to 304f, 50f to 304f), 1, 300f, 2),
        Note(100f, 216f, 72179, NoteType.SLIDER, 74168, CurveType.PERFECT, listOf(194f to 243f, 288f to 214f), 2, 200f, 3),
        Note(148f, 128f, 74665, NoteType.SLIDER, 75162, CurveType.LINEAR, listOf(148f to 28f), 1, 100f, 3),
        Note(236f, 80f, 75660, NoteType.CIRCLE, comboColor = 3),
        Note(256f, 176f, 76157, NoteType.SLIDER, 77151, CurveType.LINEAR, listOf(356f to 176f), 2, 100f, 3),
        Note(184f, 244f, 77649, NoteType.CIRCLE, comboColor = 3),
        Note(104f, 304f, 78146, NoteType.SLIDER, 79140, CurveType.PERFECT, listOf(193f to 339f, 275f to 289f), 1, 200f, 0),
        Note(184f, 244f, 79638, NoteType.CIRCLE, comboColor = 0),
        Note(247f, 168f, 80136, NoteType.SLIDER, 81627, CurveType.PERFECT, listOf(387f to 138f, 481f to 246f), 1, 300f, 0),
        Note(392f, 292f, 82125, NoteType.CIRCLE, comboColor = 1),
        Note(316f, 356f, 82622, NoteType.CIRCLE, comboColor = 1),
        Note(252f, 284f, 83119, NoteType.CIRCLE, comboColor = 1),
        Note(324f, 216f, 83617, NoteType.CIRCLE, comboColor = 1),
        Note(411f, 159f, 84114, NoteType.SLIDER, 86103, CurveType.PERFECT, listOf(328f to 112f, 242f to 154f), 2, 200f, 1),
        Note(52f, 320f, 88092, NoteType.SLIDER, 89086, CurveType.PERFECT, listOf(43f to 271f, 60f to 224f), 2, 100f, 2),
        Note(148f, 280f, 89584, NoteType.CIRCLE, comboColor = 2),
        Note(168f, 180f, 90081, NoteType.CIRCLE, comboColor = 3),
        Note(248f, 240f, 90579, NoteType.CIRCLE, comboColor = 3),
        Note(272f, 144f, 91076, NoteType.CIRCLE, comboColor = 3),
        Note(256f, 192f, 92071, NoteType.SPINNER, 94060, comboColor = 0),
        Note(168f, 112f, 96049, NoteType.CIRCLE, comboColor = 1),
        Note(260f, 68f, 96546, NoteType.CIRCLE, comboColor = 1),
        Note(248f, 168f, 97044, NoteType.CIRCLE, comboColor = 1),
        Note(439f, 123f, 98038, NoteType.SLIDER, 98535, CurveType.PERFECT, listOf(456f to 170f, 443f to 218f), 1, 100f, 2),
        Note(384f, 299f, 99033, NoteType.SLIDER, 99530, CurveType.PERFECT, listOf(348f to 333f, 299f to 344f), 1, 100f, 2),
        Note(304f, 248f, 100027, NoteType.CIRCLE, comboColor = 2),
        Note(263f, 155f, 100525, NoteType.SLIDER, 101519, CurveType.PERFECT, listOf(209f to 76f, 113f to 74f), 1, 200f, 3),
        Note(164f, 164f, 102016, NoteType.SLIDER, 103010, CurveType.LINEAR, listOf(48f to 327f), 1, 200f, 3),
        Note(256f, 72f, 106989, NoteType.SLIDER, 107486, CurveType.LINEAR, listOf(256f to 172f), 1, 100f, 0),
        Note(256f, 336f, 110968, NoteType.SLIDER, 111465, CurveType.LINEAR, listOf(256f to 236f), 1, 100f, 1),
        Note(52f, 236f, 119919, NoteType.SLIDER, 120416, CurveType.LINEAR, listOf(147f to 265f), 1, 100f, 2),
        Note(164f, 168f, 120914, NoteType.SLIDER, 121411, CurveType.LINEAR, listOf(251f to 118f), 1, 100f, 2),
        Note(296f, 208f, 121908, NoteType.SLIDER, 122902, CurveType.LINEAR, listOf(392f to 234f), 2, 100f, 2),
        Note(356f, 128f, 123400, NoteType.CIRCLE, comboColor = 2),
        Note(254f, 106f, 123897, NoteType.CIRCLE, comboColor = 3),
        Note(174f, 167f, 124395, NoteType.CIRCLE, comboColor = 3),
        Note(207f, 261f, 124892, NoteType.CIRCLE, comboColor = 3),
        Note(307f, 259f, 125389, NoteType.CIRCLE, comboColor = 3),
        Note(339f, 163f, 125887, NoteType.SLIDER, 127378, CurveType.PERFECT, listOf(397f to 287f, 279f to 356f), 1, 300f, 0),
        Note(300f, 256f, 127876, NoteType.SLIDER, 128870, CurveType.LINEAR, listOf(204f to 225f), 2, 100f, 0),
        Note(292f, 156f, 129368, NoteType.CIRCLE, comboColor = 0),
        Note(219f, 83f, 129865, NoteType.SLIDER, 130859, CurveType.PERFECT, listOf(134f to 40f, 50f to 87f), 1, 200f, 1),
        Note(136f, 140f, 131357, NoteType.CIRCLE, comboColor = 1),
        Note(60f, 204f, 131854, NoteType.SLIDER, 132351, CurveType.PERFECT, listOf(100f to 232f, 149f to 240f), 1, 100f, 1),
        Note(227f, 176f, 132849, NoteType.SLIDER, 133346, CurveType.PERFECT, listOf(277f to 177f, 320f to 202f), 1, 100f, 1),
        Note(348f, 299f, 133843, NoteType.SLIDER, 135832, CurveType.PERFECT, listOf(412f to 227f, 392f to 133f), 2, 200f, 2),
        Note(252f, 324f, 136330, NoteType.CIRCLE, comboColor = 2),
        Note(155f, 295f, 136827, NoteType.SLIDER, 137821, CurveType.PERFECT, listOf(91f to 223f, 111f to 129f), 1, 200f, 2),
        Note(136f, 176f, 139811, NoteType.CIRCLE, comboColor = 3),
        Note(208f, 108f, 140308, NoteType.CIRCLE, comboColor = 3),
        Note(308f, 108f, 140805, NoteType.CIRCLE, comboColor = 3),
        Note(376f, 180f, 141303, NoteType.CIRCLE, comboColor = 3),
        Note(342f, 276f, 141800, NoteType.SLIDER, 142794, CurveType.PERFECT, listOf(256f to 319f, 169f to 277f), 1, 200f, 3),
        Note(256f, 192f, 143789, NoteType.SPINNER, 146276, comboColor = 0),
        Note(256f, 68f, 147767, NoteType.CIRCLE, comboColor = 1),
        Note(308f, 156f, 148265, NoteType.CIRCLE, comboColor = 1),
        Note(208f, 156f, 148762, NoteType.CIRCLE, comboColor = 1),
        Note(12f, 108f, 149757, NoteType.SLIDER, 151248, CurveType.BEZIER, listOf(92f to 237f, 55f to 237f, 55f to 237f, 71f to 203f, 152f to 334f), 1, 300f, 1),
        Note(252f, 312f, 151746, NoteType.CIRCLE, comboColor = 2),
        Note(331f, 252f, 152243, NoteType.SLIDER, 153734, CurveType.PERFECT, listOf(339f to 116f, 204f to 100f), 1, 300f, 2),
        Note(256f, 140f, 155724, NoteType.CIRCLE, comboColor = 3),
        Note(308f, 228f, 156221, NoteType.CIRCLE, comboColor = 3),
        Note(208f, 228f, 156719, NoteType.CIRCLE, comboColor = 3),
        Note(65f, 175f, 157713, NoteType.SLIDER, 159702, CurveType.BEZIER, listOf(256f to 44f, 437f to 169f), 1, 400f, 0),
        Note(368f, 239f, 160200, NoteType.SLIDER, 161691, CurveType.PERFECT, listOf(255f to 322f, 143f to 238f), 1, 300f, 0),
        Note(172f, 80f, 167659, NoteType.CIRCLE, comboColor = 1),
        Note(96f, 144f, 168157, NoteType.CIRCLE, comboColor = 1),
        Note(192f, 168f, 168654, NoteType.SLIDER, 169151, CurveType.LINEAR, listOf(285f to 203f), 1, 100f, 1),
        Note(384f, 196f, 169648, NoteType.CIRCLE, comboColor = 2),
        Note(342f, 287f, 170146, NoteType.CIRCLE, comboColor = 2),
        Note(285f, 203f, 170643, NoteType.CIRCLE, comboColor = 2),
        Note(208f, 268f, 171140, NoteType.SLIDER, 171471, CurveType.LINEAR, listOf(154f to 307f), 1, 66.6666666666667f, 2),
        Note(256f, 192f, 172632, NoteType.SPINNER, 175616, comboColor = 3)
    )

    override fun getBPM() = 60000.0 / timingPoints.first().beatLength
}