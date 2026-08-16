package com.noor.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.noor.app.prayer.AyahNotifier
import com.noor.app.ui.NoorApp
import com.noor.app.ui.theme.NoorTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AyahNotifier.ensureChannel(this)
        setContent {
            NoorTheme { NoorApp() }
        }
    }
}
