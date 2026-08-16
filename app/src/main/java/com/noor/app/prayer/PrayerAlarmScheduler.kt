package com.noor.app.prayer

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.noor.app.data.SettingsStore
import java.util.Calendar
import java.util.Date
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

object PrayerAlarmScheduler {
    const val EXTRA_NAME = "prayer_name"
    private const val REQUEST_CODE = 42

    /** Compute the next prayer (today or tomorrow) and set one exact alarm for it. */
    fun scheduleNext(context: Context) {
        val settings = SettingsStore(context)
        val loc = runBlocking { settings.location.first() } ?: return
        val school = runBlocking { settings.school.first() }

        val now = Date()
        var next = PrayerCalculator.nextPrayer(
            PrayerCalculator.timesFor(loc.lat, loc.lng, school), now
        )
        if (next == null) {
            // All of today's prayers passed — take tomorrow's Fajr.
            val tomorrow = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_MONTH, 1)
            }.time
            next = PrayerCalculator.timesFor(loc.lat, loc.lng, school, tomorrow)
                .firstOrNull { it.isPrayer }
        }
        next ?: return

        val intent = Intent(context, AdhanReceiver::class.java).apply {
            putExtra(EXTRA_NAME, next.name)
        }
        val pi = PendingIntent.getBroadcast(
            context, REQUEST_CODE, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val am = context.getSystemService(AlarmManager::class.java)
        val triggerAt = next.time.time
        val canExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            am.canScheduleExactAlarms()
        } else true

        if (canExact) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
        } else {
            // Fallback: inexact but battery-safe.
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
        }
    }

    fun cancel(context: Context) {
        val intent = Intent(context, AdhanReceiver::class.java)
        val pi = PendingIntent.getBroadcast(
            context, REQUEST_CODE, intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pi != null) context.getSystemService(AlarmManager::class.java).cancel(pi)
    }
}
