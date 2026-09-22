package id.ulartangga.keluarga.game

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

class Player(
    val id: Int,
    val name: String,
    val color: Color,
    val avatar: AnimalAvatar,
    val isBot: Boolean
) {
    var position by mutableStateOf(1)
    var animatedCell by mutableStateOf(1)
    var lastRoll by mutableStateOf(0)
    val cards = mutableStateListOf<PowerCardType>()
}
