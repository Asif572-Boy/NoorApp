package com.noor.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.noor.app.data.QuranRepository
import com.noor.app.data.SettingsStore
import com.noor.app.data.SurahMeta
import com.noor.app.data.VerseRow
import com.noor.app.ui.theme.CardGreen
import com.noor.app.ui.theme.DeepGreen
import com.noor.app.ui.theme.Gold
import com.noor.app.ui.theme.GradientHeader
import com.noor.app.ui.theme.QuranArabicFont
import com.noor.app.ui.theme.SoftGold
import com.noor.app.ui.theme.UrduFont
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.launch

private val PageBg = Color(0xFFFBFAF3)   // soft parchment
private val ArabicInk = Color(0xFF14231C)

@Composable
fun ReadQuranScreen() {
    var selected by remember { mutableStateOf<SurahMeta?>(null) }
    val current = selected
    if (current == null) {
        SurahListView(onOpen = { selected = it })
    } else {
        BackHandler { selected = null }
        SurahReaderView(surah = current, onBack = { selected = null })
    }
}

@Composable
private fun SurahListView(onOpen: (SurahMeta) -> Unit) {
    val context = LocalContext.current
    val settings = remember { SettingsStore(context) }
    val bookmark by settings.bookmarkSurah.collectAsStateWithLifecycle(initialValue = null)

    var list by remember { mutableStateOf<List<SurahMeta>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) {
        try { list = QuranRepository.surahList() }
        catch (e: Exception) { error = "Couldn't load. Check your internet and reopen." }
    }

    Column(Modifier.fillMaxSize()) {
        GradientHeader(title = "Read Qur'an", subtitle = "Uthmani script with Urdu translation")
        when {
            error != null -> CenterNote(error!!)
            list.isEmpty() -> CenterLoader()
            else -> LazyColumn(
                Modifier.padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                bookmark?.let { bm ->
                    list.firstOrNull { it.number == bm }?.let { s ->
                        item {
                            Card(
                                Modifier.fillMaxWidth().clickable { onOpen(s) },
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = Gold.copy(alpha = 0.20f))
                            ) {
                                Row(Modifier.fillMaxWidth().padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Bookmark, contentDescription = null, tint = DeepGreen)
                                    Spacer(Modifier.width(12.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text("Resume reading", color = DeepGreen,
                                            fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
                                        Text(s.englishName, color = Color(0xFF3C4A42), fontSize = 13.sp)
                                    }
                                    Text(s.arabicName, fontFamily = QuranArabicFont,
                                        color = DeepGreen, fontSize = 20.sp)
                                }
                            }
                        }
                    }
                }
                items(list) { s -> SurahRow(s, s.number == bookmark) { onOpen(s) } }
            }
        }
    }
}

@Composable
private fun SurahRow(s: SurahMeta, isBookmarked: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier.size(40.dp).background(DeepGreen, CircleShape),
                contentAlignment = Alignment.Center
            ) { Text("${s.number}", color = SoftGold, fontSize = 14.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold) }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(s.englishName, fontSize = 16.sp, color = DeepGreen,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
                Text("${s.meaning} • ${s.ayahCount} ayahs", fontSize = 12.sp, color = Color(0xFF5B6B62))
            }
            if (isBookmarked) {
                Icon(Icons.Filled.Bookmark, contentDescription = null, tint = Gold,
                    modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
            }
            Text(s.arabicName, fontFamily = QuranArabicFont, fontSize = 22.sp, color = DeepGreen)
        }
    }
}

@Composable
private fun SurahReaderView(surah: SurahMeta, onBack: () -> Unit) {
    val context = LocalContext.current
    val settings = remember { SettingsStore(context) }
    val scope = rememberCoroutineScope()
    val showTranslation by settings.showTranslation.collectAsStateWithLifecycle(initialValue = true)
    val bookmarkSurah by settings.bookmarkSurah.collectAsStateWithLifecycle(initialValue = null)
    val bookmarkPage by settings.bookmarkPage.collectAsStateWithLifecycle(initialValue = 0)

    var verses by remember { mutableStateOf<List<VerseRow>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(surah.number) {
        verses = emptyList(); error = null
        try { verses = QuranRepository.surahContent(surah.number) }
        catch (e: Exception) { error = "Couldn't load this surah. Check internet." }
    }

    val pages = remember(verses) { verses.chunked(6) }
    val startPage = if (bookmarkSurah == surah.number) bookmarkPage.coerceIn(0, maxOf(0, pages.size - 1)) else 0
    val pagerState = rememberPagerState(initialPage = startPage, pageCount = { pages.size.coerceAtLeast(1) })

    Column(Modifier.fillMaxSize().background(PageBg)) {
        Row(
            Modifier.fillMaxWidth().background(DeepGreen).padding(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = SoftGold)
            }
            Column(Modifier.weight(1f)) {
                Text(surah.englishName, color = SoftGold, fontSize = 18.sp)
                Text(surah.arabicName, color = Gold, fontSize = 15.sp, fontFamily = QuranArabicFont)
            }
            IconButton(onClick = { scope.launch { settings.setShowTranslation(!showTranslation) } }) {
                Icon(Icons.Filled.Translate, contentDescription = "Translation",
                    tint = if (showTranslation) Gold else SoftGold.copy(alpha = 0.5f))
            }
            val isBm = bookmarkSurah == surah.number
            IconButton(onClick = {
                scope.launch { settings.setBookmark(surah.number, pagerState.currentPage) }
            }) {
                Icon(if (isBm) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                    contentDescription = "Bookmark", tint = Gold)
            }
        }

        when {
            error != null -> CenterNote(error!!)
            verses.isEmpty() -> CenterLoader()
            else -> HorizontalPager(state = pagerState, modifier = Modifier.weight(1f)) { page ->
                Column(Modifier.fillMaxSize().padding(18.dp).verticalScroll(rememberScrollState())) {
                    if (page == 0) {
                        Text("بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                            fontFamily = QuranArabicFont, fontSize = 26.sp, color = DeepGreen,
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                            textAlign = TextAlign.Center)
                    }
                    pages[page].forEach { v ->
                        Text(
                            "${v.arabic}  \u06DD${v.numberInSurah}",
                            style = TextStyle(textDirection = TextDirection.Rtl),
                            fontFamily = QuranArabicFont,
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                            fontSize = 26.sp, lineHeight = 52.sp, color = ArabicInk
                        )
                        if (showTranslation) {
                            Text(
                                v.urdu,
                                style = TextStyle(textDirection = TextDirection.Rtl),
                                fontFamily = UrduFont,
                                textAlign = TextAlign.End,
                                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                                fontSize = 16.sp, lineHeight = 34.sp, color = Color(0xFF3C4A42)
                            )
                        }
                        HorizontalDivider(color = Gold.copy(alpha = 0.25f))
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("Page ${page + 1} of ${pages.size}  •  swipe to turn",
                        fontSize = 12.sp, color = Color(0xFF8A9990),
                        modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                }
            }
        }
    }
}

@Composable
private fun CenterLoader() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = DeepGreen)
    }
}

@Composable
private fun CenterNote(text: String) {
    Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Text(text, textAlign = TextAlign.Center)
    }
}
