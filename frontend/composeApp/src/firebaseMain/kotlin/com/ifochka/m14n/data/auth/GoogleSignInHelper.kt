package com.ifochka.m14n.data.auth

import dev.gitlive.firebase.auth.AuthCredential

expect suspend fun getGoogleCredential(): AuthCredential?
