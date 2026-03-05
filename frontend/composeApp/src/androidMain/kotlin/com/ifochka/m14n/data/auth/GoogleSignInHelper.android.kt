package com.ifochka.m14n.data.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.ifochka.m14n.BuildKonfig
import dev.gitlive.firebase.auth.AuthCredential
import dev.gitlive.firebase.auth.GoogleAuthProvider
import org.koin.mp.KoinPlatform

suspend fun getGoogleCredential(): AuthCredential? {
    if (BuildKonfig.GOOGLE_WEB_CLIENT_ID.isEmpty()) return null
    return runCatching {
        val context: Context = KoinPlatform.getKoin().get()
        val credentialManager = CredentialManager.create(context)
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(
                GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(BuildKonfig.GOOGLE_WEB_CLIENT_ID)
                    .build(),
            )
            .build()
        val result = credentialManager.getCredential(context = context, request = request)
        val idToken = GoogleIdTokenCredential.createFrom(result.credential.data).idToken
        GoogleAuthProvider.credential(idToken = idToken, accessToken = null)
    }.getOrNull()
}
