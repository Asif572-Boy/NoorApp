package com.noor.app.prayer

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationManagerCompat
import com.noor.app.ui.theme.DeepGreen
import com.noor.app.ui.theme.Emerald
import com.noor.app.ui.theme.Gold
import com.noor.app.ui.theme.SoftGold

class AzanActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showOverLockScreen()
        val prayer = intent.getStringExtra(PrayerAlarmScheduler.EXTRA_NAME) ?: "Prayer"
        setContent { AzanUi(prayer) { dismiss() } }
    }

    private fun showOverLockScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }
    }

    private fun dismiss() {
        startService(Intent(this, AzanService::class.java).apply { action = AzanService.ACTION_STOP })
        finish()
    }
}

@Composable
private fun AzanUi(prayer: String, onDismiss: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(DeepGreen, Emerald))),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("\u262A", color = Gold, fontSize = 40.sp, textAlign = TextAlign.Center,
            modifier = Modifier.padding(16.dp))
        Text("It is time for", color = SoftGold, fontSize = 18.sp)
        Text(prayer, color = Gold, fontSize = 44.sp)
        Text("prayer", color = SoftGold, fontSize = 18.sp,
            modifier = Modifier.padding(bottom = 40.dp))
        Button(
            onClick = onDismiss,
            colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = DeepGreen),
            contentPadding = PaddingValues(horizontal = 40.dp, vertical = 12.dp)
        ) { Text("Dismiss", fontSize = 18.sp) }
    }
}
