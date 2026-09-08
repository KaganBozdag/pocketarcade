package com.example.games.flappy

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
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
import kotlin.random.Random

data class Pipe(
    var x: Float,
    val topHeightRatio: Float,
    val gapRatio: Float = 0.28f,
    var passed: Boolean = false
)

data class FlappyCoin(
    var x: Float,
    var y: Float,
    var collected: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlappyBirdScreen(
    highScore: Int,
    soundHelper: SoundHelper,
    onBack: () -> Unit,
    onGameOverRecord: (Int) -> Unit
) {
    var birdYRatio by remember { mutableFloatStateOf(0.45f) }
    var birdVelocity by remember { mutableFloatStateOf(0f) }
    var score by remember { mutableIntStateOf(0) }
    var isStarted by remember { mutableStateOf(false) }
    var isGameOver by remember { mutableStateOf(false) }
    var isNewRecord by remember { mutableStateOf(false) }

    val pipes = remember { mutableStateListOf<Pipe>() }
    val coins = remember { mutableStateListOf<FlappyCoin>() }

    fun resetGame() {
        birdYRatio = 0.45f
        birdVelocity = 0f
        score = 0
        isStarted = false
        isGameOver = false
        isNewRecord = false
        pipes.clear()
        coins.clear()
    }

    fun flap() {
        if (isGameOver) return
        if (!isStarted) {
            isStarted = true
        }
        birdVelocity = -0.015f
        soundHelper.playTone(ToneType.TAP)
    }

    // Physics & Game Loop
    LaunchedEffect(isStarted, isGameOver) {
        var frame = 0
        while (isStarted && !isGameOver) {
            delay(16)
            frame++

            // Gravity
            birdVelocity += 0.0009f
            birdYRatio += birdVelocity

            // Hit ceiling or ground
            if (birdYRatio < 0.02f || birdYRatio > 0.94f) {
                isGameOver = true
                soundHelper.playTone(ToneType.GAMEOVER)
                if (score > highScore) isNewRecord = true
                onGameOverRecord(score)
            }

            // Spawn Pipes
            if (frame % 85 == 0) {
                val topRatio = Random.nextFloat() * 0.4f + 0.15f
                val pipe = Pipe(x = 1.1f, topHeightRatio = topRatio)
                pipes.add(pipe)

                // Spawn Coin inside the gap
                val coinY = topRatio + (pipe.gapRatio / 2f)
                coins.add(FlappyCoin(x = 1.1f + 0.06f, y = coinY))
            }

            // Move Pipes
            val pipesToRemove = mutableListOf<Pipe>()
            pipes.forEach { p ->
                p.x -= 0.006f

                // Score when passing
                if (!p.passed && p.x < 0.25f) {
                    p.passed = true
                    score++
                    soundHelper.playTone(ToneType.COIN)
                }

                // Check collision with bird (bird is at x = 0.25f, radius ~ 0.035f)
                val birdX = 0.25f
                val birdRadius = 0.035f
                val pipeWidth = 0.14f

                if (birdX + birdRadius > p.x && birdX - birdRadius < p.x + pipeWidth) {
                    val bottomPipeY = p.topHeightRatio + p.gapRatio
                    if (birdYRatio - birdRadius < p.topHeightRatio || birdYRatio + birdRadius > bottomPipeY) {
                        isGameOver = true
                        soundHelper.playTone(ToneType.HIT)
                        if (score > highScore) isNewRecord = true
                        onGameOverRecord(score)
                    }
                }

                if (p.x < -0.2f) pipesToRemove.add(p)
            }
            pipes.removeAll(pipesToRemove)

            // Move Coins & check collection
            val coinsToRemove = mutableListOf<FlappyCoin>()
            coins.forEach { c ->
                c.x -= 0.006f
                val dist = kotlin.math.hypot(0.25f - c.x, birdYRatio - c.y)
                if (!c.collected && dist < 0.05f) {
                    c.collected = true
                    score += 3
                    soundHelper.playTone(ToneType.POWERUP)
                }
                if (c.x < -0.2f || c.collected) coinsToRemove.add(c)
            }
            coins.removeAll(coinsToRemove)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Zıplayan Kuş (Flappy Sky)",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontSize = 17.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("flappy_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri", tint = TextPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = { resetGame() }, modifier = Modifier.testTag("flappy_restart_button")) {
                        Icon(Icons.Default.Refresh, contentDescription = "Yeniden Başlat", tint = NeonAmber)
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
            // Stats Header
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
                    Text("$score", fontSize = 22.sp, color = NeonAmber, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("EN YÜKSEK", fontSize = 11.sp, color = TextMuted, fontWeight = FontWeight.SemiBold)
                    Text("$highScore", fontSize = 22.sp, color = NeonCyan, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Flappy Game Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFF0D9488))
                        )
                    )
                    .pointerInput(Unit) {
                        detectTapGestures {
                            flap()
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    // Draw Pipes
                    pipes.forEach { p ->
                        val px = p.x * w
                        val pipeW = 0.14f * w
                        val topH = p.topHeightRatio * h
                        val bottomY = (p.topHeightRatio + p.gapRatio) * h
                        val bottomH = h - bottomY

                        // Top pipe
                        drawRoundRect(
                            brush = Brush.horizontalGradient(
                                colors = listOf(Color(0xFF22C55E), Color(0xFF15803D))
                            ),
                            topLeft = Offset(px, 0f),
                            size = Size(pipeW, topH),
                            cornerRadius = CornerRadius(6f, 6f)
                        )
                        // Top pipe lip
                        drawRoundRect(
                            color = Color(0xFF16A34A),
                            topLeft = Offset(px - 4f, topH - 18f),
                            size = Size(pipeW + 8f, 18f),
                            cornerRadius = CornerRadius(4f, 4f)
                        )

                        // Bottom pipe
                        drawRoundRect(
                            brush = Brush.horizontalGradient(
                                colors = listOf(Color(0xFF22C55E), Color(0xFF15803D))
                            ),
                            topLeft = Offset(px, bottomY),
                            size = Size(pipeW, bottomH),
                            cornerRadius = CornerRadius(6f, 6f)
                        )
                        // Bottom pipe lip
                        drawRoundRect(
                            color = Color(0xFF16A34A),
                            topLeft = Offset(px - 4f, bottomY),
                            size = Size(pipeW + 8f, 18f),
                            cornerRadius = CornerRadius(4f, 4f)
                        )
                    }

                    // Draw Coins
                    coins.forEach { c ->
                        if (!c.collected) {
                            val cx = c.x * w
                            val cy = c.y * h
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(Color(0xFFFFEE58), Color(0xFFF59E0B)),
                                    center = Offset(cx, cy),
                                    radius = 16f
                                ),
                                radius = 14f,
                                center = Offset(cx, cy)
                            )
                        }
                    }

                    // Draw Bird
                    val bx = 0.25f * w
                    val by = birdYRatio * h
                    val birdRadius = 18.dp.toPx()

                    // Bird Body
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFFFACC15), Color(0xFFEAB308)),
                            center = Offset(bx, by),
                            radius = birdRadius
                        ),
                        radius = birdRadius,
                        center = Offset(bx, by)
                    )

                    // Bird Eye
                    drawCircle(
                        color = Color.White,
                        radius = birdRadius * 0.35f,
                        center = Offset(bx + birdRadius * 0.35f, by - birdRadius * 0.3f)
                    )
                    drawCircle(
                        color = Color.Black,
                        radius = birdRadius * 0.18f,
                        center = Offset(bx + birdRadius * 0.45f, by - birdRadius * 0.3f)
                    )

                    // Bird Beak
                    val beakPath = androidx.compose.ui.graphics.Path().apply {
                        moveTo(bx + birdRadius * 0.7f, by)
                        lineTo(bx + birdRadius * 1.3f, by + birdRadius * 0.2f)
                        lineTo(bx + birdRadius * 0.6f, by + birdRadius * 0.4f)
                        close()
                    }
                    drawPath(beakPath, Color(0xFFF97316))

                    // Bird Wing
                    drawOval(
                        color = Color(0xFFF59E0B),
                        topLeft = Offset(bx - birdRadius * 0.8f, by - birdRadius * 0.2f),
                        size = Size(birdRadius * 1.1f, birdRadius * 0.7f)
                    )
                }

                // Initial Tap to Start prompt
                if (!isStarted && !isGameOver) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xBB000000))
                            .padding(20.dp)
                    ) {
                        Text("Zıplamak İçin Ekrana Dokun!", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Borulardan kaç ve altınları topla", fontSize = 13.sp, color = NeonCyan)
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
                                text = if (isNewRecord) "🏆 YENİ UÇUŞ REKORU!" else "💥 KUŞ DÜŞTÜ!",
                                color = if (isNewRecord) NeonAmber else NeonRed,
                                fontWeight = FontWeight.Bold,
                                fontSize = 21.sp
                            )
                        }
                    },
                    text = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            Text("Skorun: $score", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("En Yüksek: ${maxOf(score, highScore)}", fontSize = 14.sp, color = TextSecondary)
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = { resetGame() },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonAmber),
                            modifier = Modifier.testTag("flappy_play_again")
                        ) {
                            Text("Tekrar Uç", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        OutlinedButton(
                            onClick = onBack,
                            modifier = Modifier.testTag("flappy_exit_lobby")
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
