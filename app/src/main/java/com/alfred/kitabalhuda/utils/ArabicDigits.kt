package com.alfred.kitabalhuda.utils

private val arabicIndicDigits = arrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')

fun Int.toArabicIndic(): String {
    return this.toString().map { c ->
        if (c.isDigit()) arabicIndicDigits[c - '0'] else c
    }.joinToString("")
}

fun Long.toArabicIndic(): String {
    return this.toString().map { c ->
        if (c.isDigit()) arabicIndicDigits[c - '0'] else c
    }.joinToString("")
}

fun Long.formatDurationArabic(): String {
    if (this <= 0) return ""
    val minutes = this / 60
    val seconds = this % 60
    return "${minutes.toArabicIndic()}:${seconds.toArabicIndic().padStart(2, '٠')}"
}
