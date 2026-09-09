package battleunit.usecases.commands

import battleunit.adapters.storage.InMemoryBattleUnitRepository
import battleunit.domain.BattleUnitMother
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import player.domain.PlayerMother
import unit.domain.UnitMother
import unit.usecases.queries.SearchUnitById

class ReplenishManaTest {
    private val battleUnitRepository = InMemoryBattleUnitRepository()
    private val searchUnitById: SearchUnitById = mock()
    private val replenishMana = ReplenishMana(battleUnitRepository, searchUnitById)

    @Test
    fun `should replenish mana when the battle unit has mana below the maximum`() {
        // Given
        val unit =
            _root_ide_package_.unit.domain.UnitMother
                .unit(manaPoints = 20, abilities = listOf("ability-1"))
                .toDto()
        val player =
            _root_ide_package_.player.domain.PlayerMother
                .player(id = "player-1")
                .toDto()
        val depletedBattleUnit =
            _root_ide_package_.battleunit.domain.BattleUnitMother
                .battleUnit(unit = unit, player = player)
                .castAbility(abilityId = "ability-1", abilityCooldown = 0, abilityCost = 15, row = 0, column = 0)
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
        val unit =
            _root_ide_package_.unit.domain.UnitMother
                .unit(manaPoints = 12, abilities = listOf("ability-1"))
                .toDto()
        val player =
            _root_ide_package_.player.domain.PlayerMother
                .player(id = "player-1")
                .toDto()
        val depletedBattleUnit =
            _root_ide_package_.battleunit.domain.BattleUnitMother
                .battleUnit(unit = unit, player = player)
                .castAbility(abilityId = "ability-1", abilityCooldown = 0, abilityCost = 5, row = 0, column = 0)
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
