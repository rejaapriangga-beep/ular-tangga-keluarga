package id.ulartangga.keluarga.game

object BoardConfig {
    val ladders: Map<Int, Int> = mapOf(
        2 to 38, 7 to 14, 8 to 31, 15 to 26, 21 to 42,
        28 to 84, 36 to 44, 51 to 67, 71 to 91, 78 to 98, 87 to 94
    )

    val snakes: Map<Int, Int> = mapOf(
        16 to 6, 46 to 25, 49 to 11, 62 to 19, 64 to 60,
        74 to 53, 89 to 68, 92 to 88, 95 to 75, 99 to 80
    )

    val cardCells: Set<Int> = setOf(5, 24, 33, 40, 58, 66, 77, 85, 93)
}
