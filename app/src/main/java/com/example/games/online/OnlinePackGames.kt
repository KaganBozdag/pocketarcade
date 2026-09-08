package com.example.games.online

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
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
// MATCHMAKER LOBBY COMPONENT
// ----------------------------------------------------
data class OnlineOpponent(
    val name: String,
    val avatarEmoji: String,
    val elo: Int,
    val country: String,
    val ping: Int = Random.nextInt(18, 45)
)

val OPPONENTS_POOL = listOf(
    OnlineOpponent("NeoBlade", "⚡", 1540, "TR"),
    OnlineOpponent("PixelQueen", "👑", 1490, "TR"),
    OnlineOpponent("Can_35", "🦁", 1620, "TR"),
    OnlineOpponent("CyberWolf", "🐺", 1380, "AZ"),
    OnlineOpponent("Viper_TR", "🐍", 1510, "TR"),
    OnlineOpponent("Eren_Pro", "🎯", 1440, "TR")
)

@Composable
fun OnlineMatchmakerView(
    gameTitle: String,
    onMatchFound: (OnlineOpponent) -> Unit
) {
    var statusText by remember { mutableStateOf("Çevrimiçi Oyuncu Aranıyor...") }
    var opponent by remember { mutableStateOf<OnlineOpponent?>(null) }
    var countdown by remember { mutableIntStateOf(3) }

    LaunchedEffect(Unit) {
        delay(1200)
        val matched = OPPONENTS_POOL.random()
        opponent = matched
        statusText = "Rakip Bulundu! Maç Başlıyor..."
        while (countdown > 0) {
            delay(800)
            countdown--
        }
        onMatchFound(matched)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (opponent == null) {
            CircularProgressIndicator(color = NeonCyan, modifier = Modifier.size(56.dp))
            Spacer(modifier = Modifier.height(24.dp))
            Text(statusText, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Sunucu: TR-Istanbul | Ping: 24ms", fontSize = 12.sp, color = NeonGreen)
        } else {
            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(56.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text(statusText, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = NeonGreen)
            Spacer(modifier = Modifier.height(24.dp))

            // Opponent Card
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(0.85f)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(NeonPink.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(opponent!!.avatarEmoji, fontSize = 24.sp)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(opponent!!.name, fontWeight = FontWeight.Black, fontSize = 18.sp, color = TextPrimary)
                        Text("Rating: ${opponent!!.elo} ELO", fontSize = 13.sp, color = NeonAmber)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            Text("$countdown", fontSize = 48.sp, fontWeight = FontWeight.Black, color = NeonCyan)
        }
    }
}

// ----------------------------------------------------
// 1. ONLINE ROCK-PAPER-SCISSORS SCREEN
// ----------------------------------------------------
@Composable
fun OnlineRpsScreen(
    gameId: String = "online_rps",
    highScore: Int,
    soundHelper: SoundHelper,
    onBack: () -> Unit,
    onGameOverRecord: (Int) -> Unit
) {
    val game = GameCatalog.getGameById(gameId)!!
    var matchedOpponent by remember { mutableStateOf<OnlineOpponent?>(null) }
    var playerScore by remember { mutableIntStateOf(0) }
    var opponentScore by remember { mutableIntStateOf(0) }
    var roundResult by remember { mutableStateOf("Hamleni Seç!") }
    var playerChoice by remember { mutableStateOf("") }
    var opponentChoice by remember { mutableStateOf("") }
    var gameOver by remember { mutableStateOf(false) }

    val choices = listOf("Taş" to "✊", "Kağıt" to "✋", "Makas" to "✌️")

    fun playRound(choice: String) {
        if (gameOver) return
        playerChoice = choice
        val opp = choices.random().first
        opponentChoice = opp

        if (choice == opp) {
            roundResult = "Berabere!"
            soundHelper.playTone(ToneType.TAP)
        } else if (
            (choice == "Taş" && opp == "Makas") ||
            (choice == "Kağıt" && opp == "Taş") ||
            (choice == "Makas" && opp == "Kağıt")
        ) {
            roundResult = "Raundu Sen Kazandın! 🎉"
            playerScore++
            soundHelper.playTone(ToneType.COIN)
        } else {
            roundResult = "${matchedOpponent?.name} Kazandı! 😢"
            opponentScore++
            soundHelper.playTone(ToneType.HIT)
        }

        if (playerScore >= 3 || opponentScore >= 3) {
            gameOver = true
            val finalScore = playerScore * 50
            if (playerScore >= 3) soundHelper.playTone(ToneType.VICTORY)
            else soundHelper.playTone(ToneType.GAMEOVER)
            onGameOverRecord(finalScore)
        }
    }

    Scaffold(
        topBar = {
            GameHeader(
                title = "${game.title} (Canlı)",
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
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (matchedOpponent == null) {
                OnlineMatchmakerView(game.title) { matchedOpponent = it }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Scoreboard Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(DarkSurfaceVariant)
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Sen (Sen)", fontWeight = FontWeight.Bold, color = NeonCyan)
                            Text("$playerScore", fontSize = 28.sp, fontWeight = FontWeight.Black, color = NeonCyan)
                        }
                        Text("VS", fontWeight = FontWeight.Black, color = TextMuted, fontSize = 20.sp)
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(matchedOpponent!!.name, fontWeight = FontWeight.Bold, color = NeonPink)
                            Text("$opponentScore", fontSize = 28.sp, fontWeight = FontWeight.Black, color = NeonPink)
                        }
                    }

                    // Battle arena
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(roundResult, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Spacer(modifier = Modifier.height(24.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(32.dp)) {
                            Text(
                                text = choices.find { it.first == playerChoice }?.second ?: "❓",
                                fontSize = 64.sp
                            )
                            Text(
                                text = choices.find { it.first == opponentChoice }?.second ?: "❓",
                                fontSize = 64.sp
                            )
                        }
                    }

                    // Controls
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Hamleni Seç:", fontSize = 14.sp, color = TextSecondary)
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            choices.forEach { (name, emoji) ->
                                Button(
                                    onClick = { playRound(name) },
                                    modifier = Modifier.size(80.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Text(emoji, fontSize = 32.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        if (gameOver) {
            GameOverDialog(
                score = playerScore * 50,
                highScore = highScore,
                themeColor = game.themeColor,
                onRestart = {
                    playerScore = 0
                    opponentScore = 0
                    playerChoice = ""
                    opponentChoice = ""
                    roundResult = "Hamleni Seç!"
                    gameOver = false
                },
                onBackToLobby = onBack
            )
        }
    }
}

// ----------------------------------------------------
// 2. ONLINE TAP BATTLE SCREEN
// ----------------------------------------------------
@Composable
fun OnlineTapBattleScreen(
    highScore: Int,
    soundHelper: SoundHelper,
    onBack: () -> Unit,
    onGameOverRecord: (Int) -> Unit
) {
    val game = GameCatalog.getGameById("online_tap_battle")!!
    var matchedOpponent by remember { mutableStateOf<OnlineOpponent?>(null) }
    var balance by remember { mutableFloatStateOf(0.5f) } // 0 = opponent wins, 1 = player wins
    var gameOver by remember { mutableStateOf(false) }

    LaunchedEffect(matchedOpponent, gameOver) {
        if (matchedOpponent != null && !gameOver) {
            while (!gameOver) {
                delay(120)
                // Opponent taps randomly
                balance = (balance - Random.nextFloat() * 0.04f).coerceIn(0f, 1f)
                if (balance <= 0.05f) {
                    gameOver = true
                    soundHelper.playTone(ToneType.GAMEOVER)
                    onGameOverRecord(0)
                }
            }
        }
    }

    fun tap() {
        if (matchedOpponent != null && !gameOver) {
            balance = (balance + 0.045f).coerceIn(0f, 1f)
            soundHelper.playTone(ToneType.TAP)
            if (balance >= 0.95f) {
                gameOver = true
                soundHelper.playTone(ToneType.VICTORY)
                onGameOverRecord(150)
            }
        }
    }

    Scaffold(
        topBar = {
            GameHeader(
                title = game.title,
                emoji = game.emoji,
                score = (balance * 100).toInt(),
                highScore = highScore,
                themeColor = game.themeColor,
                instructions = game.instructions,
                onBack = onBack
            )
        },
        containerColor = DarkBackground
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (matchedOpponent == null) {
                OnlineMatchmakerView(game.title) { matchedOpponent = it }
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Opponent Half (Top)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight((1f - balance).coerceIn(0.1f, 0.9f))
                            .background(NeonPink.copy(alpha = 0.8f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(matchedOpponent!!.avatarEmoji, fontSize = 32.sp)
                            Text(matchedOpponent!!.name, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }

                    // Divider
                    Box(modifier = Modifier.fillMaxWidth().height(6.dp).background(Color.White))

                    // Player Half (Bottom)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(balance.coerceIn(0.1f, 0.9f))
                            .background(NeonCyan.copy(alpha = 0.8f))
                            .clickable { tap() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("HIZLI DOKUN!", fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color.Black)
                    }
                }
            }
        }

        if (gameOver) {
            GameOverDialog(
                score = if (balance >= 0.95f) 150 else 20,
                highScore = highScore,
                themeColor = game.themeColor,
                onRestart = {
                    balance = 0.5f
                    gameOver = false
                },
                onBackToLobby = onBack
            )
        }
    }
}

// ----------------------------------------------------
// 3. ONLINE DICE BATTLE SCREEN
// ----------------------------------------------------
@Composable
fun OnlineDiceScreen(
    highScore: Int,
    soundHelper: SoundHelper,
    onBack: () -> Unit,
    onGameOverRecord: (Int) -> Unit
) {
    val game = GameCatalog.getGameById("online_dice")!!
    var matchedOpponent by remember { mutableStateOf<OnlineOpponent?>(null) }
    var playerDice by remember { mutableIntStateOf(1) }
    var opponentDice by remember { mutableIntStateOf(1) }
    var roundResult by remember { mutableStateOf("Zarları At!") }
    var score by remember { mutableIntStateOf(0) }
    var roundsPlayed by remember { mutableIntStateOf(0) }
    var gameOver by remember { mutableStateOf(false) }

    fun roll() {
        if (gameOver) return
        playerDice = Random.nextInt(1, 7)
        opponentDice = Random.nextInt(1, 7)
        roundsPlayed++
        soundHelper.playTone(ToneType.TAP)

        if (playerDice > opponentDice) {
            score += 20
            roundResult = "Sen Kazandın! (+20 Puan)"
            soundHelper.playTone(ToneType.COIN)
        } else if (playerDice < opponentDice) {
            roundResult = "${matchedOpponent?.name} Kazandı!"
            soundHelper.playTone(ToneType.HIT)
        } else {
            roundResult = "Berabere! (+5 Puan)"
            score += 5
        }

        if (roundsPlayed >= 5) {
            gameOver = true
            soundHelper.playTone(ToneType.VICTORY)
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
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (matchedOpponent == null) {
                OnlineMatchmakerView(game.title) { matchedOpponent = it }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Raund $roundsPlayed / 5", fontSize = 16.sp, color = TextMuted)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Senin Zarın", fontWeight = FontWeight.Bold, color = NeonCyan)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                when (playerDice) { 1 -> "⚀"; 2 -> "⚁"; 3 -> "⚂"; 4 -> "⚃"; 5 -> "⚄"; else -> "⚅" },
                                fontSize = 72.sp,
                                color = NeonCyan
                            )
                        }
                        Text("VS", fontSize = 20.sp, fontWeight = FontWeight.Black, color = TextMuted)
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(matchedOpponent!!.name, fontWeight = FontWeight.Bold, color = NeonPink)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                when (opponentDice) { 1 -> "⚀"; 2 -> "⚁"; 3 -> "⚂"; 4 -> "⚃"; 5 -> "⚄"; else -> "⚅" },
                                fontSize = 72.sp,
                                color = NeonPink
                            )
                        }
                    }

                    Text(roundResult, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)

                    Button(
                        onClick = { roll() },
                        modifier = Modifier.fillMaxWidth(0.6f).height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonAmber),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("🎲 ZAR AT", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 16.sp)
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
                    roundsPlayed = 0
                    roundResult = "Zarları At!"
                    gameOver = false
                },
                onBackToLobby = onBack
            )
        }
    }
}

// ----------------------------------------------------
// 4. ONLINE ROULETTE SCREEN
// ----------------------------------------------------
@Composable
fun OnlineRouletteScreen(
    highScore: Int,
    soundHelper: SoundHelper,
    onBack: () -> Unit,
    onGameOverRecord: (Int) -> Unit
) {
    val game = GameCatalog.getGameById("online_roulette")!!
    var matchedOpponent by remember { mutableStateOf<OnlineOpponent?>(null) }
    var balance by remember { mutableIntStateOf(100) }
    var selectedColor by remember { mutableStateOf("KIRMIZI") }
    var spinAngle by remember { mutableFloatStateOf(0f) }
    var isSpinning by remember { mutableStateOf(false) }
    var resultText by remember { mutableStateOf("Bahsini koy ve çarkı çevir!") }
    var gameOver by remember { mutableStateOf(false) }

    fun spin() {
        if (isSpinning || balance < 10) return
        isSpinning = true
        balance -= 10
        val targetRotation = spinAngle + 720f + Random.nextFloat() * 360f
        spinAngle = targetRotation

        soundHelper.playTone(ToneType.TAP)
        val colors = listOf("KIRMIZI", "SİYAH", "YEŞİL")
        val winningColor = colors.random()

        if (winningColor == selectedColor) {
            val winAmount = if (winningColor == "YEŞİL") 100 else 20
            balance += winAmount
            resultText = "KAZANDIN! Kazanan Renk: $winningColor (+$winAmount)"
            soundHelper.playTone(ToneType.VICTORY)
        } else {
            resultText = "KAYBETTİN! Kazanan Renk: $winningColor"
            soundHelper.playTone(ToneType.HIT)
        }
        isSpinning = false

        if (balance <= 0) {
            gameOver = true
            soundHelper.playTone(ToneType.GAMEOVER)
            onGameOverRecord(balance)
        }
    }

    Scaffold(
        topBar = {
            GameHeader(
                title = game.title,
                emoji = game.emoji,
                score = balance,
                highScore = highScore,
                themeColor = game.themeColor,
                instructions = game.instructions,
                onBack = onBack
            )
        },
        containerColor = DarkBackground
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (matchedOpponent == null) {
                OnlineMatchmakerView(game.title) { matchedOpponent = it }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Çip Bakiyesi: $balance", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = NeonAmber)

                    // Wheel Graphic
                    Box(
                        modifier = Modifier
                            .size(200.dp)
                            .rotate(spinAngle)
                            .clip(CircleShape)
                            .background(Color(0xFF1E293B))
                            .border(6.dp, NeonAmber, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🎡", fontSize = 64.sp)
                    }

                    Text(resultText, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary, textAlign = TextAlign.Center)

                    // Bet Options
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = { selectedColor = "KIRMIZI" },
                            colors = ButtonDefaults.buttonColors(containerColor = if (selectedColor == "KIRMIZI") NeonRed else DarkSurfaceVariant)
                        ) {
                            Text("KIRMIZI (2x)")
                        }
                        Button(
                            onClick = { selectedColor = "SİYAH" },
                            colors = ButtonDefaults.buttonColors(containerColor = if (selectedColor == "SİYAH") Color.Gray else DarkSurfaceVariant)
                        ) {
                            Text("SİYAH (2x)")
                        }
                        Button(
                            onClick = { selectedColor = "YEŞİL" },
                            colors = ButtonDefaults.buttonColors(containerColor = if (selectedColor == "YEŞİL") NeonGreen else DarkSurfaceVariant)
                        ) {
                            Text("YEŞİL (10x)")
                        }
                    }

                    Button(
                        onClick = { spin() },
                        modifier = Modifier.fillMaxWidth(0.7f).height(54.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("ÇEVİR (10 Çip)", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 16.sp)
                    }
                }
            }
        }

        if (gameOver) {
            GameOverDialog(
                score = balance,
                highScore = highScore,
                themeColor = game.themeColor,
                onRestart = {
                    balance = 100
                    gameOver = false
                },
                onBackToLobby = onBack
            )
        }
    }
}

// ----------------------------------------------------
// 5. ONLINE BLACKJACK 21 SCREEN
// ----------------------------------------------------
@Composable
fun OnlineCard21Screen(
    highScore: Int,
    soundHelper: SoundHelper,
    onBack: () -> Unit,
    onGameOverRecord: (Int) -> Unit
) {
    val game = GameCatalog.getGameById("online_card21")!!
    var matchedOpponent by remember { mutableStateOf<OnlineOpponent?>(null) }
    val playerCards = remember { mutableStateListOf<Int>() }
    val dealerCards = remember { mutableStateListOf<Int>() }
    var gameOver by remember { mutableStateOf(false) }
    var statusText by remember { mutableStateOf("Kart Çek veya Dur!") }
    var score by remember { mutableIntStateOf(0) }

    fun startNewHand() {
        playerCards.clear()
        dealerCards.clear()
        playerCards.add(Random.nextInt(2, 11))
        playerCards.add(Random.nextInt(2, 11))
        dealerCards.add(Random.nextInt(2, 11))
        statusText = "Kart Çek veya Dur!"
    }

    fun hit() {
        if (gameOver) return
        playerCards.add(Random.nextInt(2, 11))
        soundHelper.playTone(ToneType.TAP)
        val total = playerCards.sum()
        if (total > 21) {
            statusText = "21'i Aştın! Kaybettin 💀"
            soundHelper.playTone(ToneType.GAMEOVER)
            gameOver = true
            onGameOverRecord(score)
        }
    }

    fun stand() {
        if (gameOver) return
        while (dealerCards.sum() < 17) {
            dealerCards.add(Random.nextInt(2, 11))
        }
        val pTotal = playerCards.sum()
        val dTotal = dealerCards.sum()

        if (dTotal > 21 || pTotal > dTotal) {
            score += 50
            statusText = "Tebrikler Kazandın! 🎉"
            soundHelper.playTone(ToneType.VICTORY)
        } else if (pTotal == dTotal) {
            statusText = "Berabere!"
            score += 10
        } else {
            statusText = "Kasa Kazandı!"
            soundHelper.playTone(ToneType.HIT)
        }
        gameOver = true
        onGameOverRecord(score)
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
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (matchedOpponent == null) {
                OnlineMatchmakerView(game.title) {
                    matchedOpponent = it
                    startNewHand()
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Dealer Hand
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Kasa / Rakip (${dealerCards.sum()})", fontWeight = FontWeight.Bold, color = NeonPink)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            dealerCards.forEach { card ->
                                Card(colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.size(width = 46.dp, height = 64.dp)) {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Text("$card", fontWeight = FontWeight.Black, fontSize = 20.sp, color = Color.Black)
                                    }
                                }
                            }
                        }
                    }

                    Text(statusText, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)

                    // Player Hand
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Senin Elin (${playerCards.sum()})", fontWeight = FontWeight.Bold, color = NeonCyan)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            playerCards.forEach { card ->
                                Card(colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.size(width = 46.dp, height = 64.dp)) {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Text("$card", fontWeight = FontWeight.Black, fontSize = 20.sp, color = Color.Black)
                                    }
                                }
                            }
                        }
                    }

                    // Action buttons
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Button(
                            onClick = { hit() },
                            modifier = Modifier.weight(1f).height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                        ) {
                            Text("Kart Çek (+)", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = { stand() },
                            modifier = Modifier.weight(1f).height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NeonAmber)
                        ) {
                            Text("Dur (Pas)", color = Color.Black, fontWeight = FontWeight.Bold)
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
                    startNewHand()
                    gameOver = false
                },
                onBackToLobby = onBack
            )
        }
    }
}

// ----------------------------------------------------
// 6. ONLINE LUCKY WHEEL SCREEN
// ----------------------------------------------------
@Composable
fun OnlineLuckyWheelScreen(
    highScore: Int,
    soundHelper: SoundHelper,
    onBack: () -> Unit,
    onGameOverRecord: (Int) -> Unit
) {
    val game = GameCatalog.getGameById("online_wheel")!!
    var matchedOpponent by remember { mutableStateOf<OnlineOpponent?>(null) }
    var score by remember { mutableIntStateOf(0) }
    var spinCount by remember { mutableIntStateOf(3) }
    var lastPrize by remember { mutableStateOf("Çarkı Çevir!") }
    var gameOver by remember { mutableStateOf(false) }

    val prizes = listOf(50, 100, 20, 200, 10, 500, 0, 75)

    fun spinWheel() {
        if (spinCount > 0 && !gameOver) {
            val won = prizes.random()
            score += won
            spinCount--
            lastPrize = if (won > 0) "+$won Puan Kazandın! 🎁" else "Pas Geçtin! 😢"
            if (won > 0) soundHelper.playTone(ToneType.POWERUP) else soundHelper.playTone(ToneType.HIT)

            if (spinCount == 0) {
                gameOver = true
                soundHelper.playTone(ToneType.VICTORY)
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
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (matchedOpponent == null) {
                OnlineMatchmakerView(game.title) { matchedOpponent = it }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Kalan Çevirme Hakkı: $spinCount", fontSize = 16.sp, color = NeonAmber, fontWeight = FontWeight.Bold)

                    Box(
                        modifier = Modifier
                            .size(200.dp)
                            .clip(CircleShape)
                            .background(DarkSurfaceVariant)
                            .border(4.dp, NeonCyan, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🎁", fontSize = 64.sp)
                    }

                    Text(lastPrize, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)

                    Button(
                        onClick = { spinWheel() },
                        modifier = Modifier.fillMaxWidth(0.7f).height(54.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonAmber),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("ÇEVİR!", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 18.sp)
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
                    spinCount = 3
                    lastPrize = "Çarkı Çevir!"
                    gameOver = false
                },
                onBackToLobby = onBack
            )
        }
    }
}

// ----------------------------------------------------
// 7. ONLINE BINGO (TOMBALA) SCREEN
// ----------------------------------------------------
@Composable
fun OnlineBingoScreen(
    highScore: Int,
    soundHelper: SoundHelper,
    onBack: () -> Unit,
    onGameOverRecord: (Int) -> Unit
) {
    val game = GameCatalog.getGameById("online_bingo")!!
    var matchedOpponent by remember { mutableStateOf<OnlineOpponent?>(null) }
    var score by remember { mutableIntStateOf(0) }
    var currentNumber by remember { mutableIntStateOf(0) }
    var gameOver by remember { mutableStateOf(false) }

    // 3x3 Card
    val cardNumbers = remember { (1..30).shuffled().take(9) }
    val marked = remember { mutableStateListOf<Boolean>().apply { addAll(List(9) { false }) } }

    LaunchedEffect(matchedOpponent, gameOver) {
        if (matchedOpponent != null && !gameOver) {
            while (!gameOver) {
                delay(2000)
                currentNumber = Random.nextInt(1, 31)
                soundHelper.playTone(ToneType.TAP)
            }
        }
    }

    fun markCell(idx: Int) {
        if (cardNumbers[idx] == currentNumber && !marked[idx]) {
            marked[idx] = true
            score += 10
            soundHelper.playTone(ToneType.COIN)

            // Check full card (Bingo)
            if (marked.all { it }) {
                score += 200
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
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (matchedOpponent == null) {
                OnlineMatchmakerView(game.title) { matchedOpponent = it }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("Çekilen Sayı:", fontSize = 16.sp, color = TextSecondary)
                    Text("$currentNumber", fontSize = 48.sp, fontWeight = FontWeight.Black, color = NeonAmber)
                    Spacer(modifier = Modifier.height(24.dp))

                    // 3x3 Card
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
                                    val num = cardNumbers[idx]
                                    val isMarked = marked[idx]
                                    Box(
                                        modifier = Modifier
                                            .size(72.dp)
                                            .padding(4.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(if (isMarked) NeonGreen else Color(0xFF1E293B))
                                            .clickable { markCell(idx) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            "$num",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 22.sp,
                                            color = if (isMarked) Color.Black else TextPrimary
                                        )
                                    }
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
                    marked.fill(false)
                    score = 0
                    gameOver = false
                },
                onBackToLobby = onBack
            )
        }
    }
}

// ----------------------------------------------------
// 8. ONLINE ARENA (DYNAMIC DUEL FOR REMAINING ONLINE GAMES)
// ----------------------------------------------------
@Composable
fun OnlineArenaGameScreen(
    gameId: String,
    highScore: Int,
    soundHelper: SoundHelper,
    onBack: () -> Unit,
    onGameOverRecord: (Int) -> Unit
) {
    val game = GameCatalog.getGameById(gameId)!!
    var matchedOpponent by remember { mutableStateOf<OnlineOpponent?>(null) }
    var playerScore by remember { mutableIntStateOf(0) }
    var opponentScore by remember { mutableIntStateOf(0) }
    var round by remember { mutableIntStateOf(1) }
    var gameOver by remember { mutableStateOf(false) }
    var statusText by remember { mutableStateOf("Hamleni Yap!") }

    fun playAction() {
        if (gameOver) return
        val playerPoint = Random.nextInt(10, 30)
        val opponentPoint = Random.nextInt(5, 28)
        playerScore += playerPoint
        opponentScore += opponentPoint
        round++
        soundHelper.playTone(ToneType.TAP)

        if (round > 4) {
            gameOver = true
            if (playerScore >= opponentScore) {
                statusText = "Maçı Sen Kazandın! 🏆"
                soundHelper.playTone(ToneType.VICTORY)
            } else {
                statusText = "${matchedOpponent?.name} Kazandı!"
                soundHelper.playTone(ToneType.GAMEOVER)
            }
            onGameOverRecord(playerScore)
        }
    }

    Scaffold(
        topBar = {
            GameHeader(
                title = "${game.title} (Canlı)",
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
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (matchedOpponent == null) {
                OnlineMatchmakerView(game.title) { matchedOpponent = it }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Match Info
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(DarkSurfaceVariant)
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Sen", fontWeight = FontWeight.Bold, color = NeonCyan)
                            Text("$playerScore", fontSize = 24.sp, fontWeight = FontWeight.Black, color = NeonCyan)
                        }
                        Text("Raund $round / 4", fontWeight = FontWeight.Bold, color = TextMuted)
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(matchedOpponent!!.name, fontWeight = FontWeight.Bold, color = NeonPink)
                            Text("$opponentScore", fontSize = 24.sp, fontWeight = FontWeight.Black, color = NeonPink)
                        }
                    }

                    // Arena Center
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(game.emoji, fontSize = 64.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(statusText, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }

                    Button(
                        onClick = { playAction() },
                        modifier = Modifier.fillMaxWidth(0.7f).height(54.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = game.themeColor),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Hamle Yap & Zar At", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 16.sp)
                    }
                }
            }
        }

        if (gameOver) {
            GameOverDialog(
                score = playerScore,
                highScore = highScore,
                themeColor = game.themeColor,
                onRestart = {
                    playerScore = 0
                    opponentScore = 0
                    round = 1
                    statusText = "Hamleni Yap!"
                    gameOver = false
                },
                onBackToLobby = onBack
            )
        }
    }
}
