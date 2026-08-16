package com.noor.app.audio

import android.content.Context
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

/** Stores full-surah recitations under filesDir so each is downloaded only once. */
object AudioCache {
    private fun dir(context: Context): File =
        File(context.filesDir, "recitations").apply { if (!exists()) mkdirs() }

    fun fileFor(context: Context, reciter: String, surah: Int): File =
        File(dir(context), "${reciter}_$surah.mp3")

    fun isCached(context: Context, reciter: String, surah: Int): Boolean =
        fileFor(context, reciter, surah).let { it.exists() && it.length() > 0 }

    /** Downloads the url to the cache file if not already present. Returns the local file. */
    fun ensure(context: Context, reciter: String, surah: Int, url: String): File {
        val target = fileFor(context, reciter, surah)
        if (target.exists() && target.length() > 0) return target
        val part = File(target.parentFile, target.name + ".part")
        val conn = URL(url).openConnection() as HttpURLConnection
        conn.connectTimeout = 20000
        conn.readTimeout = 60000
        try {
            conn.inputStream.use { input ->
                part.outputStream().use { output -> input.copyTo(output, 64 * 1024) }
            }
            if (part.length() > 0) part.renameTo(target)
        } finally {
            conn.disconnect()
            if (part.exists()) part.delete()
        }
        return target
    }
}
