package id.ulartangga.keluarga.ui.components

import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.ulartangga.keluarga.game.BoardConfig
import id.ulartangga.keluarga.game.Player
import id.ulartangga.keluarga.ui.theme.BoardTheme
import kotlin.math.atan2
import kotlin.math.roundToInt
import kotlin.math.sqrt

private data class CellGeometry(val row: Int, val col: Int, val center: Offset)

private fun cellGeometry(cell: Int, cellPx: Float): CellGeometry {
    val columns = BoardConfig.COLUMNS
    val rows = BoardConfig.ROWS
    val idx = (cell - 1).coerceIn(0, BoardConfig.TOTAL_CELLS - 1)
    val row = idx / columns
    var col = idx % columns
    if (row % 2 == 1) col = columns - 1 - col
    val x = col * cellPx + cellPx / 2f
    val y = (rows - 1 - row) * cellPx + cellPx / 2f
    return CellGeometry(row, col, Offset(x, y))
}

private fun cellCenter(cell: Int, cellPx: Float) = cellGeometry(cell, cellPx).center

private fun quadraticPoint(p0: Offset, control: Offset, p1: Offset, t: Float): Offset {
    val u = 1f - t
    val x = u * u * p0.x + 2f * u * t * control.x + t * t * p1.x
    val y = u * u * p0.y + 2f * u * t * control.y + t * t * p1.y
    return Offset(x, y)
}

/** Tangga proper: dua rel sejajar dengan anak tangga melintang, bukan garis polos. */
private fun DrawScope.drawLadder(a: Offset, b: Offset, theme: BoardTheme, cellPx: Float) {
    val dir = Offset(b.x - a.x, b.y - a.y)
    val length = sqrt(dir.x * dir.x + dir.y * dir.y)
    if (length < 1f) return
    val unit = Offset(dir.x / length, dir.y / length)
    val perp = Offset(-unit.y, unit.x)
    val railOffset = cellPx * 0.13f

    val rail1Start = Offset(a.x + perp.x * railOffset, a.y + perp.y * railOffset)
    val rail1End = Offset(b.x + perp.x * railOffset, b.y + perp.y * railOffset)
    val rail2Start = Offset(a.x - perp.x * railOffset, a.y - perp.y * railOffset)
    val rail2End = Offset(b.x - perp.x * railOffset, b.y - perp.y * railOffset)

    val railWidth = cellPx * 0.07f
    drawLine(theme.ladderColor, rail1Start, rail1End, strokeWidth = railWidth, cap = StrokeCap.Round)
    drawLine(theme.ladderColor, rail2Start, rail2End, strokeWidth = railWidth, cap = StrokeCap.Round)

    val rungCount = (length / (cellPx * 0.45f)).roundToInt().coerceAtLeast(3)
    for (i in 1 until rungCount) {
        val t = i / rungCount.toFloat()
        val center = Offset(a.x + dir.x * t, a.y + dir.y * t)
        val rungStart = Offset(center.x + perp.x * railOffset, center.y + perp.y * railOffset)
        val rungEnd = Offset(center.x - perp.x * railOffset, center.y - perp.y * railOffset)
        drawLine(theme.ladderRungColor, rungStart, rungEnd, strokeWidth = cellPx * 0.05f, cap = StrokeCap.Round)
    }
}

/** Ular proper: badan menyambung meruncing ke ekor (bukan rangkaian bulatan/cacing), kepala menghadap keluar dengan mata sipit dan lidah bercabang. */
private fun DrawScope.drawSnake(a: Offset, b: Offset, theme: BoardTheme, cellPx: Float) {
    val control = Offset((a.x + b.x) / 2f + cellPx * 0.6f, (a.y + b.y) / 2f)
    val segments = 32
    val points = (0..segments).map { i -> quadraticPoint(a, control, b, i / segments.toFloat()) }
    val darkScale = Color(
        red = theme.snakeColor.red * 0.55f,
        green = theme.snakeColor.green * 0.55f,
        blue = theme.snakeColor.blue * 0.55f
    )

    for (i in 0 until points.size - 1) {
        val t = i / (points.size - 1).toFloat()
        val width = (cellPx * (0.30f - 0.20f * t)).coerceAtLeast(cellPx * 0.045f)
        drawLine(
            color = theme.snakeColor,
            start = points[i],
            end = points[i + 1],
            strokeWidth = width,
            cap = StrokeCap.Round
        )
    }

    listOf(4, 10, 16, 22).forEach { i ->
        if (i >= points.size - 1) return@forEach
        val t = i / (points.size - 1).toFloat()
        val width = (cellPx * (0.30f - 0.20f * t)).coerceAtLeast(cellPx * 0.045f)
        val dir = Offset(points[i + 1].x - points[i].x, points[i + 1].y - points[i].y)
        val len = sqrt(dir.x * dir.x + dir.y * dir.y)
        if (len < 0.001f) return@forEach
        val perp = Offset(-dir.y / len, dir.x / len)
        val half = width * 0.4f
        drawLine(
            color = darkScale,
            start = Offset(points[i].x + perp.x * half, points[i].y + perp.y * half),
            end = Offset(points[i].x - perp.x * half, points[i].y - perp.y * half),
            strokeWidth = cellPx * 0.028f,
            cap = StrokeCap.Round
        )
    }

    val head = points.first()
    val neck = points[minOf(3, points.size - 1)]
    val angleDeg = Math.toDegrees(atan2((head.y - neck.y).toDouble(), (head.x - neck.x).toDouble())).toFloat()

    rotate(degrees = angleDeg, pivot = head) {
        drawOval(
            color = theme.snakeColor,
            topLeft = Offset(head.x - cellPx * 0.08f, head.y - cellPx * 0.15f),
            size = Size(cellPx * 0.36f, cellPx * 0.30f)
        )

        val eyeX = head.x + cellPx * 0.14f
        listOf(head.y - cellPx * 0.09f, head.y + cellPx * 0.02f).forEach { eyeY ->
            drawOval(
                color = Color(0xFFFFD600),
                topLeft = Offset(eyeX - cellPx * 0.045f, eyeY - cellPx * 0.045f),
                size = Size(cellPx * 0.09f, cellPx * 0.09f)
            )
            drawLine(
                color = Color.Black,
                start = Offset(eyeX, eyeY - cellPx * 0.035f),
                end = Offset(eyeX, eyeY + cellPx * 0.035f),
                strokeWidth = cellPx * 0.018f,
                cap = StrokeCap.Round
            )
        }

        val tongueBase = Offset(head.x + cellPx * 0.2f, head.y - cellPx * 0.035f)
        val tongueTip = Offset(head.x + cellPx * 0.32f, head.y - cellPx * 0.035f)
        drawLine(Color(0xFFD32F2F), tongueBase, tongueTip, strokeWidth = cellPx * 0.018f, cap = StrokeCap.Round)
        drawLine(
            Color(0xFFD32F2F),
            tongueTip,
            Offset(tongueTip.x + cellPx * 0.05f, tongueTip.y - cellPx * 0.04f),
            strokeWidth = cellPx * 0.014f,
            cap = StrokeCap.Round
        )
        drawLine(
            Color(0xFFD32F2F),
            tongueTip,
            Offset(tongueTip.x + cellPx * 0.05f, tongueTip.y + cellPx * 0.04f),
            strokeWidth = cellPx * 0.014f,
            cap = StrokeCap.Round
        )
    }
}

@Composable
fun BoardView(
    players: List<Player>,
    theme: BoardTheme,
    modifier: Modifier = Modifier,
    collapsedCells: List<Int> = emptyList()
) {
    var boardPx by remember { mutableStateOf(0f) }
    val density = LocalDensity.current
    val textColor = android.graphics.Color.argb(
        140,
        (theme.onBackground.red * 255).toInt(),
        (theme.onBackground.green * 255).toInt(),
        (theme.onBackground.blue * 255).toInt()
    )

    Box(
        modifier = modifier
            .aspectRatio(
                BoardConfig.COLUMNS.toFloat() / BoardConfig.ROWS.toFloat(),
                matchHeightConstraintsFirst = true
            )
            .onGloballyPositioned { coords -> boardPx = coords.size.width.toFloat() }
    ) {
        if (boardPx > 0f) {
            val cellPx = boardPx / BoardConfig.COLUMNS.toFloat()

            Canvas(modifier = Modifier.matchParentSize()) {
                for (cell in 1..BoardConfig.TOTAL_CELLS) {
                    val geo = cellGeometry(cell, cellPx)
                    val topLeft = Offset(geo.center.x - cellPx / 2f, geo.center.y - cellPx / 2f)
                    val isEven = (geo.row + geo.col) % 2 == 0
                    drawRect(
                        color = if (isEven) theme.boardLight else theme.boardDark,
                        topLeft = topLeft,
                        size = Size(cellPx, cellPx)
                    )
                    if (cell in BoardConfig.cardCells) {
                        drawCircle(color = theme.cardCellColor, radius = cellPx * 0.12f, center = geo.center)
                    }
                    if (cell in BoardConfig.mysteryCells) {
                        drawCircle(color = theme.mysteryCellColor, radius = cellPx * 0.16f, center = geo.center)
                        drawContext.canvas.nativeCanvas.drawText(
                            "?",
                            geo.center.x - cellPx * 0.05f,
                            geo.center.y + cellPx * 0.06f,
                            android.graphics.Paint().apply {
                                color = android.graphics.Color.WHITE
                                textSize = cellPx * 0.2f
                                isFakeBoldText = true
                            }
                        )
                    }
                    if (cell in BoardConfig.miniGameCells) {
                        drawCircle(color = theme.miniGameCellColor, radius = cellPx * 0.16f, center = geo.center)
                        drawContext.canvas.nativeCanvas.drawText(
                            "⚡",
                            geo.center.x - cellPx * 0.07f,
                            geo.center.y + cellPx * 0.07f,
                            android.graphics.Paint().apply {
                                color = android.graphics.Color.WHITE
                                textSize = cellPx * 0.2f
                                isFakeBoldText = true
                            }
                        )
                    }
                    if (cell in collapsedCells) {
                        drawRect(
                            color = Color(0xAA1A1A1A),
                            topLeft = topLeft,
                            size = Size(cellPx, cellPx)
                        )
                        drawContext.canvas.nativeCanvas.drawText(
                            "✕",
                            geo.center.x - cellPx * 0.08f,
                            geo.center.y + cellPx * 0.08f,
                            android.graphics.Paint().apply {
                                color = android.graphics.Color.RED
                                textSize = cellPx * 0.28f
                                isFakeBoldText = true
                            }
                        )
                    }
                    drawContext.canvas.nativeCanvas.drawText(
                        cell.toString(),
                        topLeft.x + cellPx * 0.08f,
                        topLeft.y + cellPx * 0.22f,
                        android.graphics.Paint().apply {
                            color = textColor
                            textSize = cellPx * 0.19f
                            isFakeBoldText = true
                        }
                    )
                }

                drawRect(color = theme.boardBorder, size = size, style = Stroke(width = 3f))

                BoardConfig.ladders.forEach { (start, end) ->
                    drawLadder(cellCenter(start, cellPx), cellCenter(end, cellPx), theme, cellPx)
                }

                BoardConfig.snakes.forEach { (start, end) ->
                    drawSnake(cellCenter(start, cellPx), cellCenter(end, cellPx), theme, cellPx)
                }
            }

            players.forEachIndexed { index, player ->
                val target = cellCenter(player.animatedCell, cellPx)
                val jitterX = if (players.size > 1) ((index % 2) - 0.5f) * cellPx * 0.26f else 0f
                val jitterY = if (players.size > 1) ((index / 2) - 0.5f) * cellPx * 0.26f else 0f
                val animated by animateOffsetAsState(
                    targetValue = Offset(target.x + jitterX, target.y + jitterY),
                    animationSpec = tween(220),
                    label = "piece-${player.id}"
                )
                val pieceSizeDp = with(density) { (cellPx * 0.46f).toDp() }
                Box(
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                (animated.x - cellPx * 0.23f).roundToInt(),
                                (animated.y - cellPx * 0.23f).roundToInt()
                            )
                        }
                        .size(pieceSizeDp)
                        .clip(CircleShape)
                        .background(player.color)
                        .border(2.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = player.avatar.emoji, fontSize = (pieceSizeDp.value * 0.62f).sp)
                }
            }
        }
    }
}
