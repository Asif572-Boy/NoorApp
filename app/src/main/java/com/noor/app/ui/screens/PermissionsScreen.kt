package com.noor.app.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.runtime.DisposableEffect
import com.noor.app.permission.PermissionsManager
import com.noor.app.prayer.PrayerAlarmScheduler
import com.noor.app.ui.theme.DeepGreen
import com.noor.app.ui.theme.Gold
import com.noor.app.ui.theme.SoftGold

@Composable
fun PermissionsScreen(onBack: () -> Unit) {
    val context = LocalContext.current

    // Re-check status whenever we return to this screen from a settings page.
    var refresh by remember { mutableIntStateOf(0) }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val obs = LifecycleEventObserver { _, e ->
            if (e == Lifecycle.Event.ON_RESUME) {
                refresh++
                PrayerAlarmScheduler.scheduleNext(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(obs)
        onDispose { lifecycleOwner.lifecycle.removeObserver(obs) }
    }
    val notifLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { refresh++ }

    val notifOk = remember(refresh) { PermissionsManager.hasNotifications(context) }
    val alarmOk = remember(refresh) { PermissionsManager.hasExactAlarms(context) }
    val batteryOk = remember(refresh) { PermissionsManager.hasBatteryExemption(context) }
    val fsiOk = remember(refresh) { PermissionsManager.hasFullScreenIntent(context) }
    val allOk = notifOk && alarmOk && batteryOk && fsiOk

    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().background(DeepGreen).padding(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = SoftGold)
            }
            Text("Setup & Permissions", color = SoftGold, fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold)
        }

        Column(Modifier.padding(16.dp).verticalScroll(rememberScrollState())) {

            Text(
                if (allOk) "All set — azan will work even when the app is closed or the screen is locked."
                else "Grant these so the azan fires on time even when the app is closed or your phone is locked.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(16.dp))

            PermissionCard(
                title = "Notifications",
                why = "Lets the app show the azan alert.",
                granted = notifOk,
                action = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else PermissionsManager.openAppNotificationSettings(context)
                }
            )
            PermissionCard(
                title = "Exact alarms",
                why = "Triggers the azan at the precise minute, not late.",
                granted = alarmOk,
                action = { PermissionsManager.openExactAlarmSettings(context) }
            )
            PermissionCard(
                title = "Ignore battery optimisation",
                why = "Stops Android from freezing the app, so alarms aren't skipped.",
                granted = batteryOk,
                action = { PermissionsManager.requestBatteryExemption(context) }
            )
            PermissionCard(
                title = "Show on lock screen",
                why = "Lets the azan appear over the lock screen like an alarm.",
                granted = fsiOk,
                action = { PermissionsManager.openFullScreenIntentSettings(context) }
            )

            Spacer(Modifier.height(20.dp))
            Card(
                Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Phone brand settings", fontWeight = FontWeight.SemiBold, color = DeepGreen)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Some brands (Xiaomi, Redmi, Oppo, Vivo, Realme, Samsung) kill background " +
                            "apps aggressively. If the azan is ever late, open this app's settings and " +
                            "turn on Autostart and set Battery to \"No restrictions / Unrestricted\".",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                    Spacer(Modifier.height(10.dp))
                    Button(onClick = { PermissionsManager.openAppDetails(context) }) {
                        Text("Open app settings")
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun PermissionCard(title: String, why: String, granted: Boolean, action: () -> Unit) {
    Card(
        Modifier.fillMaxWidth().padding(bottom = 10.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                if (granted) Icons.Filled.CheckCircle else Icons.Filled.Error,
                contentDescription = null,
                tint = if (granted) Color(0xFF2E7D32) else Gold,
                modifier = Modifier.size(26.dp)
            )
            Spacer(Modifier.size(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.SemiBold)
                Text(why, fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f))
            }
            if (!granted) {
                Button(onClick = action) { Text("Enable") }
            }
        }
    }
}
