package com.example.ui

import androidx.compose.runtime.Composable
import com.example.data.GameScoreEntity
import com.example.games.arcade.*
import com.example.games.flappy.FlappyBirdScreen
import com.example.games.game2048.Game2048Screen
import com.example.games.memory.MemoryGameScreen
import com.example.games.online.*
import com.example.games.puzzle.*
import com.example.games.reflex.*
import com.example.games.snake.SnakeGameScreen
import com.example.games.space.SpaceShooterScreen
import com.example.games.word.*
import com.example.util.SoundHelper

@Composable
fun GameScreenRouter(
    gameId: String,
    scores: List<GameScoreEntity>,
    viewModel: ArcadeViewModel,
    soundHelper: SoundHelper,
    onBack: () -> Unit
) {
    val highScore = viewModel.getHighScoreFor(scores, gameId)
    val onRecord: (Int) -> Unit = { score ->
        viewModel.recordGameFinished(gameId, score)
    }

    when (gameId) {
        // Original 6
        "snake" -> SnakeGameScreen(highScore, soundHelper, onBack, onRecord)
        "space" -> SpaceShooterScreen(highScore, soundHelper, onBack, onRecord)
        "reflex" -> ReflexGameScreen(highScore, soundHelper, onBack, onRecord)
        "memory" -> MemoryGameScreen(highScore, soundHelper, onBack, onRecord)
        "2048" -> Game2048Screen(highScore, soundHelper, onBack, onRecord)
        "flappy" -> FlappyBirdScreen(highScore, soundHelper, onBack, onRecord)

        // Arcade Pack
        "pong" -> PongGameScreen(highScore, soundHelper, onBack, onRecord)
        "breakout" -> BreakoutGameScreen(highScore, soundHelper, onBack, onRecord)
        "sky_jump" -> SkyJumpScreen(highScore, soundHelper, onBack, onRecord)
        "asteroid_dodge" -> AsteroidDodgeScreen(highScore, soundHelper, onBack, onRecord)
        "frogger" -> CrossyHopScreen(highScore, soundHelper, onBack, onRecord)
        "copter" -> CopterCaveScreen(highScore, soundHelper, onBack, onRecord)
        "maze_runner" -> MazeRunnerScreen(highScore, soundHelper, onBack, onRecord)
        "pac_dash" -> PacDashScreen(highScore, soundHelper, onBack, onRecord)

        // Puzzle Pack
        "minesweeper" -> MinesweeperScreen(highScore, soundHelper, onBack, onRecord)
        "puzzle15" -> Puzzle15Screen(highScore, soundHelper, onBack, onRecord)
        "tictactoe" -> TicTacToeScreen(highScore, soundHelper, onBack, onRecord)
        "connect4" -> Connect4Screen(highScore, soundHelper, onBack, onRecord)
        "sudoku_mini" -> MiniSudokuScreen(highScore, soundHelper, onBack, onRecord)
        "lights_out" -> LightsOutScreen(highScore, soundHelper, onBack, onRecord)
        "simon" -> SimonSaysScreen(highScore, soundHelper, onBack, onRecord)
        "water_sort" -> WaterSortScreen(highScore, soundHelper, onBack, onRecord)
        "block_drop" -> BlockDropScreen(highScore, soundHelper, onBack, onRecord)
        "bulls_cows" -> MastermindScreen(highScore, soundHelper, onBack, onRecord)

        // Reflex Pack
        "color_tap" -> ColorTapScreen(highScore, soundHelper, onBack, onRecord)
        "math_sprint" -> MathSprintScreen(highScore, soundHelper, onBack, onRecord)
        "piano_tiles" -> PianoTilesScreen(highScore, soundHelper, onBack, onRecord)
        "whack_mole" -> WhackMoleScreen(highScore, soundHelper, onBack, onRecord)
        "knife_hit" -> KnifeHitScreen(highScore, soundHelper, onBack, onRecord)
        "tower_stack" -> TowerStackScreen(highScore, soundHelper, onBack, onRecord)
        "traffic_dodge" -> TrafficDodgeScreen(highScore, soundHelper, onBack, onRecord)
        "coin_catcher" -> CoinCatcherScreen(highScore, soundHelper, onBack, onRecord)
        "balance_ball" -> BalanceBallScreen(highScore, soundHelper, onBack, onRecord)
        "speed_click" -> SpeedClickScreen(highScore, soundHelper, onBack, onRecord)

        // Word & Trivia Pack
        "hangman" -> HangmanScreen(highScore, soundHelper, onBack, onRecord)
        "wordle" -> WordleScreen(highScore, soundHelper, onBack, onRecord)
        "scramble" -> ScrambleScreen(highScore, soundHelper, onBack, onRecord)
        "trivia_quiz" -> TriviaQuizScreen(highScore, soundHelper, onBack, onRecord)
        "flag_quiz" -> FlagQuizScreen(highScore, soundHelper, onBack, onRecord)
        "true_false_math" -> TrueFalseMathScreen(highScore, soundHelper, onBack, onRecord)
        "anagram_rush" -> AnagramRushScreen(highScore, soundHelper, onBack, onRecord)
        "memory_sequence" -> MemorySequenceScreen(highScore, soundHelper, onBack, onRecord)

        // Online Pack
        "online_rps" -> OnlineRpsScreen("online_rps", highScore, soundHelper, onBack, onRecord)
        "online_tap_battle" -> OnlineTapBattleScreen(highScore, soundHelper, onBack, onRecord)
        "online_dice" -> OnlineDiceScreen(highScore, soundHelper, onBack, onRecord)
        "online_roulette" -> OnlineRouletteScreen(highScore, soundHelper, onBack, onRecord)
        "online_card21" -> OnlineCard21Screen(highScore, soundHelper, onBack, onRecord)
        "online_wheel" -> OnlineLuckyWheelScreen(highScore, soundHelper, onBack, onRecord)
        "online_bingo" -> OnlineBingoScreen(highScore, soundHelper, onBack, onRecord)

        // Fallback for remaining online/arcade games
        else -> OnlineArenaGameScreen(gameId, highScore, soundHelper, onBack, onRecord)
    }
}
