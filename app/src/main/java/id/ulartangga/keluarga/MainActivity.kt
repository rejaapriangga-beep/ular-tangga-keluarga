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
import id.ulartangga.keluarga.sound.SoundManager
import id.ulartangga.keluarga.ui.components.ThemeBackdrop
import id.ulartangga.keluarga.ui.screens.GameScreen
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

@Composable
fun UlarTanggaApp() {
    var engine by remember { mutableStateOf<GameEngine?>(null) }
    var soundOn by remember { mutableStateOf(true) }
    var boardTheme by remember { mutableStateOf(BoardTheme.FOREST) }
    val soundManager = remember { SoundManager() }

    val context = LocalContext.current
    val dailyRewardManager = remember { DailyRewardManager(context) }
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

                val current = engine
                if (current == null) {
                    SetupScreen(
                        selectedTheme = boardTheme,
                        onThemeChange = { boardTheme = it },
                        onStart = { players ->
                            pendingBonusCard?.let { card ->
                                val recipient = players.firstOrNull { !it.isBot }
                                if (recipient != null && recipient.cards.size < 3) {
                                    recipient.cards.add(card)
                                }
                                pendingBonusCard = null
                            }
                            engine = GameEngine(players) { event ->
                                if (soundOn) soundManager.play(event)
                            }
                        }
                    )
                } else {
                    GameScreen(
                        engine = current,
                        theme = boardTheme,
                        soundOn = soundOn,
                        onToggleSound = { soundOn = !soundOn },
                        onExit = { engine = null }
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
