package com.noor.app.prayer

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat

class AzanService : Service() {

    private var player: MediaPlayer? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) { stopEverything(); return START_NOT_STICKY }

        val prayer = intent?.getStringExtra(PrayerAlarmScheduler.EXTRA_NAME) ?: "Prayer"
        goForeground(prayer)
        playAzan()
        return START_NOT_STICKY
    }

    private fun playAzan() {
        // Prefer a bundled Makkah azan at res/raw/azan.mp3; fall back to the alarm ringtone.
        val soundUri: Uri = run {
            val id = resources.getIdentifier("azan", "raw", packageName)
            if (id != 0) Uri.parse("android.resource://$packageName/$id")
            else RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        }
        try {
            player = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                setDataSource(this@AzanService, soundUri)
                isLooping = false
                setOnCompletionListener { stopEverything() }
                setOnPreparedListener { start() }
                prepareAsync()
            }
        } catch (e: Exception) {
            stopEverything()
        }
    }

    private fun stopEverything() {
        player?.run { try { if (isPlaying) stop() } catch (_: Exception) {}; release() }
        player = null
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun goForeground(prayer: String) {
        ensureChannel()
        val fullScreen = Intent(this, AzanActivity::class.java).apply {
            putExtra(PrayerAlarmScheduler.EXTRA_NAME, prayer)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        val fsPending = PendingIntent.getActivity(
            this, 0, fullScreen,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val stopPending = PendingIntent.getService(
            this, 1, Intent(this, AzanService::class.java).apply { action = ACTION_STOP },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(prayer)
            .setContentText("It is time for $prayer prayer.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(true)
            .setFullScreenIntent(fsPending, true)
            .setContentIntent(fsPending)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop", stopPending)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIF_ID, builder.build(), ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
        } else {
            startForeground(NOTIF_ID, builder.build())
        }
    }

    private fun ensureChannel() {
        val mgr = getSystemService(NotificationManager::class.java)
        if (mgr.getNotificationChannel(CHANNEL_ID) == null) {
            mgr.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, "Azan", NotificationManager.IMPORTANCE_HIGH).apply {
                    description = "Plays the azan at prayer time"
                    setSound(null, null)  // service plays the audio itself
                    lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
                }
            )
        }
    }

    override fun onDestroy() {
        player?.release(); player = null
        super.onDestroy()
    }

    companion object {
        const val ACTION_STOP = "com.noor.app.AZAN_STOP"
        const val CHANNEL_ID = "azan_channel_v2"
        private const val NOTIF_ID = 2001
    }
}
