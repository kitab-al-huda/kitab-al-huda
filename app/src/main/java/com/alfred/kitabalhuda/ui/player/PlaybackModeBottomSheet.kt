package com.alfred.kitabalhuda.ui.player

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.alfred.kitabalhuda.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class PlaybackModeBottomSheet(
    private val currentMode: PlaybackMode,
    private val onModeSelected: (PlaybackMode) -> Unit
) : BottomSheetDialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.bottom_sheet_playback_mode, container, false)

        view.findViewById<TextView>(R.id.mode_sequential).setOnClickListener { select(PlaybackMode.SEQUENTIAL) }
        view.findViewById<TextView>(R.id.mode_repeat_all).setOnClickListener { select(PlaybackMode.REPEAT_ALL) }
        view.findViewById<TextView>(R.id.mode_repeat_one).setOnClickListener { select(PlaybackMode.REPEAT_ONE) }
        view.findViewById<TextView>(R.id.mode_play_once_stop).setOnClickListener { select(PlaybackMode.PLAY_CURRENT_AND_STOP) }

        highlightCurrentMode(view)

        return view
    }

    private fun highlightCurrentMode(view: View) {
        val modeMap = mapOf(
            R.id.mode_sequential to PlaybackMode.SEQUENTIAL,
            R.id.mode_repeat_all to PlaybackMode.REPEAT_ALL,
            R.id.mode_repeat_one to PlaybackMode.REPEAT_ONE,
            R.id.mode_play_once_stop to PlaybackMode.PLAY_CURRENT_AND_STOP
        )
        for ((id, mode) in modeMap) {
            if (mode == currentMode) {
                view.findViewById<TextView>(id).setCompoundDrawablesRelativeWithIntrinsicBounds(
                    0, 0, android.R.drawable.checkbox_on_background, 0
                )
            }
        }
    }

    private fun select(mode: PlaybackMode) {
        onModeSelected(mode)
        dismiss()
    }

    override fun getTheme(): Int = R.style.Theme_KitabAlHuda_BottomSheet

    companion object {
        const val TAG = "PlaybackModeBottomSheet"
    }
}
