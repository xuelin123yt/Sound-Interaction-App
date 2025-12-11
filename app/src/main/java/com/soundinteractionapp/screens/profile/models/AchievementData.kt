package com.soundinteractionapp.screens.profile.models

/**
 * 成就資料模型
 * 用於表示遊戲中的成就系統
 */
data class Achievement(
    val id: Int,
    val iconResId: Int,
    val name: String,
    val description: String,
    val isUnlocked: Boolean = false,
    val unlockedDate: String = ""
)

/**
 * 成就資料提供者
 * 集中管理所有成就資料
 */
object AchievementProvider {
    fun getAllAchievements(): List<Achievement> {
        return listOf(
            Achievement(
                id = 1,
                iconResId = com.soundinteractionapp.R.drawable.achievement_01,
                name = "群星，我的歸宿",
                description = "開啟法諾銀河之旅\n恭祝喜！你是上了賊船到達！",
                isUnlocked = true,
                unlockedDate = "2023-04-26"
            ),
            Achievement(
                id = 2,
                iconResId = com.soundinteractionapp.R.drawable.achievement_02,
                name = "永冬城之夜",
                description = "在遼天雪幕之後\n終於來到了名為貝洛伯格的城市",
                isUnlocked = true,
                unlockedDate = "2023-04-26"
            ),
            Achievement(
                id = 3,
                iconResId = com.soundinteractionapp.R.drawable.achievement_03,
                name = "失落的世界",
                description = "發現難利海-V地下的秘密\n這裡有人生活在地下……",
                isUnlocked = true,
                unlockedDate = "2023-04-26"
            ),
            Achievement(
                id = 4,
                iconResId = com.soundinteractionapp.R.drawable.achievement_04,
                name = "1/2線野仙蹤",
                description = "擊敗鐵皮和小女孩\n史瓦羅與克拉拉成為你的同伴了！",
                isUnlocked = true,
                unlockedDate = "2023-04-27"
            ),
            Achievement(
                id = 5,
                iconResId = com.soundinteractionapp.R.drawable.achievement_05,
                name = "漂冬將近",
                description = "從我做起，改變貝洛伯格\n第一步，就是打倒大守護者可可莉亞……",
                isUnlocked = true,
                unlockedDate = "2023-04-27"
            ),
            Achievement(
                id = 6,
                iconResId = com.soundinteractionapp.R.drawable.achievement_06,
                name = "仙舟「羅浮」",
                description = "抵達新的世界",
                isUnlocked = false,
                unlockedDate = ""
            ),
            Achievement(
                id = 7,
                iconResId = com.soundinteractionapp.R.drawable.achievement_07,
                name = "雲騎的榮光",
                description = "協助雲騎軍平定星核危機",
                isUnlocked = false,
                unlockedDate = ""
            ),
            Achievement(
                id = 8,
                iconResId = com.soundinteractionapp.R.drawable.achievement_08,
                name = "終章",
                description = "完成主線劇情",
                isUnlocked = false,
                unlockedDate = ""
            )
        )
    }
}