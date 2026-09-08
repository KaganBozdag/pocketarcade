package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameHeader(
    title: String,
    emoji: String,
    score: Int,
    highScore: Int,
    themeColor: Color,
    instructions: String,
    onBack: () -> Unit
) {
    var showHelp by remember { mutableStateOf(false) }

    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(emoji, fontSize = 20.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        maxLines = 1
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Skor: $score",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = themeColor
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = NeonAmber,
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "En İyi: $highScore",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                }
            }
        },
        navigationIcon = {
            IconButton(
                onClick = onBack,
                modifier = Modifier.testTag("game_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Geri",
                    tint = TextPrimary
                )
            }
        },
        actions = {
            if (instructions.isNotEmpty()) {
                IconButton(onClick = { showHelp = true }) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Nasıl Oynanır",
                        tint = TextSecondary
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
    )

    if (showHelp) {
        AlertDialog(
            onDismissRequest = { showHelp = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(emoji, fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Nasıl Oynanır?",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
            },
            text = {
                Text(
                    text = instructions,
                    fontSize = 14.sp,
                    color = TextSecondary,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = { showHelp = false },
                    colors = ButtonDefaults.buttonColors(containerColor = themeColor)
                ) {
                    Text("Anladım", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = DarkSurface,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun GameOverDialog(
    score: Int,
    highScore: Int,
    isNewRecord: Boolean = score > highScore,
    themeColor: Color,
    onRestart: () -> Unit,
    onBackToLobby: () -> Unit
) {
    AlertDialog(
        onDismissRequest = {},
        title = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isNewRecord) "🎉 YENİ REKOR! 🎉" else "💀 OYUN BİTTİ",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = if (isNewRecord) NeonAmber else NeonRed,
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Skorun",
                    fontSize = 13.sp,
                    color = TextSecondary
                )
                Text(
                    text = "$score",
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Black,
                    color = themeColor
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = NeonAmber,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "En Yüksek Skor: ${maxOf(score, highScore)}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextMuted
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onRestart,
                colors = ButtonDefaults.buttonColors(containerColor = themeColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("game_over_retry_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Tekrar Oyna", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onBackToLobby,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("game_over_lobby_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
            ) {
                Text("Lobiye Dön", fontWeight = FontWeight.SemiBold)
            }
        },
        containerColor = DarkSurface,
        shape = RoundedCornerShape(20.dp)
    )
}
