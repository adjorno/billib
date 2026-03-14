package com.ifochka.auth

import android.app.Application
import com.google.firebase.FirebasePlatform
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseOptions
import dev.gitlive.firebase.initialize
import java.util.prefs.Preferences

actual fun initFirebase(config: FirebaseAuthConfig) {
    AuthConfig.current = config

    val appId = config.appId.ifEmpty {
        if (config.useEmulator) "1:000000000000:android:emulator" else return
    }

    println("[Auth] initFirebase: appId=$appId emulator=${config.useEmulator}")

    val prefs = Preferences.userRoot().node("com/ifochka/auth/firebase")

    FirebasePlatform.initializeFirebasePlatform(
        object : FirebasePlatform() {
            override fun store(
                key: String,
                value: String,
            ) {
                println("[Auth] FirebasePlatform.store: key=$key")
                prefs.put(key, value)
                try {
                    prefs.flush()
                } catch (e: Exception) {
                    println("[Auth] FirebasePlatform.store: flush failed: $e")
                }
            }

            override fun retrieve(key: String): String? {
                val value = prefs.get(key, null)
                println("[Auth] FirebasePlatform.retrieve: key=$key found=${value != null}")
                return value
            }

            override fun clear(key: String) {
                println("[Auth] FirebasePlatform.clear: key=$key")
                prefs.remove(key)
                try {
                    prefs.flush()
                } catch (e: Exception) {
                    println("[Auth] FirebasePlatform.clear: flush failed: $e")
                }
            }

            override fun log(msg: String) = println(msg)
        },
    )

    Firebase.initialize(
        context = Application(),
        options = FirebaseOptions(
            applicationId = appId,
            apiKey = config.apiKey,
            projectId = config.projectId,
        ),
    )

    println("[Auth] initFirebase: Firebase initialized")
}
