package com.noor.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.noor.app.audio.AudioCache
import com.noor.app.audio.PlaybackController
import com.noor.app.data.QuranRepository
import com.noor.app.data.RECITERS
import com.noor.app.data.SettingsStore
import com.noor.app.data.SurahMeta
import com.noor.app.ui.theme.CardGreen
import com.noor.app.ui.theme.DeepGreen
import com.noor.app.ui.theme.Gold
import com.noor.app.ui.theme.GradientHeader
import com.noor.app.ui.theme.SoftGold
import kotlinx.coroutines.launch

@Composable
fun AudioQuranScreen() {
    val context = LocalContext.current
    val settings = remember { SettingsStore(context) }
    val scope = rememberCoroutineScope()

    val reciter by settings.reciter.collectAsStateWithLifecycle(initialValue = "ar.alafasy")
    val playerState by PlaybackController.state.collectAsStateWithLifecycle()

    var list by remember { mutableStateOf<List<SurahMeta>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) {
        try { list = QuranRepository.surahList() }
        catch (e: Exception) { error = "Couldn't load surahs. Check internet and reopen." }
    }

    Column(Modifier.fillMaxSize()) {
        GradientHeader(title = "Listen", subtitle = "Choose a reciter, then a surah")

        Row(
            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            RECITERS.forEach { r ->
                FilterChip(
                    selected = r.edition == reciter,
                    onClick = { scope.launch { settings.setReciter(r.edition) } },
                    label = { Text(r.name.substringBefore(" (")) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = DeepGreen,
                        selectedLabelColor = SoftGold
                    )
                )
            }
        }

        Box(Modifier.weight(1f)) {
            when {
                error != null -> Box(Modifier.fillMaxSize().padding(32.dp),
                    contentAlignment = Alignment.Center) { Text(error!!, textAlign = TextAlign.Center) }
                list.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = DeepGreen)
                }
                else -> LazyColumn(
                    Modifier.padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 96.dp, top = 4.dp)
                ) {
                    items(list) { s ->
                        val isCurrent = playerState.surahNumber == s.number
                        val cached = AudioCache.isCached(context, reciter, s.number)
                        Card(
                            Modifier.fillMaxWidth().clickable {
                                PlaybackController.play(context, reciter, s.number, s.englishName)
                            },
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isCurrent) Gold.copy(alpha = 0.18f) else Color.White
                            )
                        ) {
                            Row(Modifier.fillMaxWidth().padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.size(38.dp).background(CardGreen, CircleShape),
                                    contentAlignment = Alignment.Center) {
                                    Text("${s.number}", color = DeepGreen, fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold)
                                }
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(s.englishName, fontSize = 16.sp, color = DeepGreen,
                                        fontWeight = FontWeight.SemiBold)
                                    Text(s.meaning, fontSize = 12.sp, color = Color(0xFF5B6B62))
                                }
                                if (cached) {
                                    Icon(Icons.Filled.CheckCircle, contentDescription = "Downloaded",
                                        tint = Color(0xFF2E7D32), modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                } else {
                                    Icon(Icons.Filled.Download, contentDescription = "Will download",
                                        tint = Color(0xFF9AA8A0), modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                }
                                Icon(
                                    if (isCurrent && playerState.isPlaying) Icons.Filled.Pause
                                    else Icons.Filled.PlayArrow,
                                    contentDescription = null, tint = DeepGreen
                                )
                            }
                        }
                    }
                }
            }

            if (playerState.surahNumber != null) {
                Card(
                    Modifier.fillMaxWidth().align(Alignment.BottomCenter).padding(10.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = DeepGreen)
                ) {
                    Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(playerState.title, color = SoftGold, fontWeight = FontWeight.SemiBold)
                            Text(
                                if (playerState.isBusy) "Downloading…"
                                else if (playerState.isPlaying) "Playing" else "Paused",
                                color = Color(0xFFCFE3D7), fontSize = 12.sp
                            )
                        }
                        if (playerState.isBusy) {
                            CircularProgressIndicator(color = Gold, modifier = Modifier.size(24.dp))
                        } else {
                            IconButton(onClick = { PlaybackController.toggle(context) }) {
                                Icon(
                                    if (playerState.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                    contentDescription = "Play/Pause", tint = Gold
                                )
                            }
                        }
                        IconButton(onClick = { PlaybackController.stop(context) }) {
                            Icon(Icons.Filled.Stop, contentDescription = "Stop", tint = SoftGold)
                        }
                    }
                }
            }
        }
    }
}
