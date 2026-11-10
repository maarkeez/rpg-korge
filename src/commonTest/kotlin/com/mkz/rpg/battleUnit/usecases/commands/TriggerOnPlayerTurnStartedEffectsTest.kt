package com.mkz.rpg.battleUnit.usecases.commands

import com.mkz.rpg.battleUnit.domain.BattleUnitEvent
import com.mkz.rpg.battleUnit.domain.BattleUnitMother.battleUnit
import com.mkz.rpg.battlefield.domain.BattlefieldMother.battlefield
import com.mkz.rpg.effect.domain.EffectMother.damageEffect
import com.mkz.rpg.player.domain.PlayerMother.player
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import com.mkz.rpg.shared.usecases.InMemoryQueries
import com.mkz.rpg.unit.domain.UnitMother.unit
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class TriggerOnPlayerTurnStartedEffectsTest {
    private val queries = InMemoryQueries()
    private val battleUnitRepository = queries.battleUnitRepository
    private val eventBus = FakeEventBus()
    private val triggerOnPlayerTurnStartedEffects =
        TriggerOnPlayerTurnStartedEffects(
            searchEffectById = queries.searchEffectById,
            searchBattleUnitsByPlayerId = queries.searchBattleUnitsByPlayerId,
            battleUnitRepository = battleUnitRepository,
            searchPosition = queries.searchPosition,
            eventBus = eventBus,
        )

    @Test
    fun `should trigger the on player turn started effect when the battle unit has one`() {
        // Given
        val effectId = "effect-1"
        queries.effectRepository.create(damageEffect(id = effectId, damage = 3, timingType = "ON_PLAYER_TURN_STARTED"))
        val unit = unit(baseMaximumHealth = 10).toDto()
        val player = player(id = "player-1").toDto()
        val battleUnit =
            battleUnit(id = "knight-1", unit = unit, player = player)
                .receiveOnPlayerTurnStartedEffect(effectId = effectId, remainingDuration = 1)
                .pullEvents()
                .second
        battleUnitRepository.create(battleUnit)
        queries.battlefieldRepository.create(battlefield("K .", occupants = mapOf('K' to "knight-1")))
        // When
        triggerOnPlayerTurnStartedEffects("player-1", setOf("knight-1"))
        // Then
        val storedBattleUnit = battleUnitRepository.searchById(battleUnit.toDto().id)?.toDto()
        storedBattleUnit!!.remainingHealth shouldBe 7
        eventBus.shouldHavePublishedEvents(
            BattleUnitEvent.OnPlayerTurnStartedEffectTriggered(battleUnitId = battleUnit.toDto().id, effectId = effectId),
            BattleUnitEvent.BattleUnitDamaged(battleUnit.toDto().id),
        )
    }

    @Test
    fun `should not trigger any effect when the player has no battle units`() {
        // Given
        // When
        triggerOnPlayerTurnStartedEffects("player-1", emptySet())
        // Then
        eventBus.publishedEvents.shouldBeEmpty()
    }

    @Test
    fun `should not trigger any effect when the battle unit has no on player turn started effects`() {
        // Given
        val battleUnit = battleUnit(player = player(id = "player-1").toDto())
        battleUnitRepository.create(battleUnit)
        // When
        triggerOnPlayerTurnStartedEffects("player-1", setOf(battleUnit.toDto().id))
        // Then
        eventBus.publishedEvents.shouldBeEmpty()
    }

    @Test
    fun `should not trigger the on player turn started effect when the battle unit is not in the battle`() {
        // Given
        val effectId = "effect-1"
        queries.effectRepository.create(damageEffect(id = effectId, damage = 3, timingType = "ON_PLAYER_TURN_STARTED"))
        val battleUnit =
            battleUnit(id = "knight-1", unit = unit(baseMaximumHealth = 10).toDto(), player = player(id = "player-1").toDto())
                .receiveOnPlayerTurnStartedEffect(effectId = effectId, remainingDuration = 1)
                .pullEvents()
                .second
        battleUnitRepository.create(battleUnit)
        queries.battlefieldRepository.create(battlefield("K .", occupants = mapOf('K' to "knight-1")))
        // When
        triggerOnPlayerTurnStartedEffects("player-1", setOf("another-battle-unit"))
        // Then
        battleUnitRepository.searchById("knight-1")!!.toDto().remainingHealth shouldBe 10
        eventBus.publishedEvents.shouldBeEmpty()
    }

    @Test
    fun `should publish one defeated event when two venoms finish off a battle unit with 3 health`() {
        // Given
        queries.effectRepository.create(damageEffect(id = "venom-damage", damage = 3, timingType = "ON_PLAYER_TURN_STARTED"))
        val battleUnit =
            battleUnit(id = "goat-wizard-1", unit = unit(baseMaximumHealth = 3).toDto(), player = player(id = "player-2").toDto())
                .receiveOnPlayerTurnStartedEffect(effectId = "venom-damage", remainingDuration = 5)
                .receiveOnPlayerTurnStartedEffect(effectId = "venom-damage", remainingDuration = 5)
                .pullEvents()
                .second
        battleUnitRepository.create(battleUnit)
        queries.battlefieldRepository.create(battlefield("G .", occupants = mapOf('G' to "goat-wizard-1")))
        // When
        triggerOnPlayerTurnStartedEffects("player-2", setOf("goat-wizard-1"))
        // Then
        battleUnitRepository.searchById("goat-wizard-1")!!.toDto().remainingHealth shouldBe 0
        eventBus.publishedEvents.filterIsInstance<BattleUnitEvent.BattleUnitDefeated>().size shouldBe 1
    }

    @Test
    fun `should deal the damage of both venoms when the battle unit has two of them`() {
        // Given
        queries.effectRepository.create(damageEffect(id = "venom-damage", damage = 3, timingType = "ON_PLAYER_TURN_STARTED"))
        val battleUnit =
            battleUnit(id = "knight-1", unit = unit(baseMaximumHealth = 10).toDto(), player = player(id = "player-1").toDto())
                .receiveOnPlayerTurnStartedEffect(effectId = "venom-damage", remainingDuration = 5)
                .receiveOnPlayerTurnStartedEffect(effectId = "venom-damage", remainingDuration = 5)
                .pullEvents()
                .second
        battleUnitRepository.create(battleUnit)
        queries.battlefieldRepository.create(battlefield("K .", occupants = mapOf('K' to "knight-1")))
        // When
        triggerOnPlayerTurnStartedEffects("player-1", setOf("knight-1"))
        // Then
        battleUnitRepository.searchById("knight-1")!!.toDto().remainingHealth shouldBe 4
    }
}
