package com.example.games.snake

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.util.SoundHelper
import com.example.util.ToneType
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.random.Random

enum class Direction {
    UP, DOWN, LEFT, RIGHT
}

data class Point(val x: Int, val y: Int)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SnakeGameScreen(
    highScore: Int,
    soundHelper: SoundHelper,
    onBack: () -> Unit,
    onGameOverRecord: (Int) -> Unit
) {
    val gridWidth = 15
    val gridHeight = 18

    var snake by remember { mutableStateOf(listOf(Point(7, 9), Point(7, 10), Point(7, 11))) }
    var direction by remember { mutableStateOf(Direction.UP) }
    var nextDirection by remember { mutableStateOf(Direction.UP) }
    var food by remember { mutableStateOf(Point(7, 4)) }
    var bonusFood by remember { mutableStateOf<Point?>(null) }
    var bonusTimer by remember { mutableIntStateOf(0) }
    var score by remember { mutableIntStateOf(0) }
    var isGameOver by remember { mutableStateOf(false) }
    var isPaused by remember { mutableStateOf(false) }
    var isNewRecord by remember { mutableStateOf(false) }

    fun resetGame() {
        snake = listOf(Point(7, 9), Point(7, 10), Point(7, 11))
        direction = Direction.UP
        nextDirection = Direction.UP
        food = Point(Random.nextInt(gridWidth), Random.nextInt(gridHeight))
        bonusFood = null
        bonusTimer = 0
        score = 0
        isGameOver = false
        isPaused = false
        isNewRecord = false
    }

    fun changeDirection(newDir: Direction) {
        if (isGameOver || isPaused) return
        val isOpposite = when (newDir) {
            Direction.UP -> direction == Direction.DOWN
            Direction.DOWN -> direction == Direction.UP
            Direction.LEFT -> direction == Direction.RIGHT
            Direction.RIGHT -> direction == Direction.LEFT
        }
        if (!isOpposite && direction != newDir) {
            nextDirection = newDir
            soundHelper.playTone(ToneType.TAP)
        }
    }

    // Main Game Loop
    LaunchedEffect(isGameOver, isPaused) {
        while (!isGameOver && !isPaused) {
            val speed = (200 - (score * 2)).coerceAtLeast(85).toLong()
            delay(speed)

            direction = nextDirection
            val head = snake.first()
            val newHead = when (direction) {
                Direction.UP -> Point(head.x, head.y - 1)
                Direction.DOWN -> Point(head.x, head.y + 1)
                Direction.LEFT -> Point(head.x - 1, head.y)
                Direction.RIGHT -> Point(head.x + 1, head.y)
            }

            // Wall Collision or Self Collision
            if (newHead.x < 0 || newHead.x >= gridWidth ||
                newHead.y < 0 || newHead.y >= gridHeight ||
                snake.contains(newHead)
            ) {
                isGameOver = true
                soundHelper.playTone(ToneType.GAMEOVER)
                if (score > highScore) {
                    isNewRecord = true
                }
                onGameOverRecord(score)
            } else {
                val newSnake = mutableListOf(newHead)
                var ateFood = false

                if (newHead == food) {
                    ateFood = true
                    score += 10
                    soundHelper.playTone(ToneType.COIN)
                    // Generate new food
                    var candidate = Point(Random.nextInt(gridWidth), Random.nextInt(gridHeight))
                    while (snake.contains(candidate)) {
                        candidate = Point(Random.nextInt(gridWidth), Random.nextInt(gridHeight))
                    }
                    food = candidate

                    // 25% chance of spawning bonus gold fruit
                    if (bonusFood == null && Random.nextInt(4) == 0) {
                        bonusFood = Point(Random.nextInt(gridWidth), Random.nextInt(gridHeight))
                        bonusTimer = 25
                    }
                } else if (bonusFood != null && newHead == bonusFood) {
                    ateFood = true
                    score += 35
                    soundHelper.playTone(ToneType.POWERUP)
                    bonusFood = null
                }

                if (ateFood) {
                    newSnake.addAll(snake)
                } else {
                    newSnake.addAll(snake.dropLast(1))
                }
                snake = newSnake

                if (bonusFood != null) {
                    bonusTimer--
                    if (bonusTimer <= 0) {
                        bonusFood = null
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Yılan (Retro Snake)",
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontSize = 18.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("snake_back_button")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Geri",
                            tint = TextPrimary
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { isPaused = !isPaused },
                        modifier = Modifier.testTag("snake_pause_button")
                    ) {
                        Icon(
                            imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                            contentDescription = "Duraklat",
                            tint = NeonCyan
                        )
                    }
                    IconButton(
                        onClick = { resetGame() },
                        modifier = Modifier.testTag("snake_restart_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Yeniden Başlat",
                            tint = NeonGreen
                        )
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
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Score Board
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
                    Text("$score", fontSize = 22.sp, color = NeonGreen, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("UZUNLUK", fontSize = 11.sp, color = TextMuted, fontWeight = FontWeight.SemiBold)
                    Text("${snake.size}", fontSize = 22.sp, color = NeonCyan, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("EN YÜKSEK", fontSize = 11.sp, color = TextMuted, fontWeight = FontWeight.SemiBold)
                    Text("$highScore", fontSize = 22.sp, color = NeonAmber, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Game Board Canvas with Swipe Gesture
            var totalDragX by remember { mutableFloatStateOf(0f) }
            var totalDragY by remember { mutableFloatStateOf(0f) }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .border(2.dp, DarkBorder, RoundedCornerShape(16.dp))
                    .background(DarkSurface)
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = {
                                totalDragX = 0f
                                totalDragY = 0f
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                totalDragX += dragAmount.x
                                totalDragY += dragAmount.y
                            },
                            onDragEnd = {
                                if (abs(totalDragX) > abs(totalDragY) && abs(totalDragX) > 30f) {
                                    if (totalDragX > 0) changeDirection(Direction.RIGHT)
                                    else changeDirection(Direction.LEFT)
                                } else if (abs(totalDragY) > 30f) {
                                    if (totalDragY > 0) changeDirection(Direction.DOWN)
                                    else changeDirection(Direction.UP)
                                }
                            }
                        )
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val cellWidth = size.width / gridWidth
                    val cellHeight = size.height / gridHeight

                    // Subtle Grid dots
                    for (x in 0 until gridWidth) {
                        for (y in 0 until gridHeight) {
                            drawCircle(
                                color = Color(0x15FFFFFF),
                                radius = 1.5f,
                                center = Offset(x * cellWidth + cellWidth / 2, y * cellHeight + cellHeight / 2)
                            )
                        }
                    }

                    // Draw Normal Food (Glowing Red Apple)
                    val foodCenter = Offset(food.x * cellWidth + cellWidth / 2, food.y * cellHeight + cellHeight / 2)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFFFF5252), Color(0xFFD50000)),
                            center = foodCenter,
                            radius = cellWidth * 0.45f
                        ),
                        radius = cellWidth * 0.42f,
                        center = foodCenter
                    )
                    // Stem
                    drawLine(
                        color = Color(0xFF4CAF50),
                        start = foodCenter + Offset(0f, -cellWidth * 0.4f),
                        end = foodCenter + Offset(cellWidth * 0.15f, -cellWidth * 0.55f),
                        strokeWidth = 3f
                    )

                    // Draw Bonus Food if available
                    bonusFood?.let { bf ->
                        val bCenter = Offset(bf.x * cellWidth + cellWidth / 2, bf.y * cellHeight + cellHeight / 2)
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(Color(0xFFFFEE58), Color(0xFFFF9800)),
                                center = bCenter,
                                radius = cellWidth * 0.5f
                            ),
                            radius = cellWidth * 0.48f,
                            center = bCenter
                        )
                    }

                    // Draw Snake
                    snake.forEachIndexed { index, pt ->
                        val cellRect = Offset(pt.x * cellWidth + 2f, pt.y * cellHeight + 2f)
                        val cellSize = Size(cellWidth - 4f, cellHeight - 4f)
                        val corner = CornerRadius(cellWidth * 0.35f, cellWidth * 0.35f)

                        if (index == 0) {
                            // Head (Cyan glowing)
                            drawRoundRect(
                                brush = Brush.linearGradient(
                                    colors = listOf(NeonCyan, Color(0xFF00E5FF))
                                ),
                                topLeft = cellRect,
                                size = cellSize,
                                cornerRadius = corner
                            )
                            // Eyes
                            val eyeOffset1 = when (direction) {
                                Direction.UP -> Offset(cellWidth * 0.3f, cellHeight * 0.3f)
                                Direction.DOWN -> Offset(cellWidth * 0.3f, cellHeight * 0.7f)
                                Direction.LEFT -> Offset(cellWidth * 0.3f, cellHeight * 0.3f)
                                Direction.RIGHT -> Offset(cellWidth * 0.7f, cellHeight * 0.3f)
                            }
                            val eyeOffset2 = when (direction) {
                                Direction.UP -> Offset(cellWidth * 0.7f, cellHeight * 0.3f)
                                Direction.DOWN -> Offset(cellWidth * 0.7f, cellHeight * 0.7f)
                                Direction.LEFT -> Offset(cellWidth * 0.3f, cellHeight * 0.7f)
                                Direction.RIGHT -> Offset(cellWidth * 0.7f, cellHeight * 0.7f)
                            }
                            drawCircle(Color.Black, radius = 3.5f, center = Offset(pt.x * cellWidth + eyeOffset1.x, pt.y * cellHeight + eyeOffset1.y))
                            drawCircle(Color.Black, radius = 3.5f, center = Offset(pt.x * cellWidth + eyeOffset2.x, pt.y * cellHeight + eyeOffset2.y))
                        } else {
                            // Body segments with color gradient fade
                            val ratio = index.toFloat() / snake.size
                            val bodyColor = Color(
                                red = (0x10 * (1f - ratio) + 0x8B * ratio).toInt(),
                                green = (0xB9 * (1f - ratio) + 0x5C * ratio).toInt(),
                                blue = (0x81 * (1f - ratio) + 0xF6 * ratio).toInt(),
                                alpha = 255
                            )
                            drawRoundRect(
                                color = bodyColor,
                                topLeft = cellRect,
                                size = cellSize,
                                cornerRadius = corner
                            )
                        }
                    }
                }

                // Pause banner
                if (isPaused && !isGameOver) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0x99000000)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "OYUN DURAKLATILDI",
                            color = NeonCyan,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // D-Pad Virtual Controls
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 6.dp)
            ) {
                IconButton(
                    onClick = { changeDirection(Direction.UP) },
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(DarkSurfaceVariant)
                        .testTag("snake_dpad_up")
                ) {
                    Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Yukarı", tint = NeonCyan, modifier = Modifier.size(32.dp))
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(36.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { changeDirection(Direction.LEFT) },
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(DarkSurfaceVariant)
                            .testTag("snake_dpad_left")
                    ) {
                        Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Sol", tint = NeonCyan, modifier = Modifier.size(32.dp))
                    }
                    IconButton(
                        onClick = { changeDirection(Direction.DOWN) },
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(DarkSurfaceVariant)
                            .testTag("snake_dpad_down")
                    ) {
                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Aşağı", tint = NeonCyan, modifier = Modifier.size(32.dp))
                    }
                    IconButton(
                        onClick = { changeDirection(Direction.RIGHT) },
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(DarkSurfaceVariant)
                            .testTag("snake_dpad_right")
                    ) {
                        Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Sağ", tint = NeonCyan, modifier = Modifier.size(32.dp))
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
                                text = if (isNewRecord) "🎉 YENİ REKOR!" else "💀 OYUN BİTTİ",
                                color = if (isNewRecord) NeonAmber else NeonPink,
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp
                            )
                        }
                    },
                    text = {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Skorun: $score",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "En Yüksek Skor: ${maxOf(score, highScore)}",
                                fontSize = 14.sp,
                                color = TextSecondary
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = { resetGame() },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                            modifier = Modifier.testTag("snake_play_again")
                        ) {
                            Text("Tekrar Oyna", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        OutlinedButton(
                            onClick = onBack,
                            modifier = Modifier.testTag("snake_exit_lobby")
                        ) {
                            Text("Menüye Dön", color = TextSecondary)
                        }
                    },
                    containerColor = DarkSurface,
                    shape = RoundedCornerShape(20.dp)
                )
            }
        }
    }
}
