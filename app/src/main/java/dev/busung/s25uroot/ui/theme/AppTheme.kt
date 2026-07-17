package dev.busung.s25uroot.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import dev.busung.s25uroot.AccentColor

private data class AccentPalette(
    val lightPrimary: Color,
    val lightContainer: Color,
    val darkPrimary: Color,
    val darkContainer: Color,
)

private val palettes = mapOf(
    AccentColor.Blue to AccentPalette(
        Color(0xFF415F91), Color(0xFFD6E3FF), Color(0xFFAAC7FF), Color(0xFF284777),
    ),
    AccentColor.Violet to AccentPalette(
        Color(0xFF6750A4), Color(0xFFEADDFF), Color(0xFFD0BCFF), Color(0xFF4F378B),
    ),
    AccentColor.Green to AccentPalette(
        Color(0xFF356A35), Color(0xFFB7F0B1), Color(0xFF9CD49A), Color(0xFF1E511F),
    ),
    AccentColor.Orange to AccentPalette(
        Color(0xFF8B4F23), Color(0xFFFFDBC6), Color(0xFFFFB783), Color(0xFF6D390E),
    ),
)

private val AppTypography = Typography(
    displaySmall = TextStyle(fontSize = 38.sp, lineHeight = 44.sp, fontWeight = FontWeight.Light),
    headlineLarge = TextStyle(fontSize = 32.sp, lineHeight = 38.sp, fontWeight = FontWeight.Normal),
    headlineSmall = TextStyle(fontSize = 25.sp, lineHeight = 31.sp, fontWeight = FontWeight.Normal),
    titleLarge = TextStyle(fontSize = 21.sp, lineHeight = 27.sp, fontWeight = FontWeight.Medium),
    titleMedium = TextStyle(fontSize = 17.sp, lineHeight = 23.sp, fontWeight = FontWeight.Medium),
    titleSmall = TextStyle(fontSize = 15.sp, lineHeight = 21.sp, fontWeight = FontWeight.Medium),
    bodyLarge = TextStyle(fontSize = 16.sp, lineHeight = 24.sp, fontWeight = FontWeight.Normal),
    bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 21.sp, fontWeight = FontWeight.Normal),
    bodySmall = TextStyle(fontSize = 12.sp, lineHeight = 18.sp, fontWeight = FontWeight.Normal),
    labelLarge = TextStyle(fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.Medium),
    labelMedium = TextStyle(fontSize = 12.sp, lineHeight = 17.sp, fontWeight = FontWeight.Medium),
)

@Composable
fun RootMyGalaxyTheme(
    accentColor: AccentColor,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val darkTheme = isSystemInDarkTheme()
    val palette = palettes[accentColor]
    val colors = when {
        accentColor == AccentColor.Dynamic && darkTheme ->
            dynamicDarkColorScheme(context)
        accentColor == AccentColor.Dynamic ->
            dynamicLightColorScheme(context)
        palette != null && darkTheme -> darkColorScheme(
            primary = palette.darkPrimary,
            primaryContainer = palette.darkContainer,
        )
        palette != null -> lightColorScheme(
            primary = palette.lightPrimary,
            primaryContainer = palette.lightContainer,
        )
        darkTheme -> darkColorScheme()
        else -> lightColorScheme()
    }

    SideEffect {
        val window = (context as Activity).window
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = !darkTheme
            isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialExpressiveTheme(
        colorScheme = colors,
        typography = AppTypography,
        motionScheme = MotionScheme.expressive(),
        content = content,
    )
}
