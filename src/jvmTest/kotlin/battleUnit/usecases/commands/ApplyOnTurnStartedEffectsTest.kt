package battleUnit.usecases.commands

import battleUnit.adapters.storage.InMemoryBattleUnitRepository
import battleUnit.domain.BattleUnitEvent
import battleUnit.domain.BattleUnitMother.battleUnit
import battleUnit.usecases.queries.SearchBattleUnitsByPlayerId
import effect.domain.EffectMother.effect
import effect.usecases.queries.SearchEffectById
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import player.domain.PlayerMother.player
import shared.domain.FakeEventBus
import shared.domain.assertThat
import unit.domain.UnitMother.unit

class ApplyOnTurnStartedEffectsTest {
    private val searchEffectById: SearchEffectById = mock()
    private val searchBattleUnitsByPlayerId: SearchBattleUnitsByPlayerId = mock()
    private val battleUnitRepository = InMemoryBattleUnitRepository()
    private val eventBus = FakeEventBus()
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
        val effect = effect(id = effectId, power = 3, applicationType = "ON_TURN_STARTED").toDto()
        val unit = unit(healthPoints = 10).toDto()
        val player = player(id = "player-1").toDto()
        val battleUnit =
            battleUnit(unit = unit, player = player)
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
        assertThat(eventBus)
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
        val battleUnit = battleUnit()
        battleUnitRepository.create(battleUnit)
        whenever(searchBattleUnitsByPlayerId("player-1")).thenReturn(listOf(battleUnit.toDto()))
        // When
        applyOnTurnStartedEffects("player-1")
        // Then
        assertThat(eventBus.publishedEvents).isEmpty()
    }
}
