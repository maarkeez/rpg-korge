package battleunit.usecases.commands

import battleunit.adapters.storage.*
import battleunit.domain.*
import battleunit.usecases.queries.*
import effect.domain.EffectMother
import effect.usecases.queries.*
import org.junit.*
import org.mockito.kotlin.*
import player.domain.PlayerMother
import shared.domain.*
import unit.domain.UnitMother

class ApplyOnTurnStartedEffectsTest {
    private val searchEffectById: SearchEffectById = mock()
    private val searchBattleUnitsByPlayerId: SearchBattleUnitsByPlayerId = mock()
    private val battleUnitRepository = InMemoryBattleUnitRepository()
    private val eventBus = FakeEventBus()
    private val applyOnTurnStartedEffects = ApplyOnTurnStartedEffects(
        searchEffectById = searchEffectById,
        searchBattleUnitsByPlayerId = searchBattleUnitsByPlayerId,
        battleUnitRepository = battleUnitRepository,
        eventBus = eventBus,
    )

    @Test
    fun `should apply delayed effect when the battle unit has a delayed ongoing effect`() {
        // Given
        val effectId = "effect-1"
        val effect = EffectMother.effect(id = effectId, power = 3, applicationType = "ON_TURN_STARTED").toDto()
        val unit = UnitMother.unit(healthPoints = 10).toDto()
        val player = PlayerMother.player(id = "player-1").toDto()
        val battleUnit = BattleUnitMother.battleUnit(unit = unit, player = player)
            .receiveDelayedEffect(effectId = effectId, turnsLeft = 1)
            .pullEvents().second
        battleUnitRepository.create(battleUnit)
        whenever(searchBattleUnitsByPlayerId("player-1")).thenReturn(listOf(battleUnit.toDto()))
        whenever(searchEffectById(effectId)).thenReturn(effect)
        // When
        applyOnTurnStartedEffects("player-1")
        // Then
        val storedBattleUnit = battleUnitRepository.searchById(battleUnit.toDto().id)?.toDto()
        org.assertj.core.api.Assertions.assertThat(storedBattleUnit!!.remainingHealthPoints).isEqualTo(7)
        assertThat(eventBus).hasPublishedEvents(BattleUnitEvent.BattleUnitDamaged(battleUnit.toDto().id))
    }

    @Test
    fun `should not apply any effect when the player has no battle units`() {
        // Given
        whenever(searchBattleUnitsByPlayerId("player-1")).thenReturn(emptyList())
        // When
        applyOnTurnStartedEffects("player-1")
        // Then
        org.assertj.core.api.Assertions.assertThat(eventBus.publishedEvents).isEmpty()
    }

    @Test
    fun `should not apply any effect when the battle unit has no delayed ongoing effects`() {
        // Given
        val battleUnit = BattleUnitMother.battleUnit()
        battleUnitRepository.create(battleUnit)
        whenever(searchBattleUnitsByPlayerId("player-1")).thenReturn(listOf(battleUnit.toDto()))
        // When
        applyOnTurnStartedEffects("player-1")
        // Then
        org.assertj.core.api.Assertions.assertThat(eventBus.publishedEvents).isEmpty()
    }
}
