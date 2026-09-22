package id.ulartangga.keluarga.game

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import id.ulartangga.keluarga.sound.SoundEvent
import kotlin.random.Random
import kotlinx.coroutines.delay

class GameEngine(
    val players: List<Player>,
    private val onSound: (SoundEvent) -> Unit = {}
) {
    var currentPlayerIndex by mutableStateOf(0)
        private set
    var diceValue by mutableStateOf(1)
        private set
    var isBusy by mutableStateOf(false)
        private set
    var winner by mutableStateOf<Player?>(null)
        private set
    var doubleDiceActive by mutableStateOf(false)
        private set
    var message by mutableStateOf<String?>(null)
        private set
    var awaitingSwapTarget by mutableStateOf(false)
        private set

    val currentPlayer: Player get() = players[currentPlayerIndex]

    fun activateDoubleDice() {
        if (!isBusy && winner == null && currentPlayer.cards.remove(PowerCardType.DOUBLE_DICE)) {
            doubleDiceActive = true
        }
    }

    fun beginSwap() {
        if (!isBusy && winner == null && currentPlayer.cards.contains(PowerCardType.SWAP)) {
            awaitingSwapTarget = true
        }
    }

    fun cancelSwap() {
        awaitingSwapTarget = false
    }

    fun selectSwapTarget(target: Player) {
        if (!awaitingSwapTarget) return
        doSwap(currentPlayer, target)
        awaitingSwapTarget = false
    }

    private fun doSwap(player: Player, target: Player) {
        if (target === player) return
        if (!player.cards.remove(PowerCardType.SWAP)) return
        val temp = player.position
        player.position = target.position
        target.position = temp
        player.animatedCell = player.position
        target.animatedCell = target.position
        onSound(SoundEvent.CARD)
        message = "${player.name} tukar posisi dengan ${target.name}!"
    }

    suspend fun rollDice() {
        if (isBusy || winner != null) return
        val player = currentPlayer
        isBusy = true
        message = null

        val roll1 = Random.nextInt(1, 7)
        val roll2 = if (doubleDiceActive) Random.nextInt(1, 7) else null
        val finalRoll = if (roll2 != null) maxOf(roll1, roll2) else roll1
        doubleDiceActive = false
        diceValue = finalRoll
        onSound(SoundEvent.TICK)
        delay(500)

        val start = player.position
        val target = (start + finalRoll).coerceAtMost(100)

        for (cell in (start + 1)..target) {
            player.animatedCell = cell
            delay(180)
        }
        player.position = target

        if (target == 100) {
            onSound(SoundEvent.WIN)
            winner = player
            isBusy = false
            return
        }

        if (target in BoardConfig.cardCells && player.cards.size < 3) {
            val newCard = PowerCardType.values().random()
            player.cards.add(newCard)
            message = "${player.name} dapat kartu ${newCard.emoji} ${newCard.label}!"
            onSound(SoundEvent.CARD)
            delay(400)
        }

        BoardConfig.ladders[target]?.let { end ->
            delay(200)
            player.animatedCell = end
            player.position = end
            message = "${player.name} naik tangga ke $end!"
            onSound(SoundEvent.LADDER)
            delay(400)
        }

        BoardConfig.snakes[target]?.let { end ->
            if (player.cards.remove(PowerCardType.SHIELD)) {
                message = "${player.name} kena ular tapi terlindungi Perisai!"
                onSound(SoundEvent.CARD)
            } else {
                delay(200)
                player.animatedCell = end
                player.position = end
                message = "${player.name} kena ular, turun ke $end!"
                onSound(SoundEvent.SNAKE)
            }
            delay(400)
        }

        if (player.position == 100) {
            onSound(SoundEvent.WIN)
            winner = player
            isBusy = false
            return
        }

        currentPlayerIndex = (currentPlayerIndex + 1) % players.size
        isBusy = false
    }

    suspend fun botTakeTurnIfNeeded() {
        val player = currentPlayer
        if (!player.isBot || isBusy || winner != null) return
        delay(700)

        val leader = players.filter { it !== player }.maxByOrNull { it.position }
        if (player.cards.contains(PowerCardType.SWAP) && leader != null && leader.position - player.position > 15) {
            doSwap(player, leader)
            delay(500)
        }
        if (player.cards.contains(PowerCardType.DOUBLE_DICE)) {
            activateDoubleDice()
            delay(300)
        }
        rollDice()
    }
}
