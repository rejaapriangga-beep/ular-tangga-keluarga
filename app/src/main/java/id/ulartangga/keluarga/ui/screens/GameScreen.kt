package id.ulartangga.keluarga.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.ulartangga.keluarga.game.GameEngine
import id.ulartangga.keluarga.game.Player
import id.ulartangga.keluarga.game.PowerCardType
import id.ulartangga.keluarga.ui.components.BoardView
import id.ulartangga.keluarga.ui.components.DiceView
import id.ulartangga.keluarga.ui.components.MiniGameDialog
import id.ulartangga.keluarga.ui.components.WoodenSign
import id.ulartangga.keluarga.ui.theme.BoardTheme
import kotlinx.coroutines.launch

@Composable
fun GameScreen(
    engine: GameEngine,
    theme: BoardTheme,
    soundOn: Boolean,
    onToggleSound: () -> Unit,
    onExit: () -> Unit
) {
    val scope = rememberCoroutineScope()

    LaunchedEffect(engine.turnToken, engine.winner) {
        engine.botTakeTurnIfNeeded()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TopBar(onExit = onExit, soundOn = soundOn, onToggleSound = onToggleSound)
            Spacer(Modifier.height(8.dp))
            PlayersRow(players = engine.players, currentPlayer = engine.currentPlayer)
            Spacer(Modifier.height(8.dp))
            BoardView(
                players = engine.players,
                theme = theme,
                collapsedCells = engine.collapsedCells,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp)
            )
            Spacer(Modifier.height(12.dp))

            engine.message?.let { msg ->
                Text(msg, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(8.dp))
            }

            val current = engine.currentPlayer
            val canAct = !engine.isBusy && engine.winner == null && !engine.coopComplete &&
                !current.isBot && !current.finished

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CardTray(
                    player = current,
                    enabled = canAct,
                    onUseDoubleDice = { engine.activateDoubleDice() },
                    onUseSwap = { engine.beginSwap() }
                )
                DiceView(
                    value = engine.diceValue,
                    isRolling = engine.isBusy,
                    enabled = canAct,
                    onRoll = { scope.launch { engine.rollDice() } }
                )
            }

            if (engine.doubleDiceActive) {
                Spacer(Modifier.height(4.dp))
                Text("Dadu Ganda aktif!", color = MaterialTheme.colorScheme.tertiary)
            }
        }

        if (engine.awaitingSwapTarget) {
            SwapTargetDialog(
                players = engine.players.filter { it !== engine.currentPlayer },
                onSelect = { engine.selectSwapTarget(it) },
                onDismiss = { engine.cancelSwap() }
            )
        }

        engine.miniGameRequest?.let { request ->
            MiniGameDialog(
                playerName = request.player.name,
                onResult = { success -> engine.resolveMiniGame(success) }
            )
        }

        engine.winner?.let { winner ->
            WinnerDialog(winner = winner, onPlayAgain = onExit)
        }

        if (engine.coopComplete) {
            CoopCompleteDialog(onPlayAgain = onExit)
        }
    }
}

@Composable
private fun TopBar(onExit: () -> Unit, soundOn: Boolean, onToggleSound: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onExit) {
            Icon(Icons.Filled.ArrowBack, contentDescription = "Keluar")
        }
        WoodenSign(text = "Ular Tangga Keluarga", style = MaterialTheme.typography.labelLarge)
        IconButton(onClick = onToggleSound) {
            Icon(
                if (soundOn) Icons.Filled.VolumeUp else Icons.Filled.VolumeOff,
                contentDescription = "Suara"
            )
        }
    }
}

@Composable
fun PlayersRow(players: List<Player>, currentPlayer: Player) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        players.forEach { player ->
            val isTurn = player === currentPlayer
            Column(
                modifier = Modifier
                    .background(
                        if (isTurn) player.color.copy(alpha = 0.25f) else Color.Transparent,
                        RoundedCornerShape(12.dp)
                    )
                    .padding(6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(player.color),
                    contentAlignment = Alignment.Center
                ) {
                    Text(player.avatar.emoji, fontSize = 16.sp)
                }
                Text(player.name, style = MaterialTheme.typography.labelSmall)
                Text(
                    if (player.finished) "🏁 Selesai" else "#${player.position}",
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

@Composable
fun CardTray(
    player: Player,
    enabled: Boolean,
    onUseDoubleDice: () -> Unit,
    onUseSwap: () -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        player.cards.forEach { card ->
            AssistChip(
                onClick = {
                    when (card) {
                        PowerCardType.DOUBLE_DICE -> onUseDoubleDice()
                        PowerCardType.SWAP -> onUseSwap()
                        PowerCardType.SHIELD -> Unit
                    }
                },
                enabled = enabled && card != PowerCardType.SHIELD,
                label = { Text("${card.emoji} ${card.label}") }
            )
        }
    }
}

@Composable
fun SwapTargetDialog(
    players: List<Player>,
    onSelect: (Player) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        },
        title = { Text("Pilih lawan untuk ditukar posisinya") },
        text = {
            Column {
                players.forEach { p ->
                    TextButton(onClick = { onSelect(p) }) {
                        Text("${p.name} (#${p.position})")
                    }
                }
            }
        }
    )
}

@Composable
fun WinnerDialog(winner: Player, onPlayAgain: () -> Unit) {
    AlertDialog(
        onDismissRequest = {},
        confirmButton = {
            TextButton(onClick = onPlayAgain) { Text("Main Lagi") }
        },
        title = { Text("Selamat!") },
        text = { Text("${winner.name} menang!") }
    )
}

@Composable
private fun CoopCompleteDialog(onPlayAgain: () -> Unit) {
    AlertDialog(
        onDismissRequest = {},
        confirmButton = {
            TextButton(onClick = onPlayAgain) { Text("Main Lagi") }
        },
        title = { Text("🎉 Semua Sampai Finish!") },
        text = { Text("Kerja tim keluarga yang hebat! Semua pemain berhasil menyelesaikan papan bersama-sama.") }
    )
}
