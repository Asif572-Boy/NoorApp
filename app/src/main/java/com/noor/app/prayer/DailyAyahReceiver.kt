package com.noor.app.prayer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.noor.app.data.QuranRepository
import java.util.Calendar
import kotlinx.coroutines.runBlocking

class DailyAyahReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        try {
            val day = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
            val globalAyah = ((day * 17) % 6236) + 1
            val ayah = runBlocking { QuranRepository.ayah(globalAyah) }
            AyahNotifier.show(context, ayah.reference, ayah.arabic, ayah.urdu)
        } catch (_: Exception) {
            AyahNotifier.show(
                context,
                "Al-Baqarah 2:255",
                "اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ",
                "اللہ (وہ زندہ اور قائم رہنے والا ہے) اس کے سوا کوئی معبود نہیں۔"
            )
        }
        // Chain the next day's ayah.
        DailyAyahScheduler.schedule(context)
    }
}
