package com.soundinteractionapp.screens.game.levels.level4.beatmaps

import com.soundinteractionapp.R
import com.soundinteractionapp.screens.game.levels.level4.Note
import com.soundinteractionapp.screens.game.levels.level4.NoteType
import com.soundinteractionapp.screens.game.levels.level4.CurveType
import com.soundinteractionapp.screens.game.levels.level4.models.TimingPoint

object OSU_05 : Beatmap {

    // ⚠️ TODO: 請更新此 ID 為正確的 Beatmap ID
    override val id = 5  // ⚠️ 請檢查並修改
    override val title = "能看見海的街道"
    override val description = "Joe Hisaishi"

    // ⚠️ TODO: 請確保音頻文件已添加到 res/raw/ 目錄
    // 預期文件名: umi_no_mieru_machi.mp3 (原始: audio.mp3)
    // ⚠️ 請檢查 R.raw.umi_no_mieru_machi 是否存在，否則請修改為正確的資源名稱
    override val audioResId = R.raw.osu_05  // ⚠️ 請檢查此資源是否存在

    // TODO: 請更新為實際的封面和背景圖片資源
    override val coverImageResId = R.drawable.osu_05  // 預設圖片
    override val backgroundImageResId = R.drawable.osu_05  // 預設圖片

    override val sliderMultiplier = 1.3f
    override val preempt = 1380L
    override val fadeIn = 920L
    override val hitWindowPerfect = 110L
    override val hitWindowGreat = 180L
    override val hitWindowGood = 250L
    override val quickTapGrace = 500L
    override val offset = 0L
    override val audioLeadIn = 0L

    override val timingPoints = listOf(
        TimingPoint(738, 722.89156626506, 40, false)
    )

    // 音符數據 (共 113 個)
    override val notes = listOf(
        Note(61f, 103f, 738, NoteType.SLIDER, 1460, CurveType.PERFECT, listOf(116f to 57f, 171f to 57f), 1, 130f, 1),
        Note(246f, 96f, 1822, NoteType.CIRCLE, comboColor = 1),
        Note(312f, 148f, 2183, NoteType.SLIDER, 2544, CurveType.PERFECT, listOf(352f to 154f, 400f to 145f), 1, 65f, 1),
        Note(461f, 158f, 2906, NoteType.SLIDER, 3267, CurveType.LINEAR, listOf(463f to 261f), 1, 65f, 1),
        Note(465f, 307f, 3629, NoteType.SLIDER, 4351, CurveType.PERFECT, listOf(432f to 361f, 340f to 371f), 1, 130f, 2),
        Note(293f, 344f, 4713, NoteType.CIRCLE, comboColor = 2),
        Note(212f, 370f, 5075, NoteType.SLIDER, 5436, CurveType.PERFECT, listOf(158f to 370f, 128f to 360f), 1, 65f, 2),
        Note(63f, 357f, 5798, NoteType.CIRCLE, comboColor = 2),
        Note(38f, 276f, 6159, NoteType.CIRCLE, comboColor = 2),
        Note(87f, 207f, 6521, NoteType.SLIDER, 7243, CurveType.LINEAR, listOf(93f to 67f), 1, 130f, 3),
        Note(153f, 18f, 7605, NoteType.CIRCLE, comboColor = 3),
        Note(216f, 73f, 7966, NoteType.SLIDER, 8327, CurveType.PERFECT, listOf(276f to 91f, 325f to 75f), 1, 65f, 3),
        Note(349f, 47f, 8689, NoteType.SLIDER, 9050, CurveType.PERFECT, listOf(409f to 29f, 458f to 45f), 1, 65f, 3),
        Note(470f, 90f, 9412, NoteType.SLIDER, 10134, CurveType.BEZIER, listOf(491f to 140f, 491f to 140f, 479f to 230f), 1, 130f, 4),
        Note(408f, 259f, 10497, NoteType.CIRCLE, comboColor = 4),
        Note(338f, 210f, 10858, NoteType.SLIDER, 11580, CurveType.LINEAR, listOf(198f to 208f), 1, 130f, 4),
        Note(129f, 176f, 11942, NoteType.CIRCLE, comboColor = 4),
        Note(69f, 235f, 12304, NoteType.SLIDER, 13026, CurveType.PERFECT, listOf(53f to 328f, 74f to 367f), 1, 130f, 5),
        Note(140f, 316f, 13388, NoteType.CIRCLE, comboColor = 5),
        Note(212f, 360f, 13750, NoteType.SLIDER, 14472, CurveType.PERFECT, listOf(228f to 267f, 207f to 228f), 1, 130f, 5),
        Note(90f, 122f, 15195, NoteType.SLIDER, 15917, CurveType.PERFECT, listOf(91f to 45f, 149f to 1f), 1, 130f, 6),
        Note(209f, 22f, 16280, NoteType.CIRCLE, comboColor = 6),
        Note(284f, 61f, 16641, NoteType.SLIDER, 17363, CurveType.BEZIER, listOf(350f to 94f, 350f to 94f, 413f to 88f), 1, 130f, 6),
        Note(499f, 229f, 18087, NoteType.SLIDER, 18448, CurveType.LINEAR, listOf(508f to 338f), 1, 65f, 7),
        Note(429f, 332f, 18810, NoteType.SLIDER, 19171, CurveType.LINEAR, listOf(423f to 267f), 1, 65f, 7),
        Note(348f, 227f, 19533, NoteType.SLIDER, 20255, CurveType.PERFECT, listOf(250f to 208f, 204f to 234f), 1, 130f, 7),
        Note(73f, 286f, 20978, NoteType.SLIDER, 21700, CurveType.PERFECT, listOf(48f to 200f, 78f to 152f), 1, 130f, 8),
        Note(99f, 90f, 22063, NoteType.CIRCLE, comboColor = 8),
        Note(172f, 131f, 22424, NoteType.SLIDER, 22785, CurveType.LINEAR, listOf(183f to 220f), 1, 65f, 8),
        Note(264f, 199f, 23147, NoteType.SLIDER, 23508, CurveType.LINEAR, listOf(275f to 110f), 1, 65f, 8),
        Note(335f, 78f, 23870, NoteType.SLIDER, 24592, CurveType.PERFECT, listOf(408f to 115f, 414f to 208f), 1, 130f, 0),
        Note(395f, 236f, 24954, NoteType.CIRCLE, comboColor = 0),
        Note(320f, 276f, 25316, NoteType.SLIDER, 26038, CurveType.PERFECT, listOf(247f to 239f, 241f to 146f), 1, 130f, 0),
        Note(185f, 34f, 26762, NoteType.SLIDER, 27484, CurveType.PERFECT, listOf(150f to 13f, 59f to 24f), 1, 130f, 1),
        Note(30f, 98f, 27846, NoteType.CIRCLE, comboColor = 1),
        Note(15f, 181f, 28207, NoteType.SLIDER, 28929, CurveType.BEZIER, listOf(5f to 248f, 5f to 248f, 29f to 328f), 1, 130f, 1),
        Note(165f, 217f, 29653, NoteType.SLIDER, 30014, CurveType.PERFECT, listOf(209f to 201f, 264f to 214f), 1, 65f, 2),
        Note(278f, 267f, 30376, NoteType.SLIDER, 30737, CurveType.PERFECT, listOf(322f to 283f, 377f to 270f), 1, 65f, 2),
        Note(423f, 278f, 31099, NoteType.SLIDER, 31821, CurveType.LINEAR, listOf(434f to 113f), 1, 130f, 2),
        Note(377f, 83f, 32183, NoteType.CIRCLE, comboColor = 2),
        Note(311f, 29f, 32545, NoteType.SLIDER, 33267, CurveType.PERFECT, listOf(260f to 16f, 179f to 35f), 1, 130f, 3),
        Note(115f, 78f, 33629, NoteType.CIRCLE, comboColor = 3),
        Note(81f, 155f, 33991, NoteType.SLIDER, 34352, CurveType.LINEAR, listOf(76f to 220f), 1, 65f, 3),
        Note(122f, 289f, 34713, NoteType.CIRCLE, comboColor = 3),
        Note(197f, 251f, 35075, NoteType.CIRCLE, comboColor = 3),
        Note(184f, 168f, 35436, NoteType.SLIDER, 36158, CurveType.PERFECT, listOf(249f to 122f, 315f to 164f), 1, 130f, 4),
        Note(370f, 166f, 36521, NoteType.CIRCLE, comboColor = 4),
        Note(410f, 240f, 36882, NoteType.SLIDER, 37604, CurveType.LINEAR, listOf(486f to 235f), 2, 65f, 4),
        Note(373f, 316f, 37966, NoteType.CIRCLE, comboColor = 4),
        Note(295f, 281f, 38328, NoteType.SLIDER, 39050, CurveType.PERFECT, listOf(229f to 258f, 126f to 314f), 1, 130f, 5),
        Note(108f, 327f, 39412, NoteType.CIRCLE, comboColor = 5),
        Note(37f, 280f, 39774, NoteType.SLIDER, 40496, CurveType.LINEAR, listOf(40f to 138f), 1, 130f, 5),
        Note(82f, 76f, 40858, NoteType.CIRCLE, comboColor = 5),
        Note(159f, 115f, 41219, NoteType.SLIDER, 41941, CurveType.PERFECT, listOf(190f to 122f, 222f to 122f), 2, 65f, 6),
        Note(82f, 76f, 42304, NoteType.CIRCLE, comboColor = 6),
        Note(30f, 142f, 42665, NoteType.SLIDER, 43387, CurveType.PERFECT, listOf(17f to 216f, 54f to 284f), 1, 130f, 6),
        Note(119f, 243f, 43750, NoteType.CIRCLE, comboColor = 6),
        Note(185f, 295f, 44111, NoteType.SLIDER, 44833, CurveType.LINEAR, listOf(189f to 405f), 2, 65f, 7),
        Note(254f, 246f, 45195, NoteType.CIRCLE, comboColor = 7),
        Note(325f, 292f, 45557, NoteType.SLIDER, 46279, CurveType.PERFECT, listOf(391f to 307f, 460f to 281f), 1, 130f, 7),
        Note(471f, 207f, 46641, NoteType.CIRCLE, comboColor = 7),
        Note(405f, 153f, 47003, NoteType.SLIDER, 47725, CurveType.LINEAR, listOf(397f to 10f), 1, 130f, 8),
        Note(318f, 52f, 48087, NoteType.CIRCLE, comboColor = 8),
        Note(242f, 14f, 48448, NoteType.SLIDER, 49170, CurveType.PERFECT, listOf(180f to 24f, 124f to 88f), 1, 130f, 8),
        Note(96f, 145f, 49533, NoteType.CIRCLE, comboColor = 8),
        Note(151f, 208f, 49894, NoteType.SLIDER, 50255, CurveType.LINEAR, listOf(151f to 308f), 1, 65f, 0),
        Note(175f, 353f, 50617, NoteType.CIRCLE, comboColor = 0),
        Note(244f, 304f, 50978, NoteType.CIRCLE, comboColor = 0),
        Note(316f, 346f, 51340, NoteType.SLIDER, 52062, CurveType.BEZIER, listOf(376f to 355f, 376f to 355f, 431f to 314f), 1, 130f, 0),
        Note(410f, 231f, 52424, NoteType.CIRCLE, comboColor = 0),
        Note(345f, 178f, 52786, NoteType.SLIDER, 53508, CurveType.LINEAR, listOf(347f to 95f), 2, 65f, 1),
        Note(272f, 221f, 53870, NoteType.CIRCLE, comboColor = 1),
        Note(200f, 265f, 54231, NoteType.SLIDER, 54953, CurveType.PERFECT, listOf(114f to 284f, 64f to 258f), 1, 130f, 1),
        Note(28f, 199f, 55316, NoteType.CIRCLE, comboColor = 1),
        Note(81f, 133f, 55677, NoteType.SLIDER, 56399, CurveType.PERFECT, listOf(114f to 66f, 220f to 63f), 1, 130f, 2),
        Note(247f, 51f, 56762, NoteType.CIRCLE, comboColor = 2),
        Note(277f, 129f, 57123, NoteType.SLIDER, 57845, CurveType.PERFECT, listOf(396f to 137f, 426f to 119f), 1, 130f, 2),
        Note(446f, 62f, 58207, NoteType.CIRCLE, comboColor = 2),
        Note(498f, 128f, 58569, NoteType.SLIDER, 59291, CurveType.LINEAR, listOf(502f to 280f), 1, 130f, 3),
        Note(438f, 314f, 59653, NoteType.CIRCLE, comboColor = 3),
        Note(363f, 273f, 60015, NoteType.SLIDER, 60737, CurveType.LINEAR, listOf(362f to 191f), 2, 65f, 3),
        Note(304f, 333f, 61099, NoteType.CIRCLE, comboColor = 3),
        Note(220f, 343f, 61460, NoteType.SLIDER, 62182, CurveType.PERFECT, listOf(136f to 321f, 73f to 350f), 1, 130f, 4),
        Note(42f, 268f, 62545, NoteType.CIRCLE, comboColor = 4),
        Note(106f, 212f, 62906, NoteType.SLIDER, 63628, CurveType.LINEAR, listOf(118f to 55f), 1, 130f, 4),
        Note(174f, 20f, 63991, NoteType.CIRCLE, comboColor = 4),
        Note(210f, 96f, 64352, NoteType.SLIDER, 65074, CurveType.PERFECT, listOf(260f to 120f, 339f to 104f), 1, 130f, 5),
        Note(386f, 43f, 65436, NoteType.CIRCLE, comboColor = 5),
        Note(452f, 94f, 65798, NoteType.SLIDER, 66159, CurveType.LINEAR, listOf(454f to 204f), 1, 65f, 5),
        Note(388f, 214f, 66521, NoteType.SLIDER, 66882, CurveType.LINEAR, listOf(389f to 301f), 1, 65f, 5),
        Note(307f, 255f, 67244, NoteType.SLIDER, 67966, CurveType.LINEAR, listOf(306f to 158f), 2, 65f, 6),
        Note(278f, 334f, 68328, NoteType.CIRCLE, comboColor = 6),
        Note(195f, 350f, 68689, NoteType.SLIDER, 69411, CurveType.BEZIER, listOf(132f to 361f, 132f to 361f, 45f to 321f), 1, 130f, 6),
        Note(39f, 254f, 69774, NoteType.CIRCLE, comboColor = 6),
        Note(77f, 178f, 70135, NoteType.SLIDER, 70496, CurveType.LINEAR, listOf(77f to 65f), 1, 65f, 7),
        Note(159f, 93f, 70858, NoteType.SLIDER, 71219, CurveType.LINEAR, listOf(159f to 206f), 1, 65f, 7),
        Note(207f, 227f, 71581, NoteType.SLIDER, 72303, CurveType.PERFECT, listOf(266f to 270f, 361f to 243f), 1, 130f, 7),
        Note(384f, 213f, 72665, NoteType.CIRCLE, comboColor = 7),
        Note(413f, 133f, 73027, NoteType.SLIDER, 73388, CurveType.LINEAR, listOf(410f to 56f), 1, 65f, 8),
        Note(492f, 90f, 73750, NoteType.SLIDER, 74111, CurveType.LINEAR, listOf(495f to 167f), 1, 65f, 8),
        Note(463f, 233f, 74472, NoteType.SLIDER, 75194, CurveType.PERFECT, listOf(369f to 297f, 328f to 289f), 1, 130f, 8),
        Note(285f, 255f, 75557, NoteType.CIRCLE, comboColor = 8),
        Note(231f, 321f, 75918, NoteType.SLIDER, 76640, CurveType.LINEAR, listOf(70f to 316f), 1, 130f, 0),
        Note(27f, 274f, 77003, NoteType.CIRCLE, comboColor = 0),
        Note(36f, 190f, 77364, NoteType.SLIDER, 78086, CurveType.PERFECT, listOf(93f to 95f, 148f to 89f), 1, 130f, 0),
        Note(171f, 142f, 78448, NoteType.CIRCLE, comboColor = 0),
        Note(221f, 74f, 78810, NoteType.SLIDER, 79532, CurveType.LINEAR, listOf(360f to 76f), 1, 130f, 1),
        Note(429f, 107f, 79894, NoteType.CIRCLE, comboColor = 1),
        Note(483f, 172f, 80256, NoteType.SLIDER, 80978, CurveType.PERFECT, listOf(497f to 255f, 473f to 310f), 1, 130f, 1),
        Note(323f, 355f, 81701, NoteType.SLIDER, 82423, CurveType.LINEAR, listOf(181f to 362f), 1, 130f, 2),
        Note(118f, 320f, 82786, NoteType.CIRCLE, comboColor = 2),
        Note(156f, 245f, 83147, NoteType.CIRCLE, comboColor = 2),
        Note(256f, 192f, 83509, NoteType.SPINNER, 87484, comboColor = 3)
    )

    override fun getBPM() = 60000.0 / timingPoints.first().beatLength
}