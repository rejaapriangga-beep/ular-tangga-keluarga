package id.ulartangga.keluarga.game

object BoardConfig {
    const val COLUMNS = 7
    const val ROWS = 10
    const val TOTAL_CELLS = COLUMNS * ROWS

    val ladders: Map<Int, Int> = mapOf(
        4 to 18, 11 to 25, 20 to 33, 29 to 43,
        39 to 53, 48 to 62
    )

    val snakes: Map<Int, Int> = mapOf(
        17 to 5, 27 to 14, 41 to 24, 51 to 36,
        61 to 45, 68 to 58
    )

    val cardCells: Set<Int> = setOf(7, 15, 23, 32, 44, 55, 65)

    val mysteryCells: Set<Int> = setOf(9, 19, 31, 42, 50, 64)

    val miniGameCells: Set<Int> = setOf(6, 21, 35, 46, 59)
}
