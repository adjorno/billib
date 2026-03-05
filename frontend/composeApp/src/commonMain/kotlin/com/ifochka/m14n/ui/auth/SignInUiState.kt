package com.ifochka.m14n.ui.auth

sealed interface SignInUiState {
    data object Idle : SignInUiState

    data object Loading : SignInUiState

    data object Success : SignInUiState

    data class Error(
        val message: String,
    ) : SignInUiState
}
