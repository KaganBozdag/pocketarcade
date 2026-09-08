package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_game_scores")
data class UserGameScoreEntity(
    @PrimaryKey
    val compositeKey: String, // format: "${userId}_${gameId}"
    val userId: String,
    val username: String,
    val avatarUrl: String?,
    val gameId: String,
    val highScore: Int = 0,
    val gamesPlayed: Int = 0,
    val lastPlayed: Long = System.currentTimeMillis()
)
