package id.ulartangga.keluarga.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.ulartangga.keluarga.game.AnimalAvatar
import id.ulartangga.keluarga.online.LobbyEntry
import id.ulartangga.keluarga.online.RoomRepository
import id.ulartangga.keluarga.ui.components.WoodenSign
import kotlin.math.abs

private enum class LobbyStep { CHOOSE, CREATE_FORM, JOIN_FORM, WAITING }

@Composable
fun OnlineLobbyScreen(
    myDeviceId: String,
    repository: RoomRepository,
    onEnterRoom: (code: String, isHost: Boolean, entries: List<LobbyEntry>) -> Unit,
    onBack: () -> Unit
) {
    var step by remember { mutableStateOf(LobbyStep.CHOOSE) }
    var name by remember { mutableStateOf("") }
    var avatar by remember { mutableStateOf(AnimalAvatar.LION) }
    var joinCodeInput by remember { mutableStateOf("") }
    var roomCode by remember { mutableStateOf<String?>(null) }
    var iAmHost by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var lobbyEntries by remember { mutableStateOf<List<LobbyEntry>>(emptyList()) }
    var hostId by remember { mutableStateOf<String?>(null) }

    DisposableEffect(roomCode) {
        val code = roomCode
        if (code == null) {
            onDispose {}
        } else {
            val detach = repository.listenRoom(code) { host, status, entries ->
                hostId = host
                lobbyEntries = entries
                if (status == "playing") {
                    onEnterRoom(code, host == myDeviceId, entries)
                }
            }
            onDispose { detach() }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        WoodenSign(text = "Main Online", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(24.dp))

        if (!repository.isAvailable) {
            Text(
                "Fitur Main Online belum dikonfigurasi. Isi FirebaseConfig.kt dengan project Firebase kamu sendiri dulu (lihat README).",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(16.dp))
            TextButton(onClick = onBack) { Text("Kembali") }
            return@Column
        }

        errorText?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(8.dp))
        }

        when (step) {
            LobbyStep.CHOOSE -> {
                Button(onClick = { step = LobbyStep.CREATE_FORM }) { Text("Buat Room") }
                Spacer(Modifier.height(12.dp))
                Button(onClick = { step = LobbyStep.JOIN_FORM }) { Text("Gabung Room") }
                Spacer(Modifier.height(20.dp))
                TextButton(onClick = onBack) { Text("Kembali") }
            }

            LobbyStep.CREATE_FORM -> {
                PlayerIdentityForm(name = name, onNameChange = { name = it }, avatar = avatar, onAvatarChange = { avatar = it })
                Spacer(Modifier.height(16.dp))
                Button(
                    enabled = name.isNotBlank() && !isLoading,
                    onClick = {
                        isLoading = true
                        errorText = null
                        val colorIndex = abs(myDeviceId.hashCode()) % 4
                        repository.createRoom(myDeviceId, name, avatar, colorIndex) { code, error ->
                            isLoading = false
                            if (code != null) {
                                roomCode = code
                                iAmHost = true
                                step = LobbyStep.WAITING
                            } else {
                                errorText = error ?: "Gagal membuat room"
                            }
                        }
                    }
                ) { Text("Buat Room") }
                if (isLoading) {
                    Spacer(Modifier.height(12.dp))
                    CircularProgressIndicator()
                }
                Spacer(Modifier.height(12.dp))
                TextButton(onClick = { step = LobbyStep.CHOOSE }) { Text("Batal") }
            }

            LobbyStep.JOIN_FORM -> {
                OutlinedTextField(
                    value = joinCodeInput,
                    onValueChange = { joinCodeInput = it.uppercase() },
                    label = { Text("Kode Room") },
                    singleLine = true
                )
                Spacer(Modifier.height(12.dp))
                PlayerIdentityForm(name = name, onNameChange = { name = it }, avatar = avatar, onAvatarChange = { avatar = it })
                Spacer(Modifier.height(16.dp))
                Button(
                    enabled = name.isNotBlank() && joinCodeInput.isNotBlank() && !isLoading,
                    onClick = {
                        isLoading = true
                        errorText = null
                        val colorIndex = abs(myDeviceId.hashCode()) % 4
                        repository.joinRoom(joinCodeInput, myDeviceId, name, avatar, colorIndex) { success, error ->
                            isLoading = false
                            if (success) {
                                roomCode = joinCodeInput
                                iAmHost = false
                                step = LobbyStep.WAITING
                            } else {
                                errorText = error ?: "Gagal gabung room"
                            }
                        }
                    }
                ) { Text("Gabung") }
                if (isLoading) {
                    Spacer(Modifier.height(12.dp))
                    CircularProgressIndicator()
                }
                Spacer(Modifier.height(12.dp))
                TextButton(onClick = { step = LobbyStep.CHOOSE }) { Text("Batal") }
            }

            LobbyStep.WAITING -> {
                Text("Kode Room", style = MaterialTheme.typography.labelMedium)
                Text(
                    roomCode.orEmpty(),
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.headlineLarge
                )
                Spacer(Modifier.height(8.dp))
                Text("Bagikan kode ini ke anggota keluarga lain", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(20.dp))

                Text("Pemain di lobby (${lobbyEntries.size})", style = MaterialTheme.typography.labelMedium)
                lobbyEntries.forEach { entry ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("${entry.avatar.emoji} ${entry.name}", style = MaterialTheme.typography.bodyMedium)
                        if (entry.deviceId == hostId) {
                            Spacer(Modifier.width(6.dp))
                            Text("(Host)", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                if (iAmHost) {
                    Button(
                        enabled = lobbyEntries.size >= 2,
                        onClick = { roomCode?.let { repository.startGame(it) } }
                    ) { Text("Mulai Permainan") }
                } else {
                    Text("Menunggu host memulai permainan...", style = MaterialTheme.typography.bodyMedium)
                }

                Spacer(Modifier.height(16.dp))
                OutlinedButton(onClick = onBack) { Text("Keluar dari Room") }
            }
        }
    }
}

@Composable
private fun PlayerIdentityForm(
    name: String,
    onNameChange: (String) -> Unit,
    avatar: AnimalAvatar,
    onAvatarChange: (AnimalAvatar) -> Unit
) {
    OutlinedTextField(
        value = name,
        onValueChange = onNameChange,
        label = { Text("Nama Kamu") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(Modifier.height(8.dp))
    AvatarDropdown(
        selected = avatar,
        takenAvatars = emptySet(),
        onSelect = onAvatarChange,
        modifier = Modifier.fillMaxWidth()
    )
}
