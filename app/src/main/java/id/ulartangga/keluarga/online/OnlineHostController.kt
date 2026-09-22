package id.ulartangga.keluarga.online

import id.ulartangga.keluarga.game.GameEngine
import id.ulartangga.keluarga.game.GameMode
import id.ulartangga.keluarga.game.Player
import id.ulartangga.keluarga.sound.SoundEvent
import id.ulartangga.keluarga.ui.theme.PlayerPalette
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Dijalankan di perangkat host: memegang satu-satunya instance GameEngine untuk room ini,
 * lalu menyiarkan hasilnya ke Firebase dan menjalankan permintaan lempar-dadu dari tamu.
 */
class OnlineHostController(
    private val repository: RoomRepository,
    private val roomCode: String,
    lobbyEntries: List<LobbyEntry>,
    private val hostDeviceId: String,
    onSound: (SoundEvent) -> Unit
) {
    val engine: GameEngine

    init {
        val players = lobbyEntries.mapIndexed { index, entry ->
            Player(
                id = index,
                name = entry.name,
                color = PlayerPalette[entry.colorIndex % PlayerPalette.size],
                avatar = entry.avatar,
                isBot = false,
                isRemote = entry.deviceId != hostDeviceId,
                remoteId = entry.deviceId
            )
        }
        // Versi pertama Main Online hanya mendukung mode Klasik.
        engine = GameEngine(players, GameMode.CLASSIC, onSound)
    }

    private val scope = CoroutineScope(Dispatchers.Main.immediate)
    private var detachActions: (() -> Unit)? = null

    fun start() {
        pushSnapshot()
        detachActions = repository.listenActions(roomCode) { actionKey, deviceId ->
            val player = engine.players.firstOrNull { it.remoteId == deviceId }
            if (player == null || player !== engine.currentPlayer || engine.isBusy) {
                repository.consumeAction(roomCode, actionKey)
                return@listenActions
            }
            scope.launch {
                engine.rollDice()
                pushSnapshot()
                repository.consumeAction(roomCode, actionKey)
            }
        }
    }

    fun isMyTurn(): Boolean = engine.currentPlayer.remoteId == hostDeviceId

    fun rollAsHost() {
        if (!isMyTurn() || engine.isBusy) return
        scope.launch {
            engine.rollDice()
            pushSnapshot()
        }
    }

    fun stop() {
        detachActions?.invoke()
        repository.endRoom(roomCode)
    }

    private fun pushSnapshot() {
        val state = mapOf(
            "currentDeviceId" to engine.currentPlayer.remoteId,
            "diceValue" to engine.diceValue,
            "isBusy" to engine.isBusy,
            "message" to engine.message,
            "winnerDeviceId" to engine.winner?.remoteId,
            "players" to engine.players.associate { p ->
                (p.remoteId ?: p.id.toString()) to mapOf(
                    "position" to p.position,
                    "animatedCell" to p.animatedCell,
                    "finished" to p.finished
                )
            }
        )
        repository.pushState(roomCode, state)
    }
}
