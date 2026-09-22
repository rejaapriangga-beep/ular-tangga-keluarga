package id.ulartangga.keluarga.game

enum class GameMode(val label: String, val emoji: String, val description: String) {
    CLASSIC("Klasik", "🏁", "Pemain pertama yang sampai kotak ${BoardConfig.TOTAL_CELLS} menang"),
    COOP("Co-op", "🤝", "Semua pemain kerja sama, menang bersama saat semua sampai ${BoardConfig.TOTAL_CELLS}"),
    BATTLE_ROYALE("Battle Royale Mini", "💥", "Kotak acak runtuh seiring waktu, hati-hati melangkah")
}
