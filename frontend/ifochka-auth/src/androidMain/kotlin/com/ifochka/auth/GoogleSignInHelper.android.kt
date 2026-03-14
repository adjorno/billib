package com.ifochka.auth

import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.ifochka.core.AppContext
import dev.gitlive.firebase.auth.AuthCredential
import dev.gitlive.firebase.auth.GoogleAuthProvider

actual suspend fun getGoogleCredential(): AuthCredential? {
    if (AuthConfig.current.googleWebClientId.isEmpty()) return null
    return runCatching {
        val context = AppContext.applicationContext
        val credentialManager = CredentialManager.create(context)
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(
                GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(AuthConfig.current.googleWebClientId)
                    .build(),
            )
            .build()
        val result = credentialManager.getCredential(context = context, request = request)
        val idToken = GoogleIdTokenCredential.createFrom(result.credential.data).idToken
        GoogleAuthProvider.credential(idToken = idToken, accessToken = null)
    }.getOrNull()
}
