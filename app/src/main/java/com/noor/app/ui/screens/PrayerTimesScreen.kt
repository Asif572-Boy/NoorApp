package com.noor.app.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness3
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Brightness2
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.noor.app.data.SettingsStore
import com.noor.app.location.LocationProvider
import com.noor.app.permission.PermissionsManager
import com.noor.app.prayer.PrayerAlarmScheduler
import com.noor.app.prayer.PrayerCalculator
import com.noor.app.prayer.School
import com.noor.app.prayer.Slot
import com.noor.app.ui.theme.DeepGreen
import com.noor.app.ui.theme.Gold
import com.noor.app.ui.theme.GradientHeader
import com.noor.app.ui.theme.SoftGold
import java.util.Date
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private fun schoolIcon(s: School): ImageVector = when (s) {
    School.HANAFI -> Icons.Filled.Brightness2
    School.SHAFI -> Icons.Filled.Star
    School.JAFFRI -> Icons.Filled.Brightness3
}

private fun prayerIcon(name: String): ImageVector = when (name) {
    "Fajr" -> Icons.Filled.WbTwilight
    "Sunrise" -> Icons.Filled.LightMode
    "Dhuhr" -> Icons.Filled.WbSunny
    "Asr" -> Icons.Filled.WbSunny
    "Maghrib" -> Icons.Filled.NightsStay
    "Isha" -> Icons.Filled.DarkMode
    else -> Icons.Filled.WbSunny
}

@Composable
fun PrayerTimesScreen(onOpenPermissions: () -> Unit = {}) {
    val context = LocalContext.current
    val settings = remember { SettingsStore(context) }
    val scope = rememberCoroutineScope()

    val school by settings.school.collectAsStateWithLifecycle(initialValue = School.HANAFI)
    val location by settings.location.collectAsStateWithLifecycle(initialValue = null)

    var slots by remember { mutableStateOf<List<Slot>>(emptyList()) }
    var status by remember { mutableStateOf("Tap the button below to set your location.") }
    var now by remember { mutableStateOf(Date()) }

    LaunchedEffect(Unit) { while (true) { now = Date(); delay(30_000) } }

    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val granted = result[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            scope.launch {
                status = "Getting your location…"
                val loc = LocationProvider.current(context)
                if (loc != null) {
                    settings.setLocation(loc.latitude, loc.longitude)
                    status = ""
                } else status = "Couldn't get location. Turn on GPS and try again."
            }
        } else status = "Location permission is needed for prayer times."
    }

    fun requestSetup() {
        val perms = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            perms.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        permLauncher.launch(perms.toTypedArray())
    }

    LaunchedEffect(location, school) {
        val loc = location
        if (loc != null) {
            slots = PrayerCalculator.timesFor(loc.lat, loc.lng, school)
            status = ""
            PrayerAlarmScheduler.scheduleNext(context)
        }
    }

    val next = PrayerCalculator.nextPrayer(slots, now)

    Column(modifier = Modifier.fillMaxSize()) {
        GradientHeader(
            title = "Noor",
            subtitle = if (next != null)
                "Next: ${next.name} • ${PrayerCalculator.format(next.time)}  (${countdown(next.time, now)})"
            else "Prayer times & azan",
            action = {
                IconButton(onClick = onOpenPermissions) {
                    Icon(Icons.Filled.Settings, contentDescription = "Setup", tint = SoftGold)
                }
            }
        )

        Column(modifier = Modifier.padding(16.dp)) {

            if (!PermissionsManager.allReady(context)) {
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { onOpenPermissions() },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Gold.copy(alpha = 0.22f))
                ) {
                    Row(
                        Modifier.fillMaxWidth().padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Settings, contentDescription = null, tint = DeepGreen)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Finish setup for reliable azan", fontWeight = FontWeight.SemiBold,
                                color = DeepGreen)
                            Text("Tap to allow alarms, notifications & lock-screen azan",
                                fontSize = 12.sp, color = DeepGreen.copy(alpha = 0.8f))
                        }
                    }
                }
                Spacer(Modifier.height(14.dp))
            }

            Text("School of thought", style = MaterialTheme.typography.labelLarge,
                color = DeepGreen, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                School.entries.forEach { s ->
                    FilterChip(
                        selected = s == school,
                        onClick = { scope.launch { settings.setSchool(s) } },
                        label = { Text(s.display) },
                        leadingIcon = {
                            Icon(schoolIcon(s), contentDescription = null,
                                modifier = Modifier.size(18.dp))
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = DeepGreen,
                            selectedLabelColor = SoftGold,
                            selectedLeadingIconColor = Gold
                        )
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            Text(
                school.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Spacer(Modifier.height(14.dp))

            if (status.isNotEmpty()) {
                Text(status, style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(10.dp))
            }

            Button(onClick = { requestSetup() }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Filled.LocationOn, contentDescription = null,
                    modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(if (location == null) "Set my location" else "Refresh location")
            }
            Spacer(Modifier.height(14.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(slots) { slot ->
                    val highlight = next != null && slot.name == next.name && slot.isPrayer
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (highlight)
                                Gold.copy(alpha = 0.20f)
                            else MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    prayerIcon(slot.name),
                                    contentDescription = null,
                                    tint = if (slot.isPrayer) DeepGreen
                                           else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(Modifier.width(14.dp))
                                Text(
                                    slot.name,
                                    fontSize = 17.sp,
                                    fontWeight = if (slot.isPrayer) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (slot.isPrayer) MaterialTheme.colorScheme.onSurface
                                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                            Text(
                                PrayerCalculator.format(slot.time),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (highlight) DeepGreen else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun countdown(target: Date, now: Date): String {
    var mins = ((target.time - now.time) / 60000L).toInt()
    if (mins < 0) mins = 0
    val h = mins / 60; val m = mins % 60
    return if (h > 0) "in ${h}h ${m}m" else "in ${m}m"
}
