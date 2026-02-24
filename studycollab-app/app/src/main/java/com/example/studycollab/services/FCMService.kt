package com.example.studycollab.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.studycollab.MainActivity
import com.example.studycollab.R
import com.example.studycollab.data.remote.ApiClient
import com.example.studycollab.utils.UserSession
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FCMService : FirebaseMessagingService() {

    companion object {
        const val CHANNEL_ID = "studycollab_notifications"
        const val CHANNEL_NAME = "StudyCollab Notifications"
        private const val TAG = "FCMService"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    /**
     * Called when a new FCM token is generated (first launch, token refresh, etc.)
     * We send it to our backend so it can target this device.
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New FCM token: $token")
        sendTokenToServer(token)
    }

    /**
     * Called when a push notification is received while the app is in the foreground.
     * (Background messages are handled automatically by the system.)
     */
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d(TAG, "Message received: ${message.data}")

        val title = message.notification?.title ?: message.data["title"] ?: "StudyCollab"
        val body = message.notification?.body ?: message.data["body"] ?: ""
        val type = message.data["type"] ?: ""
        val relatedId = message.data["relatedId"] ?: ""

        showNotification(title, body, type, relatedId)
    }

    private fun showNotification(title: String, body: String, type: String, relatedId: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigate_to", "notifications")
            putExtra("notification_type", type)
            putExtra("related_id", relatedId)
        }

        val pendingIntent = PendingIntent.getActivity(
            this, System.currentTimeMillis().toInt(), intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher) // Replace with your notification icon
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .build()

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(System.currentTimeMillis().toInt(), notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Study group updates, deadlines, and task notifications"
                enableVibration(true)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun sendTokenToServer(token: String) {
        val userId = UserSession.userId ?: return
        CoroutineScope(Dispatchers.IO).launch {
            try {
                ApiClient.apiService.registerFcmToken(
                    mapOf("userId" to userId, "fcmToken" to token)
                )
                Log.d(TAG, "FCM token sent to server")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to send FCM token: ${e.message}")
            }
        }
    }
}