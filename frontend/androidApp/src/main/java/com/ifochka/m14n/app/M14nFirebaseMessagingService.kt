package com.ifochka.m14n.app

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class M14nFirebaseMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        FirebaseMessaging.getInstance().subscribeToTopic("new-chart")
        FirebaseMessaging.getInstance().subscribeToTopic("track-of-day")
    }

    override fun onMessageReceived(message: RemoteMessage) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val title = message.notification?.title ?: return
        val body = message.notification?.body ?: return
        showNotification(title, body)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showNotification(
        title: String,
        body: String,
    ) {
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "m14n_updates"
        manager.createNotificationChannel(
            NotificationChannel(channelId, "M14N Updates", NotificationManager.IMPORTANCE_DEFAULT),
        )
        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setAutoCancel(true)
            .build()
        manager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
