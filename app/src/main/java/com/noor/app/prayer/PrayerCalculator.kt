package com.noor.app.prayer

import com.batoulapps.adhan.CalculationMethod
import com.batoulapps.adhan.CalculationParameters
import com.batoulapps.adhan.Coordinates
import com.batoulapps.adhan.Madhab
import com.batoulapps.adhan.PrayerAdjustments
import com.batoulapps.adhan.PrayerTimes
import com.batoulapps.adhan.data.DateComponents
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class Slot(
    val name: String,
    val time: Date,
    val isPrayer: Boolean  // true = has azan; false = informational (e.g. sunrise)
)

object PrayerCalculator {

    /** Karachi method is the standard reference for Pakistan (Fajr 18, Isha 18). */
    private fun parametersFor(school: School): CalculationParameters {
        return when (school) {
            School.HANAFI -> CalculationMethod.KARACHI.parameters.apply {
                madhab = Madhab.HANAFI
            }
            School.SHAFI -> CalculationMethod.KARACHI.parameters.apply {
                madhab = Madhab.SHAFI
            }
            School.JAFFRI -> CalculationMethod.OTHER.parameters.apply {
                // Shia Ithna-Ashari (Jafari): Fajr 16, Isha 14, standard Asr.
                fajrAngle = 16.0
                ishaAngle = 14.0
                madhab = Madhab.SHAFI
                // Jafari maghrib falls a few minutes after sunset; approx +4 min.
                // (fajr, sunrise, dhuhr, asr, maghrib, isha)
                methodAdjustments = PrayerAdjustments(0, 0, 0, 0, 4, 0)
            }
        }
    }

    fun timesFor(lat: Double, lng: Double, school: School, day: Date = Date()): List<Slot> {
        val coordinates = Coordinates(lat, lng)
        val cal = Calendar.getInstance().apply { time = day }
        val dc = DateComponents(
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH)
        )
        val pt = PrayerTimes(coordinates, dc, parametersFor(school))
        return listOf(
            Slot("Fajr", pt.fajr, true),
            Slot("Sunrise", pt.sunrise, false),
            Slot("Dhuhr", pt.dhuhr, true),
            Slot("Asr", pt.asr, true),
            Slot("Maghrib", pt.maghrib, true),
            Slot("Isha", pt.isha, true)
        )
    }

    private val fmt = SimpleDateFormat("h:mm a", Locale.getDefault())
    fun format(date: Date): String = fmt.format(date)

    /** The next upcoming prayer slot (with azan) from [from], or null if all passed today. */
    fun nextPrayer(slots: List<Slot>, from: Date = Date()): Slot? =
        slots.filter { it.isPrayer && it.time.after(from) }.minByOrNull { it.time }
}
