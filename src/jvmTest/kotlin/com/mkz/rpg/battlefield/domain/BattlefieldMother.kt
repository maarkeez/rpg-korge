package com.mkz.rpg.battlefield.domain

import kotlin.random.Random

object BattlefieldMother {
    fun battlefield(
        rows: Int = 3,
        columns: Int = 3,
    ): Battlefield = battlefield(rows, columns, List(rows) { List(columns) { "sand" } })

    fun battlefield(
        rows: Int,
        columns: Int,
        tiles: List<List<String>>,
    ): Battlefield = Battlefield.create(rows, columns, tiles, terrainOccupancy(tiles)).pullEvents().second

    fun terrainId() = listOf("sand", "void").random()

    fun position(
        row: Int = Random.nextInt(0, 100),
        column: Int = Random.nextInt(0, 100),
    ) = Battlefield.Dto.PositionDto(row = row, column = column)

    fun terrainOccupancy(tiles: List<List<String>>): Map<String, Boolean> =
        tiles
            .flatten()
            .distinct()
            .associateWith { terrainId ->
                when (terrainId) {
                    "void" -> false
                    else -> true
                }
            }
}
