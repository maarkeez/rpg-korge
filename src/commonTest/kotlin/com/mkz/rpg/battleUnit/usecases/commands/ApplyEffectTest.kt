package com.mkz.rpg.battleUnit.usecases.commands

import com.mkz.rpg.battleUnit.domain.BattleUnitError.FailedToReceiveAbilityEffects
import com.mkz.rpg.battleUnit.usecases.services.BattleUnitDeployment
import com.mkz.rpg.effect.domain.Effect.Dto.EffectApplicationDto
import com.mkz.rpg.effect.domain.Effect.Dto.EffectApplicationDto.ApplicationSourceDto
import com.mkz.rpg.effect.domain.Effect.Dto.EffectTargetDto
import com.mkz.rpg.effect.domain.Effect.Dto.EffectTargetDto.TypeDto.BATTLE_UNIT
import com.mkz.rpg.effect.domain.Effect.Dto.EffectTargetDto.TypeDto.POSITION
import com.mkz.rpg.effect.domain.EffectMother.decreaseHealthEffect
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldNotHavePublishedEvents
import com.mkz.rpg.shared.usecases.InMemoryQueries
import io.kotest.matchers.types.shouldBeTypeOf
import kotlin.test.Test

class ApplyEffectTest {
    private val queries = InMemoryQueries()
    private val eventBus = FakeEventBus()
    private val applyEffect =
        ApplyEffect(
            battleUnitRepository = queries.battleUnitRepository,
            eventBus = eventBus,
            searchEffectById = queries.searchEffectById,
            searchPosition = queries.searchPosition,
            battleUnitDeployment = BattleUnitDeployment(queries.battleUnitRepository, queries.searchUnitById, queries.searchPlayerById, queries.canBattlefieldTileBeOccupied),
            searchAbilityById = queries.searchAbilityById,
        )

    @Test
    fun `should fail when the battle unit target has no battle unit id`() {
        // Given
        queries.effectRepository.create(decreaseHealthEffect(id = "poison"))
        val target = EffectTargetDto(type = BATTLE_UNIT, battleUnitId = null, position = null)
        // When
        val result = runCatching { applyEffect(application(target)) }
        // Then
        result.exceptionOrNull().shouldBeTypeOf<FailedToReceiveAbilityEffects>()
        eventBus.shouldNotHavePublishedEvents()
    }

    @Test
    fun `should fail when the position target has no position`() {
        // Given
        queries.effectRepository.create(decreaseHealthEffect(id = "poison"))
        val target = EffectTargetDto(type = POSITION, battleUnitId = "rat-1", position = null)
        // When
        val result = runCatching { applyEffect(application(target)) }
        // Then
        result.exceptionOrNull().shouldBeTypeOf<FailedToReceiveAbilityEffects>()
        eventBus.shouldNotHavePublishedEvents()
    }

    private fun application(target: EffectTargetDto) = EffectApplicationDto(source = ApplicationSourceDto.battleUnit("knight-1"), target = target, effectId = "poison", destination = null)
}
