package com.mkz.rpg.battleUnit.usecases.commands

import com.mkz.rpg.battleUnit.domain.BattleUnitError.FailedToReceiveAbilityEffects
import com.mkz.rpg.battleUnit.usecases.services.BattleUnitDeployment
import com.mkz.rpg.effect.domain.Effect.Dto.EffectApplicationDto
import com.mkz.rpg.effect.domain.Effect.Dto.EffectApplicationDto.SourceDto
import com.mkz.rpg.effect.domain.Effect.Dto.RecipientDto
import com.mkz.rpg.effect.domain.Effect.Dto.RecipientDto.TypeDto.BATTLE_UNIT
import com.mkz.rpg.effect.domain.Effect.Dto.RecipientDto.TypeDto.POSITION
import com.mkz.rpg.effect.domain.EffectMother.damageEffect
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
            battleUnitDeployment =
                BattleUnitDeployment(
                    queries.battleUnitRepository,
                    queries.searchUnitById,
                    queries.searchMaximumAbilities,
                    queries.searchPlayerById,
                    queries.isTileVacant,
                ),
            searchAbilityById = queries.searchAbilityById,
        )

    @Test
    fun `should fail when the battle unit recipient has no battle unit id`() {
        // Given
        queries.effectRepository.create(damageEffect(id = "poison"))
        val recipient = RecipientDto(type = BATTLE_UNIT, battleUnitId = null, position = null)
        // When
        val result = runCatching { applyEffect(application(recipient)) }
        // Then
        result.exceptionOrNull().shouldBeTypeOf<FailedToReceiveAbilityEffects>()
        eventBus.shouldNotHavePublishedEvents()
    }

    @Test
    fun `should fail when the position recipient has no position`() {
        // Given
        queries.effectRepository.create(damageEffect(id = "poison"))
        val recipient = RecipientDto(type = POSITION, battleUnitId = "rat-1", position = null)
        // When
        val result = runCatching { applyEffect(application(recipient)) }
        // Then
        result.exceptionOrNull().shouldBeTypeOf<FailedToReceiveAbilityEffects>()
        eventBus.shouldNotHavePublishedEvents()
    }

    private fun application(recipient: RecipientDto) = EffectApplicationDto(source = SourceDto.battleUnit("knight-1"), recipient = recipient, effectId = "poison", destination = null)
}
