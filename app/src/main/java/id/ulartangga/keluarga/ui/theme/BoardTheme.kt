package id.ulartangga.keluarga.ui.theme

import androidx.compose.ui.graphics.Color

enum class BoardTheme(
    val label: String,
    val emoji: String,
    val primary: Color,
    val secondary: Color,
    val tertiary: Color,
    val backgroundLight: Color,
    val onBackground: Color,
    val boardLight: Color,
    val boardDark: Color,
    val boardBorder: Color,
    val ladderColor: Color,
    val ladderRungColor: Color,
    val snakeColor: Color,
    val cardCellColor: Color
) {
    FOREST(
        label = "Hutan Tropis",
        emoji = "🌿",
        primary = Color(0xFF2E7D32),
        secondary = Color(0xFFFFB300),
        tertiary = Color(0xFF00838F),
        backgroundLight = Color(0xFFF4F8F1),
        onBackground = Color(0xFF1B2314),
        boardLight = Color(0xFFE9F5E1),
        boardDark = Color(0xFFC8E6BE),
        boardBorder = Color(0xFF33691E),
        ladderColor = Color(0xFFFFA000),
        ladderRungColor = Color(0xFFFFE082),
        snakeColor = Color(0xFFD32F2F),
        cardCellColor = Color(0xFF7B1FA2)
    ),
    NEON_CITY(
        label = "Kota Malam",
        emoji = "🏙",
        primary = Color(0xFFE91E8C),
        secondary = Color(0xFF00E5FF),
        tertiary = Color(0xFFFFEA00),
        backgroundLight = Color(0xFF1A1033),
        onBackground = Color(0xFFF5F0FF),
        boardLight = Color(0xFF241947),
        boardDark = Color(0xFF160F2E),
        boardBorder = Color(0xFF00E5FF),
        ladderColor = Color(0xFF00E5FF),
        ladderRungColor = Color(0xFF80F5FF),
        snakeColor = Color(0xFFFF2E92),
        cardCellColor = Color(0xFFFFEA00)
    ),
    SPACE(
        label = "Luar Angkasa",
        emoji = "🪐",
        primary = Color(0xFF5C6BC0),
        secondary = Color(0xFFFF7043),
        tertiary = Color(0xFF26C6DA),
        backgroundLight = Color(0xFF0D1230),
        onBackground = Color(0xFFE8EAF6),
        boardLight = Color(0xFF1B2454),
        boardDark = Color(0xFF11173A),
        boardBorder = Color(0xFF7986CB),
        ladderColor = Color(0xFF26C6DA),
        ladderRungColor = Color(0xFFB2EBF2),
        snakeColor = Color(0xFFFF7043),
        cardCellColor = Color(0xFFFFD54F)
    )
}
