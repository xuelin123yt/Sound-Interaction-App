package com.soundinteractionapp.screens.profile.models

import com.soundinteractionapp.R

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
            // ✅ 成就 1: Score Champion／高分冠軍
            Achievement(
                id = 1,
                iconResId = R.drawable.achievement_01,
                name = "Score Champion／高分冠軍",
                description = "於「模式三・關卡一」任意難度中，取得 SSS 級評價。",
                isUnlocked = false,
                unlockedDate = ""
            ),

            // ✅ 成就 2: Combo Master／連擊大師
            Achievement(
                id = 2,
                iconResId = R.drawable.achievement_02,
                name = "Combo Master／連擊大師",
                description = "於「模式三・關卡二」中，達成 100 連擊（Combo）。",
                isUnlocked = false,
                unlockedDate = ""
            ),

            // ✅ 成就 3: Voice Flight Ace／聲控飛行高手（已修正描述）
            Achievement(
                id = 3,
                iconResId = R.drawable.achievement_03,
                name = "Voice Flight Ace／聲控飛行高手",
                description = "於「模式三・關卡三」中，成功穿越障礙並達成 3000 分。",
                isUnlocked = false,
                unlockedDate = ""
            ),

            // ✅ 成就 4: Flawless Finish／無瑕結束
            Achievement(
                id = 4,
                iconResId = R.drawable.achievement_04,
                name = "Flawless Finish／無瑕結束",
                description = "於「模式三・關卡四」完成任一首歌曲，且全程未出現任何 Miss 判定。",
                isUnlocked = false,
                unlockedDate = ""
            ),

            // ✅ 成就 5: Perfect Performance／完美演出（已修正描述）
            Achievement(
                id = 5,
                iconResId = R.drawable.achievement_05,
                name = "Perfect Performance／完美演出",
                description = "於「模式三・關卡四」總分超過 30000 分。",
                isUnlocked = false,
                unlockedDate = ""
            ),

            // ✅ 成就 6: Mode Three Completionist／模式三完成者
            Achievement(
                id = 6,
                iconResId = R.drawable.achievement_06,
                name = "Mode Three Completionist／模式三完成者",
                description = "於「模式三」中，所有關卡皆至少進行一次並成功獲得一次分數。",
                isUnlocked = false,
                unlockedDate = ""
            ),

            // ✅ 成就 7: Profile Ready／頭像設定完成
            Achievement(
                id = 7,
                iconResId = R.drawable.achievement_07,
                name = "Profile Ready／頭像設定完成",
                description = "完成個人頭像設定。",
                isUnlocked = false,
                unlockedDate = ""
            ),

            // ✅ 成就 8: Feedback Contributor／回饋貢獻者
            Achievement(
                id = 8,
                iconResId = R.drawable.achievement_08,
                name = "Feedback Contributor／回饋貢獻者",
                description = "完成一次「問題與意見反饋」提交。",
                isUnlocked = false,
                unlockedDate = ""
            )
        )
    }
}