package com.alfred.kitabalhuda.ui.player

sealed class PlayerUiState {
    object Idle : PlayerUiState()
    object Loading : PlayerUiState()
    data class Playing(
        val title: String,
        val fullTitle: String,
        val artist: String,
        val shuffleModeEnabled: Boolean = false,
        val mediaId: String? = null,
        val surahNumber: Int = -1
    ) : PlayerUiState()
    data class Paused(
        val title: String,
        val fullTitle: String,
        val artist: String,
        val shuffleModeEnabled: Boolean = false,
        val mediaId: String? = null,
        val surahNumber: Int = -1
    ) : PlayerUiState()
    data class Error(val message: String) : PlayerUiState()
}

data class PlayerProgress(
    val currentPositionMs: Long,
    val totalDurationMs: Long
)
