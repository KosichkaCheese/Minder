package com.app.minder.util.notifications

import android.app.NotificationChannel
import android.content.Context
import android.util.Log
import android.app.NotificationManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.app.minder.R
import com.app.minder.data.remote.Client
import com.app.minder.data.remote.dto.DeviceTokenRequest
import com.app.minder.util.PreferencesKeys
import com.app.minder.util.dataStore
import kotlinx.coroutines.flow.first
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class FCMService: FirebaseMessagingService() {
    companion object {
        const val CHANNEL_ID = "observer_notifications"
    }

    private fun sendTokenToServer(token: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val dataStore = applicationContext.dataStore
                val accessToken = dataStore.data.first()[PreferencesKeys.ACCESS_TOKEN]
                if (accessToken == null) {
                    Log.d("FCM", "No access token, skipping token send (will be sent after login)")
                    return@launch
                }
                val api = Client.createApi(dataStore)
                api.saveDeviceToken(DeviceTokenRequest(token))
                Log.d("FCM", "Token sent to server")
            } catch (e: Exception) {
                Log.e("FCM", "Failed to send token: ${e.message}")
            }
        }
    }

    override fun onNewToken(token: String) {
        Log.d("FCM", "New token: $token")
        sendTokenToServer(token)
    }



    override fun onMessageReceived(message: RemoteMessage) {
        Log.d("FCM", "Message received: ${message.notification?.title}")

        val title = message.notification?.title ?: "Minder"
        val body = message.notification?.body ?: ""

        showNotification(title, body)
    }

    private fun showNotification(title: String, body: String) {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE)
                as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Уведомления наблюдателя",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.notification)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}