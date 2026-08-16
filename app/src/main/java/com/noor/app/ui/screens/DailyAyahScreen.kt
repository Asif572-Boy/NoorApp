package com.noor.app.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.noor.app.data.AyahOfDay
import com.noor.app.data.QuranRepository
import com.noor.app.data.SettingsStore
import com.noor.app.prayer.DailyAyahScheduler
import com.noor.app.ui.theme.CardGreen
import com.noor.app.ui.theme.DeepGreen
import com.noor.app.ui.theme.Gold
import com.noor.app.ui.theme.GradientHeader
import java.util.Calendar
import kotlinx.coroutines.launch

@Composable
fun DailyAyahScreen() {
    val context = LocalContext.current
    val settings = remember { SettingsStore(context) }
    val scope = rememberCoroutineScope()
    val enabled by settings.dailyAyahEnabled.collectAsStateWithLifecycle(initialValue = false)

    var ayah by remember { mutableStateOf<AyahOfDay?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var reloadKey by remember { mutableStateOf(0) }

    val notifLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* proceed regardless; notification simply won't show if denied */ }

    fun todayGlobalAyah(): Int {
        val day = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        return ((day * 17) % 6236) + 1
    }

    LaunchedEffect(reloadKey) {
        ayah = null; error = null
        try { ayah = QuranRepository.ayah(todayGlobalAyah()) }
        catch (e: Exception) { error = "Couldn't load. Check your internet and try again." }
    }

    Column(Modifier.fillMaxSize()) {
        GradientHeader(title = "Ayah of the Day", subtitle = "With Urdu translation")

        Column(Modifier.padding(16.dp).verticalScroll(rememberScrollState())) {

            Card(
                Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CardGreen)
            ) {
                Column(Modifier.padding(20.dp)) {
                    when {
                        error != null -> Text(error!!, textAlign = TextAlign.Center)
                        ayah == null -> Box(Modifier.fillMaxWidth().height(120.dp),
                            contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = DeepGreen)
                        }
                        else -> {
                            Text(ayah!!.reference, color = DeepGreen, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(16.dp))
                            Text(
                                ayah!!.arabic,
                                textAlign = TextAlign.End,
                                modifier = Modifier.fillMaxWidth(),
                                fontSize = 26.sp, lineHeight = 46.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(
                                ayah!!.urdu,
                                textAlign = TextAlign.End,
                                modifier = Modifier.fillMaxWidth(),
                                fontSize = 16.sp, lineHeight = 28.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))
            OutlinedButton(onClick = { reloadKey++ }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Filled.Refresh, contentDescription = null)
                Spacer(Modifier.height(0.dp))
                Text("  Show another")
            }

            Spacer(Modifier.height(20.dp))
            Card(
                Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("Daily notification", fontWeight = FontWeight.SemiBold)
                        Text("Get an ayah every morning at 8 AM",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                    Switch(
                        checked = enabled,
                        onCheckedChange = { on ->
                            scope.launch {
                                settings.setDailyAyahEnabled(on)
                                if (on) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    }
                                    DailyAyahScheduler.schedule(context)
                                } else {
                                    DailyAyahScheduler.cancel(context)
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}
