package id.ulartangga.keluarga.game

import kotlinx.coroutines.CompletableDeferred

class MiniGameRequest(val player: Player, val deferred: CompletableDeferred<Boolean>)
