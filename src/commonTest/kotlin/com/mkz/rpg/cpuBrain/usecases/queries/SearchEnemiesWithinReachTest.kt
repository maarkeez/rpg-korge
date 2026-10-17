package com.mkz.rpg.cpuBrain.usecases.queries

import com.mkz.rpg.battleUnit.domain.BattleUnit
import com.mkz.rpg.battleUnit.domain.BattleUnitMother.battleUnit
import com.mkz.rpg.battlefield.domain.BattlefieldMother.battlefield
import com.mkz.rpg.player.domain.Player
import com.mkz.rpg.player.domain.PlayerMother.player
import com.mkz.rpg.shared.usecases.InMemoryQueries
import com.mkz.rpg.unit.domain.UnitMother.unit
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import kotlin.test.Test

class SearchEnemiesWithinReachTest {
    private val queries = InMemoryQueries()
    private val searchEnemiesWithinReach = queries.searchEnemiesWithinReach

    private val cpu = player(id = "cpu", type = Player.Dto.PlayerTypeDto.CPU).also(queries.playerRepository::create).toDto()
    private val human = player(id = "human").also(queries.playerRepository::create).toDto()
    private val knightUnit = unit(id = "knight").also(queries.unitRepository::create).toDto()
    private val occupants = mapOf('R' to "rat", 'K' to "knight-1", 'L' to "knight-2", 'A' to "ally")

    init {
        queries.battleUnitRepository.create(battleUnit(id = "rat", unit = knightUnit, player = cpu))
        queries.battleUnitRepository.create(battleUnit(id = "knight-1", unit = knightUnit, player = human))
        queries.battleUnitRepository.create(battleUnit(id = "knight-2", unit = knightUnit, player = human))
        queries.battleUnitRepository.create(battleUnit(id = "ally", unit = knightUnit, player = cpu))
    }

    private fun enemyIdsWithinReach(map: String): List<String> {
        queries.battlefieldRepository.create(battlefield(map, occupants))
        return searchEnemiesWithinReach(battleUnitId = "rat").map(BattleUnit.Dto::id)
    }

    @Test
    fun `should return the enemy when it is walked to in 3 tiles`() {
        // When
        val enemyIds = enemyIdsWithinReach("R . . K")
        // Then
        enemyIds.shouldContainExactly("knight-1")
    }

    @Test
    fun `should not return the enemy when it is walked to in 4 tiles`() {
        // When
        val enemyIds = enemyIdsWithinReach("R . . . K")
        // Then
        enemyIds.shouldBeEmpty()
    }

    @Test
    fun `should not return the enemy when the tiles walked around a wall are more than 3`() {
        // When
        val enemyIds =
            enemyIdsWithinReach(
                """
                R # K
                . # .
                . . .
                """,
            )
        // Then
        enemyIds.shouldBeEmpty()
    }

    @Test
    fun `should only return the enemies within reach when some are out of reach`() {
        // When
        val enemyIds = enemyIdsWithinReach("K . R . . . L")
        // Then
        enemyIds.shouldContainExactly("knight-1")
    }

    @Test
    fun `should not return the allies when they are next to the battle unit`() {
        // When
        val enemyIds = enemyIdsWithinReach("R A")
        // Then
        enemyIds.shouldBeEmpty()
    }
}
