package com.alfred.kitabalhuda

import com.alfred.kitabalhuda.ui.quran.SourateViewModel
import org.junit.Assert.assertEquals
import org.junit.Test

class SourateSearchTest {

    @Test
    fun testNormalizeArabicDiacritics() {
        // Test diacritic removal
        assertEquals("الفاتحه", SourateViewModel.normalizeArabic("الفَاتِحَة"))
        assertEquals("البقره", SourateViewModel.normalizeArabic("البَقَرَةُ"))
    }

    @Test
    fun testNormalizeAlifVariants() {
        // Test Alif normalization
        assertEquals("الف", SourateViewModel.normalizeArabic("ألف"))
        assertEquals("الف", SourateViewModel.normalizeArabic("إلف"))
        assertEquals("الف", SourateViewModel.normalizeArabic("آلف"))
        assertEquals("الف", SourateViewModel.normalizeArabic("ٱلف"))
        assertEquals("الف", SourateViewModel.normalizeArabic("الف"))
    }

    @Test
    fun testNormalizeTehMarbuta() {
        // Test Teh Marbuta to Heh normalization
        assertEquals("بقره", SourateViewModel.normalizeArabic("بقرة"))
        assertEquals("بقره", SourateViewModel.normalizeArabic("بقره"))
    }

    @Test
    fun testNormalizeAlefMaksura() {
        // Test Alef Maksura to Yeh normalization
        assertEquals("علي", SourateViewModel.normalizeArabic("على"))
        assertEquals("علي", SourateViewModel.normalizeArabic("علي"))
    }

    @Test
    fun testNormalizeIndicDigits() {
        // Test Arabic-Indic digits to Latin digits normalization
        assertEquals("1", SourateViewModel.normalizeArabic("١"))
        assertEquals("2", SourateViewModel.normalizeArabic("٢"))
        assertEquals("3", SourateViewModel.normalizeArabic("٣"))
        assertEquals("10", SourateViewModel.normalizeArabic("١٠"))
    }
}
