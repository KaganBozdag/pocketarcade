package com.example.games.memory

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.util.SoundHelper
import com.example.util.ToneType
import kotlinx.coroutines.delay

data class MemoryCard(
    val id: Int,
    val icon: String,
    val isFlipped: Boolean = false,
    val isMatched: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemoryGameScreen(
    highScore: Int,
    soundHelper: SoundHelper,
    onBack: () -> Unit,
    onGameOverRecord: (Int) -> Unit
) {
    val icons = listOf("👾", "🚀", "💎", "🍕", "⭐", "🎮", "⚡", "🕹️")
    var cards by remember {
        mutableStateOf(
            (icons + icons).shuffled().mapIndexed { index, icon ->
                MemoryCard(id = index, icon = icon)
            }
        )
    }

    var selectedIndices by remember { mutableStateOf<List<Int>>(emptyList()) }
    var moves by remember { mutableIntStateOf(0) }
    var matchedPairs by remember { mutableIntStateOf(0) }
    var secondsElapsed by remember { mutableIntStateOf(0) }
    var isWon by remember { mutableStateOf(false) }
    var isNewRecord by remember { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) }

    fun resetGame() {
        cards = (icons + icons).shuffled().mapIndexed { index, icon ->
            MemoryCard(id = index, icon = icon)
        }
        selectedIndices = emptyList()
        moves = 0
        matchedPairs = 0
        secondsElapsed = 0
        isWon = false
        isNewRecord = false
        isProcessing = false
    }

    // Timer
    LaunchedEffect(isWon) {
        while (!isWon) {
            delay(1000)
            secondsElapsed++
        }
    }

    // Card click handler
    fun onCardClick(index: Int) {
        if (isProcessing || isWon) return
        val card = cards[index]
        if (card.isFlipped || card.isMatched) return

        soundHelper.playTone(ToneType.TAP)

        // Reveal card
        cards = cards.toMutableList().also {
            it[index] = card.copy(isFlipped = true)
        }

        val currentSelected = selectedIndices + index
        selectedIndices = currentSelected

        if (currentSelected.size == 2) {
            moves++
            isProcessing = true
            val firstIdx = currentSelected[0]
            val secondIdx = currentSelected[1]

            if (cards[firstIdx].icon == cards[secondIdx].icon) {
                // Match!
                soundHelper.playTone(ToneType.COIN)
                cards = cards.toMutableList().also {
                    it[firstIdx] = it[firstIdx].copy(isMatched = true)
                    it[secondIdx] = it[secondIdx].copy(isMatched = true)
                }
                matchedPairs++
                selectedIndices = emptyList()
                isProcessing = false

                if (matchedPairs == 8) {
                    isWon = true
                    soundHelper.playTone(ToneType.VICTORY)
                    // Score calculation: base 1000 - (moves * 15) - (time * 5)
                    val finalScore = (1000 - (moves * 15) - (secondsElapsed * 5)).coerceAtLeast(100)
                    if (finalScore > highScore) isNewRecord = true
                    onGameOverRecord(finalScore)
                }
            } else {
                // Not match: delay then flip back
            }
        }
    }

    // Handle unflipping mismatched cards
    LaunchedEffect(selectedIndices) {
        if (selectedIndices.size == 2) {
            val firstIdx = selectedIndices[0]
            val secondIdx = selectedIndices[1]
            if (cards[firstIdx].icon != cards[secondIdx].icon) {
                delay(850)
                cards = cards.toMutableList().also {
                    it[firstIdx] = it[firstIdx].copy(isFlipped = false)
                    it[secondIdx] = it[secondIdx].copy(isFlipped = false)
                }
                selectedIndices = emptyList()
                isProcessing = false
            }
        }
    }

    val stars = when {
        moves <= 13 -> 3
        moves <= 19 -> 2
        else -> 1
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Hafıza Kartları (Memory Flip)",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontSize = 17.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("memory_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri", tint = TextPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = { resetGame() }, modifier = Modifier.testTag("memory_restart_button")) {
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
            // Stats Row
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
                    Text("HAMLE", fontSize = 11.sp, color = TextMuted, fontWeight = FontWeight.SemiBold)
                    Text("$moves", fontSize = 22.sp, color = NeonCyan, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("EŞLEŞME", fontSize = 11.sp, color = TextMuted, fontWeight = FontWeight.SemiBold)
                    Text("$matchedPairs / 8", fontSize = 22.sp, color = NeonGreen, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("SÜRE", fontSize = 11.sp, color = TextMuted, fontWeight = FontWeight.SemiBold)
                    val mins = secondsElapsed / 60
                    val secs = secondsElapsed % 60
                    Text(
                        "%02d:%02d".format(mins, secs),
                        fontSize = 22.sp,
                        color = NeonAmber,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4x4 Grid of Cards
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    for (row in 0..3) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            for (col in 0..3) {
                                val idx = row * 4 + col
                                val card = cards[idx]

                                val rotation by animateFloatAsState(
                                    targetValue = if (card.isFlipped || card.isMatched) 180f else 0f,
                                    label = "card_flip"
                                )

                                val cardBorder = when {
                                    card.isMatched -> NeonGreen
                                    card.isFlipped -> NeonCyan
                                    else -> DarkBorder
                                }

                                val cardBg = when {
                                    card.isMatched -> Color(0xFF10382B)
                                    card.isFlipped -> DarkSurfaceVariant
                                    else -> DarkSurface
                                }

                                Box(
                                    modifier = Modifier
                                        .size(74.dp)
                                        .graphicsLayer {
                                            rotationY = rotation
                                            cameraDistance = 12f * density
                                        }
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(cardBg)
                                        .border(2.dp, cardBorder, RoundedCornerShape(14.dp))
                                        .clickable { onCardClick(idx) }
                                        .testTag("memory_card_$idx"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (rotation > 90f) {
                                        Text(
                                            text = card.icon,
                                            fontSize = 32.sp,
                                            modifier = Modifier.graphicsLayer { rotationY = 180f }
                                        )
                                    } else {
                                        Text(
                                            text = "❓",
                                            fontSize = 24.sp,
                                            color = TextMuted
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Victory Dialog
        if (isWon) {
            val finalScore = (1000 - (moves * 15) - (secondsElapsed * 5)).coerceAtLeast(100)
            AnimatedVisibility(
                visible = true,
                enter = fadeIn() + scaleIn()
            ) {
                AlertDialog(
                    onDismissRequest = { resetGame() },
                    title = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            Text("🎉 TEBRİKLER!", color = NeonAmber, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row {
                                repeat(3) { i ->
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = "Yıldız",
                                        tint = if (i < stars) NeonAmber else Color(0x33FFFFFF),
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }
                        }
                    },
                    text = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            Text("Toplam Puan: $finalScore", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("$moves Hamle - ${secondsElapsed} Saniyede Tamamlandı!", fontSize = 14.sp, color = TextSecondary)
                            if (isNewRecord) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("🔥 YENİ REKOR!", fontSize = 14.sp, color = NeonGreen, fontWeight = FontWeight.Bold)
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = { resetGame() },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                            modifier = Modifier.testTag("memory_play_again")
                        ) {
                            Text("Tekrar Oyna", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        OutlinedButton(
                            onClick = onBack,
                            modifier = Modifier.testTag("memory_exit_lobby")
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
