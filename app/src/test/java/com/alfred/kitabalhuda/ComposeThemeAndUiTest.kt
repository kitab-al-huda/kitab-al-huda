package com.alfred.kitabalhuda

import androidx.compose.ui.unit.sp
import com.alfred.kitabalhuda.ui.theme.BackgroundDark
import com.alfred.kitabalhuda.ui.theme.GoldPrimary
import com.alfred.kitabalhuda.ui.theme.KitabDarkColorScheme
import com.alfred.kitabalhuda.ui.theme.KitabTypography
import com.alfred.kitabalhuda.ui.theme.SurfaceDark
import com.alfred.kitabalhuda.ui.theme.TealAccent
import com.alfred.kitabalhuda.ui.theme.TealDark
import com.alfred.kitabalhuda.ui.theme.isRtlLocale
import com.alfred.kitabalhuda.utils.DigitHelper
import com.alfred.kitabalhuda.utils.toArabicIndic
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale

class ComposeThemeAndUiTest {

    @Test
    fun testKitabDarkColorSchemeTokens() {
        assertEquals(TealAccent, KitabDarkColorScheme.primary)
        assertEquals(TealDark, KitabDarkColorScheme.primaryContainer)
        assertEquals(GoldPrimary, KitabDarkColorScheme.secondary)
        assertEquals(BackgroundDark, KitabDarkColorScheme.background)
        assertEquals(SurfaceDark, KitabDarkColorScheme.surface)
    }

    @Test
    fun testArabicTypographyZeroLetterSpacing() {
        // Arabic cursive script requires letterSpacing == 0 to prevent ligature breakage
        assertEquals(0.sp, KitabTypography.displayLarge.letterSpacing)
        assertEquals(0.sp, KitabTypography.headlineLarge.letterSpacing)
        assertEquals(0.sp, KitabTypography.titleLarge.letterSpacing)
        assertEquals(0.sp, KitabTypography.titleMedium.letterSpacing)
        assertEquals(0.sp, KitabTypography.bodyLarge.letterSpacing)
        assertEquals(0.sp, KitabTypography.bodyMedium.letterSpacing)
        assertEquals(0.sp, KitabTypography.labelLarge.letterSpacing)
    }

    @Test
    fun testArabicTypographyGenerousLineHeight() {
        // Arabic typography with stacked diacritics (tashkeel) requires lineHeight >= 1.5x font size
        val bodyLargeRatio = KitabTypography.bodyLarge.lineHeight.value / KitabTypography.bodyLarge.fontSize.value
        assertTrue("bodyLarge lineHeight ratio must be >= 1.5 to prevent diacritic clipping", bodyLargeRatio >= 1.5f)

        val titleLargeRatio = KitabTypography.titleLarge.lineHeight.value / KitabTypography.titleLarge.fontSize.value
        assertTrue("titleLarge lineHeight ratio must be >= 1.5 to prevent diacritic clipping", titleLargeRatio >= 1.5f)

        val headlineMediumRatio = KitabTypography.headlineMedium.lineHeight.value / KitabTypography.headlineMedium.fontSize.value
        assertTrue("headlineMedium lineHeight ratio must be >= 1.5 to prevent diacritic clipping", headlineMediumRatio >= 1.5f)
    }

    @Test
    fun testIsRtlLocaleDetection() {
        val originalLocale = Locale.getDefault()
        try {
            Locale.setDefault(Locale("ar", "SA"))
            assertTrue("Arabic locale should be detected as RTL", isRtlLocale())

            Locale.setDefault(Locale.FRENCH)
            assertFalse("French locale should be detected as LTR", isRtlLocale())

            Locale.setDefault(Locale.ENGLISH)
            assertFalse("English locale should be detected as LTR", isRtlLocale())
        } finally {
            Locale.setDefault(originalLocale)
        }
    }

    @Test
    fun testDigitFormattingToggle() {
        val originalPreference = DigitHelper.useArabicIndic
        try {
            DigitHelper.useArabicIndic = true
            assertEquals("١١٤", 114.toArabicIndic())
            assertEquals("١", 1.toArabicIndic())

            DigitHelper.useArabicIndic = false
            assertEquals("114", 114.toArabicIndic())
            assertEquals("1", 1.toArabicIndic())
        } finally {
            DigitHelper.useArabicIndic = originalPreference
        }
    }
}
