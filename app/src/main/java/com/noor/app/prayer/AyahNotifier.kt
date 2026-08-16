package com.noor.app.prayer

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat

object AyahNotifier {
    const val CHANNEL_ID = "ayah_channel"
    private const val NOTIF_ID = 2002

    fun ensureChannel(context: Context) {
        val mgr = context.getSystemService(NotificationManager::class.java)
        if (mgr.getNotificationChannel(CHANNEL_ID) == null) {
            mgr.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID, "Ayah of the Day",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply { description = "A daily ayah with Urdu translation" }
            )
        }
    }

    fun show(context: Context, reference: String, arabic: String, urdu: String) {
        ensureChannel(context)
        val text = "$arabic\n\n$urdu"
        val notif = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Ayah of the Day — $reference")
            .setContentText(urdu)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setAutoCancel(true)
            .build()
        context.getSystemService(NotificationManager::class.java).notify(NOTIF_ID, notif)
    }
}
