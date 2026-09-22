package id.ulartangga.keluarga.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import id.ulartangga.keluarga.online.OnlineGuestController
import id.ulartangga.keluarga.online.OnlineHostController
import id.ulartangga.keluarga.ui.components.BoardView
import id.ulartangga.keluarga.ui.components.DiceView
import id.ulartangga.keluarga.ui.components.MiniGameDialog
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

    val myTurn = controller.isMyTurn() && engine.winner == null
    val current = engine.currentPlayer

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.TopCenter
            ) {
                BoardView(players = engine.players, theme = theme)
            }

            BottomInfoPanel(
                log = engine.log,
                players = engine.players,
                currentPlayer = engine.currentPlayer
            ) {
                if (!myTurn && engine.winner == null) {
                    Text(
                        "Menunggu ${current.name}...",
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1
                    )
                    Spacer(Modifier.height(4.dp))
                }
                if (myTurn) {
                    CardTray(
                        player = current,
                        enabled = !engine.isBusy,
                        onUseDoubleDice = { engine.activateDoubleDice() },
                        onUseSwap = { engine.beginSwap() }
                    )
                    Spacer(Modifier.height(6.dp))
                }
                DiceView(
                    value = engine.diceValue,
                    isRolling = engine.isBusy,
                    enabled = myTurn && !engine.isBusy,
                    onRoll = { controller.rollAsHost() },
                    sizeDp = 56.dp
                )
            }
        }

        FloatingIconButton(onClick = onExit, modifier = Modifier.align(Alignment.TopStart).padding(8.dp)) {
            Icon(Icons.Filled.ArrowBack, contentDescription = "Keluar")
        }
        RoomCodeBadge(roomCode = roomCode, modifier = Modifier.align(Alignment.TopEnd).padding(8.dp))

        TurnPopup(
            turnKey = engine.turnToken,
            currentPlayer = engine.currentPlayer,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 56.dp)
        )

        engine.message?.let { msg ->
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 112.dp, start = 16.dp, end = 16.dp)
            ) {
                Text(
                    msg,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
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

    // Host mengirim satu ringkasan pesan per giliran; kumpulkan jadi log lokal di sisi tamu.
    val guestLog = remember { mutableStateListOf<String>() }
    LaunchedEffect(controller.message) {
        controller.message?.let { guestLog.add(it) }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.TopCenter
            ) {
                BoardView(players = controller.players, theme = theme)
            }

            if (current != null) {
                BottomInfoPanel(
                    log = guestLog,
                    players = controller.players,
                    currentPlayer = current
                ) {
                    if (!controller.isMyTurn && controller.winnerDeviceId == null) {
                        Text(
                            "Menunggu ${current.name}...",
                            style = MaterialTheme.typography.labelSmall,
                            maxLines = 1
                        )
                        Spacer(Modifier.height(4.dp))
                    }
                    DiceView(
                        value = controller.diceValue,
                        isRolling = controller.isBusy,
                        enabled = controller.isMyTurn,
                        onRoll = { controller.requestRoll() },
                        sizeDp = 56.dp
                    )
                }
            }
        }

        FloatingIconButton(onClick = onExit, modifier = Modifier.align(Alignment.TopStart).padding(8.dp)) {
            Icon(Icons.Filled.ArrowBack, contentDescription = "Keluar")
        }
        RoomCodeBadge(roomCode = roomCode, modifier = Modifier.align(Alignment.TopEnd).padding(8.dp))

        if (current != null) {
            TurnPopup(
                turnKey = controller.currentDeviceId,
                currentPlayer = current,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 56.dp)
            )
        }

        controller.message?.let { msg ->
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 112.dp, start = 16.dp, end = 16.dp)
            ) {
                Text(
                    msg,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        }

        controller.winnerDeviceId?.let { winnerId ->
            val winner = controller.players.firstOrNull { it.remoteId == winnerId }
            if (winner != null) {
                WinnerDialog(winner = winner, onPlayAgain = onExit)
            }
        }
    }
}

@Composable
private fun RoomCodeBadge(roomCode: String, modifier: Modifier = Modifier) {
    Surface(
        color = Color.White.copy(alpha = 0.75f),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Text(
            "Room $roomCode",
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}
