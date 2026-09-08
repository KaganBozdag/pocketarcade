package com.example.model

import androidx.compose.ui.graphics.Color
import com.example.data.GameScoreEntity
import com.example.data.UserGameScoreEntity
import com.example.ui.theme.*

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val icon: String,
    val category: String,
    val themeColor: Color,
    val maxProgress: Int,
    val evaluate: (AchievementContext) -> AchievementProgress
)

data class AchievementProgress(
    val current: Int,
    val max: Int,
    val isUnlocked: Boolean
) {
    val progressFraction: Float
        get() = if (max > 0) (current.toFloat() / max.toFloat()).coerceIn(0f, 1f) else 0f

    val percentage: Int
        get() = (progressFraction * 100).toInt()
}

data class AchievementContext(
    val bestScores: Map<String, Int>, // gameId -> highScore
    val playCounts: Map<String, Int>, // gameId -> gamesPlayed
    val isAuthenticated: Boolean,
    val totalScore: Int,
    val totalPlays: Int,
    val uniquePlayedCount: Int
)

object AchievementCatalog {

    val achievements: List<Achievement> = listOf(
        // The requested flagship achievement
        Achievement(
            id = "completion_100",
            title = "%100 Tamamlama",
            description = "Tüm oyunlarda en az 100 skor al",
            icon = "👑",
            category = "Özel & Prestij",
            themeColor = NeonAmber,
            maxProgress = 50,
            evaluate = { ctx ->
                val allGames = GameCatalog.games
                val qualifiedGamesCount = allGames.count { game ->
                    (ctx.bestScores[game.id] ?: 0) >= 100
                }
                AchievementProgress(
                    current = qualifiedGamesCount,
                    max = allGames.size,
                    isUnlocked = qualifiedGamesCount >= allGames.size
                )
            }
        ),

        // Journey & Milestones
        Achievement(
            id = "first_step",
            title = "İlk Adım",
            description = "Herhangi bir mini oyunu tamamla ve ilk skorunu kaydet",
            icon = "🎮",
            category = "Başlangıç",
            themeColor = NeonCyan,
            maxProgress = 1,
            evaluate = { ctx ->
                val hasPlayed = ctx.uniquePlayedCount >= 1 || ctx.totalPlays >= 1
                AchievementProgress(
                    current = if (hasPlayed) 1 else 0,
                    max = 1,
                    isUnlocked = hasPlayed
                )
            }
        ),

        Achievement(
            id = "explorer_10",
            title = "Arcade Çırağı",
            description = "10 farklı mini oyun oyna ve dene",
            icon = "🕹️",
            category = "Keşif",
            themeColor = NeonGreen,
            maxProgress = 10,
            evaluate = { ctx ->
                val count = ctx.uniquePlayedCount
                AchievementProgress(
                    current = minOf(count, 10),
                    max = 10,
                    isUnlocked = count >= 10
                )
            }
        ),

        Achievement(
            id = "explorer_25",
            title = "Arcade Ustası",
            description = "25 farklı mini oyunu oyna",
            icon = "🌟",
            category = "Keşif",
            themeColor = NeonPurple,
            maxProgress = 25,
            evaluate = { ctx ->
                val count = ctx.uniquePlayedCount
                AchievementProgress(
                    current = minOf(count, 25),
                    max = 25,
                    isUnlocked = count >= 25
                )
            }
        ),

        Achievement(
            id = "all_games_50",
            title = "Büyük Koleksiyoncu",
            description = "50 mini oyunun tamamını en az bir kez oyna",
            icon = "💎",
            category = "Keşif",
            themeColor = NeonPink,
            maxProgress = 50,
            evaluate = { ctx ->
                val count = ctx.uniquePlayedCount
                AchievementProgress(
                    current = minOf(count, 50),
                    max = 50,
                    isUnlocked = count >= 50
                )
            }
        ),

        // Category Maestros
        Achievement(
            id = "arcade_master",
            title = "Retro Nostalji",
            description = "Klasik Arcade kategorisindeki tüm oyunları oyna",
            icon = "👾",
            category = "Kategori",
            themeColor = NeonGreen,
            maxProgress = 12,
            evaluate = { ctx ->
                val arcadeGames = GameCatalog.games.filter { it.category == GameCategory.ARCADE }
                val played = arcadeGames.count { (ctx.playCounts[it.id] ?: 0) > 0 || (ctx.bestScores[it.id] ?: 0) > 0 }
                AchievementProgress(
                    current = played,
                    max = arcadeGames.size,
                    isUnlocked = played >= arcadeGames.size
                )
            }
        ),

        Achievement(
            id = "puzzle_master",
            title = "Zeka Küpü",
            description = "Bulmaca & Zeka kategorisindeki tüm oyunları tamamla",
            icon = "🧩",
            category = "Kategori",
            themeColor = NeonPurple,
            maxProgress = 10,
            evaluate = { ctx ->
                val puzzleGames = GameCatalog.games.filter { it.category == GameCategory.PUZZLE }
                val played = puzzleGames.count { (ctx.playCounts[it.id] ?: 0) > 0 || (ctx.bestScores[it.id] ?: 0) > 0 }
                AchievementProgress(
                    current = played,
                    max = puzzleGames.size,
                    isUnlocked = played >= puzzleGames.size
                )
            }
        ),

        Achievement(
            id = "reflex_master",
            title = "Refleks Canavarı",
            description = "Hız & Refleks kategorisindeki tüm oyunları dene",
            icon = "⚡",
            category = "Kategori",
            themeColor = NeonAmber,
            maxProgress = 10,
            evaluate = { ctx ->
                val reflexGames = GameCatalog.games.filter { it.category == GameCategory.REFLEX }
                val played = reflexGames.count { (ctx.playCounts[it.id] ?: 0) > 0 || (ctx.bestScores[it.id] ?: 0) > 0 }
                AchievementProgress(
                    current = played,
                    max = reflexGames.size,
                    isUnlocked = played >= reflexGames.size
                )
            }
        ),

        Achievement(
            id = "word_master",
            title = "Kelime & Bilgi Avcısı",
            description = "Kelime & Bilgi kategorisindeki tüm oyunları oyna",
            icon = "📚",
            category = "Kategori",
            themeColor = NeonCyan,
            maxProgress = 8,
            evaluate = { ctx ->
                val wordGames = GameCatalog.games.filter { it.category == GameCategory.WORD }
                val played = wordGames.count { (ctx.playCounts[it.id] ?: 0) > 0 || (ctx.bestScores[it.id] ?: 0) > 0 }
                AchievementProgress(
                    current = played,
                    max = wordGames.size,
                    isUnlocked = played >= wordGames.size
                )
            }
        ),

        Achievement(
            id = "online_master",
            title = "Düello Gladyatörü",
            description = "Çevrimiçi / Düello kategorisindeki tüm oyunları dene",
            icon = "🌐",
            category = "Kategori",
            themeColor = NeonPink,
            maxProgress = 10,
            evaluate = { ctx ->
                val onlineGames = GameCatalog.games.filter { it.category == GameCategory.ONLINE }
                val played = onlineGames.count { (ctx.playCounts[it.id] ?: 0) > 0 || (ctx.bestScores[it.id] ?: 0) > 0 }
                AchievementProgress(
                    current = played,
                    max = onlineGames.size,
                    isUnlocked = played >= onlineGames.size
                )
            }
        ),

        // High Score Prowess
        Achievement(
            id = "score_300",
            title = "Skor Canavarı",
            description = "Herhangi bir oyunda tek seferde 300 veya daha fazla skor yap",
            icon = "🔥",
            category = "Skor",
            themeColor = NeonAmber,
            maxProgress = 300,
            evaluate = { ctx ->
                val highestSingleScore = ctx.bestScores.values.maxOrNull() ?: 0
                AchievementProgress(
                    current = minOf(highestSingleScore, 300),
                    max = 300,
                    isUnlocked = highestSingleScore >= 300
                )
            }
        ),

        Achievement(
            id = "score_1000",
            title = "Efsanevi Rekorcu",
            description = "Herhangi bir oyunda tek seferde 1000 veya daha fazla skor yap",
            icon = "🏆",
            category = "Skor",
            themeColor = NeonGreen,
            maxProgress = 1000,
            evaluate = { ctx ->
                val highestSingleScore = ctx.bestScores.values.maxOrNull() ?: 0
                AchievementProgress(
                    current = minOf(highestSingleScore, 1000),
                    max = 1000,
                    isUnlocked = highestSingleScore >= 1000
                )
            }
        ),

        Achievement(
            id = "total_score_5000",
            title = "Milyoner Puan Kulübü",
            description = "Tüm oyunlardaki rekorlarının toplamı 5.000 puana ulaşsın",
            icon = "💰",
            category = "Skor",
            themeColor = NeonAmber,
            maxProgress = 5000,
            evaluate = { ctx ->
                val currentTotal = ctx.totalScore
                AchievementProgress(
                    current = minOf(currentTotal, 5000),
                    max = 5000,
                    isUnlocked = currentTotal >= 5000
                )
            }
        ),

        // Persistence & Plays
        Achievement(
            id = "matches_25",
            title = "Azimli Oyuncu",
            description = "Toplamda 25 maç tamamla",
            icon = "🎯",
            category = "Maraton",
            themeColor = NeonCyan,
            maxProgress = 25,
            evaluate = { ctx ->
                AchievementProgress(
                    current = minOf(ctx.totalPlays, 25),
                    max = 25,
                    isUnlocked = ctx.totalPlays >= 25
                )
            }
        ),

        Achievement(
            id = "matches_100",
            title = "Arcade Efsanesi",
            description = "Toplamda 100 maç tamamla",
            icon = "⚔️",
            category = "Maraton",
            themeColor = NeonPink,
            maxProgress = 100,
            evaluate = { ctx ->
                AchievementProgress(
                    current = minOf(ctx.totalPlays, 100),
                    max = 100,
                    isUnlocked = ctx.totalPlays >= 100
                )
            }
        ),

        Achievement(
            id = "cloud_sync",
            title = "Bulut Senkronizasyonu",
            description = "GitHub hesabınla giriş yaparak skorlarını buluta bağla",
            icon = "☁️",
            category = "Bağlantı",
            themeColor = NeonGreen,
            maxProgress = 1,
            evaluate = { ctx ->
                AchievementProgress(
                    current = if (ctx.isAuthenticated) 1 else 0,
                    max = 1,
                    isUnlocked = ctx.isAuthenticated
                )
            }
        )
    )

    fun buildContext(
        deviceScores: List<GameScoreEntity>,
        userScores: List<UserGameScoreEntity>,
        isAuthenticated: Boolean
    ): AchievementContext {
        val bestScores = mutableMapOf<String, Int>()
        val playCounts = mutableMapOf<String, Int>()

        deviceScores.forEach { s ->
            if (s.highScore > 0 || s.gamesPlayed > 0) {
                bestScores[s.gameId] = s.highScore
                playCounts[s.gameId] = s.gamesPlayed
            }
        }

        userScores.forEach { u ->
            val prevHigh = bestScores[u.gameId] ?: 0
            val prevPlays = playCounts[u.gameId] ?: 0
            bestScores[u.gameId] = maxOf(prevHigh, u.highScore)
            playCounts[u.gameId] = maxOf(prevPlays, u.gamesPlayed)
        }

        val totalScore = GameCatalog.games.sumOf { bestScores[it.id] ?: 0 }
        val totalPlays = playCounts.values.sum()
        val uniquePlayedCount = playCounts.count { it.value > 0 || (bestScores[it.key] ?: 0) > 0 }

        return AchievementContext(
            bestScores = bestScores,
            playCounts = playCounts,
            isAuthenticated = isAuthenticated,
            totalScore = totalScore,
            totalPlays = totalPlays,
            uniquePlayedCount = uniquePlayedCount
        )
    }

    fun getUnlockedCount(context: AchievementContext): Int {
        return achievements.count { it.evaluate(context).isUnlocked }
    }
}
