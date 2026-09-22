package id.ulartangga.keluarga.game

object BoardConfig {
    const val COLUMNS = 7
    const val ROWS = 14
    const val TOTAL_CELLS = COLUMNS * ROWS

    val ladders: Map<Int, Int> = mapOf(
        4 to 18, 10 to 31, 17 to 30, 26 to 47,
        40 to 54, 50 to 64, 59 to 73, 72 to 91
    )

    val snakes: Map<Int, Int> = mapOf(
        15 to 2, 29 to 9, 39 to 20, 53 to 35,
        63 to 44, 77 to 57, 87 to 68, 95 to 82
    )

    val cardCells: Set<Int> = setOf(6, 13, 23, 34, 43, 52, 61, 70, 80)

    val mysteryCells: Set<Int> = setOf(8, 22, 33, 42, 51, 62, 71, 84, 93)

    val miniGameCells: Set<Int> = setOf(7, 19, 28, 37, 46, 56, 66, 79)
}
