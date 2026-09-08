package com.example.ui

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.auth.AuthState
import com.example.auth.GitHubAuthManager
import com.example.auth.GitHubUser
import com.example.data.AppDatabase
import com.example.data.FavoritesPreferences
import com.example.data.GameScoreEntity
import com.example.data.GameScoreRepository
import com.example.data.UserGameScoreEntity
import com.example.model.Achievement
import com.example.model.AchievementCatalog
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class Screen {
    data object Lobby : Screen()
    data object Snake : Screen()
    data object Space : Screen()
    data object Reflex : Screen()
    data object Memory : Screen()
    data object Game2048 : Screen()
    data object Flappy : Screen()
    data class PlayGame(val gameId: String) : Screen()
}

class ArcadeViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val repository = GameScoreRepository(db.gameScoreDao())
    private val userScoreDao = db.userGameScoreDao()
    val authManager = GitHubAuthManager(application)
    val favoritesPreferences = FavoritesPreferences(application)
    val favoriteGameIds: StateFlow<Set<String>> = favoritesPreferences.favorites

    fun toggleFavorite(gameId: String): Boolean {
        return favoritesPreferences.toggleFavorite(gameId)
    }

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _newlyUnlockedAchievement = MutableStateFlow<Achievement?>(null)
    val newlyUnlockedAchievement: StateFlow<Achievement?> = _newlyUnlockedAchievement.asStateFlow()

    fun dismissUnlockedAchievement() {
        _newlyUnlockedAchievement.value = null
    }

    val allScores: StateFlow<List<GameScoreEntity>> = repository.allScores
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val currentUserScores: StateFlow<List<UserGameScoreEntity>> = _authState
        .flatMapLatest { state ->
            if (state is AuthState.Authenticated) {
                userScoreDao.getScoresForUser(state.user.id.toString())
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        // Restore session if user was previously logged in
        val savedUser = authManager.authPreferences.currentUser
        if (savedUser != null) {
            _authState.value = AuthState.Authenticated(savedUser)
        }
    }

    fun getLoginIntent(): Intent {
        _authState.value = AuthState.Loading
        return authManager.buildAuthIntent()
    }

    fun handleAuthRedirectCode(code: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val tokenResult = authManager.exchangeCodeForToken(code)
            tokenResult.onSuccess { token ->
                val profileResult = authManager.fetchUserProfile(token)
                profileResult.onSuccess { user ->
                    _authState.value = AuthState.Authenticated(user)
                }.onFailure { e ->
                    _authState.value = AuthState.Error(e.message ?: "Profil bilgisi alınamadı")
                }
            }.onFailure { e ->
                _authState.value = AuthState.Error(e.message ?: "GitHub yetkilendirme hatası")
            }
        }
    }

    fun logout() {
        authManager.logout()
        _authState.value = AuthState.Idle
    }

    fun recordGameFinished(gameId: String, score: Int, onResult: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            val prevContext = AchievementCatalog.buildContext(
                deviceScores = allScores.value,
                userScores = currentUserScores.value,
                isAuthenticated = _authState.value is AuthState.Authenticated
            )
            val prevUnlockedIds = AchievementCatalog.achievements
                .filter { it.evaluate(prevContext).isUnlocked }
                .map { it.id }
                .toSet()

            val isNewRecord = repository.recordGameFinished(gameId, score)

            // If a GitHub user is authenticated, save score to their account as well
            val currentAuth = _authState.value
            if (currentAuth is AuthState.Authenticated) {
                val user = currentAuth.user
                val compositeKey = "${user.id}_$gameId"
                val existing = userScoreDao.getScore(compositeKey)
                val currentHigh = existing?.highScore ?: 0
                val newHigh = if (score > currentHigh) score else currentHigh
                val plays = (existing?.gamesPlayed ?: 0) + 1

                userScoreDao.insertOrUpdate(
                    UserGameScoreEntity(
                        compositeKey = compositeKey,
                        userId = user.id.toString(),
                        username = user.login,
                        avatarUrl = user.avatarUrl,
                        gameId = gameId,
                        highScore = newHigh,
                        gamesPlayed = plays,
                        lastPlayed = System.currentTimeMillis()
                    )
                )
            }

            // Evaluate new achievements
            val newDeviceScores = repository.allScores.first()
            val newContext = AchievementCatalog.buildContext(
                deviceScores = newDeviceScores,
                userScores = currentUserScores.value,
                isAuthenticated = _authState.value is AuthState.Authenticated
            )
            val newlyUnlocked = AchievementCatalog.achievements.firstOrNull { ach ->
                !prevUnlockedIds.contains(ach.id) && ach.evaluate(newContext).isUnlocked
            }
            if (newlyUnlocked != null) {
                _newlyUnlockedAchievement.value = newlyUnlocked
            }

            onResult(isNewRecord)
        }
    }

    fun getHighScoreFor(scores: List<GameScoreEntity>, gameId: String): Int {
        val currentAuth = _authState.value
        if (currentAuth is AuthState.Authenticated) {
            val userScore = currentUserScores.value.find { it.gameId == gameId }?.highScore ?: 0
            val deviceScore = scores.find { it.gameId == gameId }?.highScore ?: 0
            return maxOf(userScore, deviceScore)
        }
        return scores.find { it.gameId == gameId }?.highScore ?: 0
    }
}
