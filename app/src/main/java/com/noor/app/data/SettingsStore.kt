package com.noor.app.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.noor.app.prayer.School
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "noor_settings")

data class LatLng(val lat: Double, val lng: Double)

class SettingsStore(private val context: Context) {
    private val keySchool = stringPreferencesKey("school")
    private val keyLat = doublePreferencesKey("lat")
    private val keyLng = doublePreferencesKey("lng")
    private val keyReciter = stringPreferencesKey("reciter")
    private val keyShowTranslation = booleanPreferencesKey("show_translation")
    private val keyBookmarkSurah = intPreferencesKey("bookmark_surah")
    private val keyBookmarkPage = intPreferencesKey("bookmark_page")
    private val keyDailyAyah = booleanPreferencesKey("daily_ayah")

    val school: Flow<School> = context.dataStore.data.map { School.from(it[keySchool]) }

    val location: Flow<LatLng?> = context.dataStore.data.map { p ->
        val lat = p[keyLat]; val lng = p[keyLng]
        if (lat != null && lng != null) LatLng(lat, lng) else null
    }

    val reciter: Flow<String> = context.dataStore.data.map { it[keyReciter] ?: "ar.alafasy" }
    val showTranslation: Flow<Boolean> = context.dataStore.data.map { it[keyShowTranslation] ?: true }
    val bookmarkSurah: Flow<Int?> = context.dataStore.data.map { it[keyBookmarkSurah] }
    val bookmarkPage: Flow<Int> = context.dataStore.data.map { it[keyBookmarkPage] ?: 0 }
    val dailyAyahEnabled: Flow<Boolean> = context.dataStore.data.map { it[keyDailyAyah] ?: false }

    suspend fun setSchool(s: School) { context.dataStore.edit { it[keySchool] = s.name } }
    suspend fun setLocation(lat: Double, lng: Double) {
        context.dataStore.edit { it[keyLat] = lat; it[keyLng] = lng }
    }
    suspend fun setReciter(edition: String) { context.dataStore.edit { it[keyReciter] = edition } }
    suspend fun setShowTranslation(v: Boolean) { context.dataStore.edit { it[keyShowTranslation] = v } }
    suspend fun setBookmark(surah: Int, page: Int) {
        context.dataStore.edit { it[keyBookmarkSurah] = surah; it[keyBookmarkPage] = page }
    }
    suspend fun setDailyAyahEnabled(v: Boolean) { context.dataStore.edit { it[keyDailyAyah] = v } }
}
