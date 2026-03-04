package com.ifochka.m14n.ui.auth

import androidx.lifecycle.ViewModel
import com.ifochka.m14n.data.auth.AuthRepository
import com.ifochka.m14n.data.auth.AuthState
import kotlinx.coroutines.flow.StateFlow

class AuthViewModel(
    authRepository: AuthRepository,
) : ViewModel() {
    val authState: StateFlow<AuthState> = authRepository.authState
}
