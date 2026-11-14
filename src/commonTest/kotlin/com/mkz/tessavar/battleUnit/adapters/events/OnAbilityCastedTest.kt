package com.mkz.tessavar.battleUnit.adapters.events

import com.mkz.tessavar.ability.domain.Ability.Dto.EffectTargetDto
import com.mkz.tessavar.ability.domain.Ability.Dto.TargetingDto
import com.mkz.tessavar.ability.domain.AbilityMother.ability
import com.mkz.tessavar.ability.domain.AbilityMother.abilityEffect
import com.mkz.tessavar.battleUnit.domain.BattleUnitEvent
import com.mkz.tessavar.battleUnit.domain.BattleUnitMother.battleUnit
import com.mkz.tessavar.battleUnit.usecases.commands.RequestEffectApplicationToCastTargets
import com.mkz.tessavar.battlefield.domain.Battlefield.Dto.PositionDto
import com.mkz.tessavar.battlefield.domain.BattlefieldMother.battlefield
import com.mkz.tessavar.effect.domain.EffectMother.healEffect
import com.mkz.tessavar.shared.adapters.events.InMemoryEventBus
import com.mkz.tessavar.shared.domain.subscribe
import com.mkz.tessavar.shared.usecases.InMemoryQueries
import io.kotest.matchers.collections.shouldContainExactly
import kotlin.test.Test

class OnAbilityCastedTest {
    private val eventBus = InMemoryEventBus()
    private val queries = InMemoryQueries()
    private val onAbilityCasted =
        OnAbilityCasted(
            requestEffectApplicationToCastTargets =
                RequestEffectApplicationToCastTargets(queries.searchAbilityById, queries.battleUnitRepository, queries.castResolution, eventBus),
            eventBus = eventBus,
        )
    private val resolvedCasts = mutableListOf<BattleUnitEvent.AbilityCastResolved>().also { resolved -> eventBus.subscribe<BattleUnitEvent.AbilityCastResolved> { resolved += it } }

    @Test
    fun `should request the effects for the whole cast group when an ability is casted`() {
        // Given
        queries.effectRepository.create(healEffect(id = "healing"))
        queries.abilityRepository.create(ability(id = "heal", targeting = TargetingDto.CASTER, abilityEffects = listOf(abilityEffect(effectId = "healing", target = EffectTargetDto.Type.CASTER))))
        queries.battleUnitRepository.create(battleUnit(id = "knight-1"))
        queries.battlefieldRepository.create(battlefield("K .", occupants = mapOf('K' to "knight-1")))
        eventBus.publish(BattleUnitEvent.AbilityCasted(battleUnitId = "knight-1", abilityId = "heal", castGroup = listOf(PositionDto(row = 0, column = 0))))
        // When
        eventBus.dispatch()
        // Then
        resolvedCasts.shouldContainExactly(BattleUnitEvent.AbilityCastResolved(battleUnitId = "knight-1", abilityId = "heal"))
    }
}
