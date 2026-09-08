package com.example.games.reflex

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GameCatalog
import com.example.ui.components.GameHeader
import com.example.ui.components.GameOverDialog
import com.example.ui.theme.*
import com.example.util.SoundHelper
import com.example.util.ToneType
import kotlinx.coroutines.delay
import kotlin.math.*
import kotlin.random.Random

// ----------------------------------------------------
// 1. COLOR TAP (STROOP EFFECT) SCREEN
// ----------------------------------------------------
@Composable
fun ColorTapScreen(
    highScore: Int,
    soundHelper: SoundHelper,
    onBack: () -> Unit,
    onGameOverRecord: (Int) -> Unit
) {
    val game = GameCatalog.getGameById("color_tap")!!
    var score by remember { mutableIntStateOf(0) }
    var gameOver by remember { mutableStateOf(false) }
    var timeLeft by remember { mutableIntStateOf(20) }

    val colorOptions = listOf(
        "KIRMIZI" to NeonRed,
        "MAVİ" to NeonCyan,
        "YEŞİL" to NeonGreen,
        "SARI" to NeonAmber
    )

    var currentWord by remember { mutableStateOf(colorOptions[0].first) }
    var currentDisplayColor by remember { mutableStateOf(colorOptions[1].second) }

    fun nextRound() {
        val wordItem = colorOptions.random()
        val colorItem = colorOptions.random()
        currentWord = wordItem.first
        currentDisplayColor = colorItem.second
    }

    LaunchedEffect(gameOver) {
        if (!gameOver) {
            nextRound()
            while (timeLeft > 0) {
                delay(1000)
                timeLeft--
            }
            gameOver = true
            soundHelper.playTone(ToneType.GAMEOVER)
            onGameOverRecord(score)
        }
    }

    fun handleAnswer(chosenColor: Color) {
        if (chosenColor == currentDisplayColor) {
            score += 10
            soundHelper.playTone(ToneType.COIN)
            nextRound()
        } else {
            soundHelper.playTone(ToneType.HIT)
            score = maxOf(0, score - 5)
            nextRound()
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
            Text("Kalan Süre: $timeLeft sn", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = NeonAmber)
            Spacer(modifier = Modifier.height(24.dp))

            // Stroop Word Box
            Box(
                modifier = Modifier
                    .size(width = 280.dp, height = 120.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(DarkSurfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = currentWord,
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Black,
                    color = currentDisplayColor
                )
            }

            Spacer(modifier = Modifier.height(36.dp))
            Text("Yazılan kelimeye değil, YAZI RENGİNE bas!", fontSize = 13.sp, color = TextSecondary)
            Spacer(modifier = Modifier.height(16.dp))

            // 4 Color choice buttons
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                colorOptions.forEach { opt ->
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(opt.second)
                            .clickable { handleAnswer(opt.second) }
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
                    timeLeft = 20
                    gameOver = false
                },
                onBackToLobby = onBack
            )
        }
    }
}

// ----------------------------------------------------
// 2. MATH SPRINT SCREEN
// ----------------------------------------------------
@Composable
fun MathSprintScreen(
    highScore: Int,
    soundHelper: SoundHelper,
    onBack: () -> Unit,
    onGameOverRecord: (Int) -> Unit
) {
    val game = GameCatalog.getGameById("math_sprint")!!
    var score by remember { mutableIntStateOf(0) }
    var gameOver by remember { mutableStateOf(false) }
    var timeLeft by remember { mutableIntStateOf(25) }

    var question by remember { mutableStateOf("") }
    var answers by remember { mutableStateOf(listOf<Int>()) }
    var correctAnswer by remember { mutableIntStateOf(0) }

    fun generateMath() {
        val a = Random.nextInt(2, 15)
        val b = Random.nextInt(2, 12)
        val isAdd = Random.nextBoolean()
        if (isAdd) {
            question = "$a + $b = ?"
            correctAnswer = a + b
        } else {
            question = "$a × $b = ?"
            correctAnswer = a * b
        }
        val wrongs = setOf(correctAnswer + Random.nextInt(1, 4), correctAnswer - Random.nextInt(1, 4), correctAnswer + 10)
        answers = (wrongs.take(3) + correctAnswer).shuffled()
    }

    LaunchedEffect(gameOver) {
        if (!gameOver) {
            generateMath()
            while (timeLeft > 0) {
                delay(1000)
                timeLeft--
            }
            gameOver = true
            soundHelper.playTone(ToneType.GAMEOVER)
            onGameOverRecord(score)
        }
    }

    fun chooseAnswer(ans: Int) {
        if (ans == correctAnswer) {
            score += 15
            soundHelper.playTone(ToneType.COIN)
        } else {
            soundHelper.playTone(ToneType.HIT)
            score = maxOf(0, score - 5)
        }
        generateMath()
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
            Text("Süre: $timeLeft sn", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = NeonAmber)
            Spacer(modifier = Modifier.height(24.dp))

            Text(question, fontSize = 42.sp, fontWeight = FontWeight.Black, color = NeonCyan)
            Spacer(modifier = Modifier.height(36.dp))

            // 4 answer choices in 2x2
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                for (i in 0..1) {
                    Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        for (j in 0..1) {
                            val ans = answers.getOrNull(i * 2 + j) ?: 0
                            Button(
                                onClick = { chooseAnswer(ans) },
                                modifier = Modifier.size(width = 130.dp, height = 64.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Text("$ans", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
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
                    timeLeft = 25
                    gameOver = false
                },
                onBackToLobby = onBack
            )
        }
    }
}

// ----------------------------------------------------
// 3. PIANO TILES SCREEN
// ----------------------------------------------------
data class PianoTile(val col: Int, var y: Float)

@Composable
fun PianoTilesScreen(
    highScore: Int,
    soundHelper: SoundHelper,
    onBack: () -> Unit,
    onGameOverRecord: (Int) -> Unit
) {
    val game = GameCatalog.getGameById("piano_tiles")!!
    var score by remember { mutableIntStateOf(0) }
    var gameOver by remember { mutableStateOf(false) }

    val tiles = remember {
        mutableStateListOf<PianoTile>().apply {
            add(PianoTile(Random.nextInt(4), 0.1f))
            add(PianoTile(Random.nextInt(4), -0.2f))
            add(PianoTile(Random.nextInt(4), -0.5f))
        }
    }

    LaunchedEffect(gameOver) {
        if (!gameOver) {
            while (true) {
                delay(30)
                tiles.forEach { it.y += 0.015f }
                // If a tile falls below bottom
                if (tiles.any { it.y > 1.0f }) {
                    gameOver = true
                    soundHelper.playTone(ToneType.GAMEOVER)
                    onGameOverRecord(score)
                }
            }
        }
    }

    fun tapColumn(col: Int) {
        val hit = tiles.find { it.col == col && it.y in 0.55f..0.95f }
        if (hit != null) {
            tiles.remove(hit)
            val topY = tiles.minOfOrNull { it.y } ?: 0f
            tiles.add(PianoTile(Random.nextInt(4), topY - 0.32f))
            score += 10
            soundHelper.playTone(ToneType.TAP)
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
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            for (c in 0..3) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(if (c % 2 == 0) Color(0xFF0F172A) else Color(0xFF1E293B))
                        .clickable { tapColumn(c) }
                ) {
                    tiles.filter { it.col == c }.forEach { tile ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .offset(y = (tile.y * 500).dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(NeonCyan)
                        )
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
                    tiles.clear()
                    tiles.add(PianoTile(Random.nextInt(4), 0.1f))
                    tiles.add(PianoTile(Random.nextInt(4), -0.2f))
                    tiles.add(PianoTile(Random.nextInt(4), -0.5f))
                    gameOver = false
                },
                onBackToLobby = onBack
            )
        }
    }
}

// ----------------------------------------------------
// 4. WHACK A MOLE SCREEN
// ----------------------------------------------------
@Composable
fun WhackMoleScreen(
    highScore: Int,
    soundHelper: SoundHelper,
    onBack: () -> Unit,
    onGameOverRecord: (Int) -> Unit
) {
    val game = GameCatalog.getGameById("whack_mole")!!
    var score by remember { mutableIntStateOf(0) }
    var gameOver by remember { mutableStateOf(false) }
    var activeMoleIdx by remember { mutableIntStateOf(Random.nextInt(9)) }
    var timeLeft by remember { mutableIntStateOf(20) }

    LaunchedEffect(gameOver) {
        if (!gameOver) {
            while (timeLeft > 0) {
                delay(700)
                activeMoleIdx = Random.nextInt(9)
                timeLeft--
            }
            gameOver = true
            soundHelper.playTone(ToneType.GAMEOVER)
            onGameOverRecord(score)
        }
    }

    fun whackHole(idx: Int) {
        if (idx == activeMoleIdx) {
            score += 10
            soundHelper.playTone(ToneType.HIT)
            activeMoleIdx = -1
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
            Text("Süre: $timeLeft sn", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = NeonAmber)
            Spacer(modifier = Modifier.height(24.dp))

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                for (r in 0..2) {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        for (c in 0..2) {
                            val idx = r * 3 + c
                            val hasMole = idx == activeMoleIdx
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF334155))
                                    .clickable { whackHole(idx) },
                                contentAlignment = Alignment.Center
                            ) {
                                if (hasMole) {
                                    Text("🦔", fontSize = 36.sp)
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
                    timeLeft = 20
                    gameOver = false
                },
                onBackToLobby = onBack
            )
        }
    }
}

// ----------------------------------------------------
// 5. KNIFE HIT SCREEN
// ----------------------------------------------------
@Composable
fun KnifeHitScreen(
    highScore: Int,
    soundHelper: SoundHelper,
    onBack: () -> Unit,
    onGameOverRecord: (Int) -> Unit
) {
    val game = GameCatalog.getGameById("knife_hit")!!
    var score by remember { mutableIntStateOf(0) }
    var gameOver by remember { mutableStateOf(false) }

    var logRotation by remember { mutableFloatStateOf(0f) }
    val embeddedKnives = remember { mutableStateListOf<Float>() }

    LaunchedEffect(gameOver) {
        if (!gameOver) {
            while (true) {
                delay(30)
                logRotation = (logRotation + 3f) % 360f
            }
        }
    }

    fun throwKnife() {
        if (gameOver) return
        val currentAngle = logRotation
        // Check if any existing knife is too close to this angle
        val collision = embeddedKnives.any { abs(it - currentAngle) < 18f || abs(it - currentAngle) > 342f }
        if (collision) {
            gameOver = true
            soundHelper.playTone(ToneType.HIT)
            onGameOverRecord(score)
        } else {
            embeddedKnives.add(currentAngle)
            score += 10
            soundHelper.playTone(ToneType.TAP)
            if (embeddedKnives.size >= 8) {
                score += 50
                embeddedKnives.clear()
                soundHelper.playTone(ToneType.VICTORY)
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
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Spinning Log Canvas
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF854D0E)),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val cx = size.width / 2
                    val cy = size.height / 2
                    val r = size.width / 2

                    embeddedKnives.forEach { angleDeg ->
                        val rad = Math.toRadians((angleDeg - logRotation).toDouble())
                        val kx = cx + r * cos(rad).toFloat()
                        val ky = cy + r * sin(rad).toFloat()
                        drawCircle(color = Color.White, radius = 6.dp.toPx(), center = Offset(kx, ky))
                    }
                }
                Text("🎯", fontSize = 48.sp)
            }

            // Throw Button
            Button(
                onClick = { throwKnife() },
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(56.dp)
                    .padding(bottom = 16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("🗡️ Bıçağı Fırlat", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }

        if (gameOver) {
            GameOverDialog(
                score = score,
                highScore = highScore,
                themeColor = game.themeColor,
                onRestart = {
                    score = 0
                    embeddedKnives.clear()
                    gameOver = false
                },
                onBackToLobby = onBack
            )
        }
    }
}

// ----------------------------------------------------
// 6. TOWER STACK SCREEN
// ----------------------------------------------------
@Composable
fun TowerStackScreen(
    highScore: Int,
    soundHelper: SoundHelper,
    onBack: () -> Unit,
    onGameOverRecord: (Int) -> Unit
) {
    val game = GameCatalog.getGameById("tower_stack")!!
    var score by remember { mutableIntStateOf(0) }
    var gameOver by remember { mutableStateOf(false) }

    var blockX by remember { mutableFloatStateOf(0.1f) }
    var dir by remember { mutableFloatStateOf(0.03f) }
    var currentWidth by remember { mutableFloatStateOf(0.4f) }
    var prevX by remember { mutableFloatStateOf(0.3f) }

    LaunchedEffect(gameOver) {
        if (!gameOver) {
            while (true) {
                delay(30)
                blockX += dir
                if (blockX <= 0.05f || blockX + currentWidth >= 0.95f) {
                    dir = -dir
                }
            }
        }
    }

    fun placeBlock() {
        val overlap = minOf(blockX + currentWidth, prevX + currentWidth) - maxOf(blockX, prevX)
        if (overlap > 0.05f) {
            score++
            currentWidth = overlap
            prevX = maxOf(blockX, prevX)
            soundHelper.playTone(ToneType.TAP)
        } else {
            gameOver = true
            soundHelper.playTone(ToneType.GAMEOVER)
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Canvas(modifier = Modifier.weight(1f).fillMaxWidth()) {
                val w = size.width
                val h = size.height

                // Previous base block
                drawRoundRect(
                    color = NeonCyan,
                    topLeft = Offset(prevX * w, h * 0.7f),
                    size = Size(currentWidth * w, 24.dp.toPx()),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
                )

                // Moving block
                drawRoundRect(
                    color = NeonAmber,
                    topLeft = Offset(blockX * w, h * 0.62f),
                    size = Size(currentWidth * w, 24.dp.toPx()),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
                )
            }

            Button(
                onClick = { placeBlock() },
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(56.dp)
                    .padding(bottom = 16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("DURDUR & YIĞ", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }

        if (gameOver) {
            GameOverDialog(
                score = score,
                highScore = highScore,
                themeColor = game.themeColor,
                onRestart = {
                    score = 0
                    currentWidth = 0.4f
                    prevX = 0.3f
                    blockX = 0.1f
                    gameOver = false
                },
                onBackToLobby = onBack
            )
        }
    }
}

// ----------------------------------------------------
// 7. TRAFFIC DODGE SCREEN
// ----------------------------------------------------
@Composable
fun TrafficDodgeScreen(
    highScore: Int,
    soundHelper: SoundHelper,
    onBack: () -> Unit,
    onGameOverRecord: (Int) -> Unit
) {
    val game = GameCatalog.getGameById("traffic_dodge")!!
    var score by remember { mutableIntStateOf(0) }
    var gameOver by remember { mutableStateOf(false) }

    var playerLane by remember { mutableIntStateOf(1) } // 0, 1, 2
    var enemyLane by remember { mutableIntStateOf(Random.nextInt(3)) }
    var enemyY by remember { mutableFloatStateOf(-0.2f) }

    LaunchedEffect(gameOver) {
        if (!gameOver) {
            while (true) {
                delay(30)
                enemyY += 0.025f
                score++
                if (enemyY > 1.1f) {
                    enemyY = -0.2f
                    enemyLane = Random.nextInt(3)
                }

                if (enemyY in 0.75f..0.92f && enemyLane == playerLane) {
                    gameOver = true
                    soundHelper.playTone(ToneType.HIT)
                    onGameOverRecord(score)
                }
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
        ) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val laneW = w / 3f

                    // Lane dividers
                    drawLine(color = Color.Gray, start = Offset(laneW, 0f), end = Offset(laneW, h), strokeWidth = 2f)
                    drawLine(color = Color.Gray, start = Offset(laneW * 2, 0f), end = Offset(laneW * 2, h), strokeWidth = 2f)

                    // Enemy car
                    drawRoundRect(
                        color = NeonRed,
                        topLeft = Offset(enemyLane * laneW + laneW * 0.2f, enemyY * h),
                        size = Size(laneW * 0.6f, 60.dp.toPx()),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f)
                    )

                    // Player car
                    drawRoundRect(
                        color = NeonCyan,
                        topLeft = Offset(playerLane * laneW + laneW * 0.2f, h * 0.82f),
                        size = Size(laneW * 0.6f, 60.dp.toPx()),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f)
                    )
                }
            }

            // Left / Right lane change buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = { if (playerLane > 0) playerLane-- },
                    colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Sol", tint = TextPrimary)
                }
                Button(
                    onClick = { if (playerLane < 2) playerLane++ },
                    colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Sağ", tint = TextPrimary)
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
                    playerLane = 1
                    enemyY = -0.2f
                    gameOver = false
                },
                onBackToLobby = onBack
            )
        }
    }
}

// ----------------------------------------------------
// 8. COIN CATCHER SCREEN
// ----------------------------------------------------
data class FallingCoin(var x: Float, var y: Float, val isBomb: Boolean)

@Composable
fun CoinCatcherScreen(
    highScore: Int,
    soundHelper: SoundHelper,
    onBack: () -> Unit,
    onGameOverRecord: (Int) -> Unit
) {
    val game = GameCatalog.getGameById("coin_catcher")!!
    var score by remember { mutableIntStateOf(0) }
    var gameOver by remember { mutableStateOf(false) }

    var basketX by remember { mutableFloatStateOf(0.5f) }
    val items = remember { mutableStateListOf<FallingCoin>() }

    LaunchedEffect(gameOver) {
        if (!gameOver) {
            items.clear()
            while (true) {
                delay(30)
                if (Random.nextInt(25) == 0) {
                    items.add(FallingCoin(Random.nextFloat() * 0.8f + 0.1f, -0.05f, Random.nextInt(4) == 0))
                }

                val it = items.iterator()
                while (it.hasNext()) {
                    val item = it.next()
                    item.y += 0.02f
                    // Catch
                    if (item.y in 0.85f..0.92f && abs(item.x - basketX) < 0.12f) {
                        if (item.isBomb) {
                            gameOver = true
                            soundHelper.playTone(ToneType.HIT)
                            onGameOverRecord(score)
                        } else {
                            score += 10
                            soundHelper.playTone(ToneType.COIN)
                            it.remove()
                        }
                    } else if (item.y > 1.0f) {
                        it.remove()
                    }
                }
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                        basketX = (change.position.x / size.width).coerceIn(0.1f, 0.9f)
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                items.forEach { item ->
                    if (item.isBomb) {
                        drawCircle(color = NeonRed, radius = 12.dp.toPx(), center = Offset(item.x * w, item.y * h))
                    } else {
                        drawCircle(color = Color(0xFFFBBF24), radius = 10.dp.toPx(), center = Offset(item.x * w, item.y * h))
                    }
                }

                // Basket
                drawRoundRect(
                    color = NeonAmber,
                    topLeft = Offset((basketX - 0.12f) * w, h * 0.88f),
                    size = Size(0.24f * w, 16.dp.toPx()),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
                )
            }
        }

        if (gameOver) {
            GameOverDialog(
                score = score,
                highScore = highScore,
                themeColor = game.themeColor,
                onRestart = {
                    score = 0
                    items.clear()
                    gameOver = false
                },
                onBackToLobby = onBack
            )
        }
    }
}

// ----------------------------------------------------
// 9. BALANCE BALL SCREEN
// ----------------------------------------------------
@Composable
fun BalanceBallScreen(
    highScore: Int,
    soundHelper: SoundHelper,
    onBack: () -> Unit,
    onGameOverRecord: (Int) -> Unit
) {
    val game = GameCatalog.getGameById("balance_ball")!!
    var score by remember { mutableIntStateOf(0) }
    var gameOver by remember { mutableStateOf(false) }

    var ballX by remember { mutableFloatStateOf(0.5f) }
    var ballVx by remember { mutableFloatStateOf(0f) }
    var tiltAngle by remember { mutableFloatStateOf(0f) } // in degrees -15 to +15

    LaunchedEffect(gameOver) {
        if (!gameOver) {
            while (true) {
                delay(30)
                score++
                ballVx += tiltAngle * 0.0003f
                ballX += ballVx

                if (ballX <= 0.08f || ballX >= 0.92f) {
                    gameOver = true
                    soundHelper.playTone(ToneType.GAMEOVER)
                    onGameOverRecord(score)
                }
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
        ) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    // Tilting beam
                    drawLine(
                        color = Color.LightGray,
                        start = Offset(w * 0.1f, h * 0.6f + tiltAngle * 4),
                        end = Offset(w * 0.9f, h * 0.6f - tiltAngle * 4),
                        strokeWidth = 10f
                    )

                    // Ball
                    drawCircle(
                        color = NeonCyan,
                        radius = 16.dp.toPx(),
                        center = Offset(ballX * w, h * 0.6f - 18.dp.toPx())
                    )
                }
            }

            // Tilt controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(onClick = { tiltAngle = (tiltAngle - 3f).coerceIn(-15f, 15f) }) {
                    Text("⬅️ Sola Eğ")
                }
                Button(onClick = { tiltAngle = 0f }) {
                    Text("⚖️ Düzelt")
                }
                Button(onClick = { tiltAngle = (tiltAngle + 3f).coerceIn(-15f, 15f) }) {
                    Text("Sağa Eğ ➡️")
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
                    ballX = 0.5f
                    ballVx = 0f
                    tiltAngle = 0f
                    gameOver = false
                },
                onBackToLobby = onBack
            )
        }
    }
}

// ----------------------------------------------------
// 10. SPEED CLICK (CPS RUSH) SCREEN
// ----------------------------------------------------
@Composable
fun SpeedClickScreen(
    highScore: Int,
    soundHelper: SoundHelper,
    onBack: () -> Unit,
    onGameOverRecord: (Int) -> Unit
) {
    val game = GameCatalog.getGameById("speed_click")!!
    var taps by remember { mutableIntStateOf(0) }
    var gameOver by remember { mutableStateOf(false) }
    var isRunning by remember { mutableStateOf(false) }
    var timeLeft by remember { mutableIntStateOf(10) }

    LaunchedEffect(isRunning) {
        if (isRunning) {
            while (timeLeft > 0) {
                delay(1000)
                timeLeft--
            }
            isRunning = false
            gameOver = true
            soundHelper.playTone(ToneType.VICTORY)
            onGameOverRecord(taps)
        }
    }

    fun handleTap() {
        if (!isRunning && !gameOver) {
            isRunning = true
        }
        if (isRunning) {
            taps++
            soundHelper.playTone(ToneType.TAP)
        }
    }

    Scaffold(
        topBar = {
            GameHeader(
                title = game.title,
                emoji = game.emoji,
                score = taps,
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
            Text("Kalan Süre: $timeLeft sn", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = NeonAmber)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Tıklama: $taps", fontSize = 48.sp, fontWeight = FontWeight.Black, color = NeonCyan)
            Spacer(modifier = Modifier.height(32.dp))

            Box(
                modifier = Modifier
                    .size(180.dp)
                    .clip(CircleShape)
                    .background(NeonCyan)
                    .clickable { handleTap() },
                contentAlignment = Alignment.Center
            ) {
                Text("TIKLA!", fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color.Black)
            }
        }

        if (gameOver) {
            GameOverDialog(
                score = taps,
                highScore = highScore,
                themeColor = game.themeColor,
                onRestart = {
                    taps = 0
                    timeLeft = 10
                    gameOver = false
                },
                onBackToLobby = onBack
            )
        }
    }
}
