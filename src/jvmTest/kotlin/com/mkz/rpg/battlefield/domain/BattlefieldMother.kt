package com.mkz.rpg.battlefield.domain

import kotlin.random.Random

object BattlefieldMother {
    fun battlefield(
        rows: Int = 3,
        columns: Int = 3,
    ): Battlefield = Battlefield.create(rows, columns, tileMatrix(rows, columns)).pullEvents().second

    fun terrainId() = listOf("sand", "void").random()

    private fun tileMatrix(
        rows: Int,
        columns: Int,
    ): List<List<String>> = List(rows) { List(columns) { terrainId() } }

    fun position(
        row: Int = Random.nextInt(0, 100),
        column: Int = Random.nextInt(0, 100),
    ) = Battlefield.Dto.PositionDto(row = row, column = column)
}
