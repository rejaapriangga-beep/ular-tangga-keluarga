package id.ulartangga.keluarga.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun UlarTanggaTheme(
    boardTheme: BoardTheme,
    content: @Composable () -> Unit
) {
    val colorScheme = lightColorScheme(
        primary = boardTheme.primary,
        onPrimary = Color.White,
        secondary = boardTheme.secondary,
        tertiary = boardTheme.tertiary,
        background = boardTheme.backgroundLight,
        onBackground = boardTheme.onBackground,
        surface = boardTheme.backgroundLight,
        onSurface = boardTheme.onBackground,
        surfaceVariant = boardTheme.boardDark,
        onSurfaceVariant = boardTheme.onBackground
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = UlarTanggaTypography,
        content = content
    )
}
