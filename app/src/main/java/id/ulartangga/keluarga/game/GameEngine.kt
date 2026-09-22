package id.ulartangga.keluarga.game

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import id.ulartangga.keluarga.sound.SoundEvent
import kotlin.random.Random
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.delay

class GameEngine(
    val players: List<Player>,
    val mode: GameMode = GameMode.CLASSIC,
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
    var coopComplete by mutableStateOf(false)
        private set
    var doubleDiceActive by mutableStateOf(false)
        private set
    var message by mutableStateOf<String?>(null)
        private set
    var awaitingSwapTarget by mutableStateOf(false)
        private set
    var miniGameRequest by mutableStateOf<MiniGameRequest?>(null)
        private set
    val collapsedCells = mutableStateListOf<Int>()
    private var totalTurns = 0

    val currentPlayer: Player get() = players[currentPlayerIndex]
    private val isGameOver: Boolean get() = winner != null || coopComplete

    fun resolveMiniGame(success: Boolean) {
        miniGameRequest?.deferred?.complete(success)
    }

    fun activateDoubleDice() {
        if (!isBusy && !isGameOver && currentPlayer.cards.remove(PowerCardType.DOUBLE_DICE)) {
            doubleDiceActive = true
        }
    }

    fun beginSwap() {
        if (!isBusy && !isGameOver && currentPlayer.cards.contains(PowerCardType.SWAP)) {
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
        if (isBusy || isGameOver) return
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

        if (mode == GameMode.BATTLE_ROYALE && target in collapsedCells) {
            for (cell in (start + 1)..target) {
                player.animatedCell = cell
                delay(120)
            }
            delay(150)
            for (cell in target downTo start) {
                player.animatedCell = cell
                delay(90)
            }
            message = "💥 Kotak $target runtuh! ${player.name} terpental kembali ke $start."
            onSound(SoundEvent.SNAKE)
            advanceTurn(isCombo)
            return
        }

        for (cell in (start + 1)..target) {
            player.animatedCell = cell
            delay(180)
        }
        player.position = target

        if (finishTurnIfWon(player)) return

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

        if (finishTurnIfWon(player)) return

        var miniGameMoved = false
        if (target in BoardConfig.miniGameCells) {
            val success = if (player.isBot || player.isRemote) {
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

        if (finishTurnIfWon(player)) return

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

        if (finishTurnIfWon(player)) return

        advanceTurn(isCombo)
    }

    /** Menutup giliran: proses combo/lanjut giliran, jalankan erosi papan Battle Royale, lalu buka giliran berikutnya. */
    private fun advanceTurn(isCombo: Boolean) {
        if (isCombo) {
            message = (message?.let { "$it " } ?: "") + "🔥 Dadu kembar! ${currentPlayer.name} dapat giliran ekstra!"
            onSound(SoundEvent.CARD)
        } else {
            currentPlayerIndex = nextPlayerIndex(currentPlayerIndex)
        }
        maybeCollapseCell()
        turnToken++
        isBusy = false
    }

    /** Mengembalikan true jika giliran sudah ditutup di sini (pemain menang / finish) sehingga rollDice() harus berhenti. */
    private fun finishTurnIfWon(player: Player): Boolean {
        if (player.position != 100) return false
        onSound(SoundEvent.WIN)
        if (mode == GameMode.COOP) {
            if (!player.finished) {
                player.finished = true
                message = "🎉 ${player.name} sampai finish!"
            }
            if (players.all { it.finished }) {
                coopComplete = true
                isBusy = false
                return true
            }
            currentPlayerIndex = nextPlayerIndex(currentPlayerIndex)
            turnToken++
            isBusy = false
            return true
        }
        winner = player
        isBusy = false
        return true
    }

    private fun nextPlayerIndex(from: Int): Int {
        var idx = from
        do {
            idx = (idx + 1) % players.size
        } while (mode == GameMode.COOP && players[idx].finished && players.any { !it.finished })
        return idx
    }

    private fun maybeCollapseCell() {
        if (mode != GameMode.BATTLE_ROYALE) return
        totalTurns++
        if (totalTurns % 4 != 0) return
        val occupied = players.map { it.position }.toSet()
        val reserved = BoardConfig.ladders.keys + BoardConfig.ladders.values +
            BoardConfig.snakes.keys + BoardConfig.snakes.values +
            BoardConfig.cardCells + BoardConfig.mysteryCells + BoardConfig.miniGameCells +
            collapsedCells.toSet() + occupied + setOf(1, 100)
        val candidate = (2..99).filter { it !in reserved }.randomOrNull() ?: return
        collapsedCells.add(candidate)
        message = (message?.let { "$it " } ?: "") + "⚠️ Kotak $candidate mulai retak dan runtuh!"
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
        if (!player.isBot || isBusy || isGameOver || player.finished) return
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
