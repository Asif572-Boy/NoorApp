package com.noor.app.data

import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

data class SurahMeta(
    val number: Int,
    val arabicName: String,
    val englishName: String,
    val meaning: String,
    val ayahCount: Int
)

data class VerseRow(
    val numberInSurah: Int,
    val arabic: String,
    val urdu: String
)

data class AyahOfDay(
    val reference: String,   // e.g. "Al-Baqarah 2:255"
    val arabic: String,
    val urdu: String
)

object QuranRepository {

    // --- SOURCING POLICY ---
    // Only mainstream Sunni/Shia sources are used. No Ahmadiyya (Qadiani) editions,
    // translations or reciters are referenced anywhere in this app.
    // Arabic: standard Uthmani mushaf text. Urdu: Fateh Muhammad Jalandhari.
    private const val API = "https://api.alquran.cloud/v1"
    private const val ARABIC = "quran-uthmani"
    private const val URDU = "ur.jalandhry"     // Fateh Muhammad Jalandhari (Hanafi tradition)

    private var cachedList: List<SurahMeta>? = null

    private fun httpGet(spec: String): String = (URL(spec).openConnection() as HttpURLConnection).run {
        connectTimeout = 15000
        readTimeout = 20000
        requestMethod = "GET"
        try {
            inputStream.bufferedReader().use { it.readText() }
        } finally {
            disconnect()
        }
    }

    suspend fun surahList(): List<SurahMeta> = withContext(Dispatchers.IO) {
        cachedList?.let { return@withContext it }
        val root = JSONObject(httpGet("$API/surah"))
        val arr = root.getJSONArray("data")
        val out = ArrayList<SurahMeta>(arr.length())
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            out.add(
                SurahMeta(
                    number = o.getInt("number"),
                    arabicName = o.getString("name"),
                    englishName = o.getString("englishName"),
                    meaning = o.optString("englishNameTranslation", ""),
                    ayahCount = o.getInt("numberOfAyahs")
                )
            )
        }
        cachedList = out
        out
    }

    suspend fun surahContent(number: Int): List<VerseRow> = withContext(Dispatchers.IO) {
        val root = JSONObject(httpGet("$API/surah/$number/editions/$ARABIC,$URDU"))
        val data = root.getJSONArray("data")
        val ar = data.getJSONObject(0).getJSONArray("ayahs")
        val ur = data.getJSONObject(1).getJSONArray("ayahs")
        val out = ArrayList<VerseRow>(ar.length())
        for (i in 0 until ar.length()) {
            out.add(
                VerseRow(
                    numberInSurah = ar.getJSONObject(i).getInt("numberInSurah"),
                    arabic = ar.getJSONObject(i).getString("text"),
                    urdu = ur.getJSONObject(i).getString("text")
                )
            )
        }
        out
    }

    /** globalAyah: 1..6236 */
    suspend fun ayah(globalAyah: Int): AyahOfDay = withContext(Dispatchers.IO) {
        val root = JSONObject(httpGet("$API/ayah/$globalAyah/editions/$ARABIC,$URDU"))
        val data = root.getJSONArray("data")
        val a = data.getJSONObject(0)
        val surah = a.getJSONObject("surah")
        val ref = "${surah.getString("englishName")} ${surah.getInt("number")}:${a.getInt("numberInSurah")}"
        AyahOfDay(
            reference = ref,
            arabic = a.getString("text"),
            urdu = data.getJSONObject(1).getString("text")
        )
    }

    /** Full-surah recitation stream URL from the Islamic Network CDN. */
    fun surahAudioUrl(reciterEdition: String, surahNumber: Int): String =
        "https://cdn.islamic.network/quran/audio-surah/128/$reciterEdition/$surahNumber.mp3"
}

data class Reciter(val edition: String, val name: String)

val RECITERS = listOf(
    Reciter("ar.alafasy", "Mishary Rashid Alafasy"),
    Reciter("ar.abdulbasitmurattal", "Abdul Basit (Murattal)"),
    Reciter("ar.abdurrahmaansudais", "Abdur-Rahman As-Sudais"),
    Reciter("ar.husary", "Mahmoud Khalil Al-Husary"),
    Reciter("ar.minshawi", "Mohamed Siddiq El-Minshawi"),
    Reciter("ar.hudhaify", "Ali Al-Hudhaify")
)
