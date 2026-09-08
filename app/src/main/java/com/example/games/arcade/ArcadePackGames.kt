package com.example.games.arcade

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
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
// 1. RETRO PONG GAME SCREEN
// ----------------------------------------------------
@Composable
fun PongGameScreen(
    highScore: Int,
    soundHelper: SoundHelper,
    onBack: () -> Unit,
    onGameOverRecord: (Int) -> Unit
) {
    val game = GameCatalog.getGameById("pong")!!
    var playerScore by remember { mutableIntStateOf(0) }
    var aiScore by remember { mutableIntStateOf(0) }
    var gameOver by remember { mutableStateOf(false) }

    var ballX by remember { mutableFloatStateOf(0.5f) }
    var ballY by remember { mutableFloatStateOf(0.5f) }
    var ballVx by remember { mutableFloatStateOf(0.015f) }
    var ballVy by remember { mutableFloatStateOf(0.02f) }

    var playerPaddleX by remember { mutableFloatStateOf(0.5f) }
    var aiPaddleX by remember { mutableFloatStateOf(0.5f) }
    val paddleWidth = 0.28f

    LaunchedEffect(gameOver) {
        if (!gameOver) {
            while (true) {
                delay(30)
                // Update ball
                ballX += ballVx
                ballY += ballVy

                // Bounce walls
                if (ballX <= 0.04f || ballX >= 0.96f) {
                    ballVx = -ballVx
                    soundHelper.playTone(ToneType.TAP)
                }

                // AI Paddle movement tracking ball
                val diff = ballX - aiPaddleX
                aiPaddleX = (aiPaddleX + diff * 0.12f).coerceIn(paddleWidth / 2, 1f - paddleWidth / 2)

                // Player paddle collision (bottom, y ~= 0.90)
                if (ballY >= 0.88f && ballY <= 0.92f && ballVy > 0) {
                    if (abs(ballX - playerPaddleX) <= paddleWidth / 2 + 0.04f) {
                        ballVy = -abs(ballVy) * 1.05f
                        ballVx += (ballX - playerPaddleX) * 0.03f
                        playerScore++
                        soundHelper.playTone(ToneType.COIN)
                    }
                }

                // AI paddle collision (top, y ~= 0.10)
                if (ballY <= 0.12f && ballY >= 0.08f && ballVy < 0) {
                    if (abs(ballX - aiPaddleX) <= paddleWidth / 2 + 0.04f) {
                        ballVy = abs(ballVy)
                        soundHelper.playTone(ToneType.TAP)
                    }
                }

                // Miss player (bottom)
                if (ballY >= 1.0f) {
                    aiScore++
                    soundHelper.playTone(ToneType.GAMEOVER)
                    gameOver = true
                    onGameOverRecord(playerScore)
                }

                // Miss AI (top) -> point for player!
                if (ballY <= 0f) {
                    playerScore += 3
                    ballX = 0.5f
                    ballY = 0.5f
                    ballVy = 0.02f
                    soundHelper.playTone(ToneType.POWERUP)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            GameHeader(
                title = game.title,
                emoji = game.emoji,
                score = playerScore,
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
                        val newX = (change.position.x / size.width).coerceIn(paddleWidth / 2, 1f - paddleWidth / 2)
                        playerPaddleX = newX
                    }
                }
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        playerPaddleX = (offset.x / size.width).coerceIn(paddleWidth / 2, 1f - paddleWidth / 2)
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Center line
                drawLine(
                    color = Color.DarkGray,
                    start = Offset(0f, h / 2),
                    end = Offset(w, h / 2),
                    strokeWidth = 2f
                )

                // AI Paddle (top)
                drawRoundRect(
                    color = NeonRed,
                    topLeft = Offset((aiPaddleX - paddleWidth / 2) * w, h * 0.08f),
                    size = Size(paddleWidth * w, 16.dp.toPx()),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
                )

                // Player Paddle (bottom)
                drawRoundRect(
                    color = NeonCyan,
                    topLeft = Offset((playerPaddleX - paddleWidth / 2) * w, h * 0.88f),
                    size = Size(paddleWidth * w, 16.dp.toPx()),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
                )

                // Ball
                drawCircle(
                    color = NeonGreen,
                    radius = 12.dp.toPx(),
                    center = Offset(ballX * w, ballY * h)
                )
            }

            Text(
                text = "Parmağını alt kısımda sağa sola kaydır",
                color = TextMuted,
                fontSize = 12.sp,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp)
            )
        }

        if (gameOver) {
            GameOverDialog(
                score = playerScore,
                highScore = highScore,
                themeColor = game.themeColor,
                onRestart = {
                    playerScore = 0
                    aiScore = 0
                    ballX = 0.5f
                    ballY = 0.5f
                    ballVx = 0.015f
                    ballVy = 0.02f
                    gameOver = false
                },
                onBackToLobby = onBack
            )
        }
    }
}

// ----------------------------------------------------
// 2. BREAKOUT GAME SCREEN
// ----------------------------------------------------
data class Brick(val x: Float, val y: Float, val width: Float, val height: Float, val color: Color, var alive: Boolean = true)

@Composable
fun BreakoutGameScreen(
    highScore: Int,
    soundHelper: SoundHelper,
    onBack: () -> Unit,
    onGameOverRecord: (Int) -> Unit
) {
    val game = GameCatalog.getGameById("breakout")!!
    var score by remember { mutableIntStateOf(0) }
    var gameOver by remember { mutableStateOf(false) }

    var paddleX by remember { mutableFloatStateOf(0.5f) }
    val paddleWidth = 0.30f

    var ballX by remember { mutableFloatStateOf(0.5f) }
    var ballY by remember { mutableFloatStateOf(0.7f) }
    var ballVx by remember { mutableFloatStateOf(0.012f) }
    var ballVy by remember { mutableFloatStateOf(-0.018f) }

    val rows = 5
    val cols = 6
    val colors = listOf(NeonRed, Color(0xFFF97316), NeonAmber, NeonGreen, NeonCyan)

    val bricks = remember {
        mutableStateListOf<Brick>().apply {
            for (r in 0 until rows) {
                for (c in 0 until cols) {
                    val bw = 0.90f / cols
                    val bh = 0.035f
                    val bx = 0.05f + c * bw
                    val by = 0.08f + r * (bh + 0.01f)
                    add(Brick(bx, by, bw - 0.01f, bh, colors[r % colors.size]))
                }
            }
        }
    }

    LaunchedEffect(gameOver) {
        if (!gameOver) {
            while (true) {
                delay(30)
                ballX += ballVx
                ballY += ballVy

                // Walls
                if (ballX <= 0.03f || ballX >= 0.97f) {
                    ballVx = -ballVx
                    soundHelper.playTone(ToneType.TAP)
                }
                if (ballY <= 0.02f) {
                    ballVy = abs(ballVy)
                    soundHelper.playTone(ToneType.TAP)
                }

                // Paddle collision
                if (ballY >= 0.86f && ballY <= 0.90f && ballVy > 0) {
                    if (abs(ballX - paddleX) <= paddleWidth / 2 + 0.03f) {
                        ballVy = -abs(ballVy)
                        ballVx += (ballX - paddleX) * 0.04f
                        soundHelper.playTone(ToneType.TAP)
                    }
                }

                // Brick collisions
                bricks.forEach { brick ->
                    if (brick.alive) {
                        if (ballX >= brick.x && ballX <= brick.x + brick.width &&
                            ballY >= brick.y && ballY <= brick.y + brick.height
                        ) {
                            brick.alive = false
                            ballVy = -ballVy
                            score += 10
                            soundHelper.playTone(ToneType.COIN)
                        }
                    }
                }

                // Ball dropped
                if (ballY >= 1.0f) {
                    gameOver = true
                    soundHelper.playTone(ToneType.GAMEOVER)
                    onGameOverRecord(score)
                }

                // Check victory: all bricks cleared
                if (bricks.none { it.alive }) {
                    score += 100
                    soundHelper.playTone(ToneType.VICTORY)
                    // Reset bricks
                    bricks.forEach { it.alive = true }
                    ballX = 0.5f
                    ballY = 0.7f
                    ballVy = -0.02f
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
                        paddleX = (change.position.x / size.width).coerceIn(paddleWidth / 2, 1f - paddleWidth / 2)
                    }
                }
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        paddleX = (offset.x / size.width).coerceIn(paddleWidth / 2, 1f - paddleWidth / 2)
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Draw bricks
                bricks.forEach { brick ->
                    if (brick.alive) {
                        drawRoundRect(
                            color = brick.color,
                            topLeft = Offset(brick.x * w, brick.y * h),
                            size = Size(brick.width * w, brick.height * h),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
                        )
                    }
                }

                // Draw paddle
                drawRoundRect(
                    color = NeonAmber,
                    topLeft = Offset((paddleX - paddleWidth / 2) * w, h * 0.88f),
                    size = Size(paddleWidth * w, 14.dp.toPx()),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(7f, 7f)
                )

                // Draw ball
                drawCircle(
                    color = Color.White,
                    radius = 9.dp.toPx(),
                    center = Offset(ballX * w, ballY * h)
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
                    bricks.forEach { it.alive = true }
                    ballX = 0.5f
                    ballY = 0.7f
                    ballVx = 0.012f
                    ballVy = -0.018f
                    paddleX = 0.5f
                    gameOver = false
                },
                onBackToLobby = onBack
            )
        }
    }
}

// ----------------------------------------------------
// 3. SKY JUMP SCREEN (DOODLE STYLE)
// ----------------------------------------------------
data class Platform(var x: Float, var y: Float, val w: Float = 0.22f)

@Composable
fun SkyJumpScreen(
    highScore: Int,
    soundHelper: SoundHelper,
    onBack: () -> Unit,
    onGameOverRecord: (Int) -> Unit
) {
    val game = GameCatalog.getGameById("sky_jump")!!
    var score by remember { mutableIntStateOf(0) }
    var gameOver by remember { mutableStateOf(false) }

    var playerX by remember { mutableFloatStateOf(0.5f) }
    var playerY by remember { mutableFloatStateOf(0.7f) }
    var vy by remember { mutableFloatStateOf(-0.022f) }

    val platforms = remember {
        mutableStateListOf<Platform>().apply {
            add(Platform(0.4f, 0.85f))
            add(Platform(0.2f, 0.65f))
            add(Platform(0.6f, 0.45f))
            add(Platform(0.3f, 0.25f))
            add(Platform(0.7f, 0.05f))
        }
    }

    LaunchedEffect(gameOver) {
        if (!gameOver) {
            while (true) {
                delay(30)
                vy += 0.0012f // gravity
                playerY += vy

                // Scroll when player reaches upper half
                if (playerY < 0.45f) {
                    val diff = 0.45f - playerY
                    playerY = 0.45f
                    score += (diff * 100).toInt()
                    platforms.forEach { it.y += diff }
                }

                // Recycle platforms falling below screen
                platforms.forEach { plat ->
                    if (plat.y > 1.0f) {
                        plat.y = 0.0f
                        plat.x = Random.nextFloat() * 0.75f
                    }
                }

                // Bounce on platforms when falling
                if (vy > 0) {
                    platforms.forEach { plat ->
                        if (playerY >= plat.y - 0.03f && playerY <= plat.y + 0.02f &&
                            playerX >= plat.x - 0.04f && playerX <= plat.x + plat.w + 0.04f
                        ) {
                            vy = -0.024f
                            soundHelper.playTone(ToneType.TAP)
                        }
                    }
                }

                // Fall off screen bottom
                if (playerY > 1.05f) {
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                        playerX = (change.position.x / size.width).coerceIn(0.05f, 0.95f)
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Draw platforms
                platforms.forEach { plat ->
                    drawRoundRect(
                        color = NeonGreen,
                        topLeft = Offset(plat.x * w, plat.y * h),
                        size = Size(plat.w * w, 12.dp.toPx()),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
                    )
                }

                // Draw jumper alien
                drawCircle(
                    color = NeonAmber,
                    radius = 16.dp.toPx(),
                    center = Offset(playerX * w, playerY * h)
                )
                // Eyes
                drawCircle(
                    color = Color.Black,
                    radius = 3.dp.toPx(),
                    center = Offset(playerX * w - 5.dp.toPx(), playerY * h - 4.dp.toPx())
                )
                drawCircle(
                    color = Color.Black,
                    radius = 3.dp.toPx(),
                    center = Offset(playerX * w + 5.dp.toPx(), playerY * h - 4.dp.toPx())
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
                    playerX = 0.5f
                    playerY = 0.7f
                    vy = -0.022f
                    platforms[0] = Platform(0.4f, 0.85f)
                    platforms[1] = Platform(0.2f, 0.65f)
                    platforms[2] = Platform(0.6f, 0.45f)
                    platforms[3] = Platform(0.3f, 0.25f)
                    platforms[4] = Platform(0.7f, 0.05f)
                    gameOver = false
                },
                onBackToLobby = onBack
            )
        }
    }
}

// ----------------------------------------------------
// 4. ASTEROID DODGE SCREEN
// ----------------------------------------------------
data class Meteor(var x: Float, var y: Float, val radius: Float, val speed: Float)

@Composable
fun AsteroidDodgeScreen(
    highScore: Int,
    soundHelper: SoundHelper,
    onBack: () -> Unit,
    onGameOverRecord: (Int) -> Unit
) {
    val game = GameCatalog.getGameById("asteroid_dodge")!!
    var score by remember { mutableIntStateOf(0) }
    var gameOver by remember { mutableStateOf(false) }

    var shipX by remember { mutableFloatStateOf(0.5f) }
    val meteors = remember { mutableStateListOf<Meteor>() }

    LaunchedEffect(gameOver) {
        if (!gameOver) {
            meteors.clear()
            for (i in 0..6) {
                meteors.add(Meteor(Random.nextFloat(), -Random.nextFloat() * 0.8f, Random.nextFloat() * 0.04f + 0.03f, Random.nextFloat() * 0.012f + 0.01f))
            }
            while (true) {
                delay(30)
                score++
                meteors.forEach { m ->
                    m.y += m.speed
                    if (m.y > 1.05f) {
                        m.y = -0.05f
                        m.x = Random.nextFloat()
                    }

                    // Collision with ship at (shipX, 0.88)
                    val dx = m.x - shipX
                    val dy = m.y - 0.88f
                    val dist = sqrt(dx * dx + dy * dy)
                    if (dist < m.radius + 0.04f) {
                        gameOver = true
                        soundHelper.playTone(ToneType.HIT)
                        onGameOverRecord(score)
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
                        shipX = (change.position.x / size.width).coerceIn(0.06f, 0.94f)
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Draw Meteors
                meteors.forEach { m ->
                    drawCircle(
                        color = Color(0xFFFB923C),
                        radius = m.radius * w,
                        center = Offset(m.x * w, m.y * h)
                    )
                }

                // Draw Ship
                val sx = shipX * w
                val sy = 0.88f * h
                drawCircle(color = NeonCyan, radius = 18.dp.toPx(), center = Offset(sx, sy))
                drawCircle(color = Color.White, radius = 8.dp.toPx(), center = Offset(sx, sy - 6.dp.toPx()))
            }
        }

        if (gameOver) {
            GameOverDialog(
                score = score,
                highScore = highScore,
                themeColor = game.themeColor,
                onRestart = {
                    score = 0
                    shipX = 0.5f
                    gameOver = false
                },
                onBackToLobby = onBack
            )
        }
    }
}

// ----------------------------------------------------
// 5. CROSSY HOP SCREEN
// ----------------------------------------------------
@Composable
fun CrossyHopScreen(
    highScore: Int,
    soundHelper: SoundHelper,
    onBack: () -> Unit,
    onGameOverRecord: (Int) -> Unit
) {
    val game = GameCatalog.getGameById("frogger")!!
    var score by remember { mutableIntStateOf(0) }
    var gameOver by remember { mutableStateOf(false) }

    var frogRow by remember { mutableIntStateOf(8) } // 0 = goal, 8 = start
    var frogCol by remember { mutableIntStateOf(2) } // 0..4

    // 3 car lanes (rows 2, 4, 6)
    var carPos1 by remember { mutableFloatStateOf(0f) }
    var carPos2 by remember { mutableFloatStateOf(0.5f) }
    var carPos3 by remember { mutableFloatStateOf(0.2f) }

    LaunchedEffect(gameOver) {
        if (!gameOver) {
            while (true) {
                delay(30)
                carPos1 = (carPos1 + 0.02f) % 1f
                carPos2 = (carPos2 - 0.025f + 1f) % 1f
                carPos3 = (carPos3 + 0.018f) % 1f

                val frogX = (frogCol + 0.5f) / 5f
                // Collision checks
                if (frogRow == 2 && abs(carPos1 - frogX) < 0.12f) {
                    gameOver = true
                    soundHelper.playTone(ToneType.HIT)
                    onGameOverRecord(score)
                }
                if (frogRow == 4 && abs(carPos2 - frogX) < 0.12f) {
                    gameOver = true
                    soundHelper.playTone(ToneType.HIT)
                    onGameOverRecord(score)
                }
                if (frogRow == 6 && abs(carPos3 - frogX) < 0.12f) {
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
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Road Canvas
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val rowH = h / 9f

                    // Draw lanes
                    for (r in 0..8) {
                        val color = when (r) {
                            0, 8 -> Color(0xFF15803D) // grass safe
                            1, 3, 5, 7 -> Color(0xFF334155) // safe sidewalks
                            else -> Color(0xFF1E293B) // asphalt road
                        }
                        drawRect(color = color, topLeft = Offset(0f, r * rowH), size = Size(w, rowH))
                    }

                    // Cars
                    drawRoundRect(color = NeonRed, topLeft = Offset(carPos1 * w, 2 * rowH + 6.dp.toPx()), size = Size(60.dp.toPx(), rowH - 12.dp.toPx()), cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f))
                    drawRoundRect(color = NeonAmber, topLeft = Offset(carPos2 * w, 4 * rowH + 6.dp.toPx()), size = Size(60.dp.toPx(), rowH - 12.dp.toPx()), cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f))
                    drawRoundRect(color = NeonCyan, topLeft = Offset(carPos3 * w, 6 * rowH + 6.dp.toPx()), size = Size(60.dp.toPx(), rowH - 12.dp.toPx()), cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f))

                    // Frog
                    val fx = (frogCol + 0.5f) * (w / 5f)
                    val fy = (frogRow + 0.5f) * rowH
                    drawCircle(color = NeonGreen, radius = 16.dp.toPx(), center = Offset(fx, fy))
                }
            }

            // Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                IconButton(onClick = { if (frogCol > 0) frogCol-- }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = TextPrimary)
                }
                IconButton(onClick = {
                    if (frogRow > 0) {
                        frogRow--
                        score += 5
                        soundHelper.playTone(ToneType.TAP)
                        if (frogRow == 0) {
                            score += 50
                            frogRow = 8
                            soundHelper.playTone(ToneType.VICTORY)
                        }
                    }
                }) {
                    Icon(Icons.Default.KeyboardArrowUp, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(36.dp))
                }
                IconButton(onClick = { if (frogCol < 4) frogCol++ }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = TextPrimary)
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
                    frogRow = 8
                    frogCol = 2
                    gameOver = false
                },
                onBackToLobby = onBack
            )
        }
    }
}

// ----------------------------------------------------
// 6. COPTER CAVE SCREEN
// ----------------------------------------------------
@Composable
fun CopterCaveScreen(
    highScore: Int,
    soundHelper: SoundHelper,
    onBack: () -> Unit,
    onGameOverRecord: (Int) -> Unit
) {
    val game = GameCatalog.getGameById("copter")!!
    var score by remember { mutableIntStateOf(0) }
    var gameOver by remember { mutableStateOf(false) }

    var copterY by remember { mutableFloatStateOf(0.5f) }
    var vy by remember { mutableFloatStateOf(0f) }
    var isHolding by remember { mutableStateOf(false) }

    val obstacleX = remember { mutableFloatStateOf(1f) }
    val obstacleGapY = remember { mutableFloatStateOf(0.5f) }

    LaunchedEffect(gameOver) {
        if (!gameOver) {
            while (true) {
                delay(30)
                vy += if (isHolding) -0.003f else 0.0025f
                copterY += vy

                obstacleX.floatValue -= 0.015f
                if (obstacleX.floatValue < -0.2f) {
                    obstacleX.floatValue = 1f
                    obstacleGapY.floatValue = Random.nextFloat() * 0.4f + 0.3f
                    score += 10
                    soundHelper.playTone(ToneType.COIN)
                }

                // Ceiling/Floor or Wall collision
                if (copterY <= 0.05f || copterY >= 0.95f) {
                    gameOver = true
                    soundHelper.playTone(ToneType.GAMEOVER)
                    onGameOverRecord(score)
                }

                if (obstacleX.floatValue in 0.15f..0.32f) {
                    if (abs(copterY - obstacleGapY.floatValue) > 0.16f) {
                        gameOver = true
                        soundHelper.playTone(ToneType.HIT)
                        onGameOverRecord(score)
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
                    detectTapGestures(
                        onPress = {
                            isHolding = true
                            tryAwaitRelease()
                            isHolding = false
                        }
                    )
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Obstacle (Cave stalactites)
                val ox = obstacleX.floatValue * w
                val gap = obstacleGapY.floatValue * h
                val gapH = 0.32f * h
                drawRect(color = Color(0xFF64748B), topLeft = Offset(ox, 0f), size = Size(36.dp.toPx(), gap - gapH / 2))
                drawRect(color = Color(0xFF64748B), topLeft = Offset(ox, gap + gapH / 2), size = Size(36.dp.toPx(), h - (gap + gapH / 2)))

                // Copter
                val cy = copterY * h
                drawCircle(color = NeonAmber, radius = 16.dp.toPx(), center = Offset(w * 0.25f, cy))
                // Propeller
                drawLine(color = Color.White, start = Offset(w * 0.25f - 14.dp.toPx(), cy - 18.dp.toPx()), end = Offset(w * 0.25f + 14.dp.toPx(), cy - 18.dp.toPx()), strokeWidth = 3f)
            }

            Text(
                text = "Basılı tut: Yüksel | Bırak: Süzül",
                color = TextMuted,
                fontSize = 12.sp,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            )
        }

        if (gameOver) {
            GameOverDialog(
                score = score,
                highScore = highScore,
                themeColor = game.themeColor,
                onRestart = {
                    score = 0
                    copterY = 0.5f
                    vy = 0f
                    obstacleX.floatValue = 1f
                    gameOver = false
                },
                onBackToLobby = onBack
            )
        }
    }
}

// ----------------------------------------------------
// 7. MAZE RUNNER SCREEN
// ----------------------------------------------------
@Composable
fun MazeRunnerScreen(
    highScore: Int,
    soundHelper: SoundHelper,
    onBack: () -> Unit,
    onGameOverRecord: (Int) -> Unit
) {
    val game = GameCatalog.getGameById("maze_runner")!!
    var score by remember { mutableIntStateOf(0) }
    var px by remember { mutableIntStateOf(1) }
    var py by remember { mutableIntStateOf(1) }
    var gameOver by remember { mutableStateOf(false) }

    // 9x9 Maze map (1 = wall, 0 = path, 2 = exit)
    val maze = remember {
        listOf(
            listOf(1, 1, 1, 1, 1, 1, 1, 1, 1),
            listOf(1, 0, 0, 0, 1, 0, 0, 0, 1),
            listOf(1, 0, 1, 0, 1, 0, 1, 0, 1),
            listOf(1, 0, 1, 0, 0, 0, 1, 0, 1),
            listOf(1, 0, 1, 1, 1, 0, 1, 0, 1),
            listOf(1, 0, 0, 0, 1, 0, 0, 0, 1),
            listOf(1, 1, 1, 0, 1, 1, 1, 0, 1),
            listOf(1, 0, 0, 0, 0, 0, 0, 2, 1),
            listOf(1, 1, 1, 1, 1, 1, 1, 1, 1)
        )
    }

    fun move(dx: Int, dy: Int) {
        val nx = px + dx
        val ny = py + dy
        if (maze[ny][nx] != 1) {
            px = nx
            py = ny
            score += 2
            soundHelper.playTone(ToneType.TAP)
            if (maze[ny][nx] == 2) {
                score += 100
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
            // Maze Grid
            Box(
                modifier = Modifier
                    .size(300.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkSurfaceVariant)
                    .padding(8.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val cellW = size.width / 9f
                    val cellH = size.height / 9f
                    for (r in 0..8) {
                        for (c in 0..8) {
                            when (maze[r][c]) {
                                1 -> drawRect(color = Color(0xFF3B82F6), topLeft = Offset(c * cellW, r * cellH), size = Size(cellW, cellH))
                                2 -> drawCircle(color = NeonGreen, radius = cellW * 0.35f, center = Offset((c + 0.5f) * cellW, (r + 0.5f) * cellH))
                            }
                        }
                    }
                    // Player
                    drawCircle(color = NeonAmber, radius = cellW * 0.4f, center = Offset((px + 0.5f) * cellW, (py + 0.5f) * cellH))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Direction Buttons
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = { move(0, -1) }, modifier = Modifier.background(DarkSurface, CircleShape)) {
                    Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Yukarı", tint = NeonCyan)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    IconButton(onClick = { move(-1, 0) }, modifier = Modifier.background(DarkSurface, CircleShape)) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Sol", tint = NeonCyan)
                    }
                    IconButton(onClick = { move(1, 0) }, modifier = Modifier.background(DarkSurface, CircleShape)) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Sağ", tint = NeonCyan)
                    }
                }
                IconButton(onClick = { move(0, 1) }, modifier = Modifier.background(DarkSurface, CircleShape)) {
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Aşağı", tint = NeonCyan)
                }
            }
        }

        if (gameOver) {
            GameOverDialog(
                score = score,
                highScore = highScore,
                themeColor = game.themeColor,
                onRestart = {
                    px = 1
                    py = 1
                    score = 0
                    gameOver = false
                },
                onBackToLobby = onBack
            )
        }
    }
}

// ----------------------------------------------------
// 8. PAC DASH SCREEN
// ----------------------------------------------------
@Composable
fun PacDashScreen(
    highScore: Int,
    soundHelper: SoundHelper,
    onBack: () -> Unit,
    onGameOverRecord: (Int) -> Unit
) {
    val game = GameCatalog.getGameById("pac_dash")!!
    var score by remember { mutableIntStateOf(0) }
    var gameOver by remember { mutableStateOf(false) }

    var pacX by remember { mutableFloatStateOf(0.1f) }
    var pacY by remember { mutableFloatStateOf(0.5f) }
    var ghostX by remember { mutableFloatStateOf(0.9f) }
    var ghostY by remember { mutableFloatStateOf(0.5f) }

    val dots = remember {
        mutableStateListOf<Offset>().apply {
            for (i in 1..8) {
                add(Offset(i * 0.11f, 0.5f))
                add(Offset(0.5f, i * 0.11f))
            }
        }
    }

    LaunchedEffect(gameOver) {
        if (!gameOver) {
            while (true) {
                delay(35)
                // Ghost chases pacman
                ghostX += (pacX - ghostX) * 0.02f
                ghostY += (pacY - ghostY) * 0.02f

                // Dot eating
                val it = dots.iterator()
                while (it.hasNext()) {
                    val dot = it.next()
                    if (hypot(dot.x - pacX, dot.y - pacY) < 0.06f) {
                        it.remove()
                        score += 10
                        soundHelper.playTone(ToneType.COIN)
                    }
                }

                if (dots.isEmpty()) {
                    score += 100
                    soundHelper.playTone(ToneType.VICTORY)
                    gameOver = true
                    onGameOverRecord(score)
                }

                // Ghost collision
                if (hypot(ghostX - pacX, ghostY - pacY) < 0.06f) {
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                        pacX = (change.position.x / size.width).coerceIn(0.05f, 0.95f)
                        pacY = (change.position.y / size.height).coerceIn(0.05f, 0.95f)
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Dots
                dots.forEach { dot ->
                    drawCircle(color = Color(0xFFFBBF24), radius = 5.dp.toPx(), center = Offset(dot.x * w, dot.y * h))
                }

                // Ghost
                drawCircle(color = NeonRed, radius = 16.dp.toPx(), center = Offset(ghostX * w, ghostY * h))
                // Pacman
                drawCircle(color = NeonAmber, radius = 18.dp.toPx(), center = Offset(pacX * w, pacY * h))
            }
        }

        if (gameOver) {
            GameOverDialog(
                score = score,
                highScore = highScore,
                themeColor = game.themeColor,
                onRestart = {
                    score = 0
                    pacX = 0.1f
                    pacY = 0.5f
                    ghostX = 0.9f
                    ghostY = 0.5f
                    dots.clear()
                    for (i in 1..8) {
                        dots.add(Offset(i * 0.11f, 0.5f))
                        dots.add(Offset(0.5f, i * 0.11f))
                    }
                    gameOver = false
                },
                onBackToLobby = onBack
            )
        }
    }
}
