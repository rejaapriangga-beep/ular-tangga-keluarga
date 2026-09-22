package id.ulartangga.keluarga.game

object BoardConfig {
    const val COLUMNS = 7
    const val ROWS = 12
    const val TOTAL_CELLS = COLUMNS * ROWS

    val ladders: Map<Int, Int> = mapOf(
        4 to 18, 10 to 24, 17 to 31, 26 to 40,
        34 to 48, 44 to 58, 55 to 70, 63 to 78
    )

    val snakes: Map<Int, Int> = mapOf(
        15 to 3, 25 to 9, 37 to 20, 47 to 29,
        57 to 39, 69 to 50, 79 to 61, 82 to 64
    )

    val cardCells: Set<Int> = setOf(6, 13, 22, 32, 42, 52, 62, 72)

    val mysteryCells: Set<Int> = setOf(8, 19, 28, 38, 49, 60, 71, 80)

    val miniGameCells: Set<Int> = setOf(7, 16, 23, 36, 46, 56, 66, 76)
}
