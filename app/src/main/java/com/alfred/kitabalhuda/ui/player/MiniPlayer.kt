package com.alfred.kitabalhuda.ui.player

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.alfred.kitabalhuda.R
import com.alfred.kitabalhuda.ui.theme.GoldPrimary
import com.alfred.kitabalhuda.ui.theme.TealAccent
import com.alfred.kitabalhuda.ui.theme.TealDark
import com.alfred.kitabalhuda.ui.theme.TextPrimary
import com.alfred.kitabalhuda.ui.theme.TextSecondary

@Composable
fun MiniPlayer(
    uiState: PlayerUiState,
    onTogglePlay: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (uiState is PlayerUiState.Idle) return

    val isPlaying = uiState is PlayerUiState.Playing
    val isLoading = uiState is PlayerUiState.Loading

    val (title, artist) = when (uiState) {
        is PlayerUiState.Playing -> uiState.fullTitle to uiState.artist
        is PlayerUiState.Paused -> uiState.fullTitle to uiState.artist
        is PlayerUiState.Loading -> stringResource(id = R.string.loading) to ""
        is PlayerUiState.Error -> uiState.message to ""
        is PlayerUiState.Idle -> stringResource(id = R.string.ready_to_play) to ""
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .shadow(elevation = 8.dp, shape = RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Visualizer / Thumbnail
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(TealDark),
                contentAlignment = Alignment.Center
            ) {
                RadialAudioBars(
                    modifier = Modifier.size(44.dp),
                    isPlaying = isPlaying,
                    barColor = GoldPrimary
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(TealAccent),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "📖",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Surah Title and Reciter
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (artist.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = artist,
                        style = MaterialTheme.typography.bodySmall,
                        color = GoldPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Play/Pause button - Forced LTR for audio semantics
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Box(
                    modifier = Modifier.size(44.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(28.dp),
                            color = TealAccent,
                            strokeWidth = 2.5.dp
                        )
                    } else {
                        IconButton(
                            onClick = onTogglePlay,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(TealAccent)
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = stringResource(id = R.string.play_pause),
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
