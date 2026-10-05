package com.alfred.kitabalhuda.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.alfred.kitabalhuda.R
import com.alfred.kitabalhuda.ui.theme.GoldPrimary
import com.alfred.kitabalhuda.ui.theme.TealAccent
import com.alfred.kitabalhuda.ui.theme.TextPrimary
import com.alfred.kitabalhuda.ui.theme.TextSecondary
import com.alfred.kitabalhuda.utils.DigitHelper
import com.alfred.kitabalhuda.utils.PreferenceManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onNavigateToPrivacy: () -> Unit
) {
    val context = LocalContext.current

    var useIndic by remember {
        mutableStateOf(PreferenceManager.isArabicIndicEnabled(context))
    }
    var currentSpeed by remember {
        mutableFloatStateOf(PreferenceManager.getPlaybackSpeed(context))
    }

    var showNumberStyleDialog by remember { mutableStateOf(false) }
    var showSpeedDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.settings_title),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.collapse),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Section: Appearance & Localization
            SettingsGroupHeader(title = stringResource(id = R.string.filters))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column {
                    SettingsRow(
                        icon = Icons.Default.FormatListNumbered,
                        title = stringResource(id = R.string.number_style),
                        subtitle = stringResource(
                            id = if (useIndic) R.string.arabic_indic else R.string.standard_digits
                        ),
                        onClick = { showNumberStyleDialog = true }
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )

                    val speedLabel = when (currentSpeed) {
                        0.5f -> stringResource(id = R.string.speed_050)
                        0.75f -> stringResource(id = R.string.speed_075)
                        1.25f -> stringResource(id = R.string.speed_125)
                        1.5f -> stringResource(id = R.string.speed_150)
                        2.0f -> stringResource(id = R.string.speed_200)
                        else -> stringResource(id = R.string.speed_normal)
                    }

                    SettingsRow(
                        icon = Icons.Default.Speed,
                        title = stringResource(id = R.string.playback_speed),
                        subtitle = speedLabel,
                        onClick = { showSpeedDialog = true }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Section: About & Privacy
            SettingsGroupHeader(title = stringResource(id = R.string.settings))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column {
                    SettingsRow(
                        icon = Icons.Default.Info,
                        title = stringResource(id = R.string.about_title),
                        subtitle = stringResource(id = R.string.about_version),
                        onClick = onNavigateToAbout
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )

                    SettingsRow(
                        icon = Icons.Default.PrivacyTip,
                        title = stringResource(id = R.string.privacy_policy),
                        subtitle = "kitab-al-huda.github.io/privacy.html",
                        onClick = onNavigateToPrivacy
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // Number Style Dialog
    if (showNumberStyleDialog) {
        val options = listOf(
            true to stringResource(id = R.string.arabic_indic),
            false to stringResource(id = R.string.standard_digits)
        )
        AlertDialog(
            onDismissRequest = { showNumberStyleDialog = false },
            title = {
                Text(
                    text = stringResource(id = R.string.number_style),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Column {
                    options.forEach { (isIndic, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = (useIndic == isIndic),
                                    onClick = {
                                        useIndic = isIndic
                                        PreferenceManager.setArabicIndicEnabled(context, isIndic)
                                        DigitHelper.useArabicIndic = isIndic
                                        showNumberStyleDialog = false
                                    },
                                    role = Role.RadioButton
                                )
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (useIndic == isIndic),
                                onClick = null,
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = TealAccent
                                )
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showNumberStyleDialog = false }) {
                    Text(
                        text = stringResource(id = R.string.cancel),
                        color = TealAccent
                    )
                }
            },
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Playback Speed Dialog
    if (showSpeedDialog) {
        val speedOptions = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f)
        AlertDialog(
            onDismissRequest = { showSpeedDialog = false },
            title = {
                Text(
                    text = stringResource(id = R.string.playback_speed),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Column {
                    speedOptions.forEach { speed ->
                        val label = when (speed) {
                            0.5f -> stringResource(id = R.string.speed_050)
                            0.75f -> stringResource(id = R.string.speed_075)
                            1.0f -> stringResource(id = R.string.speed_normal)
                            1.25f -> stringResource(id = R.string.speed_125)
                            1.5f -> stringResource(id = R.string.speed_150)
                            2.0f -> stringResource(id = R.string.speed_200)
                            else -> "${speed}×"
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = (currentSpeed == speed),
                                    onClick = {
                                        currentSpeed = speed
                                        PreferenceManager.setPlaybackSpeed(context, speed)
                                        showSpeedDialog = false
                                    },
                                    role = Role.RadioButton
                                )
                                .padding(vertical = 10.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (currentSpeed == speed),
                                onClick = null,
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = TealAccent
                                )
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showSpeedDialog = false }) {
                    Text(
                        text = stringResource(id = R.string.cancel),
                        color = TealAccent
                    )
                }
            },
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
private fun SettingsGroupHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = GoldPrimary,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
    )
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = TealAccent,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = TextSecondary,
            modifier = Modifier.size(20.dp)
        )
    }
}
