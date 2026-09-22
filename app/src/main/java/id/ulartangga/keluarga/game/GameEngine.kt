package id.ulartangga.keluarga.game

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import id.ulartangga.keluarga.sound.SoundEvent
import kotlin.random.Random
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.delay

class GameEngine(
    val players: List<Player>,
    private val onSound: (SoundEvent) -> Unit = {}
) {
    var currentPlayerIndex by mutableStateOf(0)
        private set
    var turnToken by mutableStateOf(0)
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
    var miniGameRequest by mutableStateOf<MiniGameRequest?>(null)
        private set

    val currentPlayer: Player get() = players[currentPlayerIndex]

    fun resolveMiniGame(success: Boolean) {
        miniGameRequest?.deferred?.complete(success)
    }

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

        val isCombo = player.lastRoll != 0 && player.lastRoll == finalRoll
        player.lastRoll = finalRoll

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

        var mysteryMoved = false
        if (target in BoardConfig.mysteryCells) {
            mysteryMoved = resolveMystery(player)
            delay(400)
        }

        if (player.position == 100) {
            onSound(SoundEvent.WIN)
            winner = player
            isBusy = false
            return
        }

        var miniGameMoved = false
        if (target in BoardConfig.miniGameCells) {
            val success = if (player.isBot) {
                Random.nextInt(100) < 55
            } else {
                val deferred = CompletableDeferred<Boolean>()
                miniGameRequest = MiniGameRequest(player, deferred)
                val result = deferred.await()
                miniGameRequest = null
                result
            }
            if (success) {
                val newPos = (player.position + 4).coerceAtMost(100)
                player.position = newPos
                player.animatedCell = newPos
                message = "${player.name} berhasil tantangan Tap Cepat! Maju 4 langkah!"
                onSound(SoundEvent.LADDER)
                miniGameMoved = true
            } else {
                message = "${player.name} belum berhasil tantangan Tap Cepat, coba lagi lain kali!"
            }
            delay(400)
        }

        if (player.position == 100) {
            onSound(SoundEvent.WIN)
            winner = player
            isBusy = false
            return
        }

        if (!mysteryMoved && !miniGameMoved) {
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
        }

        if (player.position == 100) {
            onSound(SoundEvent.WIN)
            winner = player
            isBusy = false
            return
        }

        if (isCombo) {
            message = (message?.let { "$it " } ?: "") + "🔥 Dadu kembar! ${player.name} dapat giliran ekstra!"
            onSound(SoundEvent.CARD)
        } else {
            currentPlayerIndex = (currentPlayerIndex + 1) % players.size
        }
        turnToken++
        isBusy = false
    }

    /** Mengembalikan true jika posisi pemain berpindah akibat efek Kotak Misteri. */
    private fun resolveMystery(player: Player): Boolean {
        val outcomePool = if (player.isYoungPlayer) {
            MysteryOutcome.values().filter { it != MysteryOutcome.BACKWARD }
        } else {
            MysteryOutcome.values().toList()
        }
        return when (outcomePool.random()) {
            MysteryOutcome.FORWARD -> {
                val newPos = (player.position + 5).coerceAtMost(100)
                player.position = newPos
                player.animatedCell = newPos
                message = "${player.name} kena Kotak Misteri: maju 5 langkah!"
                onSound(SoundEvent.LADDER)
                true
            }
            MysteryOutcome.BACKWARD -> {
                val newPos = (player.position - 5).coerceAtLeast(1)
                player.position = newPos
                player.animatedCell = newPos
                message = "${player.name} kena Kotak Misteri: mundur 5 langkah!"
                onSound(SoundEvent.SNAKE)
                true
            }
            MysteryOutcome.CARD -> {
                if (player.cards.size < 3) {
                    val newCard = PowerCardType.values().random()
                    player.cards.add(newCard)
                    message = "${player.name} kena Kotak Misteri: dapat kartu ${newCard.emoji} ${newCard.label}!"
                } else {
                    message = "${player.name} kena Kotak Misteri: untung kosong (kartu penuh)!"
                }
                onSound(SoundEvent.CARD)
                false
            }
            MysteryOutcome.SWAP -> {
                val target = players.filter { it !== player }.randomOrNull()
                if (target != null) {
                    val temp = player.position
                    player.position = target.position
                    target.position = temp
                    player.animatedCell = player.position
                    target.animatedCell = target.position
                    message = "${player.name} kena Kotak Misteri: tukar posisi acak dengan ${target.name}!"
                    onSound(SoundEvent.CARD)
                    true
                } else {
                    false
                }
            }
            MysteryOutcome.NOTHING -> {
                message = "${player.name} kena Kotak Misteri: untung kosong!"
                false
            }
        }
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
