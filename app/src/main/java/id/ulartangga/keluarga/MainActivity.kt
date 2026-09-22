package id.ulartangga.keluarga

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import id.ulartangga.keluarga.game.GameEngine
import id.ulartangga.keluarga.sound.SoundManager
import id.ulartangga.keluarga.ui.screens.GameScreen
import id.ulartangga.keluarga.ui.screens.SetupScreen
import id.ulartangga.keluarga.ui.theme.UlarTanggaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            UlarTanggaTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    UlarTanggaApp()
                }
            }
        }
    }
}

@Composable
fun UlarTanggaApp() {
    var engine by remember { mutableStateOf<GameEngine?>(null) }
    var soundOn by remember { mutableStateOf(true) }
    val soundManager = remember { SoundManager() }

    DisposableEffect(Unit) {
        onDispose { soundManager.release() }
    }

    val current = engine
    if (current == null) {
        SetupScreen(onStart = { players ->
            engine = GameEngine(players) { event ->
                if (soundOn) soundManager.play(event)
            }
        })
    } else {
        GameScreen(
            engine = current,
            soundOn = soundOn,
            onToggleSound = { soundOn = !soundOn },
            onExit = { engine = null }
        )
    }
}
