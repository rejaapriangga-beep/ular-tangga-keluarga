package id.ulartangga.keluarga.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import id.ulartangga.keluarga.online.OnlineGuestController
import id.ulartangga.keluarga.online.OnlineHostController
import id.ulartangga.keluarga.ui.components.BoardView
import id.ulartangga.keluarga.ui.components.DiceView
import id.ulartangga.keluarga.ui.components.MiniGameDialog
import id.ulartangga.keluarga.ui.components.WoodenSign
import id.ulartangga.keluarga.ui.theme.BoardTheme

@Composable
fun OnlineHostGameScreen(
    controller: OnlineHostController,
    roomCode: String,
    theme: BoardTheme,
    onExit: () -> Unit
) {
    val engine = controller.engine

    DisposableEffect(Unit) {
        controller.start()
        onDispose { controller.stop() }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 6.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onExit) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Keluar")
                }
                WoodenSign(text = "Room $roomCode", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.width(48.dp))
            }
            Spacer(Modifier.height(6.dp))

            PlayersRow(players = engine.players, currentPlayer = engine.currentPlayer)
            Spacer(Modifier.height(6.dp))

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.TopCenter
            ) {
                BoardView(
                    players = engine.players,
                    theme = theme
                )
            }
            Spacer(Modifier.height(8.dp))

            engine.message?.let { msg ->
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        msg,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
                Spacer(Modifier.height(6.dp))
            }

            val myTurn = controller.isMyTurn() && engine.winner == null
            val current = engine.currentPlayer

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
            ) {
                if (myTurn) {
                    CardTray(
                        player = current,
                        enabled = !engine.isBusy,
                        onUseDoubleDice = { engine.activateDoubleDice() },
                        onUseSwap = { engine.beginSwap() },
                        modifier = Modifier.align(Alignment.CenterStart)
                    )
                }
                DiceView(
                    value = engine.diceValue,
                    isRolling = engine.isBusy,
                    enabled = myTurn && !engine.isBusy,
                    onRoll = { controller.rollAsHost() },
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            if (!myTurn && engine.winner == null) {
                Spacer(Modifier.height(6.dp))
                Text("Menunggu ${current.name} melempar dadu...", style = MaterialTheme.typography.bodyMedium)
            }
        }

        TurnPopup(
            turnKey = engine.turnToken,
            currentPlayer = engine.currentPlayer,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 64.dp)
        )

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
    }
}

@Composable
fun OnlineGuestGameScreen(
    controller: OnlineGuestController,
    roomCode: String,
    theme: BoardTheme,
    onExit: () -> Unit
) {
    DisposableEffect(Unit) {
        controller.start()
        onDispose { controller.stop() }
    }

    val current = controller.players.firstOrNull { it.remoteId == controller.currentDeviceId }
        ?: controller.players.firstOrNull()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 6.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onExit) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Keluar")
                }
                WoodenSign(text = "Room $roomCode", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.width(48.dp))
            }
            Spacer(Modifier.height(6.dp))

            if (current != null) {
                PlayersRow(players = controller.players, currentPlayer = current)
                Spacer(Modifier.height(6.dp))
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.TopCenter
            ) {
                BoardView(
                    players = controller.players,
                    theme = theme
                )
            }
            Spacer(Modifier.height(8.dp))

            controller.message?.let { msg ->
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        msg,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
                Spacer(Modifier.height(6.dp))
            }

            DiceView(
                value = controller.diceValue,
                isRolling = controller.isBusy,
                enabled = controller.isMyTurn,
                onRoll = { controller.requestRoll() }
            )

            if (!controller.isMyTurn && controller.winnerDeviceId == null) {
                Spacer(Modifier.height(6.dp))
                Text("Menunggu ${current?.name ?: "pemain lain"} melempar dadu...", style = MaterialTheme.typography.bodyMedium)
            }
        }

        if (current != null) {
            TurnPopup(
                turnKey = controller.currentDeviceId,
                currentPlayer = current,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 64.dp)
            )
        }

        controller.winnerDeviceId?.let { winnerId ->
            val winner = controller.players.firstOrNull { it.remoteId == winnerId }
            if (winner != null) {
                WinnerDialog(winner = winner, onPlayAgain = onExit)
            }
        }
    }
}
