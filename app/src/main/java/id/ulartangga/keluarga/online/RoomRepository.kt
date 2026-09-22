package id.ulartangga.keluarga.online

import android.content.Context
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ChildEventListener
import id.ulartangga.keluarga.game.AnimalAvatar

/** Wrapper tipis di atas Firebase Realtime Database untuk fitur Main Online (room code). */
class RoomRepository(context: Context) {
    private val db: FirebaseDatabase? = FirebaseBootstrap.database(context.applicationContext)

    val isAvailable: Boolean get() = db != null

    private fun roomsRef(): DatabaseReference? = db?.getReference("rooms")

    fun createRoom(
        hostId: String,
        hostName: String,
        hostAvatar: AnimalAvatar,
        hostColorIndex: Int,
        onResult: (code: String?, error: String?) -> Unit
    ) {
        val rooms = roomsRef() ?: return onResult(null, "Firebase belum dikonfigurasi")
        tryGenerateUniqueCode(rooms, attempt = 0) { code ->
            if (code == null) {
                onResult(null, "Gagal membuat kode room, coba lagi")
                return@tryGenerateUniqueCode
            }
            val room = rooms.child(code)
            val payload = mapOf(
                "host" to hostId,
                "status" to "lobby",
                "createdAt" to ServerValue.TIMESTAMP,
                "lobby" to mapOf(
                    hostId to lobbyEntryPayload(hostName, hostAvatar, hostColorIndex)
                )
            )
            room.setValue(payload) { error, _ ->
                if (error == null) onResult(code, null) else onResult(null, error.message)
            }
        }
    }

    private fun tryGenerateUniqueCode(rooms: DatabaseReference, attempt: Int, onCode: (String?) -> Unit) {
        if (attempt >= 5) {
            onCode(null)
            return
        }
        val code = generateCode()
        rooms.child(code).child("host").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    tryGenerateUniqueCode(rooms, attempt + 1, onCode)
                } else {
                    onCode(code)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                onCode(null)
            }
        })
    }

    fun joinRoom(
        code: String,
        deviceId: String,
        name: String,
        avatar: AnimalAvatar,
        colorIndex: Int,
        onResult: (success: Boolean, error: String?) -> Unit
    ) {
        val room = roomsRef()?.child(code) ?: return onResult(false, "Firebase belum dikonfigurasi")
        room.child("host").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    onResult(false, "Kode room tidak ditemukan")
                    return
                }
                room.child("lobby").child(deviceId).setValue(lobbyEntryPayload(name, avatar, colorIndex)) { error, _ ->
                    if (error == null) onResult(true, null) else onResult(false, error.message)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                onResult(false, error.message)
            }
        })
    }

    private fun lobbyEntryPayload(name: String, avatar: AnimalAvatar, colorIndex: Int) = mapOf(
        "name" to name,
        "avatar" to avatar.name,
        "colorIndex" to colorIndex,
        "joinedAt" to ServerValue.TIMESTAMP
    )

    fun listenRoom(
        code: String,
        onUpdate: (hostId: String?, status: String, entries: List<LobbyEntry>) -> Unit
    ): () -> Unit {
        val room = roomsRef()?.child(code) ?: return {}
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val hostId = snapshot.child("host").getValue(String::class.java)
                val status = snapshot.child("status").getValue(String::class.java) ?: "lobby"
                val entries = snapshot.child("lobby").children.mapNotNull { child ->
                    val deviceId = child.key ?: return@mapNotNull null
                    val name = child.child("name").getValue(String::class.java) ?: return@mapNotNull null
                    val avatarName = child.child("avatar").getValue(String::class.java) ?: AnimalAvatar.LION.name
                    val avatar = runCatching { AnimalAvatar.valueOf(avatarName) }.getOrDefault(AnimalAvatar.LION)
                    val colorIndex = (child.child("colorIndex").getValue(Long::class.java) ?: 0L).toInt()
                    val joinedAt = child.child("joinedAt").getValue(Long::class.java) ?: 0L
                    LobbyEntry(deviceId, name, avatar, colorIndex, joinedAt)
                }.sortedBy { it.joinedAt }
                onUpdate(hostId, status, entries)
            }

            override fun onCancelled(error: DatabaseError) = Unit
        }
        room.addValueEventListener(listener)
        return { room.removeEventListener(listener) }
    }

    fun startGame(code: String) {
        roomsRef()?.child(code)?.child("status")?.setValue("playing")
    }

    fun endRoom(code: String) {
        roomsRef()?.child(code)?.child("status")?.setValue("ended")
    }

    fun pushState(code: String, state: Map<String, Any?>) {
        roomsRef()?.child(code)?.child("state")?.setValue(state)
    }

    fun listenState(code: String, onUpdate: (RemoteGameState?) -> Unit): () -> Unit {
        val stateRef = roomsRef()?.child(code)?.child("state") ?: return {}
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    onUpdate(null)
                    return
                }
                val players = snapshot.child("players").children.mapNotNull { child ->
                    val deviceId = child.key ?: return@mapNotNull null
                    val position = (child.child("position").getValue(Long::class.java) ?: 1L).toInt()
                    val animatedCell = (child.child("animatedCell").getValue(Long::class.java) ?: 1L).toInt()
                    val finished = child.child("finished").getValue(Boolean::class.java) ?: false
                    deviceId to RemotePlayerState(position, animatedCell, finished)
                }.toMap()
                onUpdate(
                    RemoteGameState(
                        currentDeviceId = snapshot.child("currentDeviceId").getValue(String::class.java),
                        diceValue = (snapshot.child("diceValue").getValue(Long::class.java) ?: 1L).toInt(),
                        isBusy = snapshot.child("isBusy").getValue(Boolean::class.java) ?: false,
                        message = snapshot.child("message").getValue(String::class.java),
                        winnerDeviceId = snapshot.child("winnerDeviceId").getValue(String::class.java),
                        players = players
                    )
                )
            }

            override fun onCancelled(error: DatabaseError) = Unit
        }
        stateRef.addValueEventListener(listener)
        return { stateRef.removeEventListener(listener) }
    }

    fun sendRollAction(code: String, deviceId: String) {
        val actionsRef = roomsRef()?.child(code)?.child("actions") ?: return
        actionsRef.push().setValue(mapOf("deviceId" to deviceId, "type" to "ROLL"))
    }

    fun listenActions(code: String, onRoll: (actionKey: String, deviceId: String) -> Unit): () -> Unit {
        val actionsRef = roomsRef()?.child(code)?.child("actions") ?: return {}
        val listener = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val key = snapshot.key ?: return
                val deviceId = snapshot.child("deviceId").getValue(String::class.java) ?: return
                val type = snapshot.child("type").getValue(String::class.java)
                if (type == "ROLL") onRoll(key, deviceId)
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) = Unit
            override fun onChildRemoved(snapshot: DataSnapshot) = Unit
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) = Unit
            override fun onCancelled(error: DatabaseError) = Unit
        }
        actionsRef.addChildEventListener(listener)
        return { actionsRef.removeEventListener(listener) }
    }

    fun consumeAction(code: String, actionKey: String) {
        roomsRef()?.child(code)?.child("actions")?.child(actionKey)?.removeValue()
    }

    private fun generateCode(): String {
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        return (1..5).map { chars.random() }.joinToString("")
    }
}
