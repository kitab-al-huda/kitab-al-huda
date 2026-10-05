package com.alfred.kitabalhuda.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.sp

// ====================================================================
// Kitab al-Huda Typography
// Generous lineHeight (1.5x-1.7x) to avoid Arabic diacritics clipping.
// letterSpacing = 0.sp to preserve cursive ligatures in Arabic script.
// ====================================================================

private val defaultPlatformStyle = PlatformTextStyle(
    includeFontPadding = false
)

val KitabTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 54.sp,
        lineHeight = 84.sp, // ~1.55x
        letterSpacing = 0.sp,
        textDirection = TextDirection.ContentOrRtl,
        platformStyle = defaultPlatformStyle
    ),
    displayMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 42.sp,
        lineHeight = 66.sp, // ~1.57x
        letterSpacing = 0.sp,
        textDirection = TextDirection.ContentOrRtl,
        platformStyle = defaultPlatformStyle
    ),
    displaySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 34.sp,
        lineHeight = 54.sp, // ~1.6x
        letterSpacing = 0.sp,
        textDirection = TextDirection.ContentOrRtl,
        platformStyle = defaultPlatformStyle
    ),
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 30.sp,
        lineHeight = 48.sp, // 1.6x
        letterSpacing = 0.sp,
        textDirection = TextDirection.ContentOrRtl,
        platformStyle = defaultPlatformStyle
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 26.sp,
        lineHeight = 42.sp, // ~1.6x
        letterSpacing = 0.sp,
        textDirection = TextDirection.ContentOrRtl,
        platformStyle = defaultPlatformStyle
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 36.sp, // ~1.63x
        letterSpacing = 0.sp,
        textDirection = TextDirection.ContentOrRtl,
        platformStyle = defaultPlatformStyle
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 32.sp, // 1.6x
        letterSpacing = 0.sp,
        textDirection = TextDirection.ContentOrRtl,
        platformStyle = defaultPlatformStyle
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 26.sp, // ~1.62x
        letterSpacing = 0.sp,
        textDirection = TextDirection.ContentOrRtl,
        platformStyle = defaultPlatformStyle
    ),
    titleSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 23.sp, // ~1.64x
        letterSpacing = 0.sp,
        textDirection = TextDirection.ContentOrRtl,
        platformStyle = defaultPlatformStyle
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 26.sp, // 1.62x
        letterSpacing = 0.sp,
        textDirection = TextDirection.ContentOrRtl,
        platformStyle = defaultPlatformStyle
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 23.sp, // ~1.64x
        letterSpacing = 0.sp,
        textDirection = TextDirection.ContentOrRtl,
        platformStyle = defaultPlatformStyle
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 20.sp, // ~1.66x
        letterSpacing = 0.sp,
        textDirection = TextDirection.ContentOrRtl,
        platformStyle = defaultPlatformStyle
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 22.sp, // ~1.57x
        letterSpacing = 0.sp,
        textDirection = TextDirection.ContentOrRtl,
        platformStyle = defaultPlatformStyle
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 19.sp, // ~1.58x
        letterSpacing = 0.sp,
        textDirection = TextDirection.ContentOrRtl,
        platformStyle = defaultPlatformStyle
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 18.sp, // ~1.63x
        letterSpacing = 0.sp,
        textDirection = TextDirection.ContentOrRtl,
        platformStyle = defaultPlatformStyle
    )
)
