package com.alfred.kitabalhuda.ui.settings

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.AdapterView
import android.view.View
import com.google.android.material.switchmaterial.SwitchMaterial
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.alfred.kitabalhuda.R
import com.alfred.kitabalhuda.utils.PreferenceManager
import java.util.Locale

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        setupLanguageSpinner()
    }

    private fun setupLanguageSpinner() {
        // Language selection removed. App is forced to Arabic.
    }
}
