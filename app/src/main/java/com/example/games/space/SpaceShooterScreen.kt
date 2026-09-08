package com.example.games.space

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.util.SoundHelper
import com.example.util.ToneType
import kotlinx.coroutines.delay
import kotlin.math.hypot
import kotlin.random.Random

data class Star(var x: Float, var y: Float, val speed: Float, val radius: Float)
data class Bullet(var x: Float, var y: Float)
data class Enemy(var x: Float, var y: Float, val size: Float, val speed: Float, val isUfo: Boolean, val points: Int)
data class Particle(var x: Float, var y: Float, var vx: Float, var vy: Float, var alpha: Float, val color: Color)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpaceShooterScreen(
    highScore: Int,
    soundHelper: SoundHelper,
    onBack: () -> Unit,
    onGameOverRecord: (Int) -> Unit
) {
    var playerXRatio by remember { mutableFloatStateOf(0.5f) } // 0f to 1f
    var score by remember { mutableIntStateOf(0) }
    var lives by remember { mutableIntStateOf(3) }
    var isGameOver by remember { mutableStateOf(false) }
    var isNewRecord by remember { mutableStateOf(false) }

    val stars = remember {
        mutableStateListOf<Star>().apply {
            repeat(40) {
                add(Star(Random.nextFloat(), Random.nextFloat(), Random.nextFloat() * 3f + 1f, Random.nextFloat() * 2f + 1f))
            }
        }
    }
    val bullets = remember { mutableStateListOf<Bullet>() }
    val enemies = remember { mutableStateListOf<Enemy>() }
    val particles = remember { mutableStateListOf<Particle>() }

    fun resetGame() {
        playerXRatio = 0.5f
        score = 0
        lives = 3
        isGameOver = false
        isNewRecord = false
        bullets.clear()
        enemies.clear()
        particles.clear()
    }

    fun spawnExplosion(x: Float, y: Float, color: Color) {
        repeat(12) {
            val angle = Random.nextFloat() * 6.28f
            val speed = Random.nextFloat() * 6f + 2f
            particles.add(
                Particle(
                    x = x,
                    y = y,
                    vx = kotlin.math.cos(angle) * speed,
                    vy = kotlin.math.sin(angle) * speed,
                    alpha = 1f,
                    color = color
                )
            )
        }
    }

    // Main Game Loop (Approx 60 FPS)
    LaunchedEffect(isGameOver) {
        var frame = 0
        while (!isGameOver) {
            delay(16)
            frame++

            // Scroll stars
            stars.forEach { star ->
                star.y += star.speed * 0.005f
                if (star.y > 1f) {
                    star.y = 0f
                    star.x = Random.nextFloat()
                }
            }

            // Auto-fire bullets every 15 frames
            if (frame % 14 == 0) {
                bullets.add(Bullet(playerXRatio, 0.82f))
                soundHelper.playTone(ToneType.TAP)
            }

            // Update bullets
            val bulletsToRemove = mutableListOf<Bullet>()
            bullets.forEach { b ->
                b.y -= 0.025f
                if (b.y < 0f) bulletsToRemove.add(b)
            }
            bullets.removeAll(bulletsToRemove)

            // Spawn enemies
            if (frame % 40 == 0) {
                val isUfo = Random.nextBoolean()
                val speed = Random.nextFloat() * 0.007f + 0.005f
                enemies.add(
                    Enemy(
                        x = Random.nextFloat() * 0.8f + 0.1f,
                        y = -0.05f,
                        size = if (isUfo) 0.07f else 0.08f,
                        speed = speed,
                        isUfo = isUfo,
                        points = if (isUfo) 30 else 15
                    )
                )
            }

            // Update enemies
            val enemiesToRemove = mutableListOf<Enemy>()
            enemies.forEach { e ->
                e.y += e.speed
                // Check collision with player
                val distToPlayer = hypot(e.x - playerXRatio, e.y - 0.85f)
                if (distToPlayer < 0.08f) {
                    enemiesToRemove.add(e)
                    lives--
                    spawnExplosion(e.x, e.y, NeonRed)
                    soundHelper.playTone(ToneType.HIT)
                    if (lives <= 0) {
                        isGameOver = true
                        soundHelper.playTone(ToneType.GAMEOVER)
                        if (score > highScore) isNewRecord = true
                        onGameOverRecord(score)
                    }
                } else if (e.y > 1.05f) {
                    enemiesToRemove.add(e)
                }
            }

            // Bullet - Enemy Collisions
            val hitBullets = mutableListOf<Bullet>()
            enemies.forEach { enemy ->
                bullets.forEach { bullet ->
                    val dist = hypot(bullet.x - enemy.x, bullet.y - enemy.y)
                    if (dist < 0.06f) {
                        hitBullets.add(bullet)
                        enemiesToRemove.add(enemy)
                        score += enemy.points
                        spawnExplosion(enemy.x, enemy.y, if (enemy.isUfo) NeonPink else NeonAmber)
                        soundHelper.playTone(ToneType.COIN)
                    }
                }
            }
            bullets.removeAll(hitBullets)
            enemies.removeAll(enemiesToRemove)

            // Update particles
            val deadParticles = mutableListOf<Particle>()
            particles.forEach { p ->
                p.x += p.vx * 0.001f
                p.y += p.vy * 0.001f
                p.alpha -= 0.035f
                if (p.alpha <= 0f) deadParticles.add(p)
            }
            particles.removeAll(deadParticles)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Uzay Savunucusu (Galaxy Defender)",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontSize = 17.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("space_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri", tint = TextPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = { resetGame() }, modifier = Modifier.testTag("space_restart_button")) {
                        Icon(Icons.Default.Refresh, contentDescription = "Yeniden Başlat", tint = NeonCyan)
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
                .padding(horizontal = 16.dp, vertical = 8.dp)
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
                    Text("$score", fontSize = 22.sp, color = NeonCyan, fontWeight = FontWeight.Bold)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    repeat(3) { i ->
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Can",
                            tint = if (i < lives) NeonPink else Color(0x44FFFFFF),
                            modifier = Modifier.size(24.dp).padding(horizontal = 2.dp)
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("EN YÜKSEK", fontSize = 11.sp, color = TextMuted, fontWeight = FontWeight.SemiBold)
                    Text("$highScore", fontSize = 22.sp, color = NeonAmber, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Interactive Game Arena
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF070512))
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            val deltaRatio = dragAmount.x / size.width.toFloat()
                            playerXRatio = (playerXRatio + deltaRatio).coerceIn(0.08f, 0.92f)
                        }
                    }
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            val targetRatio = offset.x / size.width.toFloat()
                            playerXRatio = targetRatio.coerceIn(0.08f, 0.92f)
                        }
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    // Draw Stars
                    stars.forEach { star ->
                        drawCircle(
                            color = Color(0xCCFFFFFF),
                            radius = star.radius,
                            center = Offset(star.x * w, star.y * h)
                        )
                    }

                    // Draw Bullets (Glowing Neon Cyan Lasers)
                    bullets.forEach { b ->
                        drawRoundRect(
                            color = NeonCyan,
                            topLeft = Offset(b.x * w - 3f, b.y * h),
                            size = Size(6f, 22f),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f, 3f)
                        )
                    }

                    // Draw Enemies
                    enemies.forEach { e ->
                        val ex = e.x * w
                        val ey = e.y * h
                        val er = e.size * w * 0.5f

                        if (e.isUfo) {
                            // Alien Flying Saucer
                            drawOval(
                                brush = Brush.radialGradient(
                                    colors = listOf(Color(0xFFFF4081), Color(0xFF7C4DFF)),
                                    center = Offset(ex, ey),
                                    radius = er * 1.2f
                                ),
                                topLeft = Offset(ex - er * 1.2f, ey - er * 0.6f),
                                size = Size(er * 2.4f, er * 1.2f)
                            )
                            // Dome
                            drawCircle(
                                color = NeonCyan,
                                radius = er * 0.45f,
                                center = Offset(ex, ey - er * 0.3f)
                            )
                        } else {
                            // Meteorite
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(Color(0xFFFFB74D), Color(0xFFE65100)),
                                    center = Offset(ex - er * 0.2f, ey - er * 0.2f),
                                    radius = er
                                ),
                                radius = er,
                                center = Offset(ex, ey)
                            )
                        }
                    }

                    // Draw Particles
                    particles.forEach { p ->
                        drawCircle(
                            color = p.color.copy(alpha = p.alpha.coerceIn(0f, 1f)),
                            radius = 4f,
                            center = Offset(p.x * w, p.y * h)
                        )
                    }

                    // Draw Player Starship
                    val px = playerXRatio * w
                    val py = 0.85f * h
                    val shipSize = 32.dp.toPx()

                    val shipPath = Path().apply {
                        moveTo(px, py - shipSize)
                        lineTo(px + shipSize * 0.7f, py + shipSize * 0.5f)
                        lineTo(px + shipSize * 0.3f, py + shipSize * 0.3f)
                        lineTo(px, py + shipSize * 0.6f)
                        lineTo(px - shipSize * 0.3f, py + shipSize * 0.3f)
                        lineTo(px - shipSize * 0.7f, py + shipSize * 0.5f)
                        close()
                    }

                    drawPath(
                        path = shipPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(NeonCyan, NeonPurple)
                        )
                    )

                    // Thruster flame
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(NeonAmber, Color.Transparent),
                            center = Offset(px, py + shipSize * 0.65f),
                            radius = 18f
                        ),
                        radius = 16f,
                        center = Offset(px, py + shipSize * 0.65f)
                    )
                }

                // Drag instruction banner at start
                Text(
                    text = "Geminizi yönlendirmek için kaydırın veya dokunun",
                    color = TextMuted,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 12.dp)
                )
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
                                text = if (isNewRecord) "🚀 YENİ REKOR!" else "💥 GEMİ İMHA EDİLDİ",
                                color = if (isNewRecord) NeonAmber else NeonPink,
                                fontWeight = FontWeight.Bold,
                                fontSize = 21.sp
                            )
                        }
                    },
                    text = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            Text("Skorun: $score", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("En Yüksek: ${maxOf(score, highScore)}", fontSize = 14.sp, color = TextSecondary)
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = { resetGame() },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                            modifier = Modifier.testTag("space_play_again")
                        ) {
                            Text("Yeniden Savaş", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        OutlinedButton(
                            onClick = onBack,
                            modifier = Modifier.testTag("space_exit_lobby")
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
