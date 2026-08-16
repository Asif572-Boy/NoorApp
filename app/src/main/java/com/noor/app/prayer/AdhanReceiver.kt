package com.noor.app.prayer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build

class AdhanReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val name = intent.getStringExtra(PrayerAlarmScheduler.EXTRA_NAME) ?: "Prayer"
        val svc = Intent(context, AzanService::class.java).apply {
            putExtra(PrayerAlarmScheduler.EXTRA_NAME, name)
        }
        // Alarm-triggered start is allowed to launch a foreground service.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(svc)
        } else {
            context.startService(svc)
        }
        // Chain the next prayer's alarm.
        PrayerAlarmScheduler.scheduleNext(context)
    }
}
