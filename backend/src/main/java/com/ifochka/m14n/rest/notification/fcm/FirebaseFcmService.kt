package com.ifochka.m14n.rest.notification.fcm

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import com.google.firebase.messaging.Notification
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.io.ByteArrayInputStream

@Service
class FirebaseFcmService : FcmService {
    private val logger = LoggerFactory.getLogger(FirebaseFcmService::class.java)

    private val messaging: FirebaseMessaging? = initFirebase()

    private fun initFirebase(): FirebaseMessaging? {
        val json = System.getenv("FIREBASE_SERVICE_ACCOUNT_JSON") ?: run {
            logger.warn("FIREBASE_SERVICE_ACCOUNT_JSON not set — FCM notifications disabled")
            return null
        }
        val credentials = GoogleCredentials.fromStream(ByteArrayInputStream(json.toByteArray()))
        val options = FirebaseOptions.builder()
            .setCredentials(credentials)
            .build()
        val app = if (FirebaseApp.getApps().isEmpty()) {
            FirebaseApp.initializeApp(options)
        } else {
            FirebaseApp.getInstance()
        }
        logger.info("FCM initialized (app: ${app.name})")
        return FirebaseMessaging.getInstance(app)
    }

    override fun sendToTopic(
        topic: String,
        title: String,
        body: String,
    ) {
        val m = messaging ?: run {
            logger.warn("FCM disabled, skipping notification to topic '$topic'")
            return
        }
        val message = Message.builder()
            .setTopic(topic)
            .setNotification(
                Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build(),
            )
            .build()
        try {
            val response = m.send(message)
            logger.info("FCM sent to topic '$topic': $response")
        } catch (e: Exception) {
            logger.error("Failed to send FCM message to topic '$topic'", e)
        }
    }
}
