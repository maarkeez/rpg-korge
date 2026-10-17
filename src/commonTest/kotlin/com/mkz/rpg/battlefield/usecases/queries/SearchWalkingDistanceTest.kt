package com.mkz.rpg.battlefield.usecases.queries

import com.mkz.rpg.battlefield.domain.BattlefieldMother.battlefield
import com.mkz.rpg.battlefield.domain.BattlefieldMother.terrainTransitionRules
import com.mkz.rpg.shared.usecases.InMemoryQueries
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class SearchWalkingDistanceTest {
    private val queries = InMemoryQueries()
    private val searchWalkingDistance = queries.searchWalkingDistance

    private val occupants = mapOf('A' to "battle-unit-a", 'B' to "battle-unit-b", 'C' to "battle-unit-c")

    @Test
    fun `should return 1 when the destination is next to the origin`() {
        // Given
        queries.battlefieldRepository.create(battlefield("A B", occupants))
        // When
        val distance = searchWalkingDistance(fromRow = 0, fromColumn = 0, toRow = 0, toColumn = 1, maximumDistance = 3)
        // Then
        distance shouldBe 1
    }

    @Test
    fun `should count the destination when it is occupied and the tiles walked before it are free`() {
        // Given
        queries.battlefieldRepository.create(battlefield("A . . B", occupants))
        // When
        val distance = searchWalkingDistance(fromRow = 0, fromColumn = 0, toRow = 0, toColumn = 3, maximumDistance = 3)
        // Then
        distance shouldBe 3
    }

    @Test
    fun `should walk around the tiles that can not be occupied when they block the straight line`() {
        // Given
        queries.battlefieldRepository.create(
            battlefield(
                """
                A # B
                . . .
                """,
                occupants,
            ),
        )
        // When
        val distance = searchWalkingDistance(fromRow = 0, fromColumn = 0, toRow = 0, toColumn = 2, maximumDistance = 10)
        // Then
        distance shouldBe 4
    }

    @Test
    fun `should walk around the battle units when they block the straight line`() {
        // Given
        queries.battlefieldRepository.create(
            battlefield(
                """
                A C B
                . . .
                """,
                occupants,
            ),
        )
        // When
        val distance = searchWalkingDistance(fromRow = 0, fromColumn = 0, toRow = 0, toColumn = 2, maximumDistance = 10)
        // Then
        distance shouldBe 4
    }

    @Test
    fun `should walk through damaging terrain when it can be occupied`() {
        // Given
        val tiles = listOf(listOf("sand", "lava", "sand"))
        queries.battlefieldRepository.create(battlefield(rows = 1, columns = 3, tiles = tiles, terrainTransitionRules = terrainTransitionRules(tiles)))
        // When
        val distance = searchWalkingDistance(fromRow = 0, fromColumn = 0, toRow = 0, toColumn = 2, maximumDistance = 3)
        // Then
        distance shouldBe 2
    }

    @Test
    fun `should return null when the destination is further than the maximum distance`() {
        // Given
        queries.battlefieldRepository.create(battlefield("A . . . B", occupants))
        // When
        val distance = searchWalkingDistance(fromRow = 0, fromColumn = 0, toRow = 0, toColumn = 4, maximumDistance = 3)
        // Then
        distance.shouldBeNull()
    }

    @Test
    fun `should return null when the destination can only be reached walking over other battle units`() {
        // Given
        queries.battlefieldRepository.create(battlefield("A C B", occupants))
        // When
        val distance = searchWalkingDistance(fromRow = 0, fromColumn = 0, toRow = 0, toColumn = 2, maximumDistance = 10)
        // Then
        distance.shouldBeNull()
    }

    @Test
    fun `should return null when the destination can only be reached jumping over tiles that can not be occupied`() {
        // Given
        queries.battlefieldRepository.create(battlefield("A # B", occupants))
        // When
        val distance = searchWalkingDistance(fromRow = 0, fromColumn = 0, toRow = 0, toColumn = 2, maximumDistance = 10)
        // Then
        distance.shouldBeNull()
    }

    @Test
    fun `should return 0 when the destination is the origin`() {
        // Given
        queries.battlefieldRepository.create(battlefield("A .", occupants))
        // When
        val distance = searchWalkingDistance(fromRow = 0, fromColumn = 0, toRow = 0, toColumn = 0, maximumDistance = 3)
        // Then
        distance shouldBe 0
    }

    @Test
    fun `should walk over other battle units when walking through battle units`() {
        // Given
        queries.battlefieldRepository.create(battlefield("A C . B", occupants))
        // When
        val distance = searchWalkingDistance(fromRow = 0, fromColumn = 0, toRow = 0, toColumn = 3, maximumDistance = 10, throughBattleUnits = true)
        // Then
        distance shouldBe 3
    }

    @Test
    fun `should still walk around the tiles that can not be occupied when walking through battle units`() {
        // Given
        queries.battlefieldRepository.create(
            battlefield(
                """
                A # B
                . C .
                """,
                occupants,
            ),
        )
        // When
        val distance = searchWalkingDistance(fromRow = 0, fromColumn = 0, toRow = 0, toColumn = 2, maximumDistance = 10, throughBattleUnits = true)
        // Then
        distance shouldBe 4
    }

    @Test
    fun `should return null when the destination can only be reached through tiles that can not be occupied, walking through battle units`() {
        // Given
        queries.battlefieldRepository.create(battlefield("A # B", occupants))
        // When
        val distance = searchWalkingDistance(fromRow = 0, fromColumn = 0, toRow = 0, toColumn = 2, maximumDistance = 10, throughBattleUnits = true)
        // Then
        distance.shouldBeNull()
    }
}
