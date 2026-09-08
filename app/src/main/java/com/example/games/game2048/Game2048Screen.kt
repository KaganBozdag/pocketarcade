package com.example.games.game2048

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.util.SoundHelper
import com.example.util.ToneType
import kotlin.math.abs
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Game2048Screen(
    highScore: Int,
    soundHelper: SoundHelper,
    onBack: () -> Unit,
    onGameOverRecord: (Int) -> Unit
) {
    var board by remember { mutableStateOf(Array(4) { IntArray(4) }) }
    var previousBoard by remember { mutableStateOf<Array<IntArray>?>(null) }
    var previousScore by remember { mutableIntStateOf(0) }
    var score by remember { mutableIntStateOf(0) }
    var isGameOver by remember { mutableStateOf(false) }
    var isNewRecord by remember { mutableStateOf(false) }

    fun spawnRandomTile(grid: Array<IntArray>) {
        val emptyCells = mutableListOf<Pair<Int, Int>>()
        for (r in 0..3) {
            for (c in 0..3) {
                if (grid[r][c] == 0) emptyCells.add(r to c)
            }
        }
        if (emptyCells.isNotEmpty()) {
            val (r, c) = emptyCells.random()
            grid[r][c] = if (Random.nextInt(10) == 0) 4 else 2
        }
    }

    fun resetGame() {
        val newBoard = Array(4) { IntArray(4) }
        spawnRandomTile(newBoard)
        spawnRandomTile(newBoard)
        board = newBoard
        previousBoard = null
        previousScore = 0
        score = 0
        isGameOver = false
        isNewRecord = false
    }

    LaunchedEffect(Unit) {
        resetGame()
    }

    fun canMove(grid: Array<IntArray>): Boolean {
        for (r in 0..3) {
            for (c in 0..3) {
                if (grid[r][c] == 0) return true
                if (r < 3 && grid[r][c] == grid[r + 1][c]) return true
                if (c < 3 && grid[r][c] == grid[r][c + 1]) return true
            }
        }
        return false
    }

    fun slideLeft(grid: Array<IntArray>): Pair<Array<IntArray>, Int> {
        var gainedScore = 0
        val newGrid = Array(4) { IntArray(4) }

        for (r in 0..3) {
            val row = grid[r].filter { it != 0 }.toMutableList()
            val merged = mutableListOf<Int>()
            var i = 0
            while (i < row.size) {
                if (i + 1 < row.size && row[i] == row[i + 1]) {
                    val mergedVal = row[i] * 2
                    merged.add(mergedVal)
                    gainedScore += mergedVal
                    i += 2
                } else {
                    merged.add(row[i])
                    i++
                }
            }
            for (c in 0 until merged.size) {
                newGrid[r][c] = merged[c]
            }
        }
        return newGrid to gainedScore
    }

    fun rotateClockwise(grid: Array<IntArray>): Array<IntArray> {
        val result = Array(4) { IntArray(4) }
        for (r in 0..3) {
            for (c in 0..3) {
                result[c][3 - r] = grid[r][c]
            }
        }
        return result
    }

    fun rotateCounterClockwise(grid: Array<IntArray>): Array<IntArray> {
        val result = Array(4) { IntArray(4) }
        for (r in 0..3) {
            for (c in 0..3) {
                result[3 - c][r] = grid[r][c]
            }
        }
        return result
    }

    fun areBoardsEqual(b1: Array<IntArray>, b2: Array<IntArray>): Boolean {
        for (r in 0..3) {
            for (c in 0..3) {
                if (b1[r][c] != b2[r][c]) return false
            }
        }
        return true
    }

    fun makeMove(dir: String) {
        if (isGameOver) return

        val rotated: Array<IntArray> = when (dir) {
            "LEFT" -> board
            "RIGHT" -> rotateClockwise(rotateClockwise(board))
            "UP" -> rotateCounterClockwise(board)
            "DOWN" -> rotateClockwise(board)
            else -> board
        }

        val (slid, points) = slideLeft(rotated)

        val unrotated = when (dir) {
            "LEFT" -> slid
            "RIGHT" -> rotateClockwise(rotateClockwise(slid))
            "UP" -> rotateClockwise(slid)
            "DOWN" -> rotateCounterClockwise(slid)
            else -> slid
        }

        if (!areBoardsEqual(board, unrotated)) {
            previousBoard = Array(4) { r -> board[r].clone() }
            previousScore = score

            spawnRandomTile(unrotated)
            board = unrotated
            score += points

            if (points > 0) {
                soundHelper.playTone(ToneType.COIN)
            } else {
                soundHelper.playTone(ToneType.TAP)
            }

            if (!canMove(board)) {
                isGameOver = true
                soundHelper.playTone(ToneType.GAMEOVER)
                if (score > highScore) isNewRecord = true
                onGameOverRecord(score)
            }
        }
    }

    fun undoMove() {
        previousBoard?.let { pb ->
            board = Array(4) { r -> pb[r].clone() }
            score = previousScore
            previousBoard = null
            soundHelper.playTone(ToneType.TAP)
        }
    }

    fun getTileColor(value: Int): Color = when (value) {
        2 -> Color(0xFF334155)
        4 -> Color(0xFF475569)
        8 -> Color(0xFFF97316)
        16 -> Color(0xFFEA580C)
        32 -> Color(0xFFEF4444)
        64 -> Color(0xFFDC2626)
        128 -> Color(0xFFFACC15)
        256 -> Color(0xFFEAB308)
        512 -> Color(0xFF06B6D4)
        1024 -> Color(0xFF8B5CF6)
        2048 -> Color(0xFF10B981)
        else -> Color(0xFFEC4899)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "2048 Sayı Bulmacası",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontSize = 17.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("game2048_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri", tint = TextPrimary)
                    }
                },
                actions = {
                    IconButton(
                        onClick = { undoMove() },
                        enabled = previousBoard != null,
                        modifier = Modifier.testTag("game2048_undo_button")
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Undo,
                            contentDescription = "Geri Al",
                            tint = if (previousBoard != null) NeonCyan else Color(0x33FFFFFF)
                        )
                    }
                    IconButton(onClick = { resetGame() }, modifier = Modifier.testTag("game2048_restart_button")) {
                        Icon(Icons.Default.Refresh, contentDescription = "Yeniden Başlat", tint = NeonGreen)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
            )
        },
        containerColor = DarkBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Scores header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(DarkSurfaceVariant)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("SKOR", fontSize = 11.sp, color = TextMuted, fontWeight = FontWeight.SemiBold)
                    Text("$score", fontSize = 22.sp, color = NeonCyan, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("EN YÜKSEK", fontSize = 11.sp, color = TextMuted, fontWeight = FontWeight.SemiBold)
                    Text("$highScore", fontSize = 22.sp, color = NeonAmber, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 4x4 Game Board with Swipe Detection
            var dragX by remember { mutableFloatStateOf(0f) }
            var dragY by remember { mutableFloatStateOf(0f) }

            Box(
                modifier = Modifier
                    .size(310.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(DarkSurfaceVariant)
                    .border(2.dp, DarkBorder, RoundedCornerShape(18.dp))
                    .padding(8.dp)
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { dragX = 0f; dragY = 0f },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                dragX += dragAmount.x
                                dragY += dragAmount.y
                            },
                            onDragEnd = {
                                if (abs(dragX) > abs(dragY) && abs(dragX) > 40f) {
                                    if (dragX > 0) makeMove("RIGHT") else makeMove("LEFT")
                                } else if (abs(dragY) > 40f) {
                                    if (dragY > 0) makeMove("DOWN") else makeMove("UP")
                                }
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    for (r in 0..3) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            for (c in 0..3) {
                                val value = board[r][c]
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (value == 0) DarkSurface else getTileColor(value)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (value > 0) {
                                        Text(
                                            text = "$value",
                                            fontSize = if (value >= 1024) 16.sp else if (value >= 128) 20.sp else 24.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (value in listOf(128, 256, 512, 1024, 2048)) Color.Black else TextPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Accessible Directional Buttons
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                IconButton(
                    onClick = { makeMove("UP") },
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(DarkSurfaceVariant)
                        .testTag("game2048_btn_up")
                ) {
                    Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Yukarı", tint = NeonCyan, modifier = Modifier.size(30.dp))
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(40.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { makeMove("LEFT") },
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(DarkSurfaceVariant)
                            .testTag("game2048_btn_left")
                    ) {
                        Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Sol", tint = NeonCyan, modifier = Modifier.size(30.dp))
                    }
                    IconButton(
                        onClick = { makeMove("DOWN") },
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(DarkSurfaceVariant)
                            .testTag("game2048_btn_down")
                    ) {
                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Aşağı", tint = NeonCyan, modifier = Modifier.size(30.dp))
                    }
                    IconButton(
                        onClick = { makeMove("RIGHT") },
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(DarkSurfaceVariant)
                            .testTag("game2048_btn_right")
                    ) {
                        Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Sağ", tint = NeonCyan, modifier = Modifier.size(30.dp))
                    }
                }
            }
        }

        // Game Over Dialog
        if (isGameOver) {
            AnimatedVisibility(
                visible = true,
                enter = fadeIn() + scaleIn()
            ) {
                AlertDialog(
                    onDismissRequest = { resetGame() },
                    title = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = if (isNewRecord) "🏆 YENİ 2048 REKORU!" else "🎲 HAMLE KALMADI!",
                                color = if (isNewRecord) NeonAmber else NeonPink,
                                fontWeight = FontWeight.Bold,
                                fontSize = 21.sp
                            )
                        }
                    },
                    text = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            Text("Toplam Puan: $score", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("En Yüksek: ${maxOf(score, highScore)}", fontSize = 14.sp, color = TextSecondary)
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = { resetGame() },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                            modifier = Modifier.testTag("game2048_play_again")
                        ) {
                            Text("Tekrar Oyna", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        OutlinedButton(
                            onClick = onBack,
                            modifier = Modifier.testTag("game2048_exit_lobby")
                        ) {
                            Text("Menü", color = TextSecondary)
                        }
                    },
                    containerColor = DarkSurface,
                    shape = RoundedCornerShape(20.dp)
                )
            }
        }
    }
}
