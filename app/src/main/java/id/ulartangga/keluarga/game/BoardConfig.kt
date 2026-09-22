package id.ulartangga.keluarga.game

object BoardConfig {
    const val COLUMNS = 7
    const val ROWS = 14
    const val TOTAL_CELLS = COLUMNS * ROWS

    val ladders: Map<Int, Int> = mapOf(
        3 to 24, 9 to 28, 13 to 32, 19 to 38, 23 to 44,
        30 to 49, 34 to 53, 41 to 60, 52 to 71, 67 to 86, 76 to 95
    )

    val snakes: Map<Int, Int> = mapOf(
        17 to 5, 26 to 10, 37 to 18, 43 to 22, 54 to 33,
        61 to 40, 70 to 48, 79 to 57, 88 to 65, 96 to 75
    )

    val cardCells: Set<Int> = setOf(4, 11, 20, 29, 46, 58, 66, 77, 90)

    val mysteryCells: Set<Int> = setOf(6, 14, 21, 31, 47, 59, 68, 80, 91)

    val miniGameCells: Set<Int> = setOf(8, 16, 25, 36, 50, 64, 74, 84)
}
