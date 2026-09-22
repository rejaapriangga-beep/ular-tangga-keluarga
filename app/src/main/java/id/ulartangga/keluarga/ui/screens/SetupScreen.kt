package id.ulartangga.keluarga.ui.screens

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
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenu
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import id.ulartangga.keluarga.game.AvatarCategory
import id.ulartangga.keluarga.game.GameMode
import id.ulartangga.keluarga.game.Player
import id.ulartangga.keluarga.ui.components.WoodenSign
import id.ulartangga.keluarga.ui.theme.PlayerPalette

@Composable
fun SetupScreen(
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
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        WoodenSign(text = "Ular Tangga — Petualangan Keluarga", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(20.dp))

        DropdownField(
            label = "Mode Permainan",
            selected = selectedMode,
            options = GameMode.values().toList(),
            optionLabel = { "${it.emoji} ${it.label}" },
            onSelect = { selectedMode = it },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(4.dp))
        Text(selectedMode.description, style = MaterialTheme.typography.labelSmall)

        Spacer(Modifier.height(16.dp))

        DropdownField(
            label = "Jumlah Pemain",
            selected = playerCount,
            options = listOf(2, 3, 4),
            optionLabel = { it.toString() },
            onSelect = { playerCount = it },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = vsBot, onCheckedChange = { vsBot = it })
            Text("Pemain terakhir adalah Bot")
        }

        Spacer(Modifier.height(16.dp))

        Text("Pemain")
        Spacer(Modifier.height(8.dp))

        (0 until playerCount).forEach { i ->
            val isBot = vsBot && i == playerCount - 1
            val takenByOthers = (0 until playerCount)
                .filter { it != i }
                .map { playerAvatars.value[it] }
                .toSet()
            PlayerEditorRow(
                index = i,
                name = playerNames.value[i],
                onNameChange = { newName ->
                    playerNames.value = playerNames.value.toMutableList().also { it[i] = newName }
                },
                avatar = playerAvatars.value[i],
                takenAvatars = takenByOthers,
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

        Spacer(Modifier.height(12.dp))

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
    takenAvatars: Set<AnimalAvatar>,
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

        Spacer(Modifier.height(6.dp))

        AvatarDropdown(
            selected = avatar,
            takenAvatars = takenAvatars,
            onSelect = onAvatarChange,
            modifier = Modifier.fillMaxWidth()
        )

        if (!isBot) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = isYoung, onCheckedChange = onYoungChange)
                Text("Pemain Muda (mulai dengan Perisai, hoki Kotak Misteri lebih baik)", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

/** Dropdown pilihan generik: label, nilai terpilih, dan daftar opsi. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> DropdownField(
    label: String,
    selected: T,
    options: List<T>,
    optionLabel: (T) -> String,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = optionLabel(selected),
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(optionLabel(option)) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

/** Dropdown pemilih karakter (hewan/kerajaan/profesi), dikelompokkan per kategori. Karakter yang sudah dipakai pemain lain tidak bisa dipilih. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AvatarDropdown(
    selected: AnimalAvatar,
    takenAvatars: Set<AnimalAvatar>,
    onSelect: (AnimalAvatar) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = "${selected.emoji} ${selected.label}",
            onValueChange = {},
            readOnly = true,
            label = { Text("Karakter") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            AvatarCategory.values().forEach { category ->
                Text(
                    category.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
                AnimalAvatar.values().filter { it.category == category }.forEach { a ->
                    val disabled = a in takenAvatars && a != selected
                    DropdownMenuItem(
                        text = { Text("${a.emoji} ${a.label}") },
                        enabled = !disabled,
                        onClick = {
                            onSelect(a)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}
