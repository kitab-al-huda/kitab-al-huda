package com.alfred.kitabalhuda.ui.settings

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.alfred.kitabalhuda.ui.theme.KitabAlHudaTheme

class AboutActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            KitabAlHudaTheme {
                AboutScreen(
                    onBackClick = { finish() }
                )
            }
        }
    }
}

