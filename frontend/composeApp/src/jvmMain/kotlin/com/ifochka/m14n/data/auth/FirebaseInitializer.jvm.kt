package com.ifochka.m14n.data.auth

import com.ifochka.m14n.BuildKonfig
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseOptions
import dev.gitlive.firebase.initialize

actual fun initFirebase() {
    Firebase.initialize(
        options = FirebaseOptions(
            projectId = BuildKonfig.FIREBASE_PROJECT_ID,
            applicationId = BuildKonfig.FIREBASE_APP_ID,
            apiKey = BuildKonfig.FIREBASE_API_KEY,
        ),
    )
}
