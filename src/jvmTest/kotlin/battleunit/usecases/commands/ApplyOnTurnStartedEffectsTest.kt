package battleunit.usecases.commands

import battleunit.adapters.storage.InMemoryBattleUnitRepository
import battleunit.domain.BattleUnitEvent
import battleunit.domain.BattleUnitMother
import battleunit.usecases.queries.SearchBattleUnitsByPlayerId
import effect.domain.EffectMother
import effect.usecases.queries.SearchEffectById
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import player.domain.PlayerMother
import shared.domain.FakeEventBus
import shared.domain.assertThat
import unit.domain.UnitMother

class ApplyOnTurnStartedEffectsTest {
    private val searchEffectById: SearchEffectById = mock()
    private val searchBattleUnitsByPlayerId: SearchBattleUnitsByPlayerId = mock()
    private val battleUnitRepository = InMemoryBattleUnitRepository()
    private val eventBus = _root_ide_package_.shared.domain.FakeEventBus()
    private val applyOnTurnStartedEffects =
        ApplyOnTurnStartedEffects(
            searchEffectById = searchEffectById,
            searchBattleUnitsByPlayerId = searchBattleUnitsByPlayerId,
            battleUnitRepository = battleUnitRepository,
            eventBus = eventBus,
        )

    @Test
    fun `should apply delayed effect when the battle unit has a delayed ongoing effect`() {
        // Given
        val effectId = "effect-1"
        val effect =
            _root_ide_package_.effect.domain.EffectMother
                .effect(id = effectId, power = 3, applicationType = "ON_TURN_STARTED")
                .toDto()
        val unit =
            _root_ide_package_.unit.domain.UnitMother
                .unit(healthPoints = 10)
                .toDto()
        val player =
            _root_ide_package_.player.domain.PlayerMother
                .player(id = "player-1")
                .toDto()
        val battleUnit =
            _root_ide_package_.battleunit.domain.BattleUnitMother
                .battleUnit(unit = unit, player = player)
                .receiveDelayedEffect(effectId = effectId, turnsLeft = 1)
                .pullEvents()
                .second
        battleUnitRepository.create(battleUnit)
        whenever(searchBattleUnitsByPlayerId("player-1")).thenReturn(listOf(battleUnit.toDto()))
        whenever(searchEffectById(effectId)).thenReturn(effect)
        // When
        applyOnTurnStartedEffects("player-1")
        // Then
        val storedBattleUnit = battleUnitRepository.searchById(battleUnit.toDto().id)?.toDto()
        assertThat(storedBattleUnit!!.remainingHealthPoints).isEqualTo(7)
        _root_ide_package_.shared.domain
            .assertThat(eventBus)
            .hasPublishedEvents(BattleUnitEvent.BattleUnitDamaged(battleUnit.toDto().id))
    }

    @Test
    fun `should not apply any effect when the player has no battle units`() {
        // Given
        whenever(searchBattleUnitsByPlayerId("player-1")).thenReturn(emptyList())
        // When
        applyOnTurnStartedEffects("player-1")
        // Then
        assertThat(eventBus.publishedEvents).isEmpty()
    }

    @Test
    fun `should not apply any effect when the battle unit has no delayed ongoing effects`() {
        // Given
        val battleUnit =
            _root_ide_package_.battleunit.domain.BattleUnitMother
                .battleUnit()
        battleUnitRepository.create(battleUnit)
        whenever(searchBattleUnitsByPlayerId("player-1")).thenReturn(listOf(battleUnit.toDto()))
        // When
        applyOnTurnStartedEffects("player-1")
        // Then
        assertThat(eventBus.publishedEvents).isEmpty()
    }
}
