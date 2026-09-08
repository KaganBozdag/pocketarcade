package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_scores")
data class GameScoreEntity(
    @PrimaryKey
    val gameId: String,
    val highScore: Int = 0,
    val gamesPlayed: Int = 0,
    val lastPlayed: Long = System.currentTimeMillis()
)
