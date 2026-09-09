package battleunit.usecases.commands

import battlefield.usecases.queries.CanBattlefieldTileBeOccupied
import battleunit.adapters.storage.InMemoryBattleUnitRepository
import battleunit.domain.BattleUnitError
import battleunit.domain.BattleUnitEvent
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import player.domain.PlayerMother
import player.usecases.queries.SearchPlayerById
import shared.domain.FakeEventBus
import shared.domain.assertThat
import unit.domain.UnitMother
import unit.usecases.queries.SearchUnitById

class DeployBattleUnitTest {
    private val battleUnitRepository = InMemoryBattleUnitRepository()
    private val eventBus = _root_ide_package_.shared.domain.FakeEventBus()
    private val searchUnitById: SearchUnitById = mock()
    private val searchPlayerById: SearchPlayerById = mock()
    private val canBattlefieldTileBeOccupied: CanBattlefieldTileBeOccupied = mock()
    private val deployBattleUnit =
        DeployBattleUnit(
            battleUnitRepository = battleUnitRepository,
            eventBus = eventBus,
            searchUnitById = searchUnitById,
            searchPlayerById = searchPlayerById,
            canBattlefieldTileBeOccupied = canBattlefieldTileBeOccupied,
        )

    @Test
    fun `should deploy battle unit when the battlefield tile can be occupied`() {
        // Given
        val unit =
            _root_ide_package_.unit.domain.UnitMother
                .unit()
                .toDto()
        val player =
            _root_ide_package_.player.domain.PlayerMother
                .player()
                .toDto()
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
        assertThat(storedBattleUnit).isNotNull
        assertThat(storedBattleUnit!!.playerId).isEqualTo(player.id)
        assertThat(storedBattleUnit.unitId).isEqualTo(unit.id)
        assertThat(storedBattleUnit.remainingHealthPoints).isEqualTo(unit.healthPoints)
        _root_ide_package_.shared.domain.assertThat(eventBus).hasPublishedEvents(
            BattleUnitEvent.BattleUnitDeployed("battle-unit-1", 0, 0),
        )
    }

    @Test
    fun `should throw unit not found when the unit does not exist`() {
        // Given
        whenever(searchUnitById("unknown-unit")).thenReturn(null)
        // When
        val error =
            org.assertj.core.api.Assertions.catchThrowable {
                deployBattleUnit("battle-unit-1", "unknown-unit", "player-1", 0, 0)
            }
        // Then
        assertThat(error).isInstanceOf(BattleUnitError.UnitNotFound::class.java)
    }

    @Test
    fun `should throw player not found when the player does not exist`() {
        // Given
        val unit =
            _root_ide_package_.unit.domain.UnitMother
                .unit()
                .toDto()
        whenever(searchUnitById(unit.id)).thenReturn(unit)
        whenever(searchPlayerById("unknown-player")).thenReturn(null)
        // When
        val error =
            org.assertj.core.api.Assertions.catchThrowable {
                deployBattleUnit("battle-unit-1", unit.id, "unknown-player", 0, 0)
            }
        // Then
        assertThat(error).isInstanceOf(BattleUnitError.PlayerNotFound::class.java)
    }

    @Test
    fun `should throw tile error when the battlefield tile can not be occupied`() {
        // Given
        val unit =
            _root_ide_package_.unit.domain.UnitMother
                .unit()
                .toDto()
        val player =
            _root_ide_package_.player.domain.PlayerMother
                .player()
                .toDto()
        whenever(searchUnitById(unit.id)).thenReturn(unit)
        whenever(searchPlayerById(player.id)).thenReturn(player)
        whenever(canBattlefieldTileBeOccupied(0, 0)).thenReturn(false)
        // When
        val error =
            org.assertj.core.api.Assertions.catchThrowable {
                deployBattleUnit("battle-unit-1", unit.id, player.id, 0, 0)
            }
        // Then
        assertThat(error)
            .isInstanceOf(BattleUnitError.BattlefieldTileCanNotBeOccupied::class.java)
    }
}
