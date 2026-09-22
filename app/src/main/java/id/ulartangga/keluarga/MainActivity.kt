package id.ulartangga.keluarga

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import id.ulartangga.keluarga.data.DailyRewardManager
import id.ulartangga.keluarga.game.GameEngine
import id.ulartangga.keluarga.game.PowerCardType
import id.ulartangga.keluarga.online.DeviceId
import id.ulartangga.keluarga.online.OnlineGuestController
import id.ulartangga.keluarga.online.OnlineHostController
import id.ulartangga.keluarga.online.RoomRepository
import id.ulartangga.keluarga.sound.SoundManager
import id.ulartangga.keluarga.ui.components.ThemeBackdrop
import id.ulartangga.keluarga.ui.screens.GameScreen
import id.ulartangga.keluarga.ui.screens.OnlineGuestGameScreen
import id.ulartangga.keluarga.ui.screens.OnlineHostGameScreen
import id.ulartangga.keluarga.ui.screens.OnlineLobbyScreen
import id.ulartangga.keluarga.ui.screens.SetupScreen
import id.ulartangga.keluarga.ui.theme.BoardTheme
import id.ulartangga.keluarga.ui.theme.UlarTanggaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            UlarTanggaApp()
        }
    }
}

private sealed class AppScreen {
    object Setup : AppScreen()
    data class Local(val engine: GameEngine) : AppScreen()
    object OnlineLobby : AppScreen()
    data class OnlineHost(val controller: OnlineHostController, val roomCode: String) : AppScreen()
    data class OnlineGuest(val controller: OnlineGuestController, val roomCode: String) : AppScreen()
}

@Composable
fun UlarTanggaApp() {
    var screen by remember { mutableStateOf<AppScreen>(AppScreen.Setup) }
    var soundOn by remember { mutableStateOf(true) }
    val boardTheme = BoardTheme.FOREST
    val soundManager = remember { SoundManager() }

    val context = LocalContext.current
    val dailyRewardManager = remember { DailyRewardManager(context) }
    val repository = remember { RoomRepository(context) }
    val myDeviceId = remember { DeviceId.get(context) }
    var dailyReward by remember { mutableStateOf<DailyRewardManager.ClaimResult?>(null) }
    var pendingBonusCard by remember { mutableStateOf<PowerCardType?>(null) }

    LaunchedEffect(Unit) {
        dailyReward = dailyRewardManager.claimIfAvailable()
    }

    DisposableEffect(Unit) {
        onDispose { soundManager.release() }
    }

    UlarTanggaTheme(boardTheme = boardTheme) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Box(modifier = Modifier.fillMaxSize()) {
                ThemeBackdrop(theme = boardTheme, modifier = Modifier.fillMaxSize())

                when (val current = screen) {
                    is AppScreen.Setup -> SetupScreen(
                        onStart = { players, mode ->
                            pendingBonusCard?.let { card ->
                                val recipient = players.firstOrNull { !it.isBot }
                                if (recipient != null && recipient.cards.size < 3) {
                                    recipient.cards.add(card)
                                }
                                pendingBonusCard = null
                            }
                            val engine = GameEngine(players, mode) { event ->
                                if (soundOn) soundManager.play(event)
                            }
                            screen = AppScreen.Local(engine)
                        },
                        onPlayOnline = { screen = AppScreen.OnlineLobby }
                    )

                    is AppScreen.Local -> GameScreen(
                        engine = current.engine,
                        theme = boardTheme,
                        soundOn = soundOn,
                        onToggleSound = { soundOn = !soundOn },
                        onExit = { screen = AppScreen.Setup }
                    )

                    is AppScreen.OnlineLobby -> OnlineLobbyScreen(
                        myDeviceId = myDeviceId,
                        repository = repository,
                        onEnterRoom = { code, isHost, entries ->
                            screen = if (isHost) {
                                val controller = OnlineHostController(repository, code, entries, myDeviceId) { event ->
                                    if (soundOn) soundManager.play(event)
                                }
                                AppScreen.OnlineHost(controller, code)
                            } else {
                                AppScreen.OnlineGuest(OnlineGuestController(repository, code, myDeviceId, entries), code)
                            }
                        },
                        onBack = { screen = AppScreen.Setup }
                    )

                    is AppScreen.OnlineHost -> OnlineHostGameScreen(
                        controller = current.controller,
                        roomCode = current.roomCode,
                        theme = boardTheme,
                        onExit = { screen = AppScreen.Setup }
                    )

                    is AppScreen.OnlineGuest -> OnlineGuestGameScreen(
                        controller = current.controller,
                        roomCode = current.roomCode,
                        theme = boardTheme,
                        onExit = { screen = AppScreen.Setup }
                    )
                }

                dailyReward?.let { result ->
                    DailyRewardDialog(
                        result = result,
                        onClaim = {
                            pendingBonusCard = result.reward
                            dailyReward = null
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun DailyRewardDialog(result: DailyRewardManager.ClaimResult, onClaim: () -> Unit) {
    AlertDialog(
        onDismissRequest = {},
        confirmButton = {
            TextButton(onClick = onClaim) { Text("Ambil") }
        },
        title = { Text("Hadiah Harian!") },
        text = {
            Text(
                "Streak ${result.streak} hari main bersama! Kamu dapat kartu " +
                    "${result.reward.emoji} ${result.reward.label} gratis untuk partai berikutnya."
            )
        }
    )
}
