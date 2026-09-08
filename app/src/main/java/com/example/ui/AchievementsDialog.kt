package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.Achievement
import com.example.model.AchievementCatalog
import com.example.model.AchievementContext
import com.example.ui.theme.*

enum class AchievementFilter(val title: String) {
    ALL("Tümü"),
    UNLOCKED("Kazanılanlar"),
    LOCKED("Kilitliler")
}

@Composable
fun AchievementsDialog(
    context: AchievementContext,
    onDismiss: () -> Unit
) {
    var selectedFilter by remember { mutableStateOf(AchievementFilter.ALL) }

    val achievements = remember { AchievementCatalog.achievements }

    val evaluatedList = remember(context) {
        achievements.map { ach ->
            val progress = ach.evaluate(context)
            Triple(ach, progress, progress.isUnlocked)
        }
    }

    val unlockedCount = evaluatedList.count { it.third }
    val totalCount = achievements.size
    val totalPercentage = if (totalCount > 0) (unlockedCount * 100) / totalCount else 0

    val filteredList = remember(selectedFilter, evaluatedList) {
        when (selectedFilter) {
            AchievementFilter.ALL -> evaluatedList
            AchievementFilter.UNLOCKED -> evaluatedList.filter { it.third }
            AchievementFilter.LOCKED -> evaluatedList.filter { !it.third }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.88f)
                .clip(RoundedCornerShape(24.dp))
                .border(1.5.dp, DarkBorder, RoundedCornerShape(24.dp))
                .testTag("achievements_dialog"),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🏆", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "BAŞARIMLAR",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = TextPrimary
                            )
                            Text(
                                text = "$unlockedCount / $totalCount Tamamlandı",
                                fontSize = 11.sp,
                                color = NeonGreen,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("achievements_close_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Kapat",
                            tint = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Overall Progress Banner
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "GENEL TAMAMLAMA",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "%$totalPercentage",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = NeonAmber
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        LinearProgressIndicator(
                            progress = { (unlockedCount.toFloat() / totalCount.toFloat()).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(CircleShape),
                            color = NeonAmber,
                            trackColor = DarkBorder
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Filter Tabs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AchievementFilter.values().forEach { filter ->
                        val count = when (filter) {
                            AchievementFilter.ALL -> totalCount
                            AchievementFilter.UNLOCKED -> unlockedCount
                            AchievementFilter.LOCKED -> totalCount - unlockedCount
                        }
                        val isSelected = selectedFilter == filter

                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedFilter = filter },
                            label = {
                                Text(
                                    text = "${filter.title} ($count)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NeonCyan,
                                selectedLabelColor = Color.Black,
                                containerColor = DarkSurfaceVariant,
                                labelColor = TextSecondary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Achievement List
                if (filteredList.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (selectedFilter == AchievementFilter.UNLOCKED)
                                "Henüz açılmış bir başarım yok.\nOyun oynayarak başarımların kilidini açabilirsin!"
                            else
                                "Tebrikler! Tüm başarımları tamamladın! 🏆",
                            color = TextMuted,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 12.dp)
                    ) {
                        items(filteredList, key = { it.first.id }) { (ach, progress, isUnlocked) ->
                            AchievementCard(
                                achievement = ach,
                                current = progress.current,
                                max = progress.max,
                                percentage = progress.percentage,
                                isUnlocked = isUnlocked
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AchievementCard(
    achievement: Achievement,
    current: Int,
    max: Int,
    percentage: Int,
    isUnlocked: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = if (isUnlocked) 1.5.dp else 1.dp,
                color = if (isUnlocked) achievement.themeColor.copy(alpha = 0.8f) else DarkBorder,
                shape = RoundedCornerShape(16.dp)
            )
            .testTag("achievement_card_${achievement.id}"),
        colors = CardDefaults.cardColors(
            containerColor = if (isUnlocked) DarkSurfaceVariant.copy(alpha = 0.9f) else DarkSurface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Emoji Badge Box
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        if (isUnlocked) achievement.themeColor.copy(alpha = 0.2f)
                        else Color.White.copy(alpha = 0.05f)
                    )
                    .border(
                        1.dp,
                        if (isUnlocked) achievement.themeColor else Color.Transparent,
                        RoundedCornerShape(14.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = achievement.icon,
                    fontSize = 26.sp,
                    color = if (isUnlocked) Color.Unspecified else Color.Gray
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = achievement.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isUnlocked) NeonAmber else TextPrimary
                    )

                    // Status Pill
                    if (isUnlocked) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(NeonGreen.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "AÇILDI",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = NeonGreen
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(DarkBorder)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "KİLİTLİ",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = achievement.description,
                    fontSize = 11.sp,
                    color = TextSecondary,
                    lineHeight = 14.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Progress row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LinearProgressIndicator(
                        progress = { (current.toFloat() / max.toFloat()).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .clip(CircleShape),
                        color = if (isUnlocked) NeonGreen else achievement.themeColor,
                        trackColor = DarkBorder
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "$current / $max",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isUnlocked) NeonGreen else TextMuted
                    )
                }
            }
        }
    }
}

@Composable
fun AchievementUnlockBanner(
    achievement: Achievement,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .clickable { onDismiss() }
            .testTag("achievement_unlock_banner")
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .border(2.dp, NeonAmber, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1A0E)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(achievement.icon, fontSize = 32.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "🏆 YENİ BAŞARIM KAZANILDI!",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = NeonAmber,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = achievement.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = achievement.description,
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Kapat", tint = TextMuted)
                }
            }
        }
    }
}
