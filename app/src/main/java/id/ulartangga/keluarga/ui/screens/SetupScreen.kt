package id.ulartangga.keluarga.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import id.ulartangga.keluarga.game.AnimalAvatar
import id.ulartangga.keluarga.game.GameMode
import id.ulartangga.keluarga.game.Player
import id.ulartangga.keluarga.ui.components.WoodenSign
import id.ulartangga.keluarga.ui.theme.BoardTheme
import id.ulartangga.keluarga.ui.theme.PlayerPalette
import kotlin.math.roundToInt

@Composable
fun SetupScreen(
    selectedTheme: BoardTheme,
    onThemeChange: (BoardTheme) -> Unit,
    onStart: (List<Player>, GameMode) -> Unit,
    onPlayOnline: () -> Unit
) {
    var playerCount by remember { mutableStateOf(2) }
    var vsBot by remember { mutableStateOf(true) }
    var selectedMode by remember { mutableStateOf(GameMode.CLASSIC) }
    val playerNames = remember { mutableStateOf(listOf("Pemain 1", "Pemain 2", "Pemain 3", "Pemain 4")) }
    val playerAvatars = remember {
        mutableStateOf(listOf(AnimalAvatar.LION, AnimalAvatar.PANDA, AnimalAvatar.RABBIT, AnimalAvatar.DINO))
    }
    val youngFlags = remember { mutableStateOf(listOf(false, false, false, false)) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        WoodenSign(text = "Ular Tangga — Petualangan Keluarga", style = MaterialTheme.typography.headlineMedium)
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

        Text("Mode Permainan")
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            GameMode.values().forEach { mode ->
                FilterChip(
                    selected = mode == selectedMode,
                    onClick = { selectedMode = mode },
                    label = { Text("${mode.emoji} ${mode.label}") }
                )
            }
        }
        Text(selectedMode.description, style = MaterialTheme.typography.labelSmall)

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

        Spacer(Modifier.height(20.dp))

        Text("Pemain")
        Spacer(Modifier.height(8.dp))

        (0 until playerCount).forEach { i ->
            val isBot = vsBot && i == playerCount - 1
            PlayerEditorRow(
                index = i,
                name = playerNames.value[i],
                onNameChange = { newName ->
                    playerNames.value = playerNames.value.toMutableList().also { it[i] = newName }
                },
                avatar = playerAvatars.value[i],
                onAvatarChange = { newAvatar ->
                    playerAvatars.value = playerAvatars.value.toMutableList().also { it[i] = newAvatar }
                },
                isBot = isBot,
                isYoung = youngFlags.value[i],
                onYoungChange = { checked ->
                    youngFlags.value = youngFlags.value.toMutableList().also { it[i] = checked }
                }
            )
            Spacer(Modifier.height(8.dp))
        }

        Spacer(Modifier.height(16.dp))

        Button(onClick = {
            val players = (0 until playerCount).map { i ->
                val isBot = vsBot && i == playerCount - 1
                Player(
                    id = i,
                    name = if (isBot) "Bot" else playerNames.value[i].ifBlank { "Pemain ${i + 1}" },
                    color = PlayerPalette[i % PlayerPalette.size],
                    avatar = playerAvatars.value[i],
                    isBot = isBot,
                    isYoungPlayer = !isBot && youngFlags.value[i]
                )
            }
            onStart(players, selectedMode)
        }) {
            Text("Mulai Bermain")
        }

        Spacer(Modifier.height(12.dp))

        OutlinedButton(onClick = onPlayOnline) {
            Text("🌐 Main Online (Room Code)")
        }
    }
}

@Composable
private fun PlayerEditorRow(
    index: Int,
    name: String,
    onNameChange: (String) -> Unit,
    avatar: AnimalAvatar,
    onAvatarChange: (AnimalAvatar) -> Unit,
    isBot: Boolean,
    isYoung: Boolean,
    onYoungChange: (Boolean) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        if (isBot) {
            Text("🤖 Bot", style = MaterialTheme.typography.bodyMedium)
        } else {
            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                singleLine = true,
                label = { Text("Nama Pemain ${index + 1}") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(Modifier.height(4.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            AnimalAvatar.values().forEach { a ->
                FilterChip(
                    selected = a == avatar,
                    onClick = { onAvatarChange(a) },
                    label = { Text(a.emoji) }
                )
            }
        }

        if (!isBot) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = isYoung, onCheckedChange = onYoungChange)
                Text("Pemain Muda (mulai dengan Perisai, hoki Kotak Misteri lebih baik)", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}
