package com.ifochka.m14n.data.auth

import com.ifochka.m14n.data.api.M14nApi

expect fun createFirebaseAuthRepository(api: M14nApi): AuthRepository
