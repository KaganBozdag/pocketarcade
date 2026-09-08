package com.example.data

import kotlinx.coroutines.flow.Flow

class GameScoreRepository(private val dao: GameScoreDao) {
    val allScores: Flow<List<GameScoreEntity>> = dao.getAllScores()

    suspend fun recordGameFinished(gameId: String, newScore: Int): Boolean {
        val existing = dao.getScore(gameId)
        val currentHigh = existing?.highScore ?: 0
        val isNewRecord = newScore > currentHigh
        val newHighScore = if (isNewRecord) newScore else currentHigh
        val gamesPlayed = (existing?.gamesPlayed ?: 0) + 1

        dao.insertOrUpdate(
            GameScoreEntity(
                gameId = gameId,
                highScore = newHighScore,
                gamesPlayed = gamesPlayed,
                lastPlayed = System.currentTimeMillis()
            )
        )
        return isNewRecord
    }

    suspend fun getHighScore(gameId: String): Int {
        return dao.getScore(gameId)?.highScore ?: 0
    }
}
