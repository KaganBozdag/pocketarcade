package com.example.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.auth.AuthState
import com.example.auth.GitHubAuthDialog
import com.example.data.GameScoreEntity
import com.example.data.UserGameScoreEntity
import com.example.model.ArcadeGame
import com.example.model.GameCatalog
import com.example.model.GameCategory
import com.example.model.AchievementCatalog
import com.example.model.AchievementContext
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LobbyScreen(
    scores: List<GameScoreEntity>,
    userScores: List<UserGameScoreEntity> = emptyList(),
    authState: AuthState = AuthState.Idle,
    favoriteGameIds: Set<String> = emptySet(),
    onToggleFavorite: (String) -> Unit = {},
    onSelectGame: (Screen) -> Unit,
    onStartLogin: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val context = LocalContext.current
    var showAuthModal by remember { mutableStateOf(false) }
    var showAchievementsModal by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf<GameCategory?>(null) }
    var showFavoritesOnly by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val achievementContext = remember(scores, userScores, authState) {
        AchievementCatalog.buildContext(
            deviceScores = scores,
            userScores = userScores,
            isAuthenticated = authState is AuthState.Authenticated
        )
    }
    val unlockedAchievementsCount = remember(achievementContext) {
        AchievementCatalog.getUnlockedCount(achievementContext)
    }
    val totalAchievementsCount = remember { AchievementCatalog.achievements.size }

    val allGames = GameCatalog.games

    val filteredGames = remember(selectedCategory, showFavoritesOnly, favoriteGameIds, searchQuery) {
        allGames.filter { game ->
            val matchesFavorites = !showFavoritesOnly || favoriteGameIds.contains(game.id)
            val matchesCategory = showFavoritesOnly || selectedCategory == null || game.category == selectedCategory
            val matchesSearch = searchQuery.isBlank() ||
                    game.title.contains(searchQuery, ignoreCase = true) ||
                    game.subtitle.contains(searchQuery, ignoreCase = true)
            matchesFavorites && matchesCategory && matchesSearch
        }
    }

    // Best scores calculation
    val totalGamesPlayed = if (authState is AuthState.Authenticated) {
        maxOf(scores.sumOf { it.gamesPlayed }, userScores.sumOf { it.gamesPlayed })
    } else {
        scores.sumOf { it.gamesPlayed }
    }

    val totalScoreSum = allGames.sumOf { game ->
        val dev = scores.find { it.gameId == game.id }?.highScore ?: 0
        val usr = userScores.find { it.gameId == game.id }?.highScore ?: 0
        maxOf(dev, usr)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "🎮 50-in-1 MEGA ARCADE",
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            color = TextPrimary
                        )
                    }
                },
                actions = {
                    // Achievements Button
                    OutlinedButton(
                        onClick = { showAchievementsModal = true },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NeonAmber.copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonAmber),
                        modifier = Modifier
                            .height(32.dp)
                            .padding(end = 4.dp)
                            .testTag("achievements_top_btn")
                    ) {
                        Text(
                            text = "🏆 $unlockedAchievementsCount/$totalAchievementsCount",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // GitHub Account Profile Button
                    IconButton(
                        onClick = { showAuthModal = true },
                        modifier = Modifier.testTag("github_account_top_btn")
                    ) {
                        when (authState) {
                            is AuthState.Authenticated -> {
                                if (!authState.user.avatarUrl.isNullOrEmpty()) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(context)
                                            .data(authState.user.avatarUrl)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = "Profil",
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .border(2.dp, NeonGreen, CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(NeonGreen),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = authState.user.login.take(1).uppercase(),
                                            color = Color.Black,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                            }
                            is AuthState.Loading -> {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp,
                                    color = NeonCyan
                                )
                            }
                            else -> {
                                Icon(
                                    imageVector = Icons.Default.AccountCircle,
                                    contentDescription = "Giriş Yap",
                                    tint = NeonCyan,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
            )
        },
        containerColor = DarkBackground
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(top = 10.dp, bottom = 32.dp)
        ) {
            // GitHub Account Status Banner
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { showAuthModal = true }
                        .testTag("github_status_banner"),
                    colors = CardDefaults.cardColors(
                        containerColor = if (authState is AuthState.Authenticated) Color(0xFF16241C) else Color(0xFF1E232F)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (authState is AuthState.Authenticated) "🐙" else "☁️",
                            fontSize = 24.sp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            if (authState is AuthState.Authenticated) {
                                Text(
                                    text = "Giriş Yapıldı: ${authState.user.name ?: authState.user.login}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = NeonGreen
                                )
                                Text(
                                    text = "50 oyunun skorları GitHub hesabına senkronize",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            } else {
                                Text(
                                    text = "GitHub ile Giriş Yap",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = NeonCyan
                                )
                                Text(
                                    text = "Skorlarını hesabına kaydet ve sıralamaya gir",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                        Text(
                            text = if (authState is AuthState.Authenticated) "Hesap" else "Bağlan",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (authState is AuthState.Authenticated) NeonGreen else NeonCyan
                        )
                    }
                }
            }

            // Hero Banner
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .border(1.5.dp, DarkBorder, RoundedCornerShape(20.dp))
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.arcade_hero_banner_1788870137830),
                        contentDescription = "Arcade Banner",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    // Gradient overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0x22000000),
                                        Color(0xD90B0914)
                                    )
                                )
                            )
                    )
                    // Banner text
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(NeonGreen.copy(alpha = 0.25f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("50 OYUN DAHİL", fontSize = 10.sp, fontWeight = FontWeight.Black, color = NeonGreen)
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(NeonPink.copy(alpha = 0.25f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("ÇEVRİMİÇİ DÜELLO", fontSize = 10.sp, fontWeight = FontWeight.Black, color = NeonPink)
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Retro, Zeka, Hız & Canlı Kapışma",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                }
            }

            // Stats bar
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(DarkSurfaceVariant)
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("OYNANAN", fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.SemiBold)
                        Text("$totalGamesPlayed", fontSize = 18.sp, color = NeonCyan, fontWeight = FontWeight.Bold)
                    }
                    Box(modifier = Modifier.width(1.dp).height(24.dp).background(DarkBorder))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("TOPLAM SKOR", fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.SemiBold)
                        Text("$totalScoreSum", fontSize = 18.sp, color = NeonGreen, fontWeight = FontWeight.Bold)
                    }
                    Box(modifier = Modifier.width(1.dp).height(24.dp).background(DarkBorder))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("TOPLAM OYUN", fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.SemiBold)
                        Text("50", fontSize = 18.sp, color = NeonPink, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Achievements Banner Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, NeonAmber.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                        .clickable { showAchievementsModal = true }
                        .testTag("achievements_summary_card"),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1B170B)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(NeonAmber.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "🏆", fontSize = 22.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "BAŞARIM SİSTEMİ",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = NeonAmber,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "$unlockedAchievementsCount / $totalAchievementsCount Açıldı",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NeonGreen
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { (unlockedAchievementsCount.toFloat() / totalAchievementsCount.toFloat()).coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(5.dp)
                                    .clip(CircleShape),
                                color = NeonAmber,
                                trackColor = DarkBorder
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Özel Görev: '%100 Tamamlama' için tüm oyunlarda en az 100 skor yap!",
                                fontSize = 10.sp,
                                color = TextSecondary,
                                maxLines = 1
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "İncele >",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonAmber
                        )
                    }
                }
            }

            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("game_search_input"),
                    placeholder = { Text("50 oyun içinde ara (örn: pong, rulet, kelime)...", fontSize = 13.sp, color = TextMuted) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Ara", tint = TextSecondary, modifier = Modifier.size(18.dp))
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Temizle", tint = TextMuted, modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DarkSurface,
                        unfocusedContainerColor = DarkSurface,
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = DarkBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    singleLine = true
                )
            }

            // Category Filter Chips
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = !showFavoritesOnly && selectedCategory == null,
                        onClick = {
                            showFavoritesOnly = false
                            selectedCategory = null
                        },
                        label = { Text("TÜMÜ (${allGames.size})", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NeonCyan,
                            selectedLabelColor = Color.Black,
                            containerColor = DarkSurfaceVariant,
                            labelColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    val favoriteCount = allGames.count { favoriteGameIds.contains(it.id) }
                    FilterChip(
                        selected = showFavoritesOnly,
                        onClick = {
                            showFavoritesOnly = !showFavoritesOnly
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (showFavoritesOnly) Color.Black else NeonAmber
                            )
                        },
                        label = { Text("FAVORİLER ($favoriteCount)", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NeonAmber,
                            selectedLabelColor = Color.Black,
                            containerColor = DarkSurfaceVariant,
                            labelColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("filter_chip_favorites")
                    )

                    GameCategory.values().filter { it != GameCategory.ALL }.forEach { cat ->
                        val count = allGames.count { it.category == cat }
                        val isSel = !showFavoritesOnly && selectedCategory == cat
                        FilterChip(
                            selected = isSel,
                            onClick = {
                                showFavoritesOnly = false
                                selectedCategory = if (isSel) null else cat
                            },
                            label = { Text("${cat.icon} ${cat.title} ($count)", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NeonAmber,
                                selectedLabelColor = Color.Black,
                                containerColor = DarkSurfaceVariant,
                                labelColor = TextPrimary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }

            // Results count
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when {
                            showFavoritesOnly -> "FAVORİ OYUNLAR"
                            selectedCategory == null -> "TÜM OYUNLAR"
                            else -> selectedCategory!!.title.uppercase()
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "${filteredGames.size} Oyun Listelendi",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }
            }

            // Game Cards
            items(filteredGames, key = { it.id }) { game ->
                val localScore = scores.find { it.gameId == game.id }?.highScore ?: 0
                val userScore = userScores.find { it.gameId == game.id }?.highScore ?: 0
                val bestScore = maxOf(localScore, userScore)

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
                        .clickable { onSelectGame(Screen.PlayGame(game.id)) }
                        .testTag("game_card_${game.id}"),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Emoji Avatar Box
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(game.themeColor.copy(alpha = 0.15f))
                                .border(1.5.dp, game.themeColor.copy(alpha = 0.4f), RoundedCornerShape(14.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = game.emoji, fontSize = 26.sp)
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Game info
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = game.title,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                if (game.isOnline) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(NeonPink.copy(alpha = 0.2f))
                                            .padding(horizontal = 4.dp, vertical = 1.dp)
                                    ) {
                                        Text("CANLI", fontSize = 8.sp, fontWeight = FontWeight.Black, color = NeonPink)
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = game.subtitle,
                                fontSize = 11.sp,
                                color = TextSecondary,
                                maxLines = 2,
                                lineHeight = 14.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = NeonAmber,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = if (bestScore > 0) "En İyi: $bestScore" else "Henüz Oynanmadı",
                                    fontSize = 11.sp,
                                    color = if (bestScore > 0) NeonAmber else TextMuted,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Favorite Star Button
                        val isFavorite = favoriteGameIds.contains(game.id)
                        IconButton(
                            onClick = { onToggleFavorite(game.id) },
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("favorite_btn_${game.id}")
                        ) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = if (isFavorite) "Favorilerden Çıkar" else "Favorilere Ekle",
                                tint = if (isFavorite) NeonAmber else TextMuted,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        // Play Button
                        Button(
                            onClick = { onSelectGame(Screen.PlayGame(game.id)) },
                            colors = ButtonDefaults.buttonColors(containerColor = game.themeColor),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("play_btn_${game.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Oyna",
                                tint = Color.Black,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = if (game.isOnline) "DÜELLO" else "OYNA",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }
                    }
                }
            }

            if (filteredGames.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = if (showFavoritesOnly) "⭐" else "🔍",
                                fontSize = 42.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = if (showFavoritesOnly) "Henüz favori oyun eklemediniz" else "Aramanıza uygun oyun bulunamadı",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (showFavoritesOnly) "Oyunların yanındaki yıldız simgesine dokunarak favorilerinize ekleyebilirsiniz." else "Farklı bir arama terimi veya kategori deneyin.",
                                fontSize = 12.sp,
                                color = TextMuted,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 32.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAuthModal) {
        GitHubAuthDialog(
            authState = authState,
            onDismiss = { showAuthModal = false },
            onStartLogin = { onStartLogin() },
            onLogout = { onLogout() },
            userScores = userScores,
            deviceScores = scores
        )
    }

    if (showAchievementsModal) {
        AchievementsDialog(
            context = achievementContext,
            onDismiss = { showAchievementsModal = false }
        )
    }
}
