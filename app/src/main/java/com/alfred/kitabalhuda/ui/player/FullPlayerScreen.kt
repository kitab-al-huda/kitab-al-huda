package com.alfred.kitabalhuda.ui.player

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alfred.kitabalhuda.R
import com.alfred.kitabalhuda.ui.theme.GoldLight
import com.alfred.kitabalhuda.ui.theme.GoldPrimary
import com.alfred.kitabalhuda.ui.theme.TealAccent
import com.alfred.kitabalhuda.ui.theme.TealDark
import com.alfred.kitabalhuda.ui.theme.TealPrimary
import com.alfred.kitabalhuda.ui.theme.TextPrimary
import com.alfred.kitabalhuda.ui.theme.TextSecondary
import com.alfred.kitabalhuda.ui.theme.TextTertiary
import com.alfred.kitabalhuda.utils.formatDurationArabic

@Composable
fun FullPlayerScreen(
    uiState: PlayerUiState,
    progress: PlayerProgress,
    playbackMode: PlaybackMode,
    onTogglePlay: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onSeek: (Long) -> Unit,
    onToggleShuffle: () -> Unit,
    onSelectPlaybackMode: () -> Unit,
    onSleepTimerClick: () -> Unit,
    onChangeReciterClick: () -> Unit,
    onCollapse: () -> Unit,
    isSleepTimerActive: Boolean = false,
    modifier: Modifier = Modifier
) {
    val isPlaying = uiState is PlayerUiState.Playing
    val isLoading = uiState is PlayerUiState.Loading

    val (title, artist, shuffleEnabled) = when (uiState) {
        is PlayerUiState.Playing -> Triple(uiState.title, uiState.artist, uiState.shuffleModeEnabled)
        is PlayerUiState.Paused -> Triple(uiState.title, uiState.artist, uiState.shuffleModeEnabled)
        is PlayerUiState.Loading -> Triple(stringResource(id = R.string.loading), "", false)
        is PlayerUiState.Error -> Triple(uiState.message, "", false)
        is PlayerUiState.Idle -> Triple(stringResource(id = R.string.ready_to_play), "", false)
    }

    var isDraggingSlider by remember { mutableStateOf(false) }
    var sliderDragValue by remember { mutableFloatStateOf(0f) }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar: Collapse button and Change Reciter
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onCollapse) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = stringResource(id = R.string.collapse),
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(32.dp)
                    )
                }

                FilledTonalButton(
                    onClick = onChangeReciterClick,
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = GoldPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(id = R.string.choose_reciter),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Center Visualizer Disk with RadialAudioBars
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(TealDark, MaterialTheme.colorScheme.surfaceVariant)
                        )
                    )
                    .border(2.dp, GoldPrimary.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                RadialAudioBars(
                    modifier = Modifier.size(240.dp),
                    isPlaying = isPlaying,
                    barColor = TealAccent,
                    barCount = 16
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "﷽",
                            style = MaterialTheme.typography.headlineLarge,
                            color = GoldLight
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Surah Title & Reciter Name
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = artist,
                style = MaterialTheme.typography.titleMedium,
                color = GoldPrimary,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(28.dp))

            // ================================================================
            // Timeline & Seek Slider — FORCED LTR
            // Audio timeline conventions are strictly Left-to-Right
            // ================================================================
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    val totalDuration = progress.totalDurationMs.coerceAtLeast(0L)
                    val currentPos = if (isDraggingSlider) {
                        sliderDragValue.toLong()
                    } else {
                        progress.currentPositionMs.coerceIn(0L, totalDuration.coerceAtLeast(1L))
                    }

                    val maxSliderValue = if (totalDuration > 0) totalDuration.toFloat() else 1f
                    val currentSliderValue = currentPos.toFloat().coerceIn(0f, maxSliderValue)

                    Slider(
                        value = currentSliderValue,
                        onValueChange = { newValue ->
                            isDraggingSlider = true
                            sliderDragValue = newValue
                        },
                        onValueChangeFinished = {
                            isDraggingSlider = false
                            onSeek(sliderDragValue.toLong())
                        },
                        valueRange = 0f..maxSliderValue,
                        colors = SliderDefaults.colors(
                            thumbColor = TealAccent,
                            activeTrackColor = TealAccent,
                            inactiveTrackColor = MaterialTheme.colorScheme.outlineVariant
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = (currentPos / 1000).formatDurationArabic(),
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        Text(
                            text = (totalDuration / 1000).formatDurationArabic(),
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ================================================================
            // Primary Playback Controls — FORCED LTR
            // (Prev is left, Play is center, Next is right)
            // ================================================================
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Shuffle Button
                    IconButton(onClick = onToggleShuffle) {
                        Icon(
                            imageVector = Icons.Default.Shuffle,
                            contentDescription = stringResource(id = R.string.shuffle_mode),
                            tint = if (shuffleEnabled) GoldPrimary else TextTertiary,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    // Previous Surah Button
                    IconButton(onClick = onPrevious) {
                        Icon(
                            imageVector = Icons.Default.SkipPrevious,
                            contentDescription = stringResource(id = R.string.previous),
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    // Main Play/Pause Button
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .shadow(8.dp, CircleShape)
                            .clip(CircleShape)
                            .background(TealAccent),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(36.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 3.dp
                            )
                        } else {
                            IconButton(
                                onClick = onTogglePlay,
                                modifier = Modifier.size(72.dp)
                            ) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = stringResource(id = R.string.play_pause),
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(42.dp)
                                )
                            }
                        }
                    }

                    // Next Surah Button
                    IconButton(onClick = onNext) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = stringResource(id = R.string.next),
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    // Playback Mode Button
                    IconButton(onClick = onSelectPlaybackMode) {
                        val (modeIcon, isHighlighted) = when (playbackMode) {
                            PlaybackMode.SEQUENTIAL -> Icons.Default.Repeat to false
                            PlaybackMode.REPEAT_ALL -> Icons.Default.Repeat to true
                            PlaybackMode.REPEAT_ONE -> Icons.Default.RepeatOne to true
                            PlaybackMode.PLAY_CURRENT_AND_STOP -> Icons.Default.StopCircle to true
                        }
                        Icon(
                            imageVector = modeIcon,
                            contentDescription = stringResource(id = R.string.repeat_mode),
                            tint = if (isHighlighted) GoldPrimary else TextTertiary,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Bottom Actions: Sleep Timer
            FilledTonalButton(
                onClick = onSleepTimerClick,
                shape = RoundedCornerShape(20.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Bedtime,
                    contentDescription = null,
                    tint = if (isSleepTimerActive) GoldPrimary else TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(id = R.string.sleep_timer),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isSleepTimerActive) GoldPrimary else MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
