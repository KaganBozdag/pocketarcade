package com.example.games.reflex

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.util.SoundHelper
import com.example.util.ToneType
import kotlinx.coroutines.delay
import kotlin.random.Random

enum class TargetType(val emoji: String, val points: Int, val isBomb: Boolean, val isTimeBonus: Boolean) {
    DIAMOND("💎", 25, false, false),
    STAR("⭐", 15, false, false),
    COIN("🪙", 10, false, false),
    BOMB("💣", -20, true, false),
    CLOCK("⏰", 5, false, true)
}

data class ActiveTarget(val index: Int, val type: TargetType, val expiryTime: Long)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReflexGameScreen(
    highScore: Int,
    soundHelper: SoundHelper,
    onBack: () -> Unit,
    onGameOverRecord: (Int) -> Unit
) {
    var score by remember { mutableIntStateOf(0) }
    var timeLeft by remember { mutableIntStateOf(30) }
    var combo by remember { mutableIntStateOf(0) }
    var isRunning by remember { mutableStateOf(false) }
    var isGameOver by remember { mutableStateOf(false) }
    var isNewRecord by remember { mutableStateOf(false) }

    val activeTargets = remember { mutableStateMapOf<Int, TargetType>() }

    fun resetGame() {
        score = 0
        timeLeft = 30
        combo = 0
        isRunning = true
        isGameOver = false
        isNewRecord = false
        activeTargets.clear()
    }

    // Auto-start on screen launch
    LaunchedEffect(Unit) {
        resetGame()
    }

    // Timer loop
    LaunchedEffect(isRunning, isGameOver) {
        while (isRunning && !isGameOver && timeLeft > 0) {
            delay(1000)
            timeLeft--
            if (timeLeft <= 0) {
                isGameOver = true
                isRunning = false
                soundHelper.playTone(ToneType.GAMEOVER)
                if (score > highScore) isNewRecord = true
                onGameOverRecord(score)
            }
        }
    }

    // Target Spawner Loop
    LaunchedEffect(isRunning, isGameOver) {
        while (isRunning && !isGameOver) {
            val spawnDelay = (600 - (score.coerceAtMost(300))).coerceAtLeast(350).toLong()
            delay(spawnDelay)

            // Clear random existing or spawn new
            if (activeTargets.size >= 4) {
                val randomKey = activeTargets.keys.randomOrNull()
                if (randomKey != null) activeTargets.remove(randomKey)
            }

            // Pick an empty slot from 0..8
            val emptySlots = (0..8).filter { !activeTargets.containsKey(it) }
            if (emptySlots.isNotEmpty()) {
                val slot = emptySlots.random()
                val roll = Random.nextInt(100)
                val type = when {
                    roll < 12 -> TargetType.CLOCK
                    roll < 32 -> TargetType.BOMB
                    roll < 52 -> TargetType.DIAMOND
                    roll < 75 -> TargetType.STAR
                    else -> TargetType.COIN
                }
                activeTargets[slot] = type
            }
        }
    }

    fun onPadClick(index: Int) {
        if (!isRunning || isGameOver) return
        val target = activeTargets.remove(index)
        if (target != null) {
            if (target.isBomb) {
                combo = 0
                score = (score + target.points).coerceAtLeast(0)
                soundHelper.playTone(ToneType.HIT)
            } else {
                combo++
                val multiplier = when {
                    combo >= 10 -> 4
                    combo >= 6 -> 3
                    combo >= 3 -> 2
                    else -> 1
                }
                score += target.points * multiplier

                if (target.isTimeBonus) {
                    timeLeft = (timeLeft + 3).coerceAtMost(45)
                    soundHelper.playTone(ToneType.POWERUP)
                } else if (target == TargetType.DIAMOND) {
                    soundHelper.playTone(ToneType.POWERUP)
                } else {
                    soundHelper.playTone(ToneType.COIN)
                }
            }
        } else {
            // Misclick
            combo = 0
            soundHelper.playTone(ToneType.TAP)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Refleks Avcısı (Reflex Rush)",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontSize = 17.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("reflex_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri", tint = TextPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = { resetGame() }, modifier = Modifier.testTag("reflex_restart_button")) {
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
            // Stats Panel
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(DarkSurfaceVariant)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("SÜRE", fontSize = 11.sp, color = TextMuted, fontWeight = FontWeight.SemiBold)
                    Text(
                        "${timeLeft}s",
                        fontSize = 24.sp,
                        color = if (timeLeft <= 5) NeonRed else if (timeLeft <= 10) NeonAmber else NeonGreen,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("KOMBO", fontSize = 11.sp, color = TextMuted, fontWeight = FontWeight.SemiBold)
                    Text(
                        text = if (combo >= 10) "🔥 4x" else if (combo >= 6) "⚡ 3x" else if (combo >= 3) "✨ 2x" else "${combo}x",
                        fontSize = 20.sp,
                        color = if (combo >= 10) NeonPink else NeonCyan,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("SKOR", fontSize = 11.sp, color = TextMuted, fontWeight = FontWeight.SemiBold)
                    Text("$score", fontSize = 24.sp, color = NeonAmber, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Time progress bar
            LinearProgressIndicator(
                progress = { timeLeft / 30f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (timeLeft <= 5) NeonRed else if (timeLeft <= 10) NeonAmber else NeonCyan,
                trackColor = DarkSurfaceVariant
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 3x3 Grid of Interactive Arcade Pads
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    for (row in 0..2) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            for (col in 0..2) {
                                val index = row * 3 + col
                                val target = activeTargets[index]

                                val padBg = when {
                                    target == null -> DarkSurfaceVariant
                                    target.isBomb -> Color(0x33EF4444)
                                    target.isTimeBonus -> Color(0x333B82F6)
                                    target == TargetType.DIAMOND -> Color(0x3306B6D4)
                                    else -> Color(0x33F59E0B)
                                }

                                val padBorder = when {
                                    target == null -> DarkBorder
                                    target.isBomb -> NeonRed
                                    target.isTimeBonus -> NeonBlue
                                    target == TargetType.DIAMOND -> NeonCyan
                                    else -> NeonAmber
                                }

                                Box(
                                    modifier = Modifier
                                        .size(96.dp)
                                        .clip(RoundedCornerShape(18.dp))
                                        .background(padBg)
                                        .border(2.dp, padBorder, RoundedCornerShape(18.dp))
                                        .clickable { onPadClick(index) }
                                        .testTag("reflex_pad_$index"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (target != null) {
                                        Text(
                                            text = target.emoji,
                                            fontSize = 42.sp
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(CircleShape)
                                                .background(Color(0x33FFFFFF))
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Legend / Instructions
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Text("💎 +25", fontSize = 12.sp, color = NeonCyan)
                    Text("⭐ +15", fontSize = 12.sp, color = NeonAmber)
                    Text("🪙 +10", fontSize = 12.sp, color = NeonGreen)
                    Text("💣 -20", fontSize = 12.sp, color = NeonRed)
                    Text("⏰ +3s", fontSize = 12.sp, color = NeonBlue)
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
                                text = if (isNewRecord) "⚡ YENİ HIZ REKORU!" else "⏱️ SÜRE DOLDU!",
                                color = if (isNewRecord) NeonAmber else NeonCyan,
                                fontWeight = FontWeight.Bold,
                                fontSize = 21.sp
                            )
                        }
                    },
                    text = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            Text("Puanın: $score", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("En Yüksek: ${maxOf(score, highScore)}", fontSize = 14.sp, color = TextSecondary)
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = { resetGame() },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonAmber),
                            modifier = Modifier.testTag("reflex_play_again")
                        ) {
                            Text("Tekrar Dene", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        OutlinedButton(
                            onClick = onBack,
                            modifier = Modifier.testTag("reflex_exit_lobby")
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
