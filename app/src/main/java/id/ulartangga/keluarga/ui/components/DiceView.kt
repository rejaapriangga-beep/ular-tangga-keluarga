package id.ulartangga.keluarga.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private val DICE_PIPS: Map<Int, List<Pair<Float, Float>>> = mapOf(
    1 to listOf(0.5f to 0.5f),
    2 to listOf(0.28f to 0.28f, 0.72f to 0.72f),
    3 to listOf(0.28f to 0.28f, 0.5f to 0.5f, 0.72f to 0.72f),
    4 to listOf(0.28f to 0.28f, 0.72f to 0.28f, 0.28f to 0.72f, 0.72f to 0.72f),
    5 to listOf(0.28f to 0.28f, 0.72f to 0.28f, 0.5f to 0.5f, 0.28f to 0.72f, 0.72f to 0.72f),
    6 to listOf(0.28f to 0.25f, 0.72f to 0.25f, 0.28f to 0.5f, 0.72f to 0.5f, 0.28f to 0.75f, 0.72f to 0.75f)
)

@Composable
fun DiceView(
    value: Int,
    isRolling: Boolean,
    enabled: Boolean,
    onRoll: () -> Unit,
    modifier: Modifier = Modifier,
    sizeDp: Dp = 76.dp
) {
    val rotation by animateFloatAsState(
        targetValue = if (isRolling) 360f else 0f,
        animationSpec = tween(500),
        label = "dice-rotation"
    )
    Box(
        modifier = modifier
            .size(sizeDp)
            .graphicsLayer { rotationZ = rotation }
            .clip(RoundedCornerShape(16.dp))
            .background(if (enabled) Color.White else Color(0xFFE0E0E0))
            .border(
                width = 3.dp,
                color = if (enabled) MaterialTheme.colorScheme.primary else Color(0xFFBDBDBD),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(enabled = enabled, onClick = onRoll)
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val pips = DICE_PIPS[value.coerceIn(1, 6)].orEmpty()
            val pipRadius = size.minDimension * 0.09f
            val pipColor = if (enabled) Color(0xFF212121) else Color(0xFF9E9E9E)
            pips.forEach { (fx, fy) ->
                drawCircle(
                    color = pipColor,
                    radius = pipRadius,
                    center = Offset(size.width * fx, size.height * fy)
                )
            }
        }
    }
}
