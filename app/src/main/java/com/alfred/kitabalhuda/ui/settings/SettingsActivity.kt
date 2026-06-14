package com.alfred.kitabalhuda.ui.settings

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.alfred.kitabalhuda.R
import com.alfred.kitabalhuda.utils.DigitHelper
import com.alfred.kitabalhuda.utils.PreferenceManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        setupToolbar()
        setupNumberStyleRow()
        setupAboutRow()
        setupPlaybackSpeedRow()
        updateSummaries()
    }

    private fun setupToolbar() {
        findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)
            .setNavigationOnClickListener { finish() }
    }

    private fun updateSummaries() {
        val useIndic = PreferenceManager.isArabicIndicEnabled(this)
        findViewById<TextView>(R.id.summary_number_style).text =
            getString(if (useIndic) R.string.arabic_indic else R.string.standard_digits)

        val speed = PreferenceManager.getPlaybackSpeed(this)
        findViewById<TextView>(R.id.summary_playback_speed).text =
            when (speed) {
                0.5f -> getString(R.string.speed_050)
                0.75f -> getString(R.string.speed_075)
                1.25f -> getString(R.string.speed_125)
                1.5f -> getString(R.string.speed_150)
                2.0f -> getString(R.string.speed_200)
                else -> getString(R.string.speed_normal)
            }
    }

    private fun setupNumberStyleRow() {
        findViewById<android.view.View>(R.id.row_number_style).setOnClickListener {
            val currentIsIndic = PreferenceManager.isArabicIndicEnabled(this)
            val options = arrayOf(
                getString(R.string.arabic_indic),
                getString(R.string.standard_digits)
            )
            MaterialAlertDialogBuilder(this)
                .setTitle(R.string.number_style)
                .setSingleChoiceItems(options, if (currentIsIndic) 0 else 1) { dialog, which ->
                    val useIndic = which == 0
                    PreferenceManager.setArabicIndicEnabled(this, useIndic)
                    DigitHelper.useArabicIndic = useIndic
                    updateSummaries()
                    dialog.dismiss()
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
        }
    }

    private fun setupAboutRow() {
        findViewById<android.view.View>(R.id.row_about).setOnClickListener {
            startActivity(Intent(this, AboutActivity::class.java))
        }
    }

    private fun setupPlaybackSpeedRow() {
        findViewById<android.view.View>(R.id.row_playback_speed).setOnClickListener {
            val currentSpeed = PreferenceManager.getPlaybackSpeed(this)
            val speeds = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f)
            val labels = speeds.map { speed ->
                when (speed) {
                    0.5f -> getString(R.string.speed_050)
                    0.75f -> getString(R.string.speed_075)
                    1.0f -> getString(R.string.speed_normal)
                    1.25f -> getString(R.string.speed_125)
                    1.5f -> getString(R.string.speed_150)
                    2.0f -> getString(R.string.speed_200)
                    else -> speed.toString() + "x"
                }
            }.toTypedArray()
            val checkedIndex = speeds.indexOf(currentSpeed).coerceAtLeast(0)
            MaterialAlertDialogBuilder(this)
                .setTitle(R.string.playback_speed)
                .setSingleChoiceItems(labels, checkedIndex) { dialog, which ->
                    PreferenceManager.setPlaybackSpeed(this, speeds[which])
                    updateSummaries()
                    dialog.dismiss()
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
        }
    }
}
