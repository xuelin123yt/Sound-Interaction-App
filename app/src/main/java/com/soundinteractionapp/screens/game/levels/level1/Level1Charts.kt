package com.soundinteractionapp.screens.game.levels.level1

/**
 * 音符資料結構
 */
data class Note(val id: Long, val targetTime: Long, var isHit: Boolean = false)

object Level1Charts {

    /**
     * 自動計算滿分工具
     * 公式：(前 Rush 個音符 * 100) + (剩餘音符 * 150)
     */
    fun calculateMaxScore(totalNotes: Int, rushThreshold: Int): Int {
        if (totalNotes <= rushThreshold) return totalNotes * 100
        val basePart = rushThreshold * 100
        val rushPart = (totalNotes - rushThreshold) * 150
        return basePart + rushPart
    }

    // --- 1. 給愛麗絲 (Alice) - 精確 80 Notes ---
    val LEVEL1_EASY_CHART = listOf(
        Note(1, 317), Note(3, 1179), Note(5, 1814), Note(7, 2358), Note(9, 2948),
        Note(10, 4036), Note(12, 4626), Note(13, 5760), Note(15, 6259), Note(16, 7483),
        Note(18, 8118), Note(20, 8798), Note(22, 9388), Note(24, 9887), Note(25, 10884),
        Note(27, 11383), Note(28, 12562), Note(30, 13107), Note(31, 15238), Note(33, 15873),
        Note(35, 16508), Note(37, 17098), Note(39, 17596), Note(40, 18639), Note(42, 19138),
        Note(43, 20181), Note(45, 20635), Note(46, 21814), Note(48, 22494), Note(50, 23129),
        Note(52, 23628), Note(54, 24082), Note(56, 25397), Note(58, 26712), Note(60, 27347),
        Note(61, 28345), Note(63, 28844), Note(64, 29887), Note(66, 30295), Note(68, 31429),
        Note(70, 32834), Note(72, 33424), Note(73, 34694), Note(75, 35646), Note(77, 36281),
        Note(79, 37143), Note(81, 37732), Note(83, 38277), Note(85, 38866), Note(87, 39410),
        Note(89, 40272), Note(91, 40907), Note(93, 42449), Note(95, 42948), Note(96, 43900),
        Note(98, 44444), Note(99, 45624), Note(101, 46304), Note(103, 46848), Note(105, 47347),
        Note(107, 47846), Note(108, 48707), Note(110, 49206), Note(111, 50249), Note(113, 50794),
        Note(114, 51837), Note(116, 52290), Note(117, 53379), Note(119, 53741), Note(120, 54739),
        Note(122, 55147), Note(123, 56145), Note(125, 56599), Note(126, 57778), Note(128, 58685),
        Note(130, 59592), Note(132, 60454), Note(134, 61043), Note(136, 61678), Note(138, 62132),
        Note(140, 62812), Note(142, 63537), Note(144, 64354), Note(145, 65624), Note(147, 66168),
        Note(148, 67256), Note(150, 67710), Note(151, 68934), Note(153, 69660), Note(155, 70204),
        Note(157, 70658), Note(159, 71111), Note(161, 72381), Note(163, 73515), Note(165, 74240)
    ).filterIndexed { index, _ -> index % 2 == 0 }.take(80) // 抽稀後精確取 80 顆

    // --- 2. 卡農 (Canon) - 從後面精簡至 80 Notes ---
    val LEVEL1_NORMAL_CHART = listOf(
        Note(1, 2449), Note(2, 4444), Note(3, 6667), Note(4, 8844), Note(5, 11020),
        Note(6, 13333), Note(7, 15556), Note(8, 17732), Note(9, 19955), Note(10, 22268),
        Note(11, 24580), Note(12, 26848), Note(13, 29070), Note(14, 31202), Note(15, 33424),
        Note(16, 35556), Note(17, 37914), Note(18, 40136), Note(19, 42222), Note(20, 44444),
        Note(21, 46440), Note(22, 48662), Note(23, 50748), Note(24, 52971), Note(25, 55193),
        Note(26, 56100), Note(27, 57143), Note(28, 58095), Note(29, 59138), Note(30, 60181),
        Note(31, 61088), Note(32, 62177), Note(33, 63084), Note(34, 64127), Note(35, 65079),
        Note(36, 66077), Note(37, 66893), Note(38, 67891), Note(39, 68889), Note(40, 69841),
        Note(41, 70748), Note(42, 71202), Note(43, 71701), Note(44, 72200), Note(45, 72608),
        Note(46, 73016), Note(47, 73515), Note(48, 73923), Note(49, 74422), Note(50, 74830),
        Note(51, 75238), Note(52, 75692), Note(53, 76145), Note(54, 76553), Note(55, 77007),
        Note(56, 77460), Note(57, 77914), Note(58, 78367), Note(59, 78776), Note(60, 79229),
        Note(61, 79683), Note(62, 80136), Note(63, 80590), Note(64, 80998), Note(65, 81451),
        Note(66, 81905), Note(67, 82358), Note(68, 82812), Note(69, 83311), Note(70, 83719),
        Note(71, 84127), Note(72, 84580), Note(73, 85034), Note(74, 85488), Note(75, 85896),
        Note(76, 86349), Note(77, 86803), Note(78, 87211), Note(79, 87664), Note(80, 88073),
        Note(81, 88526), Note(82, 88934), Note(83, 89342), Note(84, 89796), Note(85, 90249),
        Note(86, 90703), Note(87, 91111), Note(88, 91565), Note(89, 92018), Note(90, 92472),
        Note(91, 92880), Note(92, 93243), Note(93, 93696), Note(94, 94104), Note(95, 94558),
        Note(96, 94966)
    ).take(80) // 精確保留前 80 顆音符

    // --- 3. 土耳其進行曲 (Turca) - 精確 150 Notes ---
    val LEVEL1_HARD_CHART = listOf(
        Note(1, 1723), Note(2, 1950), Note(3, 2313), Note(5, 2630), Note(7, 3129),
        Note(9, 3447), Note(11, 3764), Note(13, 4036), Note(15, 4354), Note(16, 4762),
        Note(18, 5170), Note(20, 5578), Note(22, 6032), Note(24, 6440), Note(26, 6893),
        Note(28, 7256), Note(30, 7710), Note(31, 8118), Note(33, 8481), Note(35, 9025),
        Note(37, 9297), Note(39, 9841), Note(41, 10159), Note(43, 10476), Note(45, 10748),
        Note(47, 11111), Note(49, 11655), Note(51, 12063), Note(53, 12562), Note(55, 12971),
        Note(57, 13379), Note(59, 13787), Note(61, 14195), Note(63, 14875), Note(65, 15329),
        Note(67, 15873), Note(69, 16190), Note(71, 17052), Note(73, 17506), Note(75, 17823),
        Note(77, 18367), Note(79, 18776), Note(81, 19365), Note(83, 19637), Note(85, 20272),
        Note(87, 20907), Note(89, 21224), Note(91, 21769), Note(93, 22086), Note(95, 22540),
        Note(97, 22902), Note(99, 23401), Note(101, 23673), Note(103, 23991), Note(105, 24263),
        Note(107, 24580), Note(109, 25215), Note(111, 25624), Note(113, 26032), Note(115, 26485),
        Note(117, 26893), Note(119, 27302), Note(121, 27755), Note(123, 28481), Note(125, 28934),
        Note(127, 29342), Note(129, 29705), Note(131, 30204), Note(133, 30612), Note(135, 31020),
        Note(137, 31338), Note(139, 31927), Note(141, 32336), Note(143, 32789), Note(145, 33107),
        Note(147, 33696), Note(149, 34059), Note(151, 34467), Note(153, 34830), Note(155, 35329),
        Note(157, 35465), Note(159, 36236), Note(161, 36553), Note(163, 37098), Note(165, 37415),
        Note(167, 37732), Note(169, 38095), Note(171, 38594), Note(173, 39002), Note(175, 39410),
        Note(177, 39819), Note(179, 40272), Note(181, 40680), Note(183, 41088), Note(185, 41497),
        Note(187, 42268), Note(189, 42676), Note(191, 43129), Note(193, 43719), Note(195, 44127),
        Note(197, 44535), Note(199, 44989), Note(201, 45578), Note(203, 46259), Note(205, 46621),
        Note(207, 47256), Note(209, 47710), Note(211, 48163), Note(213, 48798), Note(215, 49252),
        Note(217, 49887), Note(219, 50476), Note(221, 50884), Note(223, 51338), Note(225, 51791),
        Note(227, 52381), Note(229, 53016), Note(231, 53469), Note(233, 53878), Note(235, 54331),
        Note(237, 54785), Note(239, 55193)
    ).filterIndexed { index, _ -> index % 3 != 0 }.take(150) // 抽稀後取前 150 顆
}