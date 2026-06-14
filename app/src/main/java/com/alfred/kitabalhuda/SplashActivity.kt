package com.alfred.kitabalhuda

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import android.widget.TextView
import android.widget.ProgressBar

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("SplashActivity", "onCreate appelé")
        setContentView(R.layout.activity_splash)

        val textStatus = findViewById<TextView>(R.id.textStatus)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        textStatus.text = getString(R.string.loading)
        progressBar.visibility = ProgressBar.VISIBLE

        val app = application as KitabAlHudaApplication
        lifecycleScope.launch {
            // Removed legacy sync call
            // app.repository.synchronizeData()
            textStatus.text = getString(R.string.loading)
            kotlinx.coroutines.delay(1000) // Small delay for logo visibility
            
            progressBar.visibility = ProgressBar.GONE
            // Quand c'est fini, lance MainActivity
            startActivity(Intent(this@SplashActivity, MainActivity::class.java))
            finish()
        }
    }
} 