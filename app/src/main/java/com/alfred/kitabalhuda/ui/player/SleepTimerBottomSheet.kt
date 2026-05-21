package com.alfred.kitabalhuda.ui.player

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.alfred.kitabalhuda.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class SleepTimerBottomSheet(private val onTimerSelected: (Int) -> Unit) : BottomSheetDialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.bottom_sheet_sleep_timer, container, false)
        
        view.findViewById<TextView>(R.id.time_15).setOnClickListener { select(15) }
        view.findViewById<TextView>(R.id.time_30).setOnClickListener { select(30) }
        view.findViewById<TextView>(R.id.time_45).setOnClickListener { select(45) }
        view.findViewById<TextView>(R.id.time_60).setOnClickListener { select(60) }
        view.findViewById<TextView>(R.id.time_off).setOnClickListener { select(0) }
        
        return view
    }

    private fun select(minutes: Int) {
        onTimerSelected(minutes)
        dismiss()
    }

    companion object {
        const val TAG = "SleepTimerBottomSheet"
    }
}
