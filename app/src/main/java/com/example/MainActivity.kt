package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.games.flappy.FlappyBirdScreen
import com.example.games.game2048.Game2048Screen
import com.example.games.memory.MemoryGameScreen
import com.example.games.reflex.ReflexGameScreen
import com.example.games.snake.SnakeGameScreen
import com.example.games.space.SpaceShooterScreen
import com.example.ui.AchievementUnlockBanner
import com.example.ui.ArcadeViewModel
import com.example.ui.LobbyScreen
import com.example.ui.Screen
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.MyApplicationTheme
import com.example.util.SoundHelper
import com.example.util.ToneType

class MainActivity : ComponentActivity() {
  private val viewModel: ArcadeViewModel by viewModels()
  private lateinit var soundHelper: SoundHelper

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    soundHelper = SoundHelper(this)
    enableEdgeToEdge()

    // Handle deep link if app was launched via OAuth redirect
    handleIntent(intent)

    setContent {
      MyApplicationTheme {
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = DarkBackground
        ) {
          val scores by viewModel.allScores.collectAsStateWithLifecycle()
          val userScores by viewModel.currentUserScores.collectAsStateWithLifecycle()
          val authState by viewModel.authState.collectAsStateWithLifecycle()
          val favoriteGameIds by viewModel.favoriteGameIds.collectAsStateWithLifecycle()
          val unlockedAchievement by viewModel.newlyUnlockedAchievement.collectAsStateWithLifecycle()
          var currentScreen by remember { mutableStateOf<Screen>(Screen.Lobby) }

          LaunchedEffect(unlockedAchievement) {
            if (unlockedAchievement != null) {
              soundHelper.playTone(ToneType.VICTORY)
            }
          }

          // Android hardware/system back button handling
          BackHandler(enabled = currentScreen != Screen.Lobby) {
            currentScreen = Screen.Lobby
          }

          Box(modifier = Modifier.fillMaxSize()) {
            when (val scr = currentScreen) {
              is Screen.Lobby -> {
                LobbyScreen(
                  scores = scores,
                  userScores = userScores,
                  authState = authState,
                  favoriteGameIds = favoriteGameIds,
                  onToggleFavorite = { gameId ->
                    viewModel.toggleFavorite(gameId)
                  },
                  onSelectGame = { nextScreen ->
                    currentScreen = nextScreen
                  },
                  onStartLogin = {
                    val intent = viewModel.getLoginIntent()
                    startActivity(intent)
                  },
                  onLogout = {
                    viewModel.logout()
                  }
                )
              }

              is Screen.Snake -> {
                SnakeGameScreen(
                  highScore = viewModel.getHighScoreFor(scores, "snake"),
                  soundHelper = soundHelper,
                  onBack = { currentScreen = Screen.Lobby },
                  onGameOverRecord = { score ->
                    viewModel.recordGameFinished("snake", score)
                  }
                )
              }

              is Screen.Space -> {
                SpaceShooterScreen(
                  highScore = viewModel.getHighScoreFor(scores, "space"),
                  soundHelper = soundHelper,
                  onBack = { currentScreen = Screen.Lobby },
                  onGameOverRecord = { score ->
                    viewModel.recordGameFinished("space", score)
                  }
                )
              }

              is Screen.Reflex -> {
                ReflexGameScreen(
                  highScore = viewModel.getHighScoreFor(scores, "reflex"),
                  soundHelper = soundHelper,
                  onBack = { currentScreen = Screen.Lobby },
                  onGameOverRecord = { score ->
                    viewModel.recordGameFinished("reflex", score)
                  }
                )
              }

              is Screen.Memory -> {
                MemoryGameScreen(
                  highScore = viewModel.getHighScoreFor(scores, "memory"),
                  soundHelper = soundHelper,
                  onBack = { currentScreen = Screen.Lobby },
                  onGameOverRecord = { score ->
                    viewModel.recordGameFinished("memory", score)
                  }
                )
              }

              is Screen.Game2048 -> {
                Game2048Screen(
                  highScore = viewModel.getHighScoreFor(scores, "2048"),
                  soundHelper = soundHelper,
                  onBack = { currentScreen = Screen.Lobby },
                  onGameOverRecord = { score ->
                    viewModel.recordGameFinished("2048", score)
                  }
                )
              }

              is Screen.Flappy -> {
                FlappyBirdScreen(
                  highScore = viewModel.getHighScoreFor(scores, "flappy"),
                  soundHelper = soundHelper,
                  onBack = { currentScreen = Screen.Lobby },
                  onGameOverRecord = { score ->
                    viewModel.recordGameFinished("flappy", score)
                  }
                )
              }

              is Screen.PlayGame -> {
                com.example.ui.GameScreenRouter(
                  gameId = scr.gameId,
                  scores = scores,
                  viewModel = viewModel,
                  soundHelper = soundHelper,
                  onBack = { currentScreen = Screen.Lobby }
                )
              }
            }

            // Top Achievement Banner Popup
            AnimatedVisibility(
              visible = unlockedAchievement != null,
              enter = slideInVertically(initialOffsetY = { -it }),
              exit = slideOutVertically(targetOffsetY = { -it }),
              modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .zIndex(999f)
            ) {
              unlockedAchievement?.let { ach ->
                AchievementUnlockBanner(
                  achievement = ach,
                  onDismiss = { viewModel.dismissUnlockedAchievement() }
                )
              }
            }
          }
        }
      }
    }
  }

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    setIntent(intent)
    handleIntent(intent)
  }

  private fun handleIntent(intent: Intent?) {
    val data = intent?.data ?: return
    if (data.scheme == "pocketarcade" && data.host == "auth") {
      val code = data.getQueryParameter("code")
      if (!code.isNullOrEmpty()) {
        viewModel.handleAuthRedirectCode(code)
      }
    }
  }

  override fun onDestroy() {
    super.onDestroy()
    soundHelper.release()
  }
}
