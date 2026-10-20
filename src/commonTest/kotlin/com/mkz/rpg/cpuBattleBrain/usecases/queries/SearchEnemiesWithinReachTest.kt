package com.mkz.rpg.cpuBattleBrain.usecases.queries

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
    private val occupants = mapOf('R' to "rat", 'K' to "knight-1", 'L' to "knight-2", 'A' to "ally", 'B' to "bat")

    init {
        queries.battleUnitRepository.create(battleUnit(id = "rat", unit = knightUnit, player = cpu))
        queries.battleUnitRepository.create(battleUnit(id = "knight-1", unit = knightUnit, player = human))
        queries.battleUnitRepository.create(battleUnit(id = "knight-2", unit = knightUnit, player = human))
        queries.battleUnitRepository.create(battleUnit(id = "ally", unit = knightUnit, player = cpu))
        queries.battleUnitRepository.create(battleUnit(id = "bat", unit = knightUnit, player = cpu, remainingTurns = 5))
    }

    private fun enemyIdsWithinReach(
        map: String,
        battleUnitId: String = "rat",
    ): List<String> {
        queries.battlefieldRepository.create(battlefield(map, occupants))
        return searchEnemiesWithinReach(battleUnitId = battleUnitId).map(BattleUnit.Dto::id)
    }

    @Test
    fun `should return the enemy when it is walked to in 5 tiles`() {
        // When
        val enemyIds = enemyIdsWithinReach("R . . . . K")
        // Then
        enemyIds.shouldContainExactly("knight-1")
    }

    @Test
    fun `should not return the enemy when it is walked to in 6 tiles`() {
        // When
        val enemyIds = enemyIdsWithinReach("R . . . . . K")
        // Then
        enemyIds.shouldBeEmpty()
    }

    @Test
    fun `should not return the enemy when the tiles walked around a wall are more than 5`() {
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
        val enemyIds = enemyIdsWithinReach("K . R . . . . . L")
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

    @Test
    fun `should return the enemies a summon walks to, however far`() {
        // When
        val enemyIds = enemyIdsWithinReach("K . . . . . . . B . . . . . . . . . . L", battleUnitId = "bat")
        // Then
        enemyIds.shouldContainExactly("knight-1", "knight-2")
    }

    @Test
    fun `should not return the enemies a summon can not walk to`() {
        // When
        val enemyIds = enemyIdsWithinReach("B . # . K", battleUnitId = "bat")
        // Then
        enemyIds.shouldBeEmpty()
    }
}
