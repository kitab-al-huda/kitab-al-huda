package com.alfred.kitabalhuda.utils

import java.util.Calendar

object TimeOfDayManager {
    fun getGreeting(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 5..11 -> "صباح الخير" // Morning
            in 12..16 -> "مساء الخير" // Afternoon
            in 17..20 -> "مساء النور" // Evening
            else -> "ليلة سعيدة" // Night
        }
    }

    fun isFriday(): Boolean {
        return Calendar.getInstance().get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY
    }
}
