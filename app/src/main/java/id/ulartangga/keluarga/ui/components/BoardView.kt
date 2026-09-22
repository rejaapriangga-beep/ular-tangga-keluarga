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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.ulartangga.keluarga.game.BoardConfig
import id.ulartangga.keluarga.game.Player
import id.ulartangga.keluarga.ui.theme.BoardTheme
import kotlin.math.roundToInt

private data class CellGeometry(val row: Int, val col: Int, val center: Offset)

private fun cellGeometry(cell: Int, cellPx: Float): CellGeometry {
    val idx = (cell - 1).coerceIn(0, 99)
    val row = idx / 10
    var col = idx % 10
    if (row % 2 == 1) col = 9 - col
    val x = col * cellPx + cellPx / 2f
    val y = (9 - row) * cellPx + cellPx / 2f
    return CellGeometry(row, col, Offset(x, y))
}

private fun cellCenter(cell: Int, cellPx: Float) = cellGeometry(cell, cellPx).center

@Composable
fun BoardView(players: List<Player>, theme: BoardTheme, modifier: Modifier = Modifier) {
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
            .aspectRatio(1f)
            .onGloballyPositioned { coords -> boardPx = coords.size.width.toFloat() }
    ) {
        if (boardPx > 0f) {
            val cellPx = boardPx / 10f

            Canvas(modifier = Modifier.matchParentSize()) {
                for (cell in 1..100) {
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
                    drawContext.canvas.nativeCanvas.drawText(
                        cell.toString(),
                        topLeft.x + cellPx * 0.08f,
                        topLeft.y + cellPx * 0.22f,
                        android.graphics.Paint().apply {
                            color = textColor
                            textSize = cellPx * 0.16f
                        }
                    )
                }

                drawRect(color = theme.boardBorder, size = size, style = Stroke(width = 3f))

                BoardConfig.ladders.forEach { (start, end) ->
                    val a = cellCenter(start, cellPx)
                    val b = cellCenter(end, cellPx)
                    drawLine(theme.ladderColor, a, b, strokeWidth = cellPx * 0.14f, cap = StrokeCap.Round)
                    drawLine(theme.ladderRungColor, a, b, strokeWidth = cellPx * 0.05f, cap = StrokeCap.Round)
                }

                BoardConfig.snakes.forEach { (start, end) ->
                    val a = cellCenter(start, cellPx)
                    val b = cellCenter(end, cellPx)
                    val mid = Offset((a.x + b.x) / 2f + cellPx * 0.6f, (a.y + b.y) / 2f)
                    val path = Path().apply {
                        moveTo(a.x, a.y)
                        quadraticBezierTo(mid.x, mid.y, b.x, b.y)
                    }
                    drawPath(path, color = theme.snakeColor, style = Stroke(width = cellPx * 0.12f, cap = StrokeCap.Round))
                    drawCircle(color = theme.snakeColor, radius = cellPx * 0.1f, center = a)
                }
            }

            players.forEachIndexed { index, player ->
                val target = cellCenter(player.animatedCell, cellPx)
                val jitterX = if (players.size > 1) ((index % 2) - 0.5f) * cellPx * 0.22f else 0f
                val jitterY = if (players.size > 1) ((index / 2) - 0.5f) * cellPx * 0.22f else 0f
                val animated by animateOffsetAsState(
                    targetValue = Offset(target.x + jitterX, target.y + jitterY),
                    animationSpec = tween(220),
                    label = "piece-${player.id}"
                )
                val pieceSizeDp = with(density) { (cellPx * 0.34f).toDp() }
                Box(
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                (animated.x - cellPx * 0.17f).roundToInt(),
                                (animated.y - cellPx * 0.17f).roundToInt()
                            )
                        }
                        .size(pieceSizeDp)
                        .clip(CircleShape)
                        .background(player.color)
                        .border(2.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = player.avatar.emoji, fontSize = (pieceSizeDp.value * 0.6f).sp)
                }
            }
        }
    }
}
