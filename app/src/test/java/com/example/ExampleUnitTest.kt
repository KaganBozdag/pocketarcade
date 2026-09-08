package com.example

import com.example.data.GameScoreEntity
import com.example.data.UserGameScoreEntity
import com.example.model.AchievementCatalog
import com.example.model.AchievementContext
import com.example.model.GameCatalog
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun test100PercentCompletionAchievement() {
    val completionAchievement = AchievementCatalog.achievements.find { it.id == "completion_100" }
    assertNotNull("completion_100 achievement must exist", completionAchievement)
    assertEquals("%100 Tamamlama", completionAchievement?.title)
    assertEquals("Tüm oyunlarda en az 100 skor al", completionAchievement?.description)

    val allGames = GameCatalog.games
    assertEquals(50, allGames.size)

    // Case 1: Empty scores -> 0 / 50, locked
    val emptyContext = AchievementContext(
      bestScores = emptyMap(),
      playCounts = emptyMap(),
      isAuthenticated = false,
      totalScore = 0,
      totalPlays = 0,
      uniquePlayedCount = 0
    )
    val emptyProgress = completionAchievement!!.evaluate(emptyContext)
    assertEquals(0, emptyProgress.current)
    assertEquals(50, emptyProgress.max)
    assertFalse(emptyProgress.isUnlocked)

    // Case 2: 49 games have score >= 100, 1 has 99 -> 49 / 50, locked
    val partialMap = allGames.dropLast(1).associate { it.id to 120 } + (allGames.last().id to 99)
    val partialContext = AchievementContext(
      bestScores = partialMap,
      playCounts = emptyMap(),
      isAuthenticated = false,
      totalScore = 0,
      totalPlays = 0,
      uniquePlayedCount = 50
    )
    val partialProgress = completionAchievement.evaluate(partialContext)
    assertEquals(49, partialProgress.current)
    assertEquals(50, partialProgress.max)
    assertFalse(partialProgress.isUnlocked)

    // Case 3: All 50 games have score >= 100 -> 50 / 50, unlocked!
    val fullMap = allGames.associate { it.id to 100 }
    val fullContext = AchievementContext(
      bestScores = fullMap,
      playCounts = emptyMap(),
      isAuthenticated = false,
      totalScore = 5000,
      totalPlays = 50,
      uniquePlayedCount = 50
    )
    val fullProgress = completionAchievement.evaluate(fullContext)
    assertEquals(50, fullProgress.current)
    assertEquals(50, fullProgress.max)
    assertTrue(fullProgress.isUnlocked)
  }

  @Test
  fun testBuildContextAndMilestones() {
    val deviceScores = listOf(
      GameScoreEntity("snake", highScore = 120, gamesPlayed = 3),
      GameScoreEntity("space", highScore = 350, gamesPlayed = 5)
    )
    val userScores = listOf(
      UserGameScoreEntity(
        compositeKey = "u1_snake",
        userId = "u1",
        username = "tester",
        avatarUrl = null,
        gameId = "snake",
        highScore = 150,
        gamesPlayed = 4
      )
    )

    val ctx = AchievementCatalog.buildContext(
      deviceScores = deviceScores,
      userScores = userScores,
      isAuthenticated = true
    )

    assertEquals(150, ctx.bestScores["snake"]) // max of 120 and 150
    assertEquals(350, ctx.bestScores["space"])
    assertEquals(4, ctx.playCounts["snake"]) // max of 3 and 4
    assertEquals(5, ctx.playCounts["space"])
    assertTrue(ctx.isAuthenticated)

    val firstStep = AchievementCatalog.achievements.find { it.id == "first_step" }!!
    assertTrue(firstStep.evaluate(ctx).isUnlocked)

    val score300 = AchievementCatalog.achievements.find { it.id == "score_300" }!!
    assertTrue(score300.evaluate(ctx).isUnlocked)

    val cloudSync = AchievementCatalog.achievements.find { it.id == "cloud_sync" }!!
    assertTrue(cloudSync.evaluate(ctx).isUnlocked)
  }

  @Test
  fun testFavoritesFilterLogic() {
    val allGames = GameCatalog.games
    val favoriteIds = setOf("snake", "pong", "tetris")

    // Filter by favorites
    val favoriteGames = allGames.filter { favoriteIds.contains(it.id) }
    assertEquals(3, favoriteGames.size)
    assertTrue(favoriteGames.any { it.id == "snake" })
    assertTrue(favoriteGames.any { it.id == "pong" })
    assertTrue(favoriteGames.any { it.id == "tetris" })

    // Star icon state check
    assertTrue(favoriteIds.contains("snake"))
    assertFalse(favoriteIds.contains("space"))
  }
}
