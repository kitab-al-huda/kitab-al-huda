package com.alfred.kitabalhuda.utils

private val arabicIndicDigits = arrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')

object DigitHelper {
    var useArabicIndic: Boolean = true
}

fun Int.toArabicIndic(): String {
    if (!DigitHelper.useArabicIndic) return this.toString()
    return this.toString().map { c ->
        if (c.isDigit()) arabicIndicDigits[c - '0'] else c
    }.joinToString("")
}

fun Long.toArabicIndic(): String {
    if (!DigitHelper.useArabicIndic) return this.toString()
    return this.toString().map { c ->
        if (c.isDigit()) arabicIndicDigits[c - '0'] else c
    }.joinToString("")
}

fun Long.formatDurationArabic(): String {
    if (this < 0) return ""
    val minutes = this / 60
    val seconds = this % 60
    val padChar = if (DigitHelper.useArabicIndic) '٠' else '0'
    return "${minutes.toArabicIndic()}:${seconds.toArabicIndic().padStart(2, padChar)}"
}
