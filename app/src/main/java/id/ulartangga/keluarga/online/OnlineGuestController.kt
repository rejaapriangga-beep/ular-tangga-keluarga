package id.ulartangga.keluarga.online

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import id.ulartangga.keluarga.game.Player
import id.ulartangga.keluarga.ui.theme.PlayerPalette

/** Dijalankan di perangkat tamu: tidak punya GameEngine, hanya merender state dari Firebase. */
class OnlineGuestController(
    private val repository: RoomRepository,
    val roomCode: String,
    val myDeviceId: String,
    lobbyEntries: List<LobbyEntry>
) {
    val players: List<Player> = lobbyEntries.mapIndexed { index, entry ->
        Player(
            id = index,
            name = entry.name,
            color = PlayerPalette[entry.colorIndex % PlayerPalette.size],
            avatar = entry.avatar,
            isBot = false,
            isRemote = entry.deviceId != myDeviceId,
            remoteId = entry.deviceId
        )
    }

    var currentDeviceId by mutableStateOf<String?>(null)
        private set
    var diceValue by mutableStateOf(1)
        private set
    var isBusy by mutableStateOf(false)
        private set
    var message by mutableStateOf<String?>(null)
        private set
    var winnerDeviceId by mutableStateOf<String?>(null)
        private set

    val myPlayer: Player? get() = players.firstOrNull { it.remoteId == myDeviceId }
    val isMyTurn: Boolean get() = currentDeviceId == myDeviceId && winnerDeviceId == null

    private var detach: (() -> Unit)? = null

    fun start() {
        detach = repository.listenState(roomCode) { state ->
            if (state == null) return@listenState
            currentDeviceId = state.currentDeviceId
            diceValue = state.diceValue
            isBusy = state.isBusy
            message = state.message
            winnerDeviceId = state.winnerDeviceId
            state.players.forEach { (deviceId, remote) ->
                players.firstOrNull { it.remoteId == deviceId }?.let { p ->
                    p.position = remote.position
                    p.animatedCell = remote.animatedCell
                    p.finished = remote.finished
                }
            }
        }
    }

    fun stop() {
        detach?.invoke()
    }

    fun requestRoll() {
        if (isMyTurn && !isBusy) {
            repository.sendRollAction(roomCode, myDeviceId)
        }
    }
}
