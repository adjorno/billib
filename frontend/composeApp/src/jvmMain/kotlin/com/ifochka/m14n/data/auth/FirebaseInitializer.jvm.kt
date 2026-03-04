package com.ifochka.m14n.data.auth

import android.app.Application
import com.google.firebase.FirebasePlatform
import com.ifochka.m14n.BuildKonfig
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseOptions
import dev.gitlive.firebase.initialize

actual fun initFirebase() {
    val appId = BuildKonfig.FIREBASE_APP_ID.ifEmpty {
        if (BuildKonfig.USE_FIREBASE_EMULATOR) "1:000000000000:android:emulator" else return
    }

    println("[Auth] initFirebase: appId=$appId emulator=${BuildKonfig.USE_FIREBASE_EMULATOR}")

    FirebasePlatform.initializeFirebasePlatform(
        object : FirebasePlatform() {
            private val storage = mutableMapOf<String, String>()

            override fun store(
                key: String,
                value: String,
            ) {
                storage[key] = value
            }

            override fun retrieve(key: String) = storage[key]

            override fun clear(key: String) {
                storage.remove(key)
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
