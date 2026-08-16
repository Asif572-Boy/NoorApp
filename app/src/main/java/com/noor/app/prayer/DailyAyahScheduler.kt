package com.noor.app.prayer

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.util.Calendar

object DailyAyahScheduler {
    private const val REQUEST_CODE = 77
    private const val HOUR_OF_DAY = 8   // 8:00 AM local

    fun schedule(context: Context) {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, HOUR_OF_DAY)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            if (before(Calendar.getInstance())) add(Calendar.DAY_OF_MONTH, 1)
        }
        val pi = pendingIntent(context, PendingIntent.FLAG_UPDATE_CURRENT)
        val am = context.getSystemService(AlarmManager::class.java)
        val canExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            am.canScheduleExactAlarms() else true
        if (canExact) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pi)
        } else {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pi)
        }
    }

    fun cancel(context: Context) {
        pendingIntentOrNull(context)?.let {
            context.getSystemService(AlarmManager::class.java).cancel(it)
        }
    }

    private fun pendingIntent(context: Context, flags: Int): PendingIntent {
        val intent = Intent(context, DailyAyahReceiver::class.java)
        return PendingIntent.getBroadcast(
            context, REQUEST_CODE, intent, flags or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun pendingIntentOrNull(context: Context): PendingIntent? {
        val intent = Intent(context, DailyAyahReceiver::class.java)
        return PendingIntent.getBroadcast(
            context, REQUEST_CODE, intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
