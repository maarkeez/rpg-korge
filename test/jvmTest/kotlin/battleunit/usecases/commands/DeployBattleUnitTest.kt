package battleunit.usecases.commands

import battlefield.usecases.queries.*
import battleunit.adapters.storage.*
import battleunit.domain.*
import org.junit.*
import org.mockito.kotlin.*
import player.domain.PlayerMother
import player.usecases.queries.*
import shared.domain.*
import unit.domain.UnitMother
import unit.usecases.queries.*

class DeployBattleUnitTest {
    private val battleUnitRepository = InMemoryBattleUnitRepository()
    private val eventBus = FakeEventBus()
    private val searchUnitById: SearchUnitById = mock()
    private val searchPlayerById: SearchPlayerById = mock()
    private val canBattlefieldTileBeOccupied: CanBattlefieldTileBeOccupied = mock()
    private val deployBattleUnit = DeployBattleUnit(
        battleUnitRepository = battleUnitRepository,
        eventBus = eventBus,
        searchUnitById = searchUnitById,
        searchPlayerById = searchPlayerById,
        canBattlefieldTileBeOccupied = canBattlefieldTileBeOccupied,
    )

    @Test
    fun `should deploy battle unit when the battlefield tile can be occupied`() {
        // Given
        val unit = UnitMother.unit().toDto()
        val player = PlayerMother.player().toDto()
        whenever(searchUnitById(unit.id)).thenReturn(unit)
        whenever(searchPlayerById(player.id)).thenReturn(player)
        whenever(canBattlefieldTileBeOccupied(0, 0)).thenReturn(true)
        // When
        deployBattleUnit(
            battleUnitId = "battle-unit-1",
            unitId = unit.id,
            playerId = player.id,
            deployAtRow = 0,
            deployAtColumn = 0,
        )
        // Then
        val storedBattleUnit = battleUnitRepository.searchById("battle-unit-1")?.toDto()
        org.assertj.core.api.Assertions.assertThat(storedBattleUnit).isNotNull
        org.assertj.core.api.Assertions.assertThat(storedBattleUnit!!.playerId).isEqualTo(player.id)
        org.assertj.core.api.Assertions.assertThat(storedBattleUnit.unitId).isEqualTo(unit.id)
        org.assertj.core.api.Assertions.assertThat(storedBattleUnit.remainingHealthPoints).isEqualTo(unit.healthPoints)
        assertThat(eventBus).hasPublishedEvents(
            BattleUnitEvent.BattleUnitDeployed("battle-unit-1", 0, 0)
        )
    }

    @Test
    fun `should throw unit not found when the unit does not exist`() {
        // Given
        whenever(searchUnitById("unknown-unit")).thenReturn(null)
        // When
        val error = org.assertj.core.api.Assertions.catchThrowable {
            deployBattleUnit("battle-unit-1", "unknown-unit", "player-1", 0, 0)
        }
        // Then
        org.assertj.core.api.Assertions.assertThat(error).isInstanceOf(BattleUnitError.UnitNotFound::class.java)
    }

    @Test
    fun `should throw player not found when the player does not exist`() {
        // Given
        val unit = UnitMother.unit().toDto()
        whenever(searchUnitById(unit.id)).thenReturn(unit)
        whenever(searchPlayerById("unknown-player")).thenReturn(null)
        // When
        val error = org.assertj.core.api.Assertions.catchThrowable {
            deployBattleUnit("battle-unit-1", unit.id, "unknown-player", 0, 0)
        }
        // Then
        org.assertj.core.api.Assertions.assertThat(error).isInstanceOf(BattleUnitError.PlayerNotFound::class.java)
    }

    @Test
    fun `should throw tile error when the battlefield tile can not be occupied`() {
        // Given
        val unit = UnitMother.unit().toDto()
        val player = PlayerMother.player().toDto()
        whenever(searchUnitById(unit.id)).thenReturn(unit)
        whenever(searchPlayerById(player.id)).thenReturn(player)
        whenever(canBattlefieldTileBeOccupied(0, 0)).thenReturn(false)
        // When
        val error = org.assertj.core.api.Assertions.catchThrowable {
            deployBattleUnit("battle-unit-1", unit.id, player.id, 0, 0)
        }
        // Then
        org.assertj.core.api.Assertions.assertThat(error)
            .isInstanceOf(BattleUnitError.BattlefieldTileCanNotBeOccupied::class.java)
    }
}
