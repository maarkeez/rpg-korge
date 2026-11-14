package com.mkz.tessavar.battleUnit.adapters.events

import com.mkz.tessavar.battle.domain.BattleEvent
import com.mkz.tessavar.battleUnit.domain.BattleUnitMother.battleUnit
import com.mkz.tessavar.battleUnit.usecases.commands.ReplenishMana
import com.mkz.tessavar.battleUnit.usecases.commands.ResetBattleUnitActionsAndReduceCooldowns
import com.mkz.tessavar.battleUnit.usecases.commands.TriggerOnPlayerTurnStartedEffects
import com.mkz.tessavar.battlefield.domain.Battlefield.Dto.PositionDto
import com.mkz.tessavar.battlefield.domain.BattlefieldMother.battlefield
import com.mkz.tessavar.effect.domain.EffectMother.damageEffect
import com.mkz.tessavar.player.domain.PlayerMother.player
import com.mkz.tessavar.shared.adapters.events.InMemoryEventBus
import com.mkz.tessavar.shared.usecases.InMemoryQueries
import com.mkz.tessavar.unit.domain.UnitMother.unit
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class OnPlayerTurnStartedTest {
    private val eventBus = InMemoryEventBus()
    private val queries = InMemoryQueries()
    private val onPlayerTurnStarted =
        OnPlayerTurnStarted(
            resetBattleUnitActionsAndReduceCooldowns = ResetBattleUnitActionsAndReduceCooldowns(queries.battleUnitRepository),
            triggerOnPlayerTurnStartedEffects =
                TriggerOnPlayerTurnStartedEffects(
                    searchEffectById = queries.searchEffectById,
                    searchBattleUnitsByPlayerId = queries.searchBattleUnitsByPlayerId,
                    battleUnitRepository = queries.battleUnitRepository,
                    searchPosition = queries.searchPosition,
                    eventBus = eventBus,
                ),
            replenishMana = ReplenishMana(queries.battleUnitRepository),
            eventBus = eventBus,
        )

    @Test
    fun `should reset actions, trigger the on player turn started effects and replenish mana when a player turn starts`() {
        // Given
        val knight = unit(id = "knight", baseMaximumHealth = 20, baseMaximumMana = 30, startingAbilities = listOf("sword"))
        queries.unitRepository.create(knight)
        queries.effectRepository.create(damageEffect(id = "venom", damage = 3))
        queries.battlefieldRepository.create(battlefield("K .", occupants = mapOf('K' to "knight-1")))
        queries.battleUnitRepository.create(
            battleUnit(id = "knight-1", unit = knight.toDto(), player = player(id = "human").toDto())
                .castAbility(abilityId = "sword", abilityCooldown = 2, abilityManaCost = 15, castGroup = listOf(PositionDto(row = 0, column = 1)))
                .receiveOnPlayerTurnStartedEffect(effectId = "venom", remainingDuration = 2)
                .pullEvents()
                .second,
        )
        eventBus.publish(BattleEvent.PlayerTurnStarted(playerId = "human", battleUnitIds = setOf("knight-1")))
        // When
        eventBus.dispatch()
        // Then
        val updatedKnight = queries.battleUnitRepository.searchById("knight-1")!!.toDto()
        updatedKnight.remainingActions.remainingCasts shouldBe 1
        updatedKnight.knownAbilities["sword"] shouldBe 1
        updatedKnight.remainingHealth shouldBe 17
        updatedKnight.remainingMana shouldBe 25
    }
}
