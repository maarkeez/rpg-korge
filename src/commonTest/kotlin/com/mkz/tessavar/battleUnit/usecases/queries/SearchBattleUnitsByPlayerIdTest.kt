package com.mkz.tessavar.battleUnit.usecases.queries

import com.mkz.tessavar.battleUnit.adapters.storage.InMemoryBattleUnitRepository
import com.mkz.tessavar.battleUnit.domain.BattleUnitMother.battleUnit
import com.mkz.tessavar.player.domain.PlayerMother.player
import com.mkz.tessavar.unit.domain.UnitMother.unit
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import kotlin.test.Test

class SearchBattleUnitsByPlayerIdTest {
    private val battleUnitRepository = InMemoryBattleUnitRepository()
    private val searchBattleUnitsByPlayerId = SearchBattleUnitsByPlayerId(battleUnitRepository)

    @Test
    fun `should return battle units when the player has battle units`() {
        // Given
        val player =
            player(id = "player-1")
        val firstBattleUnit =
            battleUnit(player = player.toDto())
        val secondBattleUnit =
            battleUnit(player = player.toDto())
        battleUnitRepository.create(firstBattleUnit)
        battleUnitRepository.create(secondBattleUnit)
        // When
        val result = searchBattleUnitsByPlayerId("player-1")
        // Then
        result.shouldContainExactlyInAnyOrder(firstBattleUnit.toDto(), secondBattleUnit.toDto())
    }

    @Test
    fun `should not return defeated battle units when the player has defeated battle units`() {
        // Given
        val player = player(id = "player-1")
        val aliveBattleUnit =
            battleUnit(
                player = player.toDto(),
                unit = unit(baseMaximumHealth = 10).toDto(),
            )
        val defeatedBattleUnit =
            battleUnit(
                player = player.toDto(),
                unit = unit(baseMaximumHealth = 0).toDto(),
            )
        battleUnitRepository.create(aliveBattleUnit)
        battleUnitRepository.create(defeatedBattleUnit)
        // When
        val result = searchBattleUnitsByPlayerId("player-1")
        // Then
        result.shouldContainExactly(aliveBattleUnit.toDto())
    }

    @Test
    fun `should return empty list when the player has no battle units`() {
        // Given
        // When
        val result = searchBattleUnitsByPlayerId("player-1")
        // Then
        result.shouldBeEmpty()
    }
}
