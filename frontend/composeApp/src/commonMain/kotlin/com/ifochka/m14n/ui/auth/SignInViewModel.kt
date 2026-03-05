package com.ifochka.m14n.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ifochka.m14n.data.auth.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SignInViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow<SignInUiState>(SignInUiState.Idle)
    val uiState: StateFlow<SignInUiState> = _uiState.asStateFlow()

    fun signInWithEmail(
        email: String,
        password: String,
    ) {
        viewModelScope.launch {
            _uiState.value = SignInUiState.Loading
            val result = authRepository.linkWithEmailCredential(
                email = email,
                password = password,
            )
            _uiState.value = result.fold(
                onSuccess = { SignInUiState.Success },
                onFailure = { SignInUiState.Error(it.message ?: "Sign in failed") },
            )
        }
    }

    fun resetState() {
        _uiState.value = SignInUiState.Idle
    }
}
