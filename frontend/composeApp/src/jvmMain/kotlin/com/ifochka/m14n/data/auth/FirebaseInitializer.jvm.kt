package com.ifochka.m14n.data.auth

import android.app.Application
import com.google.firebase.FirebasePlatform
import com.ifochka.m14n.BuildKonfig
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseOptions
import dev.gitlive.firebase.initialize
import java.util.prefs.Preferences

actual fun initFirebase() {
    val appId = BuildKonfig.FIREBASE_APP_ID.ifEmpty {
        if (BuildKonfig.USE_FIREBASE_EMULATOR) "1:000000000000:android:emulator" else return
    }

    println("[Auth] initFirebase: appId=$appId emulator=${BuildKonfig.USE_FIREBASE_EMULATOR}")

    val prefs = Preferences.userRoot().node("com/ifochka/m14n/firebase")

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
            apiKey = BuildKonfig.FIREBASE_API_KEY,
            projectId = BuildKonfig.FIREBASE_PROJECT_ID,
        ),
    )

    println("[Auth] initFirebase: Firebase initialized")
}
