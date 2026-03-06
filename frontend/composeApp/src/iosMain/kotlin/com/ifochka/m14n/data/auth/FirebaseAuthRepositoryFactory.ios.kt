package com.ifochka.m14n.data.auth

import com.ifochka.m14n.data.api.M14nApi

actual fun createFirebaseAuthRepository(api: M14nApi): AuthRepository = FirebaseAuthRepository(api)
