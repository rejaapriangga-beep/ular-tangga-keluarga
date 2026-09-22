package id.ulartangga.keluarga.game

enum class PowerCardType(val label: String, val emoji: String, val description: String) {
    SHIELD("Perisai", "🛡", "Melindungi dari satu gigitan ular secara otomatis"),
    DOUBLE_DICE("Dadu Ganda", "🎲", "Lempar dua dadu, pakai angka tertinggi"),
    SWAP("Tukar Posisi", "🔁", "Tukar posisi dengan pemain lain")
}
