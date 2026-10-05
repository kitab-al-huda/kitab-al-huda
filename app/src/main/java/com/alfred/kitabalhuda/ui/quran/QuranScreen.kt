package com.alfred.kitabalhuda.ui.quran

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alfred.kitabalhuda.R
import com.alfred.kitabalhuda.database.entity.SourateEntity
import com.alfred.kitabalhuda.ui.player.RadialAudioBars
import com.alfred.kitabalhuda.ui.theme.GoldContainer
import com.alfred.kitabalhuda.ui.theme.GoldLight
import com.alfred.kitabalhuda.ui.theme.GoldPrimary
import com.alfred.kitabalhuda.ui.theme.TealAccent
import com.alfred.kitabalhuda.ui.theme.TealContainer
import com.alfred.kitabalhuda.ui.theme.TealDark
import com.alfred.kitabalhuda.ui.theme.TextPrimary
import com.alfred.kitabalhuda.ui.theme.TextSecondary
import com.alfred.kitabalhuda.ui.theme.TextTertiary
import com.alfred.kitabalhuda.utils.toArabicIndic

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun QuranScreen(
    sourates: List<SourateEntity>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedFilter: String, // "ALL", "MECCA", "MEDINA"
    onFilterSelected: (String) -> Unit,
    selectedReciterName: String,
    onReciterChipClick: () -> Unit,
    availableSurahNumbers: Set<Int>,
    currentPlayingSurahNumber: Int,
    isAudioPlaying: Boolean,
    onSurahClick: (SourateEntity) -> Unit,
    onOptionsClick: (SourateEntity) -> Unit,
    onSurahLongClick: (SourateEntity) -> Unit,
    bottomPadding: Int = 0,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .imePadding()
    ) {
        // Top Filter & Search Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Search Input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        text = stringResource(id = R.string.search_hint),
                        color = TextTertiary
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = TealAccent
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = null,
                                tint = TextSecondary
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = TealAccent,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Reciter & Location Filter Chips Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Selected Reciter Chip
                FilterChip(
                    selected = true,
                    onClick = onReciterChipClick,
                    label = {
                        Text(
                            text = selectedReciterName,
                            style = MaterialTheme.typography.labelSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = GoldPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        selectedLabelColor = GoldLight
                    )
                )

                // Mecca Filter
                FilterChip(
                    selected = selectedFilter == "MECCA",
                    onClick = {
                        onFilterSelected(if (selectedFilter == "MECCA") "ALL" else "MECCA")
                    },
                    label = {
                        Text(
                            text = stringResource(id = R.string.filter_mecca),
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = TealContainer,
                        selectedLabelColor = TealAccent
                    )
                )

                // Medina Filter
                FilterChip(
                    selected = selectedFilter == "MEDINA",
                    onClick = {
                        onFilterSelected(if (selectedFilter == "MEDINA") "ALL" else "MEDINA")
                    },
                    label = {
                        Text(
                            text = stringResource(id = R.string.filter_medina),
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = TealContainer,
                        selectedLabelColor = TealAccent
                    )
                )
            }
        }

        // Surah List
        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 4.dp,
                bottom = (bottomPadding + 16).dp
            ),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(sourates, key = { it.numero }) { sourate ->
                val isAvailable = availableSurahNumbers.isEmpty() || availableSurahNumbers.contains(sourate.numero)
                val isCurrentlyPlaying = currentPlayingSurahNumber == sourate.numero

                SurahItemCard(
                    sourate = sourate,
                    isAvailable = isAvailable,
                    isCurrentlyPlaying = isCurrentlyPlaying,
                    isAudioPlaying = isAudioPlaying,
                    onClick = { onSurahClick(sourate) },
                    onOptionsClick = { onOptionsClick(sourate) },
                    onLongClick = { onSurahLongClick(sourate) }
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SurahItemCard(
    sourate: SourateEntity,
    isAvailable: Boolean,
    isCurrentlyPlaying: Boolean,
    isAudioPlaying: Boolean,
    onClick: () -> Unit,
    onOptionsClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(16.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrentlyPlaying) {
                MaterialTheme.colorScheme.surfaceVariant
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Surah Number Disc with RadialAudioBars animation when playing
            Box(
                modifier = Modifier.size(46.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isCurrentlyPlaying) {
                    RadialAudioBars(
                        modifier = Modifier.size(46.dp),
                        isPlaying = isAudioPlaying,
                        barColor = TealAccent
                    )
                }

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            if (isCurrentlyPlaying) TealDark else MaterialTheme.colorScheme.surfaceVariant
                        )
                        .border(
                            width = 1.dp,
                            color = if (isCurrentlyPlaying) TealAccent else MaterialTheme.colorScheme.outlineVariant,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = sourate.numero.toArabicIndic(),
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isCurrentlyPlaying) TealAccent else GoldPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Surah Name and Verses Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = sourate.nomArabe,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isAvailable) MaterialTheme.colorScheme.onSurface else TextTertiary,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(2.dp))

                val place = if (sourate.lieuRevelation == "MECCA") {
                    stringResource(id = R.string.filter_mecca)
                } else {
                    stringResource(id = R.string.filter_medina)
                }
                val verses = "${sourate.nombreVersets.toArabicIndic()} ${stringResource(id = R.string.ayahs_count)}"

                Text(
                    text = if (isAvailable) "$place • $verses" else stringResource(id = R.string.surah_unavailable),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isAvailable) TextSecondary else MaterialTheme.colorScheme.error
                )
            }

            // Options (3 dots) button
            IconButton(onClick = onOptionsClick) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = stringResource(id = R.string.options),
                    tint = TextSecondary
                )
            }
        }
    }
}
