package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UserGameScoreDao {
    @Query("SELECT * FROM user_game_scores WHERE userId = :userId")
    fun getScoresForUser(userId: String): Flow<List<UserGameScoreEntity>>

    @Query("SELECT * FROM user_game_scores WHERE compositeKey = :key")
    suspend fun getScore(key: String): UserGameScoreEntity?

    @Query("SELECT * FROM user_game_scores WHERE gameId = :gameId ORDER BY highScore DESC LIMIT 10")
    fun getLeaderboardForGame(gameId: String): Flow<List<UserGameScoreEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(score: UserGameScoreEntity)
}
