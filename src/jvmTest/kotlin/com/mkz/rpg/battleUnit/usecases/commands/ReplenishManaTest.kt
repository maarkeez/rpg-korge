package com.mkz.rpg.battleUnit.usecases.commands

import com.mkz.rpg.battleUnit.adapters.storage.InMemoryBattleUnitRepository
import com.mkz.rpg.battlefield.domain.Battlefield
import com.mkz.rpg.player.domain.PlayerMother.player
import com.mkz.rpg.unit.domain.UnitMother.unit
import com.mkz.rpg.unit.usecases.queries.SearchUnitById
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class ReplenishManaTest {
    private val battleUnitRepository = InMemoryBattleUnitRepository()
    private val searchUnitById: SearchUnitById = mock()
    private val replenishMana = ReplenishMana(battleUnitRepository, searchUnitById)

    @Test
    fun `should replenish mana when the battle unit has mana below the maximum`() {
        // Given
        val unit =
            unit(manaPoints = 20, abilities = listOf("ability-1"))
                .toDto()
        val player =
            player(id = "player-1")
                .toDto()
        val depletedBattleUnit =
            com.mkz.rpg.battleUnit.domain.BattleUnitMother
                .battleUnit(unit = unit, player = player)
                .castAbility(abilityId = "ability-1", abilityCooldown = 0, abilityCost = 15, castGroup = listOf(Battlefield.Dto.PositionDto(0, 0)))
                .pullEvents()
                .second
        battleUnitRepository.create(depletedBattleUnit)
        whenever(searchUnitById(unit.id)).thenReturn(unit)
        // When
        replenishMana("player-1")
        // Then
        val storedBattleUnit = battleUnitRepository.searchById(depletedBattleUnit.toDto().id)?.toDto()
        assertThat(storedBattleUnit!!.remainingManaPoints).isEqualTo(15)
    }

    @Test
    fun `should not exceed maximum mana when replenishing`() {
        // Given
        val unit = unit(manaPoints = 12, abilities = listOf("ability-1")).toDto()
        val player = player(id = "player-1").toDto()
        val depletedBattleUnit =
            com.mkz.rpg.battleUnit.domain.BattleUnitMother
                .battleUnit(unit = unit, player = player)
                .castAbility(abilityId = "ability-1", abilityCooldown = 0, abilityCost = 5, castGroup = listOf(Battlefield.Dto.PositionDto(0, 0)))
                .pullEvents()
                .second
        battleUnitRepository.create(depletedBattleUnit)
        whenever(searchUnitById(unit.id)).thenReturn(unit)
        // When
        replenishMana("player-1")
        // Then
        val storedBattleUnit = battleUnitRepository.searchById(depletedBattleUnit.toDto().id)?.toDto()
        assertThat(storedBattleUnit!!.remainingManaPoints).isEqualTo(12)
    }

    @Test
    fun `should not update any battle unit when the player has no battle units`() {
        // Given
        // When
        replenishMana("player-1")
        // Then
        assertThat(battleUnitRepository.searchAll()).isEmpty()
    }
}
