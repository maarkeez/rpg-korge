package battlefield.domain

import kotlin.random.*

object BattlefieldMother {

    fun battlefield(
        rows: Int = 3,
        columns: Int = 3,
    ): Battlefield =
        Battlefield.create(rows, columns, tileMatrix(rows, columns)).pullEvents().second

    fun terrainId() = "terrain-${Random.nextInt(1, 100)}"

    private fun tileMatrix(rows: Int, columns: Int): List<List<String>> =
        List(rows) { List(columns) { terrainId() } }
}
