package id.ulartangga.keluarga.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import id.ulartangga.keluarga.ui.theme.BoardTheme

@Composable
fun ThemeBackdrop(theme: BoardTheme, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        when (theme) {
            BoardTheme.FOREST -> drawForestBackdrop()
            BoardTheme.NEON_CITY -> drawCityBackdrop()
            BoardTheme.SPACE -> drawSpaceBackdrop()
        }
    }
}

private fun DrawScope.drawForestBackdrop() {
    val w = size.width
    val h = size.height
    val hillColor = Color(0x3355A860)
    drawCircle(color = hillColor, radius = w * 0.55f, center = Offset(w * 0.15f, h * 0.1f))
    drawCircle(color = hillColor, radius = w * 0.45f, center = Offset(w * 0.9f, h * 0.05f))

    val treeColor = Color(0x552E7D32)
    val trees = listOf(
        0.05f to 0.82f, 0.14f to 0.9f, 0.9f to 0.86f, 0.96f to 0.78f,
        0.06f to 0.4f, 0.94f to 0.45f
    )
    trees.forEach { (fx, fy) ->
        drawCircle(color = treeColor, radius = w * 0.045f, center = Offset(w * fx, h * fy))
    }
}

private fun DrawScope.drawCityBackdrop() {
    val w = size.width
    val h = size.height
    val buildingColor = Color(0x3300E5FF)
    val heights = listOf(0.5f, 0.7f, 0.4f, 0.65f, 0.55f, 0.35f)
    val bw = w / heights.size
    heights.forEachIndexed { i, hf ->
        drawRect(
            color = buildingColor,
            topLeft = Offset(i * bw, h * (1f - hf)),
            size = Size(bw * 0.8f, h * hf)
        )
    }
}

private fun DrawScope.drawSpaceBackdrop() {
    val w = size.width
    val h = size.height
    val starColor = Color(0x66FFFFFF)
    val stars = listOf(
        0.1f to 0.1f, 0.3f to 0.2f, 0.5f to 0.08f, 0.7f to 0.25f, 0.9f to 0.15f,
        0.2f to 0.4f, 0.6f to 0.5f, 0.85f to 0.45f, 0.4f to 0.6f
    )
    stars.forEach { (fx, fy) ->
        drawCircle(color = starColor, radius = w * 0.006f, center = Offset(w * fx, h * fy))
    }
    drawCircle(color = Color(0x335C6BC0), radius = w * 0.15f, center = Offset(w * 0.8f, h * 0.75f))
}
