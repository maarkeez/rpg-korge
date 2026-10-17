package com.mkz.rpg.battleUnit.usecases.commands

import com.mkz.rpg.battleUnit.domain.BattleUnitEvent
import com.mkz.rpg.battleUnit.domain.BattleUnitMother.battleUnit
import com.mkz.rpg.battlefield.domain.BattlefieldMother.battlefield
import com.mkz.rpg.effect.domain.EffectMother.decreaseHealthEffect
import com.mkz.rpg.player.domain.PlayerMother.player
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import com.mkz.rpg.shared.usecases.InMemoryQueries
import com.mkz.rpg.unit.domain.UnitMother.unit
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class ApplyOnTurnStartedEffectsTest {
    private val queries = InMemoryQueries()
    private val battleUnitRepository = queries.battleUnitRepository
    private val eventBus = FakeEventBus()
    private val applyOnTurnStartedEffects =
        ApplyOnTurnStartedEffects(
            searchEffectById = queries.searchEffectById,
            searchBattleUnitsByPlayerId = queries.searchBattleUnitsByPlayerId,
            battleUnitRepository = battleUnitRepository,
            searchPosition = queries.searchPosition,
            eventBus = eventBus,
        )

    @Test
    fun `should apply on turn started effect when the battle unit has a delayed ongoing effect`() {
        // Given
        val effectId = "effect-1"
        queries.effectRepository.create(decreaseHealthEffect(id = effectId, damage = 3, applicationType = "ON_TURN_STARTED"))
        val unit = unit(healthPoints = 10).toDto()
        val player = player(id = "player-1").toDto()
        val battleUnit =
            battleUnit(id = "knight-1", unit = unit, player = player)
                .receiveOnTurnStartedEffect(effectId = effectId, turnsLeft = 1)
                .pullEvents()
                .second
        battleUnitRepository.create(battleUnit)
        queries.battlefieldRepository.create(battlefield("K .", occupants = mapOf('K' to "knight-1")))
        // When
        applyOnTurnStartedEffects("player-1", setOf("knight-1"))
        // Then
        val storedBattleUnit = battleUnitRepository.searchById(battleUnit.toDto().id)?.toDto()
        storedBattleUnit!!.remainingHealthPoints shouldBe 7
        eventBus.shouldHavePublishedEvents(
            BattleUnitEvent.OnTurnStartedEffectApplied(battleUnitId = battleUnit.toDto().id, effectId = effectId),
            BattleUnitEvent.BattleUnitDamaged(battleUnit.toDto().id),
        )
    }

    @Test
    fun `should not apply any effect when the player has no battle units`() {
        // Given
        // When
        applyOnTurnStartedEffects("player-1", emptySet())
        // Then
        eventBus.publishedEvents.shouldBeEmpty()
    }

    @Test
    fun `should not apply any effect when the battle unit has no delayed ongoing effects`() {
        // Given
        val battleUnit = battleUnit(player = player(id = "player-1").toDto())
        battleUnitRepository.create(battleUnit)
        // When
        applyOnTurnStartedEffects("player-1", setOf(battleUnit.toDto().id))
        // Then
        eventBus.publishedEvents.shouldBeEmpty()
    }

    @Test
    fun `should not apply on turn started effect when the battle unit is not in the battle`() {
        // Given
        val effectId = "effect-1"
        queries.effectRepository.create(decreaseHealthEffect(id = effectId, damage = 3, applicationType = "ON_TURN_STARTED"))
        val battleUnit =
            battleUnit(id = "knight-1", unit = unit(healthPoints = 10).toDto(), player = player(id = "player-1").toDto())
                .receiveOnTurnStartedEffect(effectId = effectId, turnsLeft = 1)
                .pullEvents()
                .second
        battleUnitRepository.create(battleUnit)
        queries.battlefieldRepository.create(battlefield("K .", occupants = mapOf('K' to "knight-1")))
        // When
        applyOnTurnStartedEffects("player-1", setOf("another-battle-unit"))
        // Then
        battleUnitRepository.searchById("knight-1")!!.toDto().remainingHealthPoints shouldBe 10
        eventBus.publishedEvents.shouldBeEmpty()
    }
}
