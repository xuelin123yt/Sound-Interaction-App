package com.soundinteractionapp.screens.game.levels.level4.beatmaps

import com.soundinteractionapp.R
import com.soundinteractionapp.screens.game.levels.level4.Note
import com.soundinteractionapp.screens.game.levels.level4.NoteType
import com.soundinteractionapp.screens.game.levels.level4.CurveType
import com.soundinteractionapp.screens.game.levels.level4.models.TimingPoint

object OSU_04 : Beatmap {

    // ⚠️ TODO: 請更新此 ID 為正確的 Beatmap ID
    override val id = 4  // ⚠️ 請檢查並修改
    override val title = "伴隨著你 (純音樂版)"
    override val description = "Joe Hisaishi"

    // ⚠️ TODO: 請確保音頻文件已添加到 res/raw/ 目錄
    // 預期文件名: kimi_wo_nosete_instrumental.mp3 (原始: t.mp3)
    // ⚠️ 請檢查 R.raw.kimi_wo_nosete_instrumental 是否存在，否則請修改為正確的資源名稱
    override val audioResId = R.raw.osu_04  // ⚠️ 請檢查此資源是否存在

    // TODO: 請更新為實際的封面和背景圖片資源
    override val coverImageResId = R.drawable.osu_04  // 預設圖片
    override val backgroundImageResId = R.drawable.osu_04  // 預設圖片

    override val sliderMultiplier = 1.2f
    override val preempt = 1320L
    override val fadeIn = 880L
    override val hitWindowPerfect = 110L
    override val hitWindowGreat = 180L
    override val hitWindowGood = 250L
    override val quickTapGrace = 500L
    override val offset = 0L
    override val audioLeadIn = 2000L

    override val timingPoints = listOf(
        TimingPoint(259, 645.161290322581, 35, false),
        TimingPoint(88000, -100.0, 45, true),
        TimingPoint(101226, -100.0, 35, true),
        TimingPoint(101388, -100.0, 45, true),
        TimingPoint(113807, -100.0, 45, true),
        TimingPoint(124452, -100.0, 35, true),
        TimingPoint(124613, -100.0, 45, true),
        TimingPoint(129613, -100.0, 35, true),
        TimingPoint(129775, -100.0, 45, true),
        TimingPoint(130904, -100.0, 35, true),
        TimingPoint(131065, -100.0, 45, true),
        TimingPoint(133484, -100.0, 35, true),
        TimingPoint(133646, -100.0, 45, true),
        TimingPoint(134452, -100.0, 45, true),
        TimingPoint(142517, -100.0, 35, true),
        TimingPoint(142678, -100.0, 45, true),
        TimingPoint(145097, -100.0, 35, true),
        TimingPoint(145258, -100.0, 45, true),
        TimingPoint(147678, -100.0, 35, true),
        TimingPoint(147839, -100.0, 45, true),
        TimingPoint(148968, -100.0, 35, true),
        TimingPoint(149129, -100.0, 45, true),
        TimingPoint(152678, -100.0, 35, true),
        TimingPoint(153162, -100.0, 25, true),
        TimingPoint(153807, -100.0, 15, true),
        TimingPoint(154452, -100.0, 5, true)
    )

    // 音符數據 (共 141 個)
    override val notes = listOf(
        Note(248f, 93f, 259, NoteType.SLIDER, 904, CurveType.BEZIER, listOf(320f to 71f, 365f to 128f), 1, 120f, 0),
        Note(390f, 169f, 1226, NoteType.CIRCLE, comboColor = 0),
        Note(362f, 285f, 1871, NoteType.SLIDER, 3161, CurveType.BEZIER, listOf(397f to 236f, 494f to 250f), 2, 120f, 0),
        Note(203f, 318f, 5420, NoteType.SLIDER, 6065, CurveType.BEZIER, listOf(166f to 249f, 84f to 263f), 1, 120f, 1),
        Note(45f, 268f, 6388, NoteType.CIRCLE, comboColor = 1),
        Note(108f, 166f, 7033, NoteType.SLIDER, 8323, CurveType.BEZIER, listOf(182f to 158f, 202f to 84f), 2, 120f, 1),
        Note(317f, 76f, 10581, NoteType.SLIDER, 10903, CurveType.BEZIER, listOf(385f to 65f), 1, 60f, 2),
        Note(399f, 121f, 11226, NoteType.SLIDER, 11548, CurveType.BEZIER, listOf(467f to 110f), 1, 60f, 2),
        Note(399f, 216f, 12194, NoteType.SLIDER, 12839, CurveType.BEZIER, listOf(467f to 205f), 2, 60f, 2),
        Note(329f, 313f, 13484, NoteType.CIRCLE, comboColor = 2),
        Note(188f, 334f, 15742, NoteType.SLIDER, 16064, CurveType.BEZIER, listOf(121f to 308f), 1, 60f, 3),
        Note(158f, 257f, 16388, NoteType.SLIDER, 16710, CurveType.BEZIER, listOf(91f to 231f), 1, 60f, 3),
        Note(203f, 171f, 17355, NoteType.SLIDER, 18000, CurveType.BEZIER, listOf(136f to 145f), 2, 60f, 3),
        Note(256f, 192f, 18323, NoteType.SPINNER, 20904, comboColor = 0),
        Note(365f, 148f, 21871, NoteType.SLIDER, 22193, CurveType.BEZIER, listOf(404f to 163f, 440f to 148f), 1, 60f, 1),
        Note(411f, 272f, 22839, NoteType.SLIDER, 23484, CurveType.BEZIER, listOf(456f to 264f, 472f to 227f), 2, 60f, 1),
        Note(310f, 338f, 24129, NoteType.SLIDER, 24451, CurveType.BEZIER, listOf(268f to 346f, 236f to 322f), 1, 60f, 2),
        Note(133f, 354f, 25097, NoteType.SLIDER, 25419, CurveType.BEZIER, listOf(109f to 321f, 108f to 280f), 1, 60f, 2),
        Note(49f, 301f, 25742, NoteType.SLIDER, 26064, CurveType.BEZIER, listOf(47f to 255f, 72f to 229f), 1, 60f, 2),
        Note(91f, 126f, 26710, NoteType.SLIDER, 27355, CurveType.BEZIER, listOf(50f to 124f, 25f to 148f), 2, 60f, 3),
        Note(210f, 132f, 28000, NoteType.SLIDER, 28322, CurveType.BEZIER, listOf(251f to 135f, 276f to 110f), 1, 60f, 3),
        Note(311f, 77f, 28646, NoteType.SLIDER, 28968, CurveType.BEZIER, listOf(349f to 91f, 390f to 74f), 1, 60f, 0),
        Note(313f, 186f, 29613, NoteType.CIRCLE, comboColor = 0),
        Note(364f, 218f, 29936, NoteType.CIRCLE, comboColor = 0),
        Note(423f, 212f, 30259, NoteType.SLIDER, 30904, CurveType.BEZIER, listOf(446f to 194f, 452f to 154f), 2, 60f, 0),
        Note(441f, 269f, 31226, NoteType.SLIDER, 31548, CurveType.BEZIER, listOf(420f to 304f, 375f to 304f), 1, 60f, 1),
        Note(280f, 261f, 32194, NoteType.SLIDER, 32516, CurveType.BEZIER, listOf(284f to 301f, 254f to 333f), 1, 60f, 1),
        Note(145f, 312f, 33162, NoteType.SLIDER, 33807, CurveType.BEZIER, listOf(175f to 339f, 174f to 379f), 2, 60f, 1),
        Note(49f, 240f, 34452, NoteType.SLIDER, 35097, CurveType.BEZIER, listOf(39f to 270f, 7f to 288f), 2, 60f, 2),
        Note(41f, 180f, 35420, NoteType.SLIDER, 35742, CurveType.BEZIER, listOf(68f to 151f, 106f to 141f), 1, 60f, 2),
        Note(142f, 117f, 36065, NoteType.SLIDER, 36387, CurveType.BEZIER, listOf(180f to 136f, 220f to 132f), 1, 60f, 2),
        Note(308f, 81f, 37033, NoteType.SLIDER, 37678, CurveType.BEZIER, listOf(347f to 84f, 379f to 56f), 2, 60f, 3),
        Note(397f, 160f, 38323, NoteType.SLIDER, 38968, CurveType.BEZIER, listOf(437f to 141f, 438f to 100f), 2, 60f, 3),
        Note(476f, 250f, 39613, NoteType.SLIDER, 39935, CurveType.BEZIER, listOf(439f to 260f, 405f to 244f), 1, 60f, 0),
        Note(406f, 368f, 40581, NoteType.SLIDER, 40903, CurveType.BEZIER, listOf(368f to 349f, 361f to 308f), 1, 60f, 0),
        Note(307f, 338f, 41226, NoteType.SLIDER, 41871, CurveType.BEZIER, listOf(295f to 295f, 265f to 264f), 2, 60f, 0),
        Note(187f, 324f, 42517, NoteType.SLIDER, 42839, CurveType.BEZIER, listOf(154f to 345f, 111f to 336f), 1, 60f, 1),
        Note(64f, 238f, 43484, NoteType.SLIDER, 44129, CurveType.BEZIER, listOf(49f to 274f, 5f to 287f), 2, 60f, 1),
        Note(136f, 142f, 44775, NoteType.SLIDER, 45097, CurveType.BEZIER, listOf(94f to 150f, 62f to 126f), 1, 60f, 2),
        Note(158f, 47f, 45743, NoteType.SLIDER, 46065, CurveType.BEZIER, listOf(195f to 42f, 228f to 76f), 1, 60f, 2),
        Note(255f, 107f, 46388, NoteType.SLIDER, 46710, CurveType.BEZIER, listOf(288f to 125f, 330f to 110f), 1, 60f, 2),
        Note(431f, 96f, 47355, NoteType.SLIDER, 48000, CurveType.BEZIER, listOf(466f to 88f, 488f to 51f), 2, 60f, 3),
        Note(475f, 206f, 48646, NoteType.SLIDER, 48968, CurveType.BEZIER, listOf(434f to 203f, 409f to 228f), 1, 60f, 3),
        Note(387f, 272f, 49292, NoteType.SLIDER, 49614, CurveType.BEZIER, listOf(349f to 258f, 308f to 275f), 1, 60f, 0),
        Note(230f, 337f, 50259, NoteType.SLIDER, 50581, CurveType.BEZIER, listOf(193f to 317f, 151f to 329f), 1, 60f, 0),
        Note(112f, 319f, 50905, NoteType.SLIDER, 51550, CurveType.BEZIER, listOf(84f to 346f, 52f to 346f), 2, 60f, 0),
        Note(148f, 271f, 51872, NoteType.SLIDER, 52194, CurveType.BEZIER, listOf(150f to 234f, 138f to 198f), 1, 60f, 1),
        Note(32f, 162f, 52840, NoteType.SLIDER, 53162, CurveType.BEZIER, listOf(28f to 122f, 58f to 90f), 1, 60f, 1),
        Note(157f, 60f, 53808, NoteType.SLIDER, 54453, CurveType.BEZIER, listOf(160f to 100f, 185f to 133f), 2, 60f, 1),
        Note(276f, 57f, 55097, NoteType.SLIDER, 55419, CurveType.BEZIER, listOf(316f to 79f, 352f to 73f), 1, 60f, 2),
        Note(445f, 31f, 56066, NoteType.SLIDER, 56388, CurveType.BEZIER, listOf(478f to 50f, 481f to 86f), 1, 60f, 2),
        Note(461f, 134f, 56711, NoteType.SLIDER, 57033, CurveType.BEZIER, listOf(455f to 175f, 481f to 207f), 1, 60f, 2),
        Note(456f, 309f, 57679, NoteType.SLIDER, 58324, CurveType.BEZIER, listOf(418f to 296f, 400f to 258f), 2, 60f, 3),
        Note(343f, 347f, 58969, NoteType.SLIDER, 59614, CurveType.BEZIER, listOf(313f to 313f, 328f to 275f), 2, 60f, 3),
        Note(238f, 288f, 60259, NoteType.SLIDER, 60581, CurveType.BEZIER, listOf(241f to 249f, 269f to 223f), 1, 60f, 0),
        Note(145f, 269f, 61227, NoteType.SLIDER, 61872, CurveType.BEZIER, listOf(128f to 301f, 93f to 312f), 2, 60f, 0),
        Note(80f, 88f, 64129, NoteType.CIRCLE, comboColor = 1),   // 左側
        Note(144f, 88f, 64775, NoteType.CIRCLE, comboColor = 1),  // 右側
        Note(291f, 108f, 65742, NoteType.SLIDER, 66064, CurveType.BEZIER, listOf(333f to 112f, 367f to 85f), 1, 60f, 2),
        Note(467f, 125f, 66710, NoteType.SLIDER, 67032, CurveType.BEZIER, listOf(454f to 161f, 460f to 200f), 1, 60f, 2),
        Note(481f, 239f, 67355, NoteType.SLIDER, 67677, CurveType.BEZIER, listOf(455f to 260f, 448f to 307f), 1, 60f, 3),
        Note(338f, 330f, 68323, NoteType.SLIDER, 68645, CurveType.BEZIER, listOf(301f to 329f, 274f to 348f), 1, 60f, 3),
        Note(168f, 300f, 69291, NoteType.SLIDER, 69613, CurveType.BEZIER, listOf(140f to 325f, 95f to 313f), 1, 60f, 3),
        Note(91f, 259f, 69936, NoteType.SLIDER, 70258, CurveType.BEZIER, listOf(44f to 258f, 26f to 228f), 1, 60f, 0),
        Note(103f, 140f, 70904, NoteType.SLIDER, 71226, CurveType.BEZIER, listOf(79f to 102f, 80f to 72f), 1, 60f, 0),
        Note(200f, 80f, 71871, NoteType.SLIDER, 72193, CurveType.BEZIER, listOf(225f to 42f, 260f to 36f), 1, 60f, 0),
        Note(292f, 75f, 72516, NoteType.SLIDER, 72838, CurveType.BEZIER, listOf(336f to 67f, 374f to 77f), 1, 60f, 1),
        Note(421f, 170f, 73484, NoteType.SLIDER, 73806, CurveType.BEZIER, listOf(436f to 200f, 430f to 238f), 1, 60f, 1),
        Note(395f, 276f, 74129, NoteType.SLIDER, 74451, CurveType.BEZIER, listOf(392f to 321f, 350f to 340f), 1, 60f, 1),
        Note(249f, 335f, 75097, NoteType.SLIDER, 75742, CurveType.BEZIER, listOf(174f to 353f, 112f to 308f), 1, 120f, 2),
        Note(46f, 237f, 76388, NoteType.SLIDER, 77033, CurveType.BEZIER, listOf(110f to 210f, 170f to 243f), 1, 120f, 2),
        Note(107f, 132f, 77678, NoteType.SLIDER, 78323, CurveType.BEZIER, listOf(176f to 154f, 232f to 111f), 1, 120f, 3),
        Note(279f, 97f, 78646, NoteType.SLIDER, 78968, CurveType.BEZIER, listOf(315f to 108f, 345f to 103f), 1, 60f, 3),
        Note(396f, 90f, 79291, NoteType.SLIDER, 79613, CurveType.BEZIER, listOf(439f to 90f, 464f to 118f), 1, 60f, 3),
        Note(371f, 195f, 80259, NoteType.SLIDER, 80904, CurveType.BEZIER, listOf(425f to 234f, 433f to 301f), 1, 120f, 0),
        Note(331f, 359f, 81549, NoteType.SLIDER, 82839, CurveType.BEZIER, listOf(277f to 320f, 269f to 253f), 2, 120f, 0),
        Note(256f, 192f, 83162, NoteType.SPINNER, 86710, comboColor = 1),
        Note(317f, 117f, 87355, NoteType.SLIDER, 88000, CurveType.BEZIER, listOf(355f to 126f, 397f to 112f), 2, 60f, 2),
        Note(368f, 225f, 88646, NoteType.SLIDER, 89291, CurveType.BEZIER, listOf(408f to 213f, 425f to 186f), 2, 60f, 2),
        Note(442f, 318f, 89936, NoteType.SLIDER, 90258, CurveType.BEZIER, listOf(402f to 325f, 364f to 300f), 1, 60f, 2),
        Note(323f, 315f, 90581, NoteType.SLIDER, 90903, CurveType.BEZIER, listOf(284f to 314f, 256f to 338f), 1, 60f, 3),
        Note(152f, 292f, 91549, NoteType.SLIDER, 91871, CurveType.BEZIER, listOf(121f to 299f, 105f to 333f), 1, 60f, 3),
        Note(38f, 230f, 92517, NoteType.SLIDER, 93162, CurveType.BEZIER, listOf(19f to 256f, 24f to 297f), 2, 60f, 3),
        Note(97f, 125f, 93807, NoteType.SLIDER, 94129, CurveType.BEZIER, listOf(77f to 97f, 80f to 68f), 1, 60f, 0),
        Note(160f, 53f, 94775, NoteType.CIRCLE, comboColor = 0),  // 圓圈往左移
        Note(199f, 53f, 95097, NoteType.SLIDER, 95419, CurveType.BEZIER, listOf(243f to 62f, 256f to 84f), 1, 60f, 0),
        Note(310f, 91f, 95742, NoteType.SLIDER, 96064, CurveType.BEZIER, listOf(350f to 92f, 376f to 66f), 1, 60f, 1),
        Note(416f, 185f, 96710, NoteType.SLIDER, 97355, CurveType.BEZIER, listOf(450f to 162f, 452f to 128f), 2, 60f, 1),
        Note(385f, 237f, 97678, NoteType.SLIDER, 98323, CurveType.BEZIER, listOf(402f to 270f, 437f to 279f), 2, 60f, 1),
        Note(274f, 282f, 98968, NoteType.SLIDER, 99613, CurveType.BEZIER, listOf(306f to 288f, 341f to 314f), 2, 60f, 2),
        Note(155f, 301f, 100259, NoteType.SLIDER, 100581, CurveType.BEZIER, listOf(123f to 293f, 98f to 308f), 1, 60f, 2),
        Note(40f, 287f, 100904, NoteType.SLIDER, 101549, CurveType.BEZIER, listOf(56f to 228f, 28f to 157f), 1, 120f, 3),
        Note(151f, 154f, 102194, NoteType.SLIDER, 102516, CurveType.BEZIER, listOf(136f to 126f, 104f to 112f), 1, 60f, 3),
        Note(96f, 54f, 102839, NoteType.SLIDER, 103484, CurveType.BEZIER, listOf(56f to 52f, 25f to 68f), 2, 60f, 3),
        Note(212f, 80f, 104129, NoteType.SLIDER, 104451, CurveType.BEZIER, listOf(253f to 74f, 274f to 42f), 1, 60f, 0),
        Note(323f, 66f, 104775, NoteType.SLIDER, 105420, CurveType.BEZIER, listOf(364f to 74f, 398f to 67f), 2, 60f, 0),
        Note(318f, 185f, 106065, NoteType.SLIDER, 106387, CurveType.BEZIER, listOf(364f to 176f, 388f to 184f), 1, 60f, 0),
        Note(410f, 231f, 106710, NoteType.SLIDER, 107032, CurveType.BEZIER, listOf(440f to 233f, 464f to 265f), 1, 60f, 0),
        Note(358f, 324f, 107678, NoteType.CIRCLE, comboColor = 0),
        Note(300f, 339f, 108000, NoteType.SLIDER, 108645, CurveType.BEZIER, listOf(265f to 351f, 230f to 338f), 2, 60f, 0),
        Note(172f, 113f, 113162, NoteType.SLIDER, 113807, CurveType.BEZIER, listOf(138f to 127f, 101f to 118f), 2, 60f, 1),
        Note(117f, 219f, 114452, NoteType.SLIDER, 115097, CurveType.BEZIER, listOf(71f to 215f, 40f to 184f), 2, 60f, 1),
        Note(63f, 326f, 115742, NoteType.SLIDER, 116064, CurveType.BEZIER, listOf(105f to 327f, 134f to 303f), 1, 60f, 1),
        Note(178f, 293f, 116388, NoteType.SLIDER, 116710, CurveType.BEZIER, listOf(216f to 304f, 231f to 332f), 1, 60f, 2),
        Note(346f, 327f, 117355, NoteType.SLIDER, 117677, CurveType.BEZIER, listOf(401f to 328f, 421f to 307f), 1, 60f, 2),
        Note(438f, 268f, 118000, NoteType.SLIDER, 118322, CurveType.BEZIER, listOf(469f to 238f, 499f to 237f), 1, 60f, 2),
        Note(407f, 150f, 118968, NoteType.SLIDER, 119290, CurveType.BEZIER, listOf(380f to 126f, 376f to 92f), 1, 60f, 3),
        Note(257f, 109f, 119936, NoteType.SLIDER, 120258, CurveType.BEZIER, listOf(254f to 72f, 243f to 43f), 1, 60f, 3),
        Note(125f, 54f, 120904, NoteType.SLIDER, 121549, CurveType.BEZIER, listOf(120f to 79f, 138f to 112f), 2, 60f, 3),
        Note(65f, 49f, 121871, NoteType.SLIDER, 122516, CurveType.BEZIER, listOf(40f to 80f, 2f to 85f), 2, 60f, 0),
        Note(30f, 163f, 123162, NoteType.SLIDER, 123807, CurveType.BEZIER, listOf(67f to 161f, 93f to 185f), 2, 60f, 0),
        Note(35f, 222f, 124129, NoteType.SLIDER, 124774, CurveType.BEZIER, listOf(22f to 299f, 73f to 340f), 1, 120f, 1),
        Note(181f, 304f, 125420, NoteType.SLIDER, 125742, CurveType.BEZIER, listOf(220f to 291f, 256f to 308f), 1, 60f, 1),
        Note(357f, 326f, 126388, NoteType.SLIDER, 126710, CurveType.BEZIER, listOf(400f to 319f, 416f to 288f), 1, 60f, 1),
        Note(424f, 240f, 127033, NoteType.SLIDER, 127678, CurveType.BEZIER, listOf(461f to 237f, 492f to 246f), 2, 60f, 2),
        Note(364f, 135f, 128323, NoteType.SLIDER, 128968, CurveType.BEZIER, listOf(399f to 119f, 408f to 84f), 2, 60f, 2),
        Note(304f, 125f, 129291, NoteType.SLIDER, 129936, CurveType.BEZIER, listOf(260f to 72f, 182f to 77f), 1, 120f, 3),
        Note(85f, 115f, 130581, NoteType.SLIDER, 131226, CurveType.BEZIER, listOf(129f to 168f, 207f to 163f), 1, 120f, 3),
        Note(89f, 227f, 131871, NoteType.SLIDER, 132193, CurveType.BEZIER, listOf(105f to 260f, 91f to 287f), 1, 60f, 0),
        Note(59f, 335f, 132517, NoteType.SLIDER, 132839, CurveType.BEZIER, listOf(95f to 337f, 118f to 358f), 1, 60f, 0),
        Note(172f, 339f, 133162, NoteType.SLIDER, 133807, CurveType.BEZIER, listOf(237f to 367f, 306f to 324f), 1, 120f, 0),
        Note(407f, 324f, 134452, NoteType.SLIDER, 134774, CurveType.BEZIER, listOf(421f to 289f, 412f to 251f), 1, 60f, 1),
        Note(322f, 187f, 135420, NoteType.SLIDER, 135742, CurveType.BEZIER, listOf(351f to 167f, 390f to 175f), 1, 60f, 1),
        Note(352f, 56f, 136388, NoteType.SLIDER, 137033, CurveType.BEZIER, listOf(397f to 66f, 414f to 96f), 2, 60f, 1),
        Note(240f, 99f, 137678, NoteType.SLIDER, 138000, CurveType.BEZIER, listOf(195f to 109f, 164f to 88f), 1, 60f, 2),
        Note(60f, 97f, 138646, NoteType.SLIDER, 138968, CurveType.BEZIER, listOf(52f to 132f, 63f to 162f), 1, 60f, 2),
        Note(34f, 210f, 139291, NoteType.SLIDER, 139613, CurveType.BEZIER, listOf(54f to 245f, 48f to 279f), 1, 60f, 2),
        Note(156f, 321f, 140259, NoteType.SLIDER, 140904, CurveType.BEZIER, listOf(126f to 350f, 87f to 350f), 2, 60f, 3),
        Note(274f, 302f, 141549, NoteType.SLIDER, 141871, CurveType.BEZIER, listOf(315f to 300f, 345f to 320f), 1, 60f, 3),
        Note(392f, 307f, 142194, NoteType.SLIDER, 142839, CurveType.BEZIER, listOf(372f to 238f, 404f to 167f), 1, 120f, 0),
        Note(452f, 83f, 143484, NoteType.SLIDER, 143806, CurveType.BEZIER, listOf(415f to 71f, 379f to 85f), 1, 60f, 0),
        Note(337f, 102f, 144129, NoteType.SLIDER, 144451, CurveType.BEZIER, listOf(299f to 112f, 271f to 86f), 1, 60f, 0),
        Note(224f, 69f, 144775, NoteType.SLIDER, 145420, CurveType.BEZIER, listOf(151f to 64f, 104f to 118f), 1, 120f, 1),
        Note(55f, 211f, 146065, NoteType.SLIDER, 146710, CurveType.BEZIER, listOf(92f to 219f, 121f to 190f), 2, 60f, 1),
        Note(115f, 314f, 147355, NoteType.SLIDER, 148000, CurveType.BEZIER, listOf(186f to 310f, 247f to 352f), 1, 120f, 2),
        Note(317f, 259f, 148646, NoteType.SLIDER, 149291, CurveType.BEZIER, listOf(246f to 263f, 185f to 221f), 1, 120f, 2),
        Note(271f, 133f, 149936, NoteType.SLIDER, 150258, CurveType.BEZIER, listOf(307f to 122f, 344f to 140f), 1, 60f, 3),
        Note(409f, 223f, 150904, NoteType.SLIDER, 151226, CurveType.BEZIER, listOf(437f to 235f, 458f to 256f), 1, 60f, 3),
        Note(451f, 316f, 151549, NoteType.SLIDER, 151871, CurveType.BEZIER, listOf(422f to 338f, 388f to 340f), 1, 60f, 3),
        Note(298f, 269f, 152517, NoteType.CIRCLE, comboColor = 3),
        Note(256f, 192f, 152839, NoteType.SPINNER, 155097, comboColor = 0)
    )

    override fun getBPM() = 60000.0 / timingPoints.first().beatLength
}