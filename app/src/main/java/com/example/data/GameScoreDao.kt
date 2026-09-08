package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GameScoreDao {
    @Query("SELECT * FROM game_scores")
    fun getAllScores(): Flow<List<GameScoreEntity>>

    @Query("SELECT * FROM game_scores WHERE gameId = :gameId")
    suspend fun getScore(gameId: String): GameScoreEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(score: GameScoreEntity)
}
