package com.ifochka.m14n.data.auth

sealed interface AuthState {
    data object Loading : AuthState

    data object Anonymous : AuthState

    data class SignedIn(
        val email: String?,
    ) : AuthState
}
