package com.example.model

import androidx.compose.ui.graphics.Color

enum class GameCategory(val title: String, val icon: String) {
    ALL("Tümü", "🌟"),
    ARCADE("Klasik Arcade", "🕹️"),
    PUZZLE("Bulmaca & Zeka", "🧩"),
    REFLEX("Hız & Refleks", "⚡"),
    WORD("Kelime & Bilgi", "📚"),
    ONLINE("Çevrimiçi & Düello", "🌐")
}

data class ArcadeGame(
    val id: String,
    val title: String,
    val subtitle: String,
    val emoji: String,
    val category: GameCategory,
    val themeColor: Color,
    val actionText: String = "OYNA",
    val isOnline: Boolean = false,
    val instructions: String = ""
)
