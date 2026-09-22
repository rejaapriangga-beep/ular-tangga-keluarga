package id.ulartangga.keluarga.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.ulartangga.keluarga.game.GameEngine
import id.ulartangga.keluarga.game.Player
import id.ulartangga.keluarga.game.PowerCardType
import id.ulartangga.keluarga.ui.components.BoardView
import id.ulartangga.keluarga.ui.components.DiceView
import id.ulartangga.keluarga.ui.components.MiniGameDialog
import id.ulartangga.keluarga.ui.theme.BoardTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

val TOP_SPACER_HEIGHT = 28.dp
val BOTTOM_PANEL_HEIGHT = 148.dp

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

    val current = engine.currentPlayer
    val canAct = !engine.isBusy && engine.winner == null && !engine.coopComplete &&
        !current.isBot && !current.finished

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(Modifier.height(TOP_SPACER_HEIGHT))
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.TopCenter
            ) {
                BoardView(
                    players = engine.players,
                    theme = theme,
                    collapsedCells = engine.collapsedCells
                )
            }

            BottomInfoPanel(
                log = engine.log,
                players = engine.players,
                currentPlayer = engine.currentPlayer
            ) {
                if (engine.doubleDiceActive) {
                    Text("Dadu Ganda aktif!", color = MaterialTheme.colorScheme.tertiary, style = MaterialTheme.typography.labelSmall)
                    Spacer(Modifier.height(4.dp))
                }
                CardTray(
                    player = current,
                    enabled = canAct,
                    onUseDoubleDice = { engine.activateDoubleDice() },
                    onUseSwap = { engine.beginSwap() }
                )
                Spacer(Modifier.height(6.dp))
                DiceView(
                    value = engine.diceValue,
                    isRolling = engine.isBusy,
                    enabled = canAct,
                    onRoll = { scope.launch { engine.rollDice() } },
                    sizeDp = 56.dp
                )
            }
        }

        FloatingIconButton(onClick = onExit, modifier = Modifier.align(Alignment.TopStart).padding(8.dp)) {
            Icon(Icons.Filled.ArrowBack, contentDescription = "Keluar", modifier = Modifier.size(16.dp))
        }
        FloatingIconButton(onClick = onToggleSound, modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)) {
            Icon(
                if (soundOn) Icons.Filled.VolumeUp else Icons.Filled.VolumeOff,
                contentDescription = "Suara",
                modifier = Modifier.size(16.dp)
            )
        }

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

        if (engine.coopComplete) {
            CoopCompleteDialog(onPlayAgain = onExit)
        }
    }
}

/** Panel bawah 3 kolom (masing-masing ~sepertiga lebar): log langkah (kiri), dadu & kartu power-up (tengah), daftar pemain (kanan). */
@Composable
fun BottomInfoPanel(
    log: List<String>,
    players: List<Player>,
    currentPlayer: Player,
    modifier: Modifier = Modifier,
    diceColumn: @Composable ColumnScope.() -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(BOTTOM_PANEL_HEIGHT)
            .padding(horizontal = 6.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        MoveLogPanel(log = log, players = players, modifier = Modifier.weight(1f).fillMaxSize())
        Column(
            modifier = Modifier.weight(1f).fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            content = diceColumn
        )
        PlayerStatusList(
            players = players,
            currentPlayer = currentPlayer,
            modifier = Modifier.weight(1f).fillMaxSize()
        )
    }
}

/** Log langkah semua pemain; setiap baris diwarnai sesuai warna pemain yang disebut di teksnya, dengan jarak ekstra saat pemain berganti. */
@Composable
fun MoveLogPanel(log: List<String>, players: List<Player>, modifier: Modifier = Modifier) {
    Surface(
        color = Color.White.copy(alpha = 0.6f),
        shape = RoundedCornerShape(10.dp),
        modifier = modifier
    ) {
        if (log.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Belum ada langkah", style = MaterialTheme.typography.labelSmall)
            }
        } else {
            val reversedLog = log.asReversed()
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(6.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                itemsIndexed(reversedLog) { index, entry ->
                    val player = players.firstOrNull { entry.startsWith(it.name) }
                    val previousPlayer = if (index > 0) {
                        players.firstOrNull { reversedLog[index - 1].startsWith(it.name) }
                    } else {
                        null
                    }
                    if (index > 0 && previousPlayer !== player) {
                        Spacer(Modifier.height(6.dp))
                    }
                    Text(
                        entry,
                        style = MaterialTheme.typography.labelSmall,
                        color = player?.color ?: MaterialTheme.colorScheme.onSurface,
                        maxLines = 2
                    )
                }
            }
        }
    }
}

@Composable
fun PlayerStatusList(players: List<Player>, currentPlayer: Player, modifier: Modifier = Modifier) {
    Surface(
        color = Color.White.copy(alpha = 0.6f),
        shape = RoundedCornerShape(10.dp),
        modifier = modifier
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(players) { player ->
                val isTurn = player === currentPlayer
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (isTurn) player.color.copy(alpha = 0.3f) else Color.Transparent,
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(player.color),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            player.avatar.initial.toString(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Spacer(Modifier.width(6.dp))
                    Text(
                        player.name.take(10),
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        if (player.finished) "🏁" else "#${player.position}",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}

/** Tombol ikon melayang di atas papan, dengan latar putih transparan agar tetap terlihat di papan yang ramai warna. */
@Composable
fun FloatingIconButton(onClick: () -> Unit, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    IconButton(
        onClick = onClick,
        modifier = modifier
            .size(30.dp)
            .background(Color.White.copy(alpha = 0.75f), CircleShape)
    ) {
        content()
    }
}

@Composable
fun TurnIndicator(currentPlayer: Player, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(currentPlayer.color.copy(alpha = 0.95f))
            .padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Text(
                currentPlayer.avatar.initial.toString(),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = currentPlayer.color
            )
        }
        Spacer(Modifier.width(8.dp))
        Text(
            "Giliran ${currentPlayer.name}",
            style = MaterialTheme.typography.titleMedium,
            color = Color.White
        )
    }
}

/** Info giliran ditampilkan sebagai popup singkat (bukan bar permanen) supaya papan mendapat ruang vertikal lebih. */
@Composable
fun TurnPopup(turnKey: Any?, currentPlayer: Player, modifier: Modifier = Modifier) {
    var visible by remember { mutableStateOf(true) }
    LaunchedEffect(turnKey) {
        visible = true
        delay(1600)
        visible = false
    }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInVertically(initialOffsetY = { -it / 2 }),
        exit = fadeOut(),
        modifier = modifier
    ) {
        TurnIndicator(currentPlayer = currentPlayer)
    }
}

@Composable
fun CardTray(
    player: Player,
    enabled: Boolean,
    onUseDoubleDice: () -> Unit,
    onUseSwap: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
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
