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
        // Credential resolution order:
        // 1. Emulator mode (FIREBASE_AUTH_EMULATOR_HOST set) — stub, never used for verification.
        // 2. FIREBASE_SERVICE_ACCOUNT_JSON env var — service account JSON stored as a Railway/CI secret.
        // 3. Application Default Credentials — GCP-hosted environments (Cloud Run, GCE, etc.).
        // Outside emulator mode at least one of (2) or (3) must succeed; failure is intentionally loud.
        val isEmulator = System.getenv("FIREBASE_AUTH_EMULATOR_HOST") != null
        val serviceAccountJson = System.getenv("FIREBASE_SERVICE_ACCOUNT_JSON")
        val credentials = when {
            isEmulator -> GoogleCredentials.create(AccessToken("emulator-stub", null))
            !serviceAccountJson.isNullOrEmpty() -> GoogleCredentials.fromStream(serviceAccountJson.byteInputStream())
            else -> GoogleCredentials.getApplicationDefault()
        }
        return FirebaseApp.initializeApp(
            FirebaseOptions.builder()
                .setCredentials(credentials)
                .setProjectId(projectId)
                .build(),
        )
    }
}
