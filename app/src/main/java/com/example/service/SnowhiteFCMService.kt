package com.example.service

import android.util.Log
import com.example.network.SnowhiteRepository
import com.example.util.SnowhiteNotificationHelper
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SnowhiteFCMService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val title = remoteMessage.notification?.title
            ?: remoteMessage.data["title"]
            ?: "Snowhite Alert"

        val body = remoteMessage.notification?.body
            ?: remoteMessage.data["body"]
            ?: remoteMessage.data["message"]
            ?: "You have a new update from Snowhite."

        Log.d("SnowhiteFCMService", "FCM Message Received: title=$title, body=$body")

        SnowhiteNotificationHelper.showNotification(
            context = applicationContext,
            title = title,
            message = body
        )
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("SnowhiteFCMService", "New FCM Token generated: $token")

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val repository = SnowhiteRepository()
                repository.updateFcmToken(userId = null, fcmToken = token)
            } catch (e: Exception) {
                Log.e("SnowhiteFCMService", "Failed to send FCM token to backend", e)
            }
        }
    }
}
