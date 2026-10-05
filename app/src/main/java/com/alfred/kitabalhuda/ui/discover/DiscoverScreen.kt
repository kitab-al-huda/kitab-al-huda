package com.alfred.kitabalhuda.ui.discover

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alfred.kitabalhuda.R
import com.alfred.kitabalhuda.database.dao.ListeningHistoryDao.HistoryItem
import com.alfred.kitabalhuda.database.entity.ReciteurEntity
import com.alfred.kitabalhuda.ui.theme.GoldContainer
import com.alfred.kitabalhuda.ui.theme.GoldLight
import com.alfred.kitabalhuda.ui.theme.GoldPrimary
import com.alfred.kitabalhuda.ui.theme.TealAccent
import com.alfred.kitabalhuda.ui.theme.TealContainer
import com.alfred.kitabalhuda.ui.theme.TealDark
import com.alfred.kitabalhuda.ui.theme.TextPrimary
import com.alfred.kitabalhuda.ui.theme.TextSecondary
import com.alfred.kitabalhuda.ui.theme.TextTertiary

@Composable
fun DiscoverScreen(
    greeting: String,
    lastPlayedItem: HistoryItem?,
    hadithText: String?,
    hadithRef: String?,
    reciters: List<ReciteurEntity>,
    onResumeReadingClick: () -> Unit,
    onReciterClick: (ReciteurEntity) -> Unit,
    bottomPadding: Int = 0,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = bottomPadding.dp)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Time of Day Greeting Header
        Text(
            text = greeting,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = stringResource(id = R.string.discover_beauty),
            style = MaterialTheme.typography.bodyMedium,
            color = GoldPrimary,
            modifier = Modifier.padding(top = 2.dp, bottom = 16.dp)
        )

        // Hero Card: Continue Reading / Resume Playback
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(6.dp, RoundedCornerShape(20.dp))
                .clickable(onClick = onResumeReadingClick),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(TealContainer, MaterialTheme.colorScheme.surfaceVariant)
                        )
                    )
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                contentDescription = null,
                                tint = GoldPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(id = R.string.continue_reading),
                                style = MaterialTheme.typography.labelMedium,
                                color = GoldPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        val surahTitle = lastPlayedItem?.sourate?.nomArabe
                            ?: stringResource(id = R.string.surah_al_mulk)
                        val reciterTitle = lastPlayedItem?.reciteur?.nom
                            ?: stringResource(id = R.string.reciter_default_name)

                        Text(
                            text = surahTitle,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = reciterTitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }

                    // Play Circular Action Button
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .shadow(6.dp, CircleShape)
                            .clip(CircleShape)
                            .background(TealAccent),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = stringResource(id = R.string.play_all),
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Daily Hadith Card (if available)
        if (!hadithText.isNullOrEmpty()) {
            Text(
                text = stringResource(id = R.string.hadith_of_the_day),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = GoldPrimary.copy(alpha = 0.35f),
                        shape = RoundedCornerShape(16.dp)
                    ),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = GoldLight,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(id = R.string.hadith_of_the_day),
                            style = MaterialTheme.typography.labelSmall,
                            color = GoldLight,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Text(
                        text = "« $hadithText »",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 28.sp,
                        textAlign = TextAlign.Start
                    )

                    if (!hadithRef.isNullOrEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = hadithRef,
                            style = MaterialTheme.typography.bodySmall,
                            color = GoldPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Featured Reciters Section
        Text(
            text = stringResource(id = R.string.featured_reciters),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(reciters, key = { it.id }) { reciter ->
                ReciterCard(
                    reciter = reciter,
                    onClick = { onReciterClick(reciter) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun ReciterCard(
    reciter: ReciteurEntity,
    onClick: () -> Unit
) {
    val isZeroRated = reciter.id == 1 || reciter.id == 6

    Card(
        modifier = Modifier
            .width(130.dp)
            .shadow(4.dp, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar Placeholder with Qari Icon
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(listOf(TealDark, TealAccent))
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = reciter.nom.firstOrNull()?.toString() ?: "ق",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = reciter.nom,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            if (isZeroRated) {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = GoldContainer
                ) {
                    Text(
                        text = stringResource(id = R.string.badge_free),
                        style = MaterialTheme.typography.labelSmall,
                        color = GoldLight,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}
