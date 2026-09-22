package id.ulartangga.keluarga.online

import id.ulartangga.keluarga.game.AnimalAvatar

data class LobbyEntry(
    val deviceId: String,
    val name: String,
    val avatar: AnimalAvatar,
    val colorIndex: Int,
    val joinedAt: Long
)

data class RemotePlayerState(
    val position: Int,
    val animatedCell: Int,
    val finished: Boolean
)

data class RemoteGameState(
    val currentDeviceId: String?,
    val diceValue: Int,
    val isBusy: Boolean,
    val message: String?,
    val winnerDeviceId: String?,
    val players: Map<String, RemotePlayerState>
)
