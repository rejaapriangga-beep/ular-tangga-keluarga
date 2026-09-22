package id.ulartangga.keluarga.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun WoodenSign(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.titleMedium
) {
    Text(
        text = text,
        color = Color(0xFFFFF3E0),
        fontWeight = FontWeight.Bold,
        style = style,
        modifier = modifier
            .background(Color(0xFF6D4C41), RoundedCornerShape(10.dp))
            .border(3.dp, Color(0xFF4E342E), RoundedCornerShape(10.dp))
            .padding(horizontal = 16.dp, vertical = 10.dp)
    )
}
