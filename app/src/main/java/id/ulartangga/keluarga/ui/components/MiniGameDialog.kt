package id.ulartangga.keluarga.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.delay

private const val CHALLENGE_MS = 3000L
private const val TICK_MS = 100L
private const val TAP_THRESHOLD = 8

@Composable
fun MiniGameDialog(playerName: String, onResult: (Boolean) -> Unit) {
    var tapCount by remember { mutableStateOf(0) }
    var remainingMs by remember { mutableStateOf(CHALLENGE_MS) }
    var finished by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (remainingMs > 0) {
            delay(TICK_MS)
            remainingMs -= TICK_MS
        }
        if (!finished) {
            finished = true
            onResult(tapCount >= TAP_THRESHOLD)
        }
    }

    Dialog(onDismissRequest = {}) {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("⚡ Tantangan Tap Cepat!", style = MaterialTheme.typography.titleMedium)
            Text("$playerName, tap lingkaran secepat mungkin!", style = MaterialTheme.typography.bodyMedium)
            Text("Sisa waktu: ${remainingMs / 1000f}s")
            Text("Ketukan: $tapCount / $TAP_THRESHOLD", style = MaterialTheme.typography.bodyMedium)

            Box(
                modifier = Modifier
                    .padding(top = 16.dp)
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .clickable(enabled = !finished) { tapCount++ },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    tapCount.toString(),
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.headlineLarge
                )
            }
        }
    }
}
