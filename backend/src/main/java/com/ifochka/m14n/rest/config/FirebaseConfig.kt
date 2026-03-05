package com.ifochka.m14n.rest.config

import com.google.auth.oauth2.AccessToken
import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class FirebaseConfig(
    @param:Value("\${firebase.project-id}") private val projectId: String,
) {
    @Bean
    fun firebaseApp(): FirebaseApp {
        if (FirebaseApp.getApps().isNotEmpty()) return FirebaseApp.getInstance()
        // In emulator mode (FIREBASE_AUTH_EMULATOR_HOST set), verifyIdToken() bypasses
        // signature verification entirely — credentials are never used, so a stub is safe.
        // Outside emulator mode, getApplicationDefault() must succeed; failure means
        // misconfigured production credentials and we fail loudly rather than silently.
        val isEmulator = System.getenv("FIREBASE_AUTH_EMULATOR_HOST") != null
        val credentials = if (isEmulator) {
            GoogleCredentials.create(AccessToken("emulator-stub", null))
        } else {
            GoogleCredentials.getApplicationDefault()
        }
        return FirebaseApp.initializeApp(
            FirebaseOptions.builder()
                .setCredentials(credentials)
                .setProjectId(projectId)
                .build(),
        )
    }
}
