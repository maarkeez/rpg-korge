package com.mkz.rpg.battleUnit.adapters.events

import com.mkz.rpg.ability.domain.Ability.Dto.TargetExpressionDto
import com.mkz.rpg.ability.domain.Ability.Dto.TargetingDto
import com.mkz.rpg.ability.domain.AbilityMother.ability
import com.mkz.rpg.ability.domain.AbilityMother.effectSpec
import com.mkz.rpg.battleUnit.domain.BattleUnitEvent
import com.mkz.rpg.battleUnit.domain.BattleUnitMother.battleUnit
import com.mkz.rpg.battleUnit.usecases.commands.RequestEffectApplicationToCastTargets
import com.mkz.rpg.battlefield.domain.Battlefield.Dto.PositionDto
import com.mkz.rpg.battlefield.domain.BattlefieldMother.battlefield
import com.mkz.rpg.effect.domain.EffectMother.increaseHealthEffect
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import com.mkz.rpg.shared.domain.subscribe
import com.mkz.rpg.shared.usecases.InMemoryQueries
import io.kotest.matchers.collections.shouldContainExactly
import kotlin.test.Test

class OnAbilityCastedTest {
    private val eventBus = InMemoryEventBus()
    private val queries = InMemoryQueries()
    private val onAbilityCasted =
        OnAbilityCasted(
            requestEffectApplicationToCastTargets =
                RequestEffectApplicationToCastTargets(queries.searchAbilityById, queries.battleUnitRepository, queries.abilityExecution, eventBus),
            eventBus = eventBus,
        )
    private val resolvedCasts = mutableListOf<BattleUnitEvent.AbilityCastResolved>().also { resolved -> eventBus.subscribe<BattleUnitEvent.AbilityCastResolved> { resolved += it } }

    @Test
    fun `should request the effects for the whole cast group when an ability is casted`() {
        // Given
        queries.effectRepository.create(increaseHealthEffect(id = "healing"))
        queries.abilityRepository.create(ability(id = "heal", targeting = TargetingDto.SELF, effectSpecs = listOf(effectSpec(effectId = "healing", target = TargetExpressionDto.Type.CASTER))))
        queries.battleUnitRepository.create(battleUnit(id = "knight-1"))
        queries.battlefieldRepository.create(battlefield("K .", occupants = mapOf('K' to "knight-1")))
        eventBus.publish(BattleUnitEvent.AbilityCasted(battleUnitId = "knight-1", abilityId = "heal", castGroup = listOf(PositionDto(row = 0, column = 0))))
        // When
        eventBus.dispatch()
        // Then
        resolvedCasts.shouldContainExactly(BattleUnitEvent.AbilityCastResolved(battleUnitId = "knight-1", abilityId = "heal"))
    }
}
