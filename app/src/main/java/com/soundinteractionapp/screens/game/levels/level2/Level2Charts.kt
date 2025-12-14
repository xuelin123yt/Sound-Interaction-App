package com.soundinteractionapp.screens.game.levels.level2

// MusicNote 定義
data class MusicNote(val timeMs: Long, val lane: Int)

object Level2Charts {

    // ==========================================
    // 1. 天空之城 (Castle in the Sky) - 簡單模式
    // ==========================================
    private val castleRawSeconds = listOf(
        2.267574, 2.539683, 6.938776, 7.528345, 10.521542, 14.693878, 19.002268, 19.410431,
        19.682540, 20.816327, 21.133787, 21.950113, 22.811791, 25.170068, 25.804989, 26.802721,
        27.210884, 27.981859, 28.843537, 31.111111, 31.882086, 32.653061, 33.333333, 34.104308,
        34.920635, 37.142857, 37.777778, 38.911565, 39.410431, 40.136054, 40.907029, 42.585034,
        43.446712, 43.764172, 44.217687, 45.124717, 45.532880, 45.850340, 46.439909, 47.210884,
        48.843537, 49.569161, 50.385488, 51.383220, 51.746032, 52.380952, 53.197279, 55.646259,
        56.326531, 57.006803, 57.324263, 58.412698, 59.138322, 59.863946, 60.272109, 62.267574,
        62.585034, 62.811791, 63.446712, 64.217687, 65.124717, 67.482993, 67.891156, 68.208617,
        69.387755, 69.795918, 70.476190, 71.247166, 72.698413, 73.287982, 74.013605, 75.056689,
        75.555556, 76.054422, 76.780045, 79.863946, 80.544218, 80.770975, 81.179138, 81.451247,
        81.814059, 82.539683, 83.628118, 85.487528, 85.804989, 86.802721, 87.482993, 88.390023,
        89.614512, 89.886621, 90.249433, 90.702948, 91.020408, 91.746032, 92.154195, 92.471655,
        93.151927, 93.741497, 94.285714, 95.510204, 96.961451, 97.324263, 97.641723, 98.095238,
        100.317460, 100.907029, 101.269841, 102.403628, 102.857143, 104.399093, 104.988662, 105.714286,
        107.210884, 108.662132, 109.115646, 109.478458, 111.655329, 112.335601, 112.607710, 113.877551,
        114.648526, 117.097506, 117.732426, 118.140590, 119.410431, 119.954649, 120.816327, 121.814059,
        125.079365, 125.668934, 125.895692, 126.167800, 126.394558, 126.666667, 127.709751, 128.072562,
        128.843537, 129.342404, 129.931973, 130.839002
    )

    // 自動轉換: 秒 -> 毫秒 + 分配軌道
    val castle: List<MusicNote> by lazy {
        val chart = mutableListOf<MusicNote>()
        var lastTimeMs = -1L
        var laneIndex = 0

        castleRawSeconds.forEachIndexed { index, seconds ->
            val timeMs = (seconds * 1000).toLong()

            if (timeMs == lastTimeMs) {
                // 如果時間相同(和弦)，切換軌道
                laneIndex = (laneIndex + 1) % 4
            } else {
                // 如果時間不同，切換軌道並加一點隨機性
                laneIndex = (laneIndex + 1) % 4
                if (index % 4 == 0) laneIndex = (laneIndex + 2) % 4
            }
            chart.add(MusicNote(timeMs, laneIndex))
            lastTimeMs = timeMs
        }
        // 結束標記
        if (chart.isNotEmpty()) {
            chart.add(MusicNote(chart.last().timeMs + 3000, 4))
        }
        chart
    }

    // ==========================================
    // 2. 龍貓 (Totoro) - 普通模式
    // ==========================================
    private val totoroRawSeconds = listOf(
        2.222222, 3.219955, 3.446712, 3.945578, 4.489796, 5.124717, 7.120181, 7.755102,
        8.027211, 8.662132, 9.206349, 9.886621, 13.287982, 13.560091, 13.968254, 14.195011,
        15.691610, 16.281179, 16.462585, 16.643991, 18.866213, 19.546485, 21.904762, 22.448980,
        23.038549, 23.310658, 26.575964, 27.165533, 27.709751, 28.027211, 30.657596, 31.201814,
        31.791383, 32.380952, 32.743764, 33.514739, 34.149660, 34.739229, 35.328798, 36.190476,
        36.507937, 37.732426, 38.548753, 38.911565, 40.634921, 41.269841, 41.814059, 42.131519,
        43.628118, 45.396825, 45.941043, 46.485261, 46.802721, 49.478458, 50.022676, 50.249433,
        50.612245, 50.929705, 51.247166, 51.564626, 53.333333, 53.605442, 53.968254, 54.240363,
        54.512472, 54.829932, 55.374150, 55.873016, 56.235828, 58.866213, 59.455782, 60.045351,
        60.634921, 60.952381, 61.678005, 61.859410, 62.403628, 62.675737, 62.993197, 63.310658,
        67.619048, 67.845805, 68.072562, 68.344671, 68.888889, 69.206349, 69.841270, 70.340136,
        70.929705, 73.015873, 73.605442, 73.922902, 74.512472, 75.102041, 75.646259, 77.732426,
        78.866213, 79.365079, 80.045351, 80.680272, 80.952381, 82.448980, 82.721088, 83.038549,
        83.356009, 83.628118, 83.945578, 84.217687, 84.535147, 85.986395, 86.258503, 86.575964,
        86.893424, 87.165533, 87.755102, 88.027211, 88.571429, 89.251701, 89.750567, 91.836735,
        92.471655, 92.743764, 93.333333, 93.922902, 94.467120, 96.780045, 97.188209, 97.732426,
        98.004535, 98.321995, 98.594104, 98.911565, 99.818594, 100.861678, 101.224490, 101.496599,
        101.814059, 102.131519, 102.448980, 102.766440, 103.083900, 103.356009, 106.757370, 107.074830,
        107.392290, 107.709751, 108.027211, 108.344671, 109.886621, 110.204082, 110.430839, 111.247166,
        111.882086, 113.605442, 114.149660, 114.739229, 115.102041, 117.777778, 118.367347, 118.730159,
        119.455782, 120.045351, 120.680272, 121.179138, 122.494331, 122.947846, 123.310658, 123.628118,
        123.945578, 124.761905, 125.714286, 127.301587, 127.755102, 128.163265, 128.662132, 129.160998,
        129.750567, 131.791383, 132.426304, 132.698413, 133.378685, 133.968254, 134.512472, 136.598639,
        137.142857, 137.777778, 138.276644, 138.911565, 139.455782, 139.637188, 139.909297, 140.907029,
        141.224490, 141.496599, 141.859410, 142.176871, 142.448980, 142.766440, 143.038549, 143.356009,
        143.673469, 144.852608, 145.124717, 145.396825, 145.668934, 145.986395, 146.575964, 146.848073,
        147.437642, 148.027211, 148.571429, 150.657596, 151.292517, 151.564626, 152.154195, 152.743764,
        153.333333, 155.419501, 155.963719, 156.553288, 156.825397, 157.142857, 157.460317, 157.777778,
        158.594104, 159.138322, 159.818594, 160.090703, 160.362812, 160.680272, 160.997732, 161.315193,
        161.587302, 161.904762, 162.176871, 162.494331, 163.582766, 163.809524, 163.990930, 164.217687,
        164.489796, 164.807256, 165.895692, 166.848073, 167.437642, 169.433107, 170.113379, 170.385488,
        170.975057, 171.564626, 172.154195, 174.285714, 174.875283, 175.238095, 175.691610, 176.235828,
        176.780045, 178.911565, 179.546485, 179.773243, 180.453515, 180.997732, 181.541950, 184.081633,
        184.535147, 185.034014, 185.668934, 186.213152, 188.344671, 188.888889, 189.160998, 189.841270,
        190.385488, 190.929705, 193.015873, 193.605442, 193.922902, 194.467120, 195.056689, 195.646259,
        197.732426, 198.276644, 198.594104, 199.229025, 199.818594, 200.317460, 202.494331, 203.038549,
        204.217687, 204.535147, 204.761905, 206.213152, 206.485261, 206.802721, 207.165533, 209.387755,
        209.614512, 210.204082, 212.834467, 213.061224, 213.287982, 213.469388, 213.696145, 213.922902,
        214.195011, 214.421769, 214.693878, 214.965986, 215.238095, 215.555556, 216.825397
    )

    val totoro: List<MusicNote> by lazy {
        val chart = mutableListOf<MusicNote>()
        var lastTimeMs = -1L
        var laneIndex = 0

        totoroRawSeconds.forEachIndexed { index, seconds ->
            val timeMs = (seconds * 1000).toLong()
            if (timeMs == lastTimeMs) {
                laneIndex = (laneIndex + 1) % 4
            } else {
                laneIndex = (laneIndex + 1) % 4
                if (index % 5 == 0) laneIndex = (laneIndex + 2) % 4
            }
            chart.add(MusicNote(timeMs, laneIndex))
            lastTimeMs = timeMs
        }
        if (chart.isNotEmpty()) {
            chart.add(MusicNote(chart.last().timeMs + 2000, 4))
        }
        chart
    }

    // ==========================================
    // 3. Maria - 困難模式
    // ==========================================
    val maria: List<MusicNote> = listOf(
        MusicNote(6908, 0), MusicNote(7633, 1), MusicNote(7951, 3), MusicNote(8132, 2),
        MusicNote(8767, 0), MusicNote(9039, 1), MusicNote(9266, 2), MusicNote(9493, 3),
        MusicNote(9765, 1), MusicNote(9992, 0), MusicNote(10853, 2), MusicNote(11126, 3),
        MusicNote(11352, 1), MusicNote(11624, 0), MusicNote(11851, 2), MusicNote(12486, 3),
        MusicNote(12758, 1), MusicNote(12985, 0), MusicNote(13257, 2), MusicNote(13484, 3),
        MusicNote(14527, 0), MusicNote(14799, 1), MusicNote(15026, 2), MusicNote(15298, 3),
        MusicNote(15525, 1), MusicNote(16341, 0), MusicNote(16522, 2), MusicNote(16749, 3),
        MusicNote(16976, 1), MusicNote(17203, 0), MusicNote(17429, 2), MusicNote(18291, 3),
        MusicNote(18563, 1), MusicNote(18790, 0), MusicNote(19062, 2), MusicNote(19289, 3),
        MusicNote(20060, 0), MusicNote(20287, 1), MusicNote(20513, 2), MusicNote(20740, 3),
        MusicNote(20967, 1), MusicNote(22101, 0), MusicNote(22327, 2), MusicNote(22554, 3),
        MusicNote(22781, 1), MusicNote(23008, 0), MusicNote(23688, 2), MusicNote(23915, 3),
        MusicNote(24141, 1), MusicNote(24413, 0), MusicNote(24640, 2), MusicNote(24867, 3),
        MusicNote(25683, 0), MusicNote(26001, 1), MusicNote(26273, 2), MusicNote(26454, 3),
        MusicNote(26726, 1), MusicNote(27407, 0), MusicNote(27633, 2), MusicNote(27906, 3),
        MusicNote(28132, 1), MusicNote(28359, 0), MusicNote(29448, 2), MusicNote(29992, 3),
        MusicNote(30445, 1), MusicNote(30853, 0), MusicNote(30989, 2), MusicNote(31262, 3),
        MusicNote(32441, 0), MusicNote(32713, 1), MusicNote(32940, 2), MusicNote(33212, 3),
        MusicNote(33665, 1), MusicNote(34391, 0), MusicNote(34618, 2), MusicNote(36205, 3),
        MusicNote(36432, 1), MusicNote(36658, 0), MusicNote(36931, 2), MusicNote(37429, 3),
        MusicNote(38064, 0), MusicNote(38291, 1), MusicNote(38563, 2), MusicNote(38790, 3),
        MusicNote(39516, 1), MusicNote(39969, 0), MusicNote(40196, 2), MusicNote(40649, 3),
        MusicNote(41057, 1), MusicNote(41556, 0), MusicNote(42010, 2), MusicNote(42463, 3),
        MusicNote(42735, 1), MusicNote(42962, 0), MusicNote(43234, 2), MusicNote(44368, 3),
        MusicNote(44822, 0), MusicNote(45003, 1), MusicNote(45275, 2), MusicNote(45547, 3),
        MusicNote(45774, 1), MusicNote(46001, 0), MusicNote(46318, 2), MusicNote(46545, 3),
        MusicNote(46772, 1), MusicNote(46999, 0), MusicNote(47225, 2), MusicNote(47543, 3),
        MusicNote(47724, 1), MusicNote(48178, 0), MusicNote(48404, 2), MusicNote(48631, 3),
        MusicNote(48858, 1), MusicNote(49085, 0), MusicNote(49448, 2), MusicNote(49901, 3),
        MusicNote(50128, 1), MusicNote(50400, 0), MusicNote(50627, 2), MusicNote(50899, 3),
        MusicNote(51352, 1), MusicNote(51579, 0), MusicNote(51896, 2), MusicNote(52123, 3),
        MusicNote(52350, 1), MusicNote(52577, 0), MusicNote(52804, 2), MusicNote(53030, 3),
        MusicNote(53257, 1), MusicNote(53484, 0), MusicNote(53711, 2), MusicNote(53937, 3),
        MusicNote(54164, 1), MusicNote(54391, 0), MusicNote(54572, 2), MusicNote(54890, 3),
        MusicNote(55116, 1), MusicNote(55343, 0), MusicNote(55615, 2), MusicNote(55842, 3),
        MusicNote(56023, 1), MusicNote(56386, 0), MusicNote(56568, 2), MusicNote(56885, 3),
        MusicNote(57021, 1), MusicNote(57384, 0), MusicNote(57565, 2), MusicNote(57792, 3),
        MusicNote(59924, 0), MusicNote(60241, 1), MusicNote(60649, 2), MusicNote(61103, 3),
        MusicNote(61330, 1), MusicNote(61556, 0), MusicNote(62237, 2), MusicNote(62509, 3),
        MusicNote(62735, 1), MusicNote(62962, 0), MusicNote(63234, 2), MusicNote(63461, 3),
        MusicNote(64051, 1), MusicNote(64368, 0), MusicNote(64595, 2), MusicNote(64822, 3),
        MusicNote(65048, 1), MusicNote(65275, 0), MusicNote(66001, 2), MusicNote(66228, 3),
        MusicNote(66454, 1), MusicNote(66681, 0), MusicNote(66953, 2), MusicNote(67225, 3),
        MusicNote(67996, 1), MusicNote(68586, 0), MusicNote(68813, 2), MusicNote(69085, 3),
        MusicNote(69720, 1), MusicNote(69992, 0), MusicNote(70218, 2), MusicNote(70445, 3),
        MusicNote(70672, 1), MusicNote(70899, 0), MusicNote(71579, 2), MusicNote(71806, 3),
        MusicNote(72078, 1), MusicNote(72305, 0), MusicNote(72531, 2), MusicNote(72758, 3),
        MusicNote(72985, 1), MusicNote(73212, 0), MusicNote(73348, 2), MusicNote(73665, 3),
        MusicNote(74164, 1), MusicNote(74300, 0), MusicNote(74618, 2), MusicNote(74754, 3),
        MusicNote(75116, 1), MusicNote(75615, 0), MusicNote(76023, 2), MusicNote(76568, 3),
        MusicNote(76931, 1), MusicNote(77384, 0), MusicNote(77838, 2), MusicNote(78246, 3),
        MusicNote(78745, 1), MusicNote(78926, 0), MusicNote(79153, 2), MusicNote(79379, 3),
        MusicNote(79697, 1), MusicNote(80196, 0), MusicNote(80649, 2), MusicNote(81103, 3),
        MusicNote(81330, 1), MusicNote(81919, 0), MusicNote(82146, 2), MusicNote(82327, 3),
        MusicNote(82554, 1), MusicNote(82781, 0), MusicNote(83008, 2), MusicNote(83416, 3),
        MusicNote(83960, 1), MusicNote(84368, 0), MusicNote(84822, 2), MusicNote(85094, 3),
        MusicNote(85683, 1), MusicNote(85865, 0), MusicNote(86137, 2), MusicNote(86364, 3),
        MusicNote(86545, 1), MusicNote(86772, 0), MusicNote(87225, 2), MusicNote(87679, 3),
        MusicNote(88087, 1), MusicNote(88586, 0), MusicNote(88813, 2), MusicNote(89311, 3),
        MusicNote(89538, 1), MusicNote(89765, 0), MusicNote(89992, 2), MusicNote(90400, 3),
        MusicNote(90536, 1), MusicNote(90989, 0), MusicNote(91488, 2), MusicNote(91851, 3),
        MusicNote(91987, 1), MusicNote(92395, 0), MusicNote(92577, 2), MusicNote(93302, 3),
        MusicNote(93484, 1), MusicNote(93711, 0), MusicNote(93937, 2), MusicNote(94164, 3),
        MusicNote(94436, 1), MusicNote(95116, 0), MusicNote(95343, 2), MusicNote(95933, 3),
        MusicNote(96160, 1), MusicNote(96341, 0), MusicNote(96885, 2), MusicNote(97112, 3),
        MusicNote(97339, 1), MusicNote(97565, 0), MusicNote(97838, 2), MusicNote(98064, 3),
        MusicNote(98835, 1), MusicNote(98971, 0), MusicNote(99289, 2), MusicNote(99470, 3),
        MusicNote(99652, 1), MusicNote(99833, 0), MusicNote(100150, 2), MusicNote(100332, 3),
        MusicNote(100513, 1), MusicNote(100649, 0), MusicNote(100831, 2), MusicNote(101012, 3),
        MusicNote(101148, 1), MusicNote(101420, 0), MusicNote(101556, 2), MusicNote(101828, 3),
        MusicNote(102146, 1), MusicNote(102282, 0), MusicNote(102463, 2), MusicNote(102645, 3),
        MusicNote(102826, 1), MusicNote(103008, 0), MusicNote(103189, 2), MusicNote(103370, 3),
        MusicNote(103552, 1), MusicNote(103824, 0), MusicNote(104051, 2), MusicNote(104232, 3),
        MusicNote(105139, 1), MusicNote(105366, 0), MusicNote(105774, 2), MusicNote(106182, 3),
        MusicNote(106409, 1), MusicNote(106681, 0), MusicNote(106908, 2), MusicNote(107180, 3),
        MusicNote(107407, 1), MusicNote(107633, 0), MusicNote(107906, 2), MusicNote(108132, 3),
        MusicNote(108359, 1), MusicNote(108586, 0), MusicNote(108813, 2), MusicNote(109039, 3),
        MusicNote(109538, 1), MusicNote(109765, 0), MusicNote(109992, 2), MusicNote(110218, 3),
        MusicNote(110445, 1), MusicNote(110672, 0), MusicNote(110899, 2), MusicNote(111126, 3),
        MusicNote(111398, 1), MusicNote(111624, 0), MusicNote(111851, 2), MusicNote(112078, 3),
        MusicNote(112305, 1), MusicNote(112758, 0), MusicNote(112940, 2), MusicNote(113257, 3),
        MusicNote(113711, 1), MusicNote(113847, 0), MusicNote(114164, 2), MusicNote(114436, 3),
        MusicNote(114663, 1), MusicNote(114890, 0), MusicNote(115116, 2), MusicNote(115343, 3),
        MusicNote(115570, 1), MusicNote(115797, 0), MusicNote(116069, 2), MusicNote(116296, 3),
        MusicNote(116522, 1), MusicNote(116885, 0), MusicNote(117384, 2), MusicNote(117838, 3),
        MusicNote(118291, 1), MusicNote(118609, 0), MusicNote(118790, 2), MusicNote(119334, 3),
        MusicNote(119742, 1), MusicNote(120196, 0), MusicNote(120649, 2), MusicNote(120876, 3),
        MusicNote(121148, 1), MusicNote(121783, 0), MusicNote(122055, 2), MusicNote(122282, 3),
        MusicNote(122554, 1), MusicNote(122781, 0), MusicNote(123008, 2), MusicNote(123688, 3),
        MusicNote(123915, 1), MusicNote(124187, 0), MusicNote(124413, 2), MusicNote(124640, 3),
        MusicNote(124867, 1), MusicNote(125547, 0), MusicNote(125774, 2), MusicNote(126046, 3),
        MusicNote(126273, 1), MusicNote(126500, 0), MusicNote(126726, 2), MusicNote(127543, 3),
        MusicNote(128087, 1), MusicNote(128314, 0), MusicNote(128586, 2), MusicNote(129221, 3),
        MusicNote(129493, 1), MusicNote(129720, 0), MusicNote(129992, 2), MusicNote(130218, 3),
        MusicNote(130445, 1), MusicNote(131080, 0), MusicNote(131352, 2), MusicNote(131579, 3),
        MusicNote(131851, 1), MusicNote(132078, 0), MusicNote(132305, 2), MusicNote(132531, 3),
        MusicNote(132804, 1), MusicNote(133212, 0), MusicNote(133665, 2), MusicNote(133847, 3),
        MusicNote(134119, 1), MusicNote(134300, 0), MusicNote(134663, 2), MusicNote(134844, 3),
        MusicNote(135116, 1), MusicNote(135525, 0), MusicNote(136023, 2), MusicNote(136477, 3),
        MusicNote(136931, 1), MusicNote(137429, 0), MusicNote(137883, 2), MusicNote(138110, 3),
        MusicNote(138336, 1), MusicNote(138563, 0), MusicNote(138790, 2), MusicNote(139289, 3),
        MusicNote(139788, 1), MusicNote(140196, 0), MusicNote(140649, 2), MusicNote(140876, 3),
        MusicNote(141511, 1), MusicNote(141692, 0), MusicNote(141919, 2), MusicNote(142146, 3),
        MusicNote(142373, 1), MusicNote(142599, 0), MusicNote(143008, 2), MusicNote(143461, 3),
        MusicNote(143915, 1), MusicNote(144368, 0), MusicNote(144595, 2), MusicNote(145184, 3),
        MusicNote(145411, 1), MusicNote(145638, 0), MusicNote(145865, 2), MusicNote(146092, 3),
        MusicNote(146273, 1), MusicNote(146726, 0), MusicNote(147225, 2), MusicNote(147633, 3),
        MusicNote(148087, 1), MusicNote(148359, 0), MusicNote(148949, 2), MusicNote(149221, 3),
        MusicNote(149448, 1), MusicNote(149901, 0), MusicNote(150037, 2), MusicNote(150853, 3),
        MusicNote(151216, 1), MusicNote(151398, 0), MusicNote(151806, 2), MusicNote(151987, 3),
        MusicNote(152214, 1), MusicNote(152395, 0), MusicNote(152758, 2), MusicNote(153166, 3),
        MusicNote(153348, 1), MusicNote(153620, 0), MusicNote(153937, 2), MusicNote(154436, 3),
        MusicNote(155116, 1), MusicNote(155525, 0), MusicNote(155706, 2), MusicNote(156023, 3),
        MusicNote(156296, 1), MusicNote(156885, 0), MusicNote(157021, 2), MusicNote(157475, 3),
        MusicNote(157883, 1), MusicNote(158064, 0), MusicNote(158336, 2), MusicNote(159878, 3),
        MusicNote(160287, 1), MusicNote(160468, 0), MusicNote(160785, 2), MusicNote(161012, 3),
        MusicNote(161874, 1), MusicNote(162191, 0), MusicNote(162599, 2), MusicNote(162735, 3),
        MusicNote(163098, 1), MusicNote(163370, 0), MusicNote(164504, 2), MusicNote(164912, 3),
        MusicNote(165094, 1), MusicNote(165411, 0), MusicNote(165683, 2), MusicNote(166590, 3),
        MusicNote(166908, 1), MusicNote(167316, 0), MusicNote(167452, 2), MusicNote(167770, 3),
        MusicNote(168359, 1), MusicNote(168677, 0), MusicNote(168949, 2), MusicNote(169357, 3),
        MusicNote(171896, 1), MusicNote(172214, 0), MusicNote(172486, 2), MusicNote(172894, 3),
        MusicNote(173166, 1), MusicNote(173393, 0), MusicNote(174028, 2), MusicNote(174300, 3),
        MusicNote(174527, 1), MusicNote(174799, 0), MusicNote(175026, 2), MusicNote(175298, 3),
        MusicNote(175978, 1), MusicNote(176250, 0), MusicNote(176477, 2), MusicNote(176704, 3),
        MusicNote(176931, 1), MusicNote(177157, 0), MusicNote(177883, 2), MusicNote(178110, 3),
        MusicNote(178336, 1), MusicNote(178563, 0), MusicNote(178835, 2), MusicNote(179017, 3),
        MusicNote(179969, 1), MusicNote(180423, 0), MusicNote(180649, 2), MusicNote(180876, 3),
        MusicNote(181556, 1), MusicNote(181783, 0), MusicNote(182010, 2), MusicNote(182282, 3),
        MusicNote(182509, 1), MusicNote(182735, 0), MusicNote(183461, 2), MusicNote(183688, 3),
        MusicNote(183915, 1), MusicNote(184141, 0), MusicNote(184413, 2), MusicNote(184640, 3),
        MusicNote(185003, 1), MusicNote(185502, 0), MusicNote(185955, 2), MusicNote(186092, 3),
        MusicNote(186454, 1), MusicNote(186636, 0), MusicNote(186999, 2), MusicNote(187407, 3),
        MusicNote(187906, 1), MusicNote(188450, 0), MusicNote(188767, 2), MusicNote(189221, 3),
        MusicNote(189538, 1), MusicNote(190128, 0), MusicNote(190355, 2), MusicNote(190581, 3),
        MusicNote(190808, 1), MusicNote(191035, 0), MusicNote(191307, 2), MusicNote(191534, 3),
        MusicNote(192078, 1), MusicNote(192486, 0), MusicNote(192940, 2), MusicNote(193166, 3),
        MusicNote(193847, 1), MusicNote(194028, 0), MusicNote(194209, 2), MusicNote(194391, 3),
        MusicNote(194618, 1), MusicNote(194844, 0), MusicNote(195026, 2), MusicNote(195343, 3),
        MusicNote(195797, 1), MusicNote(196205, 0), MusicNote(196613, 2), MusicNote(196794, 3),
        MusicNote(197520, 1), MusicNote(197701, 0), MusicNote(197928, 2), MusicNote(198155, 3),
        MusicNote(198336, 1), MusicNote(198563, 0), MusicNote(199017, 2), MusicNote(199470, 3),
        MusicNote(199969, 1), MusicNote(200377, 0), MusicNote(200604, 2), MusicNote(201239, 3),
        MusicNote(201466, 1), MusicNote(201692, 0), MusicNote(201919, 2), MusicNote(202237, 3),
        MusicNote(202418, 1), MusicNote(202781, 0),
        MusicNote(204000, 4) // 結束標記
    )
}