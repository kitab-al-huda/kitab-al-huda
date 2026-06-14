package com.alfred.kitabalhuda.ui.settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.alfred.kitabalhuda.R

class AboutActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)
            .setNavigationOnClickListener { finish() }
    }
}
