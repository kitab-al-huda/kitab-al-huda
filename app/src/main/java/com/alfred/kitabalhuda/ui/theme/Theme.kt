package com.alfred.kitabalhuda.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.text.TextUtilsCompat
import androidx.core.view.ViewCompat
import java.util.Locale

val KitabDarkColorScheme = darkColorScheme(
    primary = TealAccent,
    onPrimary = SurfaceDark,
    primaryContainer = TealDark,
    onPrimaryContainer = TextPrimary,
    secondary = GoldPrimary,
    onSecondary = OnGold,
    secondaryContainer = GoldContainer,
    onSecondaryContainer = GoldLight,
    tertiary = TealLight,
    onTertiary = TextPrimary,
    tertiaryContainer = TealContainer,
    onTertiaryContainer = TealAccent,
    background = BackgroundDark,
    onBackground = TextPrimary,
    surface = SurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = TextSecondary,
    outline = OutlineDark,
    outlineVariant = OutlineVariant,
    error = ErrorColor,
    onError = TextPrimary,
    errorContainer = ErrorContainer,
    onErrorContainer = ErrorColor
)

fun isRtlLocale(): Boolean {
    val locale = Locale.getDefault()
    if (locale.language == "ar") return true
    return try {
        TextUtilsCompat.getLayoutDirectionFromLocale(locale) == 1
    } catch (_: Throwable) {
        false
    }
}

@Composable
fun KitabAlHudaTheme(
    layoutDirection: LayoutDirection = if (isRtlLocale()) LayoutDirection.Rtl else LayoutDirection.Ltr,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalLayoutDirection provides layoutDirection
    ) {
        MaterialTheme(
            colorScheme = KitabDarkColorScheme,
            typography = KitabTypography,
            content = content
        )
    }
}
