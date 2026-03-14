package com.ifochka.auth

import dev.gitlive.firebase.auth.AuthCredential

expect suspend fun getAppleCredential(): AuthCredential?
