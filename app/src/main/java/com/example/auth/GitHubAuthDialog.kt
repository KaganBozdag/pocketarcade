package com.example.auth

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.GameScoreEntity
import com.example.data.UserGameScoreEntity
import com.example.model.AchievementCatalog
import com.example.model.AchievementContext
import com.example.model.GameCatalog
import com.example.ui.theme.*

@Composable
fun GitHubAuthDialog(
    authState: AuthState,
    onDismiss: () -> Unit,
    onStartLogin: () -> Unit,
    onLogout: () -> Unit,
    userScores: List<UserGameScoreEntity>,
    deviceScores: List<GameScoreEntity> = emptyList()
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Octocat Icon / Logo
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🐙", fontSize = 20.sp)
                }
                Text(
                    text = "GitHub Hesap Sistemi",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when (authState) {
                    is AuthState.Idle -> {
                        Text(
                            text = "GitHub hesabınızla giriş yaparak tüm oyunlardaki en yüksek skorlarınızı ve rekorlarınızı profilinize kaydedin.",
                            fontSize = 14.sp,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Features List
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(DarkSurfaceVariant)
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("✅", fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Profilinize özel skor tablosu", fontSize = 13.sp, color = TextPrimary)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("☁️", fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("GitHub avatarı ve kullanıcı adı", fontSize = 13.sp, color = TextPrimary)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🏆", fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("6 mini oyunda ayrı ayrı skor takibi", fontSize = 13.sp, color = TextPrimary)
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = onStartLogin,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF238636)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("github_login_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("🐙", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "GitHub ile Giriş Yap",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 15.sp
                            )
                        }
                    }

                    is AuthState.Loading -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            CircularProgressIndicator(color = NeonCyan)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "GitHub hesabına bağlanılıyor...",
                                fontSize = 14.sp,
                                color = TextPrimary,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Tarayıcıdaki yetkilendirmeyi tamamlayın",
                                fontSize = 12.sp,
                                color = TextMuted
                            )
                        }
                    }

                    is AuthState.Error -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(8.dp)
                        ) {
                            Text("⚠️", fontSize = 36.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Giriş Hatası",
                                fontWeight = FontWeight.Bold,
                                color = NeonRed,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = authState.message,
                                fontSize = 13.sp,
                                color = TextSecondary,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = onStartLogin,
                                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Tekrar Dene", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    is AuthState.Authenticated -> {
                        val user = authState.user
                        // Profile Header Card
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(DarkSurfaceVariant)
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (!user.avatarUrl.isNullOrEmpty()) {
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(user.avatarUrl)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "GitHub Avatar",
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(CircleShape)
                                        .border(2.dp, NeonCyan, CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(CircleShape)
                                        .background(NeonCyan),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = user.login.take(1).uppercase(),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 22.sp,
                                        color = Color.Black
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = user.name ?: user.login,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "@${user.login}",
                                    fontSize = 13.sp,
                                    color = NeonCyan
                                )
                                if (!user.bio.isNullOrEmpty()) {
                                    Text(
                                        text = user.bio,
                                        fontSize = 11.sp,
                                        color = TextMuted,
                                        maxLines = 1
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Achievements Card in Profile
                        val achContext = remember(userScores, deviceScores) {
                            AchievementCatalog.buildContext(
                                deviceScores = deviceScores,
                                userScores = userScores,
                                isAuthenticated = true
                            )
                        }
                        val unlockedAchCount = remember(achContext) {
                            AchievementCatalog.getUnlockedCount(achContext)
                        }
                        val totalAchCount = remember { AchievementCatalog.achievements.size }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF1B170B))
                                .border(1.dp, NeonAmber.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🏆", fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("BAŞARIM İLERLEMESİ", fontSize = 10.sp, fontWeight = FontWeight.Black, color = NeonAmber)
                                    Text("$unlockedAchCount / $totalAchCount Başarım Açıldı", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                }
                            }
                            Text(
                                text = "%${if (totalAchCount > 0) (unlockedAchCount * 100) / totalAchCount else 0}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = NeonGreen
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Consolidate played games only
                        val playedGames = remember(userScores, deviceScores) {
                            val playedMap = mutableMapOf<String, Pair<Int, Int>>() // gameId -> (highScore, gamesPlayed)

                            userScores.forEach { us ->
                                if (us.highScore > 0 || us.gamesPlayed > 0) {
                                    playedMap[us.gameId] = Pair(us.highScore, us.gamesPlayed)
                                }
                            }

                            deviceScores.forEach { ds ->
                                if (ds.highScore > 0 || ds.gamesPlayed > 0) {
                                    val current = playedMap[ds.gameId]
                                    val maxHigh = maxOf(current?.first ?: 0, ds.highScore)
                                    val maxPlays = maxOf(current?.second ?: 0, ds.gamesPlayed)
                                    if (maxHigh > 0 || maxPlays > 0) {
                                        playedMap[ds.gameId] = Pair(maxHigh, maxPlays)
                                    }
                                }
                            }

                            playedMap.map { (gid, pair) ->
                                val game = GameCatalog.getGameById(gid)
                                object {
                                    val id = gid
                                    val title = game?.title ?: gid.replace("_", " ").replaceFirstChar { it.uppercase() }
                                    val emoji = game?.emoji ?: "🎮"
                                    val themeColor = game?.themeColor ?: NeonCyan
                                    val highScore = pair.first
                                    val gamesPlayed = pair.second
                                }
                            }.sortedByDescending { it.highScore }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "OYNANAN OYUNLAR (${playedGames.size})",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary
                            )
                            if (playedGames.isNotEmpty()) {
                                Text(
                                    text = "Toplam Rekor: ${playedGames.sumOf { it.highScore }}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = NeonGreen
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (playedGames.isEmpty()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(DarkSurfaceVariant)
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("🎮", fontSize = 28.sp)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Henüz Oynanmış Oyun Yok",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Bir oyun oynadığınızda rekorunuz otomatik olarak burada listelenecektir.",
                                    fontSize = 11.sp,
                                    color = TextMuted,
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 240.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(DarkSurfaceVariant)
                                    .verticalScroll(rememberScrollState())
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                playedGames.forEach { item ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 5.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(item.emoji, fontSize = 16.sp)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text(
                                                    text = item.title,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = TextPrimary,
                                                    maxLines = 1
                                                )
                                                if (item.gamesPlayed > 0) {
                                                    Text(
                                                        text = "${item.gamesPlayed} maç yapıldı",
                                                        fontSize = 10.sp,
                                                        color = TextMuted
                                                    )
                                                }
                                            }
                                        }
                                        Text(
                                            text = "${item.highScore}",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Black,
                                            color = item.themeColor
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Logout Button
                        OutlinedButton(
                            onClick = onLogout,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonRed),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("github_logout_button"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ExitToApp,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Çıkış Yap", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("github_dialog_close")
            ) {
                Text("Kapat", color = NeonCyan, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = DarkSurface,
        shape = RoundedCornerShape(20.dp)
    )
}
