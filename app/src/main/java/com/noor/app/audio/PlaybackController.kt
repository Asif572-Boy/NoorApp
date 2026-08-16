package com.noor.app.audio

import android.content.Context
import android.content.Intent
import android.os.Build
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class PlaybackUiState(
    val surahNumber: Int? = null,
    val title: String = "",
    val isPlaying: Boolean = false,
    val isBusy: Boolean = false   // downloading or buffering
)

object PlaybackController {
    internal val mutable = MutableStateFlow(PlaybackUiState())
    val state: StateFlow<PlaybackUiState> = mutable

    fun play(context: Context, reciter: String, surah: Int, title: String) {
        send(context, PlaybackService.ACTION_PLAY) {
            putExtra(PlaybackService.EXTRA_RECITER, reciter)
            putExtra(PlaybackService.EXTRA_SURAH, surah)
            putExtra(PlaybackService.EXTRA_TITLE, title)
        }
    }

    fun toggle(context: Context) = send(context, PlaybackService.ACTION_TOGGLE) {}
    fun stop(context: Context) = send(context, PlaybackService.ACTION_STOP) {}

    private inline fun send(context: Context, action: String, extras: Intent.() -> Unit) {
        val intent = Intent(context, PlaybackService::class.java).apply {
            this.action = action
            extras()
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }
}
