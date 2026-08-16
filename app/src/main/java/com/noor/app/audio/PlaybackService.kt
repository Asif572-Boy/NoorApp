package com.noor.app.audio

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.noor.app.MainActivity
import com.noor.app.data.QuranRepository
import kotlin.concurrent.thread

class PlaybackService : Service() {

    private var player: MediaPlayer? = null
    private var currentReciter: String? = null
    private var currentSurah: Int = -1
    private var currentTitle: String = ""

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY -> {
                val reciter = intent.getStringExtra(EXTRA_RECITER) ?: "ar.alafasy"
                val surah = intent.getIntExtra(EXTRA_SURAH, 1)
                val title = intent.getStringExtra(EXTRA_TITLE) ?: "Surah $surah"
                startPlayback(reciter, surah, title)
            }
            ACTION_TOGGLE -> toggle()
            ACTION_STOP -> stopEverything()
        }
        return START_NOT_STICKY
    }

    private fun startPlayback(reciter: String, surah: Int, title: String) {
        currentReciter = reciter; currentSurah = surah; currentTitle = title
        releasePlayer()
        update(busy = true, playing = false)
        goForeground(playing = false, busy = true)

        thread {
            try {
                val url = QuranRepository.surahAudioUrl(reciter, surah)
                val file = AudioCache.ensure(applicationContext, reciter, surah, url)
                val mp = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    setDataSource(file.absolutePath)
                    setOnCompletionListener { stopEverything() }
                    setOnPreparedListener {
                        start()
                        update(busy = false, playing = true)
                        goForeground(playing = true, busy = false)
                    }
                    prepare()   // local file: prepares fast
                }
                player = mp
            } catch (e: Exception) {
                update(busy = false, playing = false, clear = true)
                stopSelf()
            }
        }
    }

    private fun toggle() {
        val p = player ?: return
        if (p.isPlaying) {
            p.pause(); update(playing = false); goForeground(playing = false, busy = false)
        } else {
            p.start(); update(playing = true); goForeground(playing = true, busy = false)
        }
    }

    private fun stopEverything() {
        releasePlayer()
        update(clear = true)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun releasePlayer() {
        player?.run { try { if (isPlaying) stop() } catch (_: Exception) {}; release() }
        player = null
    }

    private fun update(
        playing: Boolean = PlaybackController.mutable.value.isPlaying,
        busy: Boolean = PlaybackController.mutable.value.isBusy,
        clear: Boolean = false
    ) {
        PlaybackController.mutable.value = if (clear) PlaybackUiState()
        else PlaybackUiState(currentSurah.takeIf { it > 0 }, currentTitle, playing, busy)
    }

    private fun goForeground(playing: Boolean, busy: Boolean) {
        ensureChannel()
        val openApp = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val toggleAction = NotificationCompat.Action(
            if (playing) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play,
            if (playing) "Pause" else "Play",
            servicePending(ACTION_TOGGLE)
        )
        val stopAction = NotificationCompat.Action(
            android.R.drawable.ic_menu_close_clear_cancel, "Stop", servicePending(ACTION_STOP)
        )
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentTitle(currentTitle)
            .setContentText(if (busy) "Loading…" else if (playing) "Playing" else "Paused")
            .setContentIntent(openApp)
            .setOngoing(playing || busy)
            .setOnlyAlertOnce(true)
            .addAction(toggleAction)
            .addAction(stopAction)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIF_ID, builder.build(), ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
        } else {
            startForeground(NOTIF_ID, builder.build())
        }
    }

    private fun servicePending(action: String): PendingIntent {
        val i = Intent(this, PlaybackService::class.java).apply { this.action = action }
        return PendingIntent.getService(
            this, action.hashCode(), i,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun ensureChannel() {
        val mgr = getSystemService(NotificationManager::class.java)
        if (mgr.getNotificationChannel(CHANNEL_ID) == null) {
            mgr.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, "Recitation player", NotificationManager.IMPORTANCE_LOW)
                    .apply { description = "Controls for Qur'an recitation" }
            )
        }
    }

    override fun onDestroy() {
        releasePlayer()
        super.onDestroy()
    }

    companion object {
        const val ACTION_PLAY = "com.noor.app.PLAY"
        const val ACTION_TOGGLE = "com.noor.app.TOGGLE"
        const val ACTION_STOP = "com.noor.app.STOP"
        const val EXTRA_RECITER = "reciter"
        const val EXTRA_SURAH = "surah"
        const val EXTRA_TITLE = "title"
        const val CHANNEL_ID = "recitation_channel"
        private const val NOTIF_ID = 3001
    }
}
