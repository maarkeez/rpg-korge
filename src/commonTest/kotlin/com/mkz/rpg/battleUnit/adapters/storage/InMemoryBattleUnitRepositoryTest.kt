package com.mkz.rpg.battleUnit.adapters.storage

import com.mkz.rpg.battleUnit.domain.BattleUnitMother
import com.mkz.rpg.player.domain.PlayerMother
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class InMemoryBattleUnitRepositoryTest {
    private val battleUnitRepository = InMemoryBattleUnitRepository()

    @Test
    fun `Create should store the battle unit when the battle unit is created`() {
        // Given
        val battleUnit = BattleUnitMother.battleUnit()
        // When
        battleUnitRepository.create(battleUnit)
        // Then
        val storedBattleUnit = battleUnitRepository.searchById(battleUnit.toDto().id)
        storedBattleUnit shouldBe battleUnit
    }

    @Test
    fun `Update should replace the stored battle unit when the battle unit is updated`() {
        // Given
        val battleUnit = BattleUnitMother.battleUnit()
        battleUnitRepository.create(battleUnit)
        val updatedBattleUnit = battleUnit.resetActions()
        // When
        battleUnitRepository.update(updatedBattleUnit)
        // Then
        val storedBattleUnit = battleUnitRepository.searchById(battleUnit.toDto().id)
        storedBattleUnit shouldBe updatedBattleUnit
    }

    @Test
    fun `SearchById should return the battle unit when the battle unit is stored`() {
        // Given
        val battleUnit = BattleUnitMother.battleUnit()
        battleUnitRepository.create(battleUnit)
        // When
        val storedBattleUnit = battleUnitRepository.searchById(battleUnit.toDto().id)
        // Then
        storedBattleUnit shouldBe battleUnit
    }

    @Test
    fun `SearchById should return null when the battle unit is not stored`() {
        // When
        val storedBattleUnit = battleUnitRepository.searchById("battle-unit-unknown")
        // Then
        storedBattleUnit.shouldBeNull()
    }

    @Test
    fun `SearchByPlayerId should return the battle units of the player when other players have battle units stored`() {
        // Given
        val player = PlayerMother.player(id = "player-1").toDto()
        val otherPlayer = PlayerMother.player(id = "player-2").toDto()
        val playerBattleUnit = BattleUnitMother.battleUnit(player = player)
        val otherPlayerBattleUnit = BattleUnitMother.battleUnit(player = otherPlayer)
        battleUnitRepository.create(playerBattleUnit)
        battleUnitRepository.create(otherPlayerBattleUnit)
        // When
        val storedBattleUnits = battleUnitRepository.searchByPlayerId(playerId = player.id)
        // Then
        storedBattleUnits.shouldContainExactly(playerBattleUnit)
    }

    @Test
    fun `SearchByPlayerId should return an empty list when the player has no battle units stored`() {
        // Given
        val otherPlayer = PlayerMother.player(id = "player-2").toDto()
        battleUnitRepository.create(BattleUnitMother.battleUnit(player = otherPlayer))
        // When
        val storedBattleUnits = battleUnitRepository.searchByPlayerId(playerId = "player-1")
        // Then
        storedBattleUnits.shouldBeEmpty()
    }

    @Test
    fun `SearchAll should return all the stored battle units when battle units are stored`() {
        // Given
        val firstBattleUnit = BattleUnitMother.battleUnit()
        val secondBattleUnit = BattleUnitMother.battleUnit()
        battleUnitRepository.create(firstBattleUnit)
        battleUnitRepository.create(secondBattleUnit)
        // When
        val storedBattleUnits = battleUnitRepository.searchAll()
        // Then
        storedBattleUnits.shouldContainExactlyInAnyOrder(firstBattleUnit, secondBattleUnit)
    }

    @Test
    fun `SearchAll should return an empty list when no battle unit is stored`() {
        // When
        val storedBattleUnits = battleUnitRepository.searchAll()
        // Then
        storedBattleUnits.shouldBeEmpty()
    }

    @Test
    fun `Delete should remove the battle unit when it is stored`() {
        // Given
        val deletedBattleUnit = BattleUnitMother.battleUnit()
        val keptBattleUnit = BattleUnitMother.battleUnit()
        battleUnitRepository.create(deletedBattleUnit)
        battleUnitRepository.create(keptBattleUnit)
        // When
        battleUnitRepository.delete(battleUnitId = deletedBattleUnit.toDto().id)
        // Then
        battleUnitRepository.searchById(deletedBattleUnit.toDto().id) shouldBe null
        battleUnitRepository.searchAll().shouldContainExactlyInAnyOrder(keptBattleUnit)
    }
}
