package com.ifochka.m14n.data.auth

import android.app.Application
import com.ifochka.m14n.BuildKonfig
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseOptions
import dev.gitlive.firebase.initialize

actual fun initFirebase() {
    if (BuildKonfig.FIREBASE_APP_ID.isEmpty()) return

    Firebase.initialize(
        context = Application(),
        options = FirebaseOptions(
            applicationId = BuildKonfig.FIREBASE_APP_ID,
            apiKey = BuildKonfig.FIREBASE_API_KEY,
            projectId = BuildKonfig.FIREBASE_PROJECT_ID,
        ),
    )
}
