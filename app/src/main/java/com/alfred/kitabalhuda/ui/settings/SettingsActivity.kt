package com.alfred.kitabalhuda.ui.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.alfred.kitabalhuda.ui.theme.KitabAlHudaTheme

class SettingsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            KitabAlHudaTheme {
                SettingsScreen(
                    onBackClick = { finish() },
                    onNavigateToAbout = {
                        startActivity(Intent(this, AboutActivity::class.java))
                    },
                    onNavigateToPrivacy = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://kitab-al-huda.github.io/privacy.html"))
                        startActivity(intent)
                    }
                )
            }
        }
    }
}

