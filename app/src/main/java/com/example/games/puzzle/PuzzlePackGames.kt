package com.example.games.puzzle

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GameCatalog
import com.example.ui.components.GameHeader
import com.example.ui.components.GameOverDialog
import com.example.ui.theme.*
import com.example.util.SoundHelper
import com.example.util.ToneType
import kotlinx.coroutines.delay
import kotlin.random.Random

// ----------------------------------------------------
// 1. MINESWEEPER SCREEN
// ----------------------------------------------------
data class MineCell(val isMine: Boolean, var isRevealed: Boolean = false, var isFlagged: Boolean = false, var neighborMines: Int = 0)

@Composable
fun MinesweeperScreen(
    highScore: Int,
    soundHelper: SoundHelper,
    onBack: () -> Unit,
    onGameOverRecord: (Int) -> Unit
) {
    val game = GameCatalog.getGameById("minesweeper")!!
    var score by remember { mutableIntStateOf(0) }
    var gameOver by remember { mutableStateOf(false) }
    var flagMode by remember { mutableStateOf(false) }

    val rows = 6
    val cols = 6
    val totalMines = 5

    val grid = remember {
        mutableStateListOf<MineCell>().apply {
            val mineIndices = (0 until rows * cols).shuffled().take(totalMines).toSet()
            for (i in 0 until rows * cols) {
                add(MineCell(isMine = i in mineIndices))
            }
            // calculate neighbor mines
            for (r in 0 until rows) {
                for (c in 0 until cols) {
                    val idx = r * cols + c
                    if (!this[idx].isMine) {
                        var count = 0
                        for (dr in -1..1) {
                            for (dc in -1..1) {
                                val nr = r + dr
                                val nc = c + dc
                                if (nr in 0 until rows && nc in 0 until cols) {
                                    if (this[nr * cols + nc].isMine) count++
                                }
                            }
                        }
                        this[idx].neighborMines = count
                    }
                }
            }
        }
    }

    fun revealCell(idx: Int) {
        val cell = grid[idx]
        if (cell.isRevealed) return

        if (flagMode) {
            cell.isFlagged = !cell.isFlagged
            soundHelper.playTone(ToneType.TAP)
            return
        }

        if (cell.isFlagged) return

        cell.isRevealed = true
        if (cell.isMine) {
            gameOver = true
            soundHelper.playTone(ToneType.GAMEOVER)
            onGameOverRecord(score)
        } else {
            score += 10
            soundHelper.playTone(ToneType.TAP)
            // check win
            val unrevealedSafe = grid.count { !it.isRevealed && !it.isMine }
            if (unrevealedSafe == 0) {
                score += 150
                soundHelper.playTone(ToneType.VICTORY)
                gameOver = true
                onGameOverRecord(score)
            }
        }
    }

    Scaffold(
        topBar = {
            GameHeader(
                title = game.title,
                emoji = game.emoji,
                score = score,
                highScore = highScore,
                themeColor = game.themeColor,
                instructions = game.instructions,
                onBack = onBack
            )
        },
        containerColor = DarkBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Mode toggle
            Button(
                onClick = { flagMode = !flagMode },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (flagMode) NeonAmber else DarkSurfaceVariant
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(if (flagMode) "🚩 Bayrak Modu Açık" else "⛏️ Kazma Modu Açık", color = if (flagMode) Color.Black else TextPrimary)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 6x6 Grid
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(DarkSurfaceVariant)
                    .padding(8.dp)
            ) {
                for (r in 0 until rows) {
                    Row {
                        for (c in 0 until cols) {
                            val idx = r * cols + c
                            val cell = grid[idx]
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .padding(2.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (cell.isRevealed) {
                                            if (cell.isMine) NeonRed else Color(0xFF1E293B)
                                        } else Color(0xFF334155)
                                    )
                                    .clickable { revealCell(idx) },
                                contentAlignment = Alignment.Center
                            ) {
                                if (cell.isRevealed) {
                                    if (cell.isMine) {
                                        Text("💣", fontSize = 18.sp)
                                    } else if (cell.neighborMines > 0) {
                                        Text("${cell.neighborMines}", fontWeight = FontWeight.Bold, color = NeonCyan, fontSize = 16.sp)
                                    }
                                } else if (cell.isFlagged) {
                                    Text("🚩", fontSize = 18.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        if (gameOver) {
            GameOverDialog(
                score = score,
                highScore = highScore,
                themeColor = game.themeColor,
                onRestart = {
                    score = 0
                    val mineIndices = (0 until rows * cols).shuffled().take(totalMines).toSet()
                    grid.forEachIndexed { i, c ->
                        val isM = i in mineIndices
                        c.isRevealed = false
                        c.isFlagged = false
                    }
                    gameOver = false
                },
                onBackToLobby = onBack
            )
        }
    }
}

// ----------------------------------------------------
// 2. 15-PUZZLE (SLIDING TILES)
// ----------------------------------------------------
@Composable
fun Puzzle15Screen(
    highScore: Int,
    soundHelper: SoundHelper,
    onBack: () -> Unit,
    onGameOverRecord: (Int) -> Unit
) {
    val game = GameCatalog.getGameById("puzzle15")!!
    var moves by remember { mutableIntStateOf(0) }
    var gameOver by remember { mutableStateOf(false) }

    // 0 represents empty space
    val tiles = remember {
        mutableStateListOf<Int>().apply {
            val list = (1..15).toMutableList()
            list.shuffle()
            list.add(0)
            addAll(list)
        }
    }

    fun moveTile(idx: Int) {
        val emptyIdx = tiles.indexOf(0)
        val r1 = idx / 4
        val c1 = idx % 4
        val r2 = emptyIdx / 4
        val c2 = emptyIdx % 4

        if ((kotlin.math.abs(r1 - r2) == 1 && c1 == c2) || (kotlin.math.abs(c1 - c2) == 1 && r1 == r2)) {
            tiles[emptyIdx] = tiles[idx]
            tiles[idx] = 0
            moves++
            soundHelper.playTone(ToneType.TAP)

            // Check if sorted 1..15
            if (tiles.take(15) == (1..15).toList()) {
                val score = maxOf(10, 1000 - moves * 10)
                soundHelper.playTone(ToneType.VICTORY)
                gameOver = true
                onGameOverRecord(score)
            }
        }
    }

    Scaffold(
        topBar = {
            GameHeader(
                title = game.title,
                emoji = game.emoji,
                score = moves,
                highScore = highScore,
                themeColor = game.themeColor,
                instructions = game.instructions,
                onBack = onBack
            )
        },
        containerColor = DarkBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Hamle: $moves", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(DarkSurfaceVariant)
                    .padding(8.dp)
            ) {
                for (r in 0..3) {
                    Row {
                        for (c in 0..3) {
                            val idx = r * 4 + c
                            val value = tiles[idx]
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .padding(3.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (value != 0) NeonCyan else Color.Transparent)
                                    .clickable { if (value != 0) moveTile(idx) },
                                contentAlignment = Alignment.Center
                            ) {
                                if (value != 0) {
                                    Text(
                                        text = "$value",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 20.sp,
                                        color = Color.Black
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (gameOver) {
            GameOverDialog(
                score = maxOf(10, 1000 - moves * 10),
                highScore = highScore,
                themeColor = game.themeColor,
                onRestart = {
                    moves = 0
                    tiles.clear()
                    val list = (1..15).toMutableList()
                    list.shuffle()
                    list.add(0)
                    tiles.addAll(list)
                    gameOver = false
                },
                onBackToLobby = onBack
            )
        }
    }
}

// ----------------------------------------------------
// 3. TIC TAC TOE SCREEN
// ----------------------------------------------------
@Composable
fun TicTacToeScreen(
    highScore: Int,
    soundHelper: SoundHelper,
    onBack: () -> Unit,
    onGameOverRecord: (Int) -> Unit
) {
    val game = GameCatalog.getGameById("tictactoe")!!
    var score by remember { mutableIntStateOf(0) }
    var board by remember { mutableStateOf(List(9) { "" }) }
    var gameOver by remember { mutableStateOf(false) }
    var statusText by remember { mutableStateOf("Sıra Sende (X)") }

    fun checkWinner(b: List<String>): String? {
        val lines = listOf(
            listOf(0, 1, 2), listOf(3, 4, 5), listOf(6, 7, 8),
            listOf(0, 3, 6), listOf(1, 4, 7), listOf(2, 5, 8),
            listOf(0, 4, 8), listOf(2, 4, 6)
        )
        for (line in lines) {
            if (b[line[0]].isNotEmpty() && b[line[0]] == b[line[1]] && b[line[1]] == b[line[2]]) {
                return b[line[0]]
            }
        }
        if (b.none { it.isEmpty() }) return "Berabere"
        return null
    }

    fun makeAiMove(currentBoard: List<String>) {
        val emptyIndices = currentBoard.indices.filter { currentBoard[it].isEmpty() }
        if (emptyIndices.isNotEmpty()) {
            val choice = emptyIndices.random()
            val newB = currentBoard.toMutableList()
            newB[choice] = "O"
            board = newB
            soundHelper.playTone(ToneType.TAP)
            val win = checkWinner(newB)
            if (win != null) {
                gameOver = true
                statusText = if (win == "Berabere") "Berabere Bitti!" else "Bot Kazandı!"
                soundHelper.playTone(ToneType.GAMEOVER)
                onGameOverRecord(score)
            }
        }
    }

    fun handleCellClick(index: Int) {
        if (board[index].isEmpty() && !gameOver) {
            val newB = board.toMutableList()
            newB[index] = "X"
            board = newB
            soundHelper.playTone(ToneType.TAP)

            val win = checkWinner(newB)
            if (win == "X") {
                score += 25
                statusText = "Tebrikler Kazandın!"
                soundHelper.playTone(ToneType.VICTORY)
                gameOver = true
                onGameOverRecord(score)
            } else if (win == "Berabere") {
                statusText = "Berabere!"
                gameOver = true
            } else {
                // AI moves
                makeAiMove(newB)
            }
        }
    }

    Scaffold(
        topBar = {
            GameHeader(
                title = game.title,
                emoji = game.emoji,
                score = score,
                highScore = highScore,
                themeColor = game.themeColor,
                instructions = game.instructions,
                onBack = onBack
            )
        },
        containerColor = DarkBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(statusText, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(modifier = Modifier.height(24.dp))

            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(DarkSurfaceVariant)
                    .padding(8.dp)
            ) {
                for (r in 0..2) {
                    Row {
                        for (c in 0..2) {
                            val idx = r * 3 + c
                            Box(
                                modifier = Modifier
                                    .size(90.dp)
                                    .padding(4.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF1E293B))
                                    .clickable { handleCellClick(idx) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = board[idx],
                                    fontWeight = FontWeight.Black,
                                    fontSize = 36.sp,
                                    color = if (board[idx] == "X") NeonCyan else NeonPink
                                )
                            }
                        }
                    }
                }
            }
        }

        if (gameOver) {
            GameOverDialog(
                score = score,
                highScore = highScore,
                themeColor = game.themeColor,
                onRestart = {
                    board = List(9) { "" }
                    statusText = "Sıra Sende (X)"
                    gameOver = false
                },
                onBackToLobby = onBack
            )
        }
    }
}

// ----------------------------------------------------
// 4. CONNECT FOUR SCREEN
// ----------------------------------------------------
@Composable
fun Connect4Screen(
    highScore: Int,
    soundHelper: SoundHelper,
    onBack: () -> Unit,
    onGameOverRecord: (Int) -> Unit
) {
    val game = GameCatalog.getGameById("connect4")!!
    var score by remember { mutableIntStateOf(0) }
    var gameOver by remember { mutableStateOf(false) }

    val rows = 6
    val cols = 7
    // 0 = empty, 1 = Player (Red), 2 = AI (Yellow)
    var board by remember { mutableStateOf(List(rows * cols) { 0 }) }

    fun checkWin(b: List<Int>, player: Int): Boolean {
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                // Horizontal
                if (c + 3 < cols && (0..3).all { b[r * cols + (c + it)] == player }) return true
                // Vertical
                if (r + 3 < rows && (0..3).all { b[(r + it) * cols + c] == player }) return true
                // Diagonal \
                if (r + 3 < rows && c + 3 < cols && (0..3).all { b[(r + it) * cols + (c + it)] == player }) return true
                // Diagonal /
                if (r + 3 < rows && c - 3 >= 0 && (0..3).all { b[(r + it) * cols + (c - it)] == player }) return true
            }
        }
        return false
    }

    fun dropInCol(col: Int) {
        if (gameOver) return
        val newB = board.toMutableList()
        // Find bottom empty row in col
        var placedRow = -1
        for (r in rows - 1 downTo 0) {
            if (newB[r * cols + col] == 0) {
                newB[r * cols + col] = 1
                placedRow = r
                break
            }
        }
        if (placedRow != -1) {
            soundHelper.playTone(ToneType.TAP)
            if (checkWin(newB, 1)) {
                score += 50
                board = newB
                soundHelper.playTone(ToneType.VICTORY)
                gameOver = true
                onGameOverRecord(score)
                return
            }

            // AI Turn
            val availableCols = (0 until cols).filter { c -> newB[c] == 0 }
            if (availableCols.isNotEmpty()) {
                val aiCol = availableCols.random()
                for (r in rows - 1 downTo 0) {
                    if (newB[r * cols + aiCol] == 0) {
                        newB[r * cols + aiCol] = 2
                        break
                    }
                }
                if (checkWin(newB, 2)) {
                    board = newB
                    soundHelper.playTone(ToneType.GAMEOVER)
                    gameOver = true
                    onGameOverRecord(score)
                    return
                }
            }
            board = newB
        }
    }

    Scaffold(
        topBar = {
            GameHeader(
                title = game.title,
                emoji = game.emoji,
                score = score,
                highScore = highScore,
                themeColor = game.themeColor,
                instructions = game.instructions,
                onBack = onBack
            )
        },
        containerColor = DarkBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Sütuna dokunarak pul bırak!", fontSize = 14.sp, color = TextSecondary)
            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF1E3A8A))
                    .padding(8.dp)
            ) {
                for (r in 0 until rows) {
                    Row {
                        for (c in 0 until cols) {
                            val v = board[r * cols + c]
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .padding(3.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when (v) {
                                            1 -> NeonRed
                                            2 -> NeonAmber
                                            else -> DarkBackground
                                        }
                                    )
                                    .clickable { dropInCol(c) }
                            )
                        }
                    }
                }
            }
        }

        if (gameOver) {
            GameOverDialog(
                score = score,
                highScore = highScore,
                themeColor = game.themeColor,
                onRestart = {
                    board = List(rows * cols) { 0 }
                    gameOver = false
                },
                onBackToLobby = onBack
            )
        }
    }
}

// ----------------------------------------------------
// 5. MINI SUDOKU 4X4 SCREEN
// ----------------------------------------------------
@Composable
fun MiniSudokuScreen(
    highScore: Int,
    soundHelper: SoundHelper,
    onBack: () -> Unit,
    onGameOverRecord: (Int) -> Unit
) {
    val game = GameCatalog.getGameById("sudoku_mini")!!
    var score by remember { mutableIntStateOf(0) }
    var gameOver by remember { mutableStateOf(false) }

    // Pre-filled clues (0 = empty)
    val puzzle = remember { listOf(1, 0, 0, 4, 0, 0, 2, 0, 0, 1, 0, 0, 4, 0, 0, 3) }
    val solution = listOf(1, 2, 3, 4, 3, 4, 2, 1, 2, 1, 4, 3, 4, 3, 1, 2)
    val board = remember { mutableStateListOf<Int>().apply { addAll(puzzle) } }
    var selectedIdx by remember { mutableIntStateOf(-1) }

    fun selectNumber(num: Int) {
        if (selectedIdx != -1 && puzzle[selectedIdx] == 0) {
            board[selectedIdx] = num
            soundHelper.playTone(ToneType.TAP)
            if (board == solution) {
                score = 100
                soundHelper.playTone(ToneType.VICTORY)
                gameOver = true
                onGameOverRecord(score)
            }
        }
    }

    Scaffold(
        topBar = {
            GameHeader(
                title = game.title,
                emoji = game.emoji,
                score = score,
                highScore = highScore,
                themeColor = game.themeColor,
                instructions = game.instructions,
                onBack = onBack
            )
        },
        containerColor = DarkBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(DarkSurfaceVariant)
                    .padding(8.dp)
            ) {
                for (r in 0..3) {
                    Row {
                        for (c in 0..3) {
                            val idx = r * 4 + c
                            val v = board[idx]
                            val isClue = puzzle[idx] != 0
                            Box(
                                modifier = Modifier
                                    .size(68.dp)
                                    .padding(3.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (selectedIdx == idx) NeonCyan.copy(alpha = 0.3f)
                                        else if (isClue) Color(0xFF1E293B)
                                        else Color(0xFF334155)
                                    )
                                    .clickable { if (!isClue) selectedIdx = idx },
                                contentAlignment = Alignment.Center
                            ) {
                                if (v != 0) {
                                    Text(
                                        "$v",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 22.sp,
                                        color = if (isClue) Color.White else NeonGreen
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Number Selector 1..4
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                for (n in 1..4) {
                    Button(
                        onClick = { selectNumber(n) },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                    ) {
                        Text("$n", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                }
            }
        }

        if (gameOver) {
            GameOverDialog(
                score = score,
                highScore = highScore,
                themeColor = game.themeColor,
                onRestart = {
                    board.clear()
                    board.addAll(puzzle)
                    selectedIdx = -1
                    gameOver = false
                },
                onBackToLobby = onBack
            )
        }
    }
}

// ----------------------------------------------------
// 6. LIGHTS OUT SCREEN
// ----------------------------------------------------
@Composable
fun LightsOutScreen(
    highScore: Int,
    soundHelper: SoundHelper,
    onBack: () -> Unit,
    onGameOverRecord: (Int) -> Unit
) {
    val game = GameCatalog.getGameById("lights_out")!!
    var score by remember { mutableIntStateOf(0) }
    var gameOver by remember { mutableStateOf(false) }

    val size = 4
    val lights = remember {
        mutableStateListOf<Boolean>().apply {
            // initial random state with some lights on
            val initial = listOf(true, false, true, false, false, true, false, true, true, true, false, false, false, true, true, false)
            addAll(initial)
        }
    }

    fun toggleLight(idx: Int) {
        val r = idx / size
        val c = idx % size
        val deltas = listOf(0 to 0, -1 to 0, 1 to 0, 0 to -1, 0 to 1)
        deltas.forEach { (dr, dc) ->
            val nr = r + dr
            val nc = c + dc
            if (nr in 0 until size && nc in 0 until size) {
                val nIdx = nr * size + nc
                lights[nIdx] = !lights[nIdx]
            }
        }
        score++
        soundHelper.playTone(ToneType.TAP)

        if (lights.all { !it }) {
            soundHelper.playTone(ToneType.VICTORY)
            gameOver = true
            onGameOverRecord(score)
        }
    }

    Scaffold(
        topBar = {
            GameHeader(
                title = game.title,
                emoji = game.emoji,
                score = score,
                highScore = highScore,
                themeColor = game.themeColor,
                instructions = game.instructions,
                onBack = onBack
            )
        },
        containerColor = DarkBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Tüm ışıkları söndür!", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(modifier = Modifier.height(20.dp))

            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(DarkSurfaceVariant)
                    .padding(8.dp)
            ) {
                for (r in 0 until size) {
                    Row {
                        for (c in 0 until size) {
                            val idx = r * size + c
                            val isOn = lights[idx]
                            Box(
                                modifier = Modifier
                                    .size(68.dp)
                                    .padding(4.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isOn) NeonAmber else Color(0xFF1E293B))
                                    .clickable { toggleLight(idx) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(if (isOn) "💡" else "⚫", fontSize = 24.sp)
                            }
                        }
                    }
                }
            }
        }

        if (gameOver) {
            GameOverDialog(
                score = maxOf(10, 500 - score * 10),
                highScore = highScore,
                themeColor = game.themeColor,
                onRestart = {
                    lights.clear()
                    val initial = listOf(true, false, true, false, false, true, false, true, true, true, false, false, false, true, true, false)
                    lights.addAll(initial)
                    score = 0
                    gameOver = false
                },
                onBackToLobby = onBack
            )
        }
    }
}

// ----------------------------------------------------
// 7. SIMON SAYS COLOR MEMORY SCREEN
// ----------------------------------------------------
@Composable
fun SimonSaysScreen(
    highScore: Int,
    soundHelper: SoundHelper,
    onBack: () -> Unit,
    onGameOverRecord: (Int) -> Unit
) {
    val game = GameCatalog.getGameById("simon")!!
    var score by remember { mutableIntStateOf(0) }
    var gameOver by remember { mutableStateOf(false) }

    val colors = listOf(NeonGreen, NeonRed, NeonAmber, NeonCyan)
    val sequence = remember { mutableStateListOf<Int>() }
    var playerStep by remember { mutableIntStateOf(0) }
    var activeFlash by remember { mutableIntStateOf(-1) }
    var isPlayingSequence by remember { mutableStateOf(false) }

    fun addNextStep() {
        sequence.add(Random.nextInt(4))
        playerStep = 0
    }

    LaunchedEffect(Unit) {
        addNextStep()
    }

    LaunchedEffect(sequence.size) {
        if (sequence.isNotEmpty() && !gameOver) {
            isPlayingSequence = true
            delay(500)
            for (colorIdx in sequence) {
                activeFlash = colorIdx
                soundHelper.playTone(ToneType.TAP)
                delay(400)
                activeFlash = -1
                delay(200)
            }
            isPlayingSequence = false
        }
    }

    fun handlePress(colorIdx: Int) {
        if (isPlayingSequence || gameOver) return

        if (sequence[playerStep] == colorIdx) {
            soundHelper.playTone(ToneType.TAP)
            playerStep++
            if (playerStep == sequence.size) {
                score++
                soundHelper.playTone(ToneType.POWERUP)
                addNextStep()
            }
        } else {
            soundHelper.playTone(ToneType.GAMEOVER)
            gameOver = true
            onGameOverRecord(score)
        }
    }

    Scaffold(
        topBar = {
            GameHeader(
                title = game.title,
                emoji = game.emoji,
                score = score,
                highScore = highScore,
                themeColor = game.themeColor,
                instructions = game.instructions,
                onBack = onBack
            )
        },
        containerColor = DarkBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = if (isPlayingSequence) "Sırayı İzle..." else "Senin Sıran!",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = if (isPlayingSequence) NeonAmber else NeonGreen
            )
            Spacer(modifier = Modifier.height(24.dp))

            // 2x2 colored pads
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(RoundedCornerShape(topStart = 60.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp))
                            .background(if (activeFlash == 0) Color.White else colors[0])
                            .clickable { handlePress(0) }
                    )
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 60.dp, bottomStart = 16.dp, bottomEnd = 16.dp))
                            .background(if (activeFlash == 1) Color.White else colors[1])
                            .clickable { handlePress(1) }
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 60.dp, bottomEnd = 16.dp))
                            .background(if (activeFlash == 2) Color.White else colors[2])
                            .clickable { handlePress(2) }
                    )
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 60.dp))
                            .background(if (activeFlash == 3) Color.White else colors[3])
                            .clickable { handlePress(3) }
                    )
                }
            }
        }

        if (gameOver) {
            GameOverDialog(
                score = score,
                highScore = highScore,
                themeColor = game.themeColor,
                onRestart = {
                    score = 0
                    sequence.clear()
                    addNextStep()
                    gameOver = false
                },
                onBackToLobby = onBack
            )
        }
    }
}

// ----------------------------------------------------
// 8. WATER SORT SCREEN
// ----------------------------------------------------
@Composable
fun WaterSortScreen(
    highScore: Int,
    soundHelper: SoundHelper,
    onBack: () -> Unit,
    onGameOverRecord: (Int) -> Unit
) {
    val game = GameCatalog.getGameById("water_sort")!!
    var score by remember { mutableIntStateOf(0) }
    var gameOver by remember { mutableStateOf(false) }

    // 4 tubes, tube 1 & 2 mixed, tube 3 & 4 empty
    val tubes = remember {
        mutableStateListOf(
            mutableStateListOf(NeonCyan, NeonRed, NeonCyan, NeonRed),
            mutableStateListOf(NeonRed, NeonCyan, NeonRed, NeonCyan),
            mutableStateListOf<Color>(),
            mutableStateListOf<Color>()
        )
    }
    var selectedTube by remember { mutableIntStateOf(-1) }

    fun selectOrPour(tubeIdx: Int) {
        if (selectedTube == -1) {
            if (tubes[tubeIdx].isNotEmpty()) {
                selectedTube = tubeIdx
                soundHelper.playTone(ToneType.TAP)
            }
        } else {
            if (selectedTube != tubeIdx) {
                val from = tubes[selectedTube]
                val to = tubes[tubeIdx]
                if (from.isNotEmpty() && to.size < 4) {
                    val colorToPour = from.last()
                    if (to.isEmpty() || to.last() == colorToPour) {
                        from.removeAt(from.size - 1)
                        to.add(colorToPour)
                        soundHelper.playTone(ToneType.COIN)
                        score += 5

                        // Check Win
                        val sortedTubes = tubes.count { it.size == 4 && it.toSet().size == 1 }
                        if (sortedTubes == 2) {
                            score += 100
                            soundHelper.playTone(ToneType.VICTORY)
                            gameOver = true
                            onGameOverRecord(score)
                        }
                    }
                }
            }
            selectedTube = -1
        }
    }

    Scaffold(
        topBar = {
            GameHeader(
                title = game.title,
                emoji = game.emoji,
                score = score,
                highScore = highScore,
                themeColor = game.themeColor,
                instructions = game.instructions,
                onBack = onBack
            )
        },
        containerColor = DarkBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Aynı renkleri aynı tüpe dök!", fontSize = 15.sp, color = TextSecondary)
            Spacer(modifier = Modifier.height(24.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                tubes.forEachIndexed { idx, tube ->
                    Column(
                        modifier = Modifier
                            .width(56.dp)
                            .height(180.dp)
                            .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                            .background(if (selectedTube == idx) DarkSurfaceVariant else Color(0xFF1E293B))
                            .border(2.dp, if (selectedTube == idx) NeonCyan else Color.Gray, RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                            .clickable { selectOrPour(idx) }
                            .padding(4.dp),
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        tube.forEach { color ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(38.dp)
                                    .padding(vertical = 1.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(color)
                            )
                        }
                    }
                }
            }
        }

        if (gameOver) {
            GameOverDialog(
                score = score,
                highScore = highScore,
                themeColor = game.themeColor,
                onRestart = {
                    score = 0
                    tubes[0].clear(); tubes[0].addAll(listOf(NeonCyan, NeonRed, NeonCyan, NeonRed))
                    tubes[1].clear(); tubes[1].addAll(listOf(NeonRed, NeonCyan, NeonRed, NeonCyan))
                    tubes[2].clear()
                    tubes[3].clear()
                    selectedTube = -1
                    gameOver = false
                },
                onBackToLobby = onBack
            )
        }
    }
}

// ----------------------------------------------------
// 9. BLOCK DROP SCREEN (TETRO GRID)
// ----------------------------------------------------
@Composable
fun BlockDropScreen(
    highScore: Int,
    soundHelper: SoundHelper,
    onBack: () -> Unit,
    onGameOverRecord: (Int) -> Unit
) {
    val game = GameCatalog.getGameById("block_drop")!!
    var score by remember { mutableIntStateOf(0) }
    var gameOver by remember { mutableStateOf(false) }

    val grid = remember { mutableStateListOf<Boolean>().apply { addAll(List(64) { false }) } }

    fun placeRandomShapeAt(r: Int, c: Int) {
        if (r in 0..6 && c in 0..6) {
            val indices = listOf(r * 8 + c, r * 8 + c + 1, (r + 1) * 8 + c)
            if (indices.all { !grid[it] }) {
                indices.forEach { grid[it] = true }
                score += 15
                soundHelper.playTone(ToneType.TAP)

                // Check full row clear
                for (row in 0..7) {
                    val rowIndices = (0..7).map { row * 8 + it }
                    if (rowIndices.all { grid[it] }) {
                        rowIndices.forEach { grid[it] = false }
                        score += 50
                        soundHelper.playTone(ToneType.POWERUP)
                    }
                }
            } else {
                gameOver = true
                soundHelper.playTone(ToneType.GAMEOVER)
                onGameOverRecord(score)
            }
        }
    }

    Scaffold(
        topBar = {
            GameHeader(
                title = game.title,
                emoji = game.emoji,
                score = score,
                highScore = highScore,
                themeColor = game.themeColor,
                instructions = game.instructions,
                onBack = onBack
            )
        },
        containerColor = DarkBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Izgaraya dokunup blok yerleştir!", fontSize = 14.sp, color = TextSecondary)
            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(DarkSurfaceVariant)
                    .padding(8.dp)
            ) {
                for (r in 0..7) {
                    Row {
                        for (c in 0..7) {
                            val idx = r * 8 + c
                            val filled = grid[idx]
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .padding(2.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (filled) Color(0xFF6366F1) else Color(0xFF1E293B))
                                    .clickable { placeRandomShapeAt(r, c) }
                            )
                        }
                    }
                }
            }
        }

        if (gameOver) {
            GameOverDialog(
                score = score,
                highScore = highScore,
                themeColor = game.themeColor,
                onRestart = {
                    score = 0
                    grid.fill(false)
                    gameOver = false
                },
                onBackToLobby = onBack
            )
        }
    }
}

// ----------------------------------------------------
// 10. MASTERMIND / BULLS & COWS SCREEN
// ----------------------------------------------------
@Composable
fun MastermindScreen(
    highScore: Int,
    soundHelper: SoundHelper,
    onBack: () -> Unit,
    onGameOverRecord: (Int) -> Unit
) {
    val game = GameCatalog.getGameById("bulls_cows")!!
    var score by remember { mutableIntStateOf(0) }
    var gameOver by remember { mutableStateOf(false) }

    val secret = remember { (0..9).shuffled().take(4).joinToString("") }
    var currentGuess by remember { mutableStateOf("") }
    val history = remember { mutableStateListOf<Pair<String, Pair<Int, Int>>>() } // guess to (exact, partial)

    fun submitGuess() {
        if (currentGuess.length == 4) {
            var exact = 0
            var partial = 0
            for (i in 0..3) {
                if (currentGuess[i] == secret[i]) exact++
                else if (secret.contains(currentGuess[i])) partial++
            }
            history.add(currentGuess to (exact to partial))
            currentGuess = ""
            soundHelper.playTone(ToneType.TAP)

            if (exact == 4) {
                score = maxOf(10, 100 - history.size * 10)
                soundHelper.playTone(ToneType.VICTORY)
                gameOver = true
                onGameOverRecord(score)
            }
        }
    }

    Scaffold(
        topBar = {
            GameHeader(
                title = game.title,
                emoji = game.emoji,
                score = score,
                highScore = highScore,
                themeColor = game.themeColor,
                instructions = game.instructions,
                onBack = onBack
            )
        },
        containerColor = DarkBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("4 basamaklı gizli sayıyı tahmin et!", fontSize = 14.sp, color = TextSecondary)
            Spacer(modifier = Modifier.height(8.dp))

            // History list
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkSurfaceVariant)
                    .padding(8.dp)
            ) {
                history.forEach { (guess, clues) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(guess, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
                        Text("🟢 ${clues.first} Tam  🟡 ${clues.second} Yanlış Yer", fontSize = 14.sp, color = NeonAmber)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Input display
            Text(currentGuess.padEnd(4, '_'), fontSize = 32.sp, fontWeight = FontWeight.Black, color = NeonCyan)
            Spacer(modifier = Modifier.height(12.dp))

            // Keypad
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    (1..5).forEach { d ->
                        Button(onClick = { if (currentGuess.length < 4) currentGuess += d.toString() }) {
                            Text("$d")
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    (6..9).forEach { d ->
                        Button(onClick = { if (currentGuess.length < 4) currentGuess += d.toString() }) {
                            Text("$d")
                        }
                    }
                    Button(onClick = { if (currentGuess.isNotEmpty()) currentGuess = currentGuess.dropLast(1) }) {
                        Text("⌫")
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { submitGuess() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = NeonAmber)
            ) {
                Text("Tahmin Et", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }

        if (gameOver) {
            GameOverDialog(
                score = score,
                highScore = highScore,
                themeColor = game.themeColor,
                onRestart = {
                    history.clear()
                    currentGuess = ""
                    score = 0
                    gameOver = false
                },
                onBackToLobby = onBack
            )
        }
    }
}
