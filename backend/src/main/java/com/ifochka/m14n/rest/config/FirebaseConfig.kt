package com.ifochka.m14n.rest.config

import com.google.auth.oauth2.AccessToken
import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.io.IOException

@Configuration
class FirebaseConfig(
    @param:Value("\${firebase.project-id}") private val projectId: String,
) {
    @Bean
    fun firebaseApp(): FirebaseApp {
        val useEmulator = System.getenv("USE_FIREBASE_EMULATOR") == "true"
        if (FirebaseApp.getApps().isNotEmpty()) return FirebaseApp.getInstance()
        // Credential resolution order:
        // 1. USE_FIREBASE_EMULATOR=true in gradle.properties — stub, never used for verification.
        // 2. FIREBASE_SERVICE_ACCOUNT_JSON env var — service account JSON stored as a Railway/CI secret.
        // 3. Application Default Credentials — GCP-hosted environments (Cloud Run, GCE, etc.).
        // Outside emulator mode at least one of (2) or (3) must succeed; failure is intentionally loud.
        val serviceAccountJson = System.getenv("FIREBASE_SERVICE_ACCOUNT_JSON")
        val serviceAccountPath = System.getenv("FIREBASE_SERVICE_ACCOUNT_PATH")
        val credentials = when {
            useEmulator -> GoogleCredentials.create(AccessToken("emulator-stub", null))
            !serviceAccountJson.isNullOrEmpty() -> try {
                GoogleCredentials.fromStream(serviceAccountJson.byteInputStream())
            } catch (e: IOException) {
                throw IllegalStateException("FIREBASE_SERVICE_ACCOUNT_JSON contains invalid JSON: ${e.message}", e)
            }
            !serviceAccountPath.isNullOrEmpty() -> try {
                GoogleCredentials.fromStream(java.io.File(serviceAccountPath).inputStream())
            } catch (e: IOException) {
                throw IllegalStateException(
                    "FIREBASE_SERVICE_ACCOUNT_PATH not readable ($serviceAccountPath): ${e.message}",
                    e,
                )
            }
            else -> try {
                GoogleCredentials.getApplicationDefault()
            } catch (e: IOException) {
                throw IllegalStateException(
                    "No Firebase credentials configured. Set one of:\n" +
                        "  FIREBASE_SERVICE_ACCOUNT_PATH=/path/to/sa.json in ~/.gradle/gradle.properties\n" +
                        "  FIREBASE_SERVICE_ACCOUNT_JSON=<json> env var\n" +
                        "  USE_FIREBASE_EMULATOR=true in ~/.gradle/gradle.properties",
                    e,
                )
            }
        }
        return FirebaseApp.initializeApp(
            FirebaseOptions.builder()
                .setCredentials(credentials)
                .setProjectId(projectId)
                .build(),
        )
    }
}
