package com.example.meettalk.ui.theme

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = BackgroundSecondaryBlack,
    tertiary = Pink80,
    background = BackgroundBlack,
    secondaryContainer = SecondaryBlackBackground
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = BackgroundSecondaryLight,
    tertiary = Pink40,
    secondaryContainer = SecondaryLightBackground,
    background = BackgroundLight,

    /* Other default colors to override
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun MeetTalkTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colors = if (!darkTheme) {
        LightColorScheme
    } else {
        DarkColorScheme
    }


    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
        content = content
    )
}