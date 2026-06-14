package com.alfred.kitabalhuda.ui.player

sealed class PlayerUiState {
    object Idle : PlayerUiState()
    object Loading : PlayerUiState()
    data class Playing(val title: String, val artist: String) : PlayerUiState()
    data class Paused(val title: String, val artist: String) : PlayerUiState()
    data class Error(val message: String) : PlayerUiState()
}
