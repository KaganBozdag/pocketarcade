package com.example.games.word

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import kotlin.random.Random

// ----------------------------------------------------
// 1. HANGMAN (ADAM ASMACA) SCREEN
// ----------------------------------------------------
@Composable
fun HangmanScreen(
    highScore: Int,
    soundHelper: SoundHelper,
    onBack: () -> Unit,
    onGameOverRecord: (Int) -> Unit
) {
    val game = GameCatalog.getGameById("hangman")!!
    var score by remember { mutableIntStateOf(0) }
    var gameOver by remember { mutableStateOf(false) }

    val words = listOf("YAZILIM", "KOTLIN", "ARCADE", "PIKSEL", "ROBOT", "EKRAN", "OYUNCU", "ROKET", "GALAKSI", "KODLAMA")
    var currentWord by remember { mutableStateOf(words.random()) }
    val guessedLetters = remember { mutableStateListOf<Char>() }
    var wrongCount by remember { mutableIntStateOf(0) }

    fun guess(char: Char) {
        if (!guessedLetters.contains(char) && !gameOver) {
            guessedLetters.add(char)
            if (currentWord.contains(char)) {
                soundHelper.playTone(ToneType.TAP)
                val allGuessed = currentWord.all { guessedLetters.contains(it) }
                if (allGuessed) {
                    score += 50
                    soundHelper.playTone(ToneType.VICTORY)
                    gameOver = true
                    onGameOverRecord(score)
                }
            } else {
                wrongCount++
                soundHelper.playTone(ToneType.HIT)
                if (wrongCount >= 6) {
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Hata: $wrongCount / 6", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = if (wrongCount > 4) NeonRed else NeonAmber)

            // Hangman visual emoji progression
            Text(
                when (wrongCount) {
                    0 -> "🌱"
                    1 -> "😐"
                    2 -> "😟"
                    3 -> "😨"
                    4 -> "😱"
                    5 -> "💀"
                    else -> "🪦"
                },
                fontSize = 54.sp
            )

            // Word display
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                currentWord.forEach { char ->
                    val isRevealed = guessedLetters.contains(char) || (gameOver && wrongCount >= 6)
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .border(1.dp, NeonCyan, RoundedCornerShape(6.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(if (isRevealed) "$char" else "_", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                }
            }

            // Turkish Keyboard
            val alphabet = "ABCÇDEFGĞHIİJKLMNOÖPRSŞTUÜVYZ"
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                alphabet.chunked(7).forEach { rowLetters ->
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        rowLetters.forEach { char ->
                            val used = guessedLetters.contains(char)
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (used) DarkSurfaceVariant else NeonCyan.copy(alpha = 0.2f))
                                    .clickable(enabled = !used) { guess(char) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text("$char", fontWeight = FontWeight.Bold, color = if (used) TextMuted else TextPrimary)
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
                    currentWord = words.random()
                    guessedLetters.clear()
                    wrongCount = 0
                    score = 0
                    gameOver = false
                },
                onBackToLobby = onBack
            )
        }
    }
}

// ----------------------------------------------------
// 2. WORDLE 5 LETTER SCREEN
// ----------------------------------------------------
@Composable
fun WordleScreen(
    highScore: Int,
    soundHelper: SoundHelper,
    onBack: () -> Unit,
    onGameOverRecord: (Int) -> Unit
) {
    val game = GameCatalog.getGameById("wordle")!!
    var score by remember { mutableIntStateOf(0) }
    var gameOver by remember { mutableStateOf(false) }

    val words = listOf("KALEM", "KİTAP", "GÜNEŞ", "DENİZ", "DÜNYA", "RADYO", "ŞEHİR", "BULUT")
    val targetWord = remember { words.random() }
    val guesses = remember { mutableStateListOf<String>() }
    var currentInput by remember { mutableStateOf("") }

    fun submitGuess() {
        if (currentInput.length == 5 && guesses.size < 6 && !gameOver) {
            guesses.add(currentInput)
            if (currentInput == targetWord) {
                score = (6 - guesses.size + 1) * 25
                soundHelper.playTone(ToneType.VICTORY)
                gameOver = true
                onGameOverRecord(score)
            } else if (guesses.size == 6) {
                soundHelper.playTone(ToneType.GAMEOVER)
                gameOver = true
                onGameOverRecord(score)
            } else {
                soundHelper.playTone(ToneType.TAP)
            }
            currentInput = ""
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 6 Guess Rows
            for (row in 0..5) {
                val rowGuess = guesses.getOrNull(row) ?: if (row == guesses.size) currentInput.padEnd(5, ' ') else "     "
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(vertical = 3.dp)) {
                    for (col in 0..4) {
                        val ch = rowGuess.getOrNull(col) ?: ' '
                        val bgColor = if (guesses.size > row) {
                            if (ch == targetWord[col]) NeonGreen
                            else if (targetWord.contains(ch)) NeonAmber
                            else Color(0xFF334155)
                        } else Color(0xFF1E293B)

                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(bgColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("$ch", fontSize = 22.sp, fontWeight = FontWeight.Black, color = Color.White)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Quick Virtual Alphabet buttons
            val keys = "ABCÇDEFGĞHIİJKLMNOÖPRSŞTUÜVYZ"
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                keys.chunked(10).forEach { chunk ->
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        chunk.forEach { c ->
                            Button(
                                onClick = { if (currentInput.length < 5) currentInput += c },
                                contentPadding = PaddingValues(0.dp),
                                modifier = Modifier.size(width = 32.dp, height = 42.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant)
                            ) {
                                Text("$c", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            }
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = { if (currentInput.isNotEmpty()) currentInput = currentInput.dropLast(1) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Sil")
                    }
                    Button(
                        onClick = { submitGuess() },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)
                    ) {
                        Text("Onayla", color = Color.Black, fontWeight = FontWeight.Bold)
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
                    guesses.clear()
                    currentInput = ""
                    score = 0
                    gameOver = false
                },
                onBackToLobby = onBack
            )
        }
    }
}

// ----------------------------------------------------
// 3. SCRAMBLE (KARIŞIK KELİME) SCREEN
// ----------------------------------------------------
@Composable
fun ScrambleScreen(
    highScore: Int,
    soundHelper: SoundHelper,
    onBack: () -> Unit,
    onGameOverRecord: (Int) -> Unit
) {
    val game = GameCatalog.getGameById("scramble")!!
    var score by remember { mutableIntStateOf(0) }
    var gameOver by remember { mutableStateOf(false) }

    val puzzlePairs = listOf(
        "ELMA" to "Kırmızı tatlı bir meyve",
        "KÖPEK" to "Sadık evcil hayvan",
        "TÜRKİYE" to "Başkenti Ankara olan ülke",
        "YILDIZ" to "Gökyüzünde parlayan gök cismi",
        "KAHVE" to "Sabahları içilen sıcak içecek"
    )

    var currentIdx by remember { mutableIntStateOf(0) }
    val currentPair = puzzlePairs[currentIdx]
    val scrambled = remember(currentPair) { currentPair.first.toList().shuffled().joinToString("") }
    var userLetters by remember { mutableStateOf("") }

    fun addLetter(c: Char) {
        userLetters += c
        soundHelper.playTone(ToneType.TAP)
        if (userLetters == currentPair.first) {
            score += 30
            soundHelper.playTone(ToneType.VICTORY)
            if (currentIdx < puzzlePairs.size - 1) {
                currentIdx++
                userLetters = ""
            } else {
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
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("İpucu: ${currentPair.second}", fontSize = 16.sp, color = NeonAmber, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(24.dp))

            Text("Karışık: $scrambled", fontSize = 32.sp, fontWeight = FontWeight.Black, color = NeonCyan)
            Spacer(modifier = Modifier.height(24.dp))

            Text(userLetters.padEnd(currentPair.first.length, '_'), fontSize = 36.sp, fontWeight = FontWeight.Black, color = TextPrimary)
            Spacer(modifier = Modifier.height(32.dp))

            // Available scrambled letter buttons
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                scrambled.forEach { char ->
                    Button(
                        onClick = { addLetter(char) },
                        colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant)
                    ) {
                        Text("$char", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { userLetters = "" }) {
                Text("Temizle")
            }
        }

        if (gameOver) {
            GameOverDialog(
                score = score,
                highScore = highScore,
                themeColor = game.themeColor,
                onRestart = {
                    currentIdx = 0
                    userLetters = ""
                    score = 0
                    gameOver = false
                },
                onBackToLobby = onBack
            )
        }
    }
}

// ----------------------------------------------------
// 4. TRIVIA QUIZ SCREEN
// ----------------------------------------------------
data class TriviaQ(val question: String, val options: List<String>, val correctIdx: Int)

@Composable
fun TriviaQuizScreen(
    highScore: Int,
    soundHelper: SoundHelper,
    onBack: () -> Unit,
    onGameOverRecord: (Int) -> Unit
) {
    val game = GameCatalog.getGameById("trivia_quiz")!!
    var score by remember { mutableIntStateOf(0) }
    var gameOver by remember { mutableStateOf(false) }

    val questions = listOf(
        TriviaQ("Güneş Sistemindeki en büyük gezegen hangisidir?", listOf("Mars", "Jüpiter", "Satürn", "Venüs"), 1),
        TriviaQ("Türkiye'nin yüzölçümü en büyük ili hangisidir?", listOf("İstanbul", "Ankara", "Konya", "İzmir"), 2),
        TriviaQ("Dünyanın ilk video oyunu sayılan tenis simülasyonu?", listOf("Pong", "Tennis for Two", "Pac-Man", "Tetris"), 1),
        TriviaQ("Android işletim sisteminin ilk sürüm kodu nedir?", listOf("Cupcake", "Donut", "Alpha", "Eclair"), 0),
        TriviaQ("Hangisi asal sayıdır?", listOf("9", "15", "17", "21"), 2)
    )

    var currentQIdx by remember { mutableIntStateOf(0) }

    fun answer(idx: Int) {
        if (idx == questions[currentQIdx].correctIdx) {
            score += 25
            soundHelper.playTone(ToneType.COIN)
        } else {
            soundHelper.playTone(ToneType.HIT)
        }
        if (currentQIdx < questions.size - 1) {
            currentQIdx++
        } else {
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val q = questions[currentQIdx]
            Text("Soru ${currentQIdx + 1} / ${questions.size}", fontSize = 14.sp, color = TextMuted)
            Spacer(modifier = Modifier.height(16.dp))

            Text(q.question, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(modifier = Modifier.height(32.dp))

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                q.options.forEachIndexed { i, opt ->
                    Button(
                        onClick = { answer(i) },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(opt, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
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
                    currentQIdx = 0
                    score = 0
                    gameOver = false
                },
                onBackToLobby = onBack
            )
        }
    }
}

// ----------------------------------------------------
// 5. FLAG QUIZ SCREEN
// ----------------------------------------------------
@Composable
fun FlagQuizScreen(
    highScore: Int,
    soundHelper: SoundHelper,
    onBack: () -> Unit,
    onGameOverRecord: (Int) -> Unit
) {
    val game = GameCatalog.getGameById("flag_quiz")!!
    var score by remember { mutableIntStateOf(0) }
    var gameOver by remember { mutableStateOf(false) }

    val flags = listOf(
        Pair("🇹🇷", listOf("Türkiye", "Japonya", "Almanya", "İtalya")),
        Pair("🇯🇵", listOf("Çin", "Japonya", "Kore", "Tayvan")),
        Pair("🇧🇷", listOf("Arjantin", "Brezilya", "Şili", "Kolombiya")),
        Pair("🇩🇪", listOf("Fransa", "Belçika", "Almanya", "Avusturya")),
        Pair("🇨🇦", listOf("ABD", "Kanada", "İngiltere", "Avustralya"))
    )

    var currentIdx by remember { mutableIntStateOf(0) }

    fun pick(country: String) {
        val correct = when (flags[currentIdx].first) {
            "🇹🇷" -> "Türkiye"
            "🇯🇵" -> "Japonya"
            "🇧🇷" -> "Brezilya"
            "🇩🇪" -> "Almanya"
            else -> "Kanada"
        }
        if (country == correct) {
            score += 20
            soundHelper.playTone(ToneType.COIN)
        } else {
            soundHelper.playTone(ToneType.HIT)
        }
        if (currentIdx < flags.size - 1) {
            currentIdx++
        } else {
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(flags[currentIdx].first, fontSize = 72.sp)
            Spacer(modifier = Modifier.height(24.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                flags[currentIdx].second.forEach { country ->
                    Button(
                        onClick = { pick(country) },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant)
                    ) {
                        Text(country, fontSize = 16.sp, color = TextPrimary)
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
                    currentIdx = 0
                    score = 0
                    gameOver = false
                },
                onBackToLobby = onBack
            )
        }
    }
}

// ----------------------------------------------------
// 6. TRUE / FALSE MATH SCREEN
// ----------------------------------------------------
@Composable
fun TrueFalseMathScreen(
    highScore: Int,
    soundHelper: SoundHelper,
    onBack: () -> Unit,
    onGameOverRecord: (Int) -> Unit
) {
    val game = GameCatalog.getGameById("true_false_math")!!
    var score by remember { mutableIntStateOf(0) }
    var gameOver by remember { mutableStateOf(false) }
    var equation by remember { mutableStateOf("") }
    var isActuallyTrue by remember { mutableStateOf(true) }

    fun nextEq() {
        val a = Random.nextInt(1, 20)
        val b = Random.nextInt(1, 20)
        val correct = a + b
        val isTrue = Random.nextBoolean()
        isActuallyTrue = isTrue
        val displayed = if (isTrue) correct else correct + Random.nextInt(-3, 4).let { if (it == 0) 2 else it }
        equation = "$a + $b = $displayed"
    }

    LaunchedEffect(Unit) {
        nextEq()
    }

    fun answer(userSaysTrue: Boolean) {
        if (userSaysTrue == isActuallyTrue) {
            score += 10
            soundHelper.playTone(ToneType.COIN)
            nextEq()
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(equation, fontSize = 40.sp, fontWeight = FontWeight.Black, color = TextPrimary)
            Spacer(modifier = Modifier.height(48.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(32.dp)) {
                Button(
                    onClick = { answer(false) },
                    modifier = Modifier.size(90.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = NeonRed)
                ) {
                    Text("❌", fontSize = 32.sp)
                }
                Button(
                    onClick = { answer(true) },
                    modifier = Modifier.size(90.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)
                ) {
                    Text("✔️", fontSize = 32.sp)
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
                    nextEq()
                    gameOver = false
                },
                onBackToLobby = onBack
            )
        }
    }
}

// ----------------------------------------------------
// 7. ANAGRAM RUSH SCREEN
// ----------------------------------------------------
@Composable
fun AnagramRushScreen(
    highScore: Int,
    soundHelper: SoundHelper,
    onBack: () -> Unit,
    onGameOverRecord: (Int) -> Unit
) {
    val game = GameCatalog.getGameById("anagram_rush")!!
    var score by remember { mutableIntStateOf(0) }
    var gameOver by remember { mutableStateOf(false) }

    val pool = listOf('K', 'A', 'R', 'T', 'E', 'L')
    val validWords = setOf("KART", "KALE", "KARE", "TERK", "KARTEL", "LAKE", "TELA", "ERAT")
    val foundWords = remember { mutableStateListOf<String>() }
    var currentWord by remember { mutableStateOf("") }

    fun submitWord() {
        if (validWords.contains(currentWord) && !foundWords.contains(currentWord)) {
            foundWords.add(currentWord)
            score += currentWord.length * 10
            soundHelper.playTone(ToneType.COIN)
        } else {
            soundHelper.playTone(ToneType.HIT)
        }
        currentWord = ""
        if (foundWords.size >= 4) {
            soundHelper.playTone(ToneType.VICTORY)
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Harflerden Türkçe kelimeler üret!", fontSize = 14.sp, color = TextSecondary)
            Spacer(modifier = Modifier.height(16.dp))

            Text(currentWord.ifEmpty { "..." }, fontSize = 32.sp, fontWeight = FontWeight.Black, color = NeonCyan)
            Spacer(modifier = Modifier.height(24.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                pool.forEach { ch ->
                    Button(
                        onClick = { currentWord += ch },
                        colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant)
                    ) {
                        Text("$ch", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(onClick = { currentWord = "" }) { Text("Sil") }
                Button(onClick = { submitWord() }, colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)) {
                    Text("Gönder", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("Bulunan Kelimeler: ${foundWords.joinToString(", ")}", color = TextMuted)
        }

        if (gameOver) {
            GameOverDialog(
                score = score,
                highScore = highScore,
                themeColor = game.themeColor,
                onRestart = {
                    foundWords.clear()
                    currentWord = ""
                    score = 0
                    gameOver = false
                },
                onBackToLobby = onBack
            )
        }
    }
}

// ----------------------------------------------------
// 8. MEMORY SEQUENCE SCREEN
// ----------------------------------------------------
@Composable
fun MemorySequenceScreen(
    highScore: Int,
    soundHelper: SoundHelper,
    onBack: () -> Unit,
    onGameOverRecord: (Int) -> Unit
) {
    val game = GameCatalog.getGameById("memory_sequence")!!
    var score by remember { mutableIntStateOf(0) }
    var gameOver by remember { mutableStateOf(false) }

    var digitsCount by remember { mutableIntStateOf(3) }
    var targetDigits by remember { mutableStateOf("") }
    var userDigits by remember { mutableStateOf("") }
    var isShowingDigits by remember { mutableStateOf(true) }

    fun generateNewSequence() {
        targetDigits = (1..digitsCount).map { Random.nextInt(0, 10) }.joinToString("")
        userDigits = ""
        isShowingDigits = true
    }

    LaunchedEffect(digitsCount) {
        generateNewSequence()
        delay(1500)
        isShowingDigits = false
    }

    fun enterDigit(d: Int) {
        if (isShowingDigits || gameOver) return
        userDigits += d.toString()
        soundHelper.playTone(ToneType.TAP)
        if (userDigits.length == targetDigits.length) {
            if (userDigits == targetDigits) {
                score += digitsCount * 10
                soundHelper.playTone(ToneType.POWERUP)
                digitsCount++
            } else {
                soundHelper.playTone(ToneType.GAMEOVER)
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
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (isShowingDigits) {
                Text("Ezberle!", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = NeonAmber)
                Spacer(modifier = Modifier.height(16.dp))
                Text(targetDigits, fontSize = 44.sp, fontWeight = FontWeight.Black, color = NeonCyan)
            } else {
                Text("Hatırla ve Gir:", fontSize = 18.sp, color = TextSecondary)
                Spacer(modifier = Modifier.height(16.dp))
                Text(userDigits.padEnd(targetDigits.length, '_'), fontSize = 44.sp, fontWeight = FontWeight.Black, color = TextPrimary)
                Spacer(modifier = Modifier.height(24.dp))

                // Keypad 0..9
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        (1..3).forEach { d -> Button(onClick = { enterDigit(d) }) { Text("$d") } }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        (4..6).forEach { d -> Button(onClick = { enterDigit(d) }) { Text("$d") } }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        (7..9).forEach { d -> Button(onClick = { enterDigit(d) }) { Text("$d") } }
                    }
                    Button(onClick = { enterDigit(0) }, modifier = Modifier.align(Alignment.CenterHorizontally)) { Text("0") }
                }
            }
        }

        if (gameOver) {
            GameOverDialog(
                score = score,
                highScore = highScore,
                themeColor = game.themeColor,
                onRestart = {
                    digitsCount = 3
                    score = 0
                    gameOver = false
                },
                onBackToLobby = onBack
            )
        }
    }
}
