package id.ulartangga.keluarga.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import id.ulartangga.keluarga.game.Player
import id.ulartangga.keluarga.ui.theme.BoardTheme
import id.ulartangga.keluarga.ui.theme.PlayerPalette
import kotlin.math.roundToInt

@Composable
fun SetupScreen(
    selectedTheme: BoardTheme,
    onThemeChange: (BoardTheme) -> Unit,
    onStart: (List<Player>) -> Unit
) {
    var playerCount by remember { mutableStateOf(2) }
    var vsBot by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Ular Tangga Keluarga", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(24.dp))

        Text("Pilih Tema")
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            BoardTheme.values().forEach { theme ->
                FilterChip(
                    selected = theme == selectedTheme,
                    onClick = { onThemeChange(theme) },
                    label = { Text("${theme.emoji} ${theme.label}") }
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        Text("Jumlah Pemain: $playerCount")
        Slider(
            value = playerCount.toFloat(),
            onValueChange = { playerCount = it.roundToInt() },
            valueRange = 2f..4f,
            steps = 1
        )

        Spacer(Modifier.height(12.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = vsBot, onCheckedChange = { vsBot = it })
            Text("Pemain terakhir adalah Bot")
        }

        Spacer(Modifier.height(24.dp))

        Button(onClick = {
            val players = (0 until playerCount).map { i ->
                val isBot = vsBot && i == playerCount - 1
                Player(
                    id = i,
                    name = if (isBot) "Bot" else "Pemain ${i + 1}",
                    color = PlayerPalette[i % PlayerPalette.size],
                    isBot = isBot
                )
            }
            onStart(players)
        }) {
            Text("Mulai Bermain")
        }
    }
}
