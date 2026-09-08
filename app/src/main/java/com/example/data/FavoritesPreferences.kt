package com.example.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FavoritesPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("arcade_favorites_prefs", Context.MODE_PRIVATE)

    private val keyFavorites = "favorite_game_ids"

    private val _favorites = MutableStateFlow<Set<String>>(loadFavorites())
    val favorites: StateFlow<Set<String>> = _favorites.asStateFlow()

    private fun loadFavorites(): Set<String> {
        return prefs.getStringSet(keyFavorites, emptySet()) ?: emptySet()
    }

    fun isFavorite(gameId: String): Boolean {
        return _favorites.value.contains(gameId)
    }

    fun toggleFavorite(gameId: String): Boolean {
        val current = _favorites.value.toMutableSet()
        val isNowFavorite = if (current.contains(gameId)) {
            current.remove(gameId)
            false
        } else {
            current.add(gameId)
            true
        }
        prefs.edit().putStringSet(keyFavorites, current).apply()
        _favorites.value = current
        return isNowFavorite
    }

    fun addFavorite(gameId: String) {
        val current = _favorites.value.toMutableSet()
        if (current.add(gameId)) {
            prefs.edit().putStringSet(keyFavorites, current).apply()
            _favorites.value = current
        }
    }

    fun removeFavorite(gameId: String) {
        val current = _favorites.value.toMutableSet()
        if (current.remove(gameId)) {
            prefs.edit().putStringSet(keyFavorites, current).apply()
            _favorites.value = current
        }
    }
}
