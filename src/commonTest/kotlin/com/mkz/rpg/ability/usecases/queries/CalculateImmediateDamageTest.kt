package com.mkz.rpg.ability.usecases.queries

import com.mkz.rpg.ability.adapters.storage.InMemoryAbilityRepository
import com.mkz.rpg.ability.domain.AbilityMother.ability
import com.mkz.rpg.ability.domain.AbilityMother.effectSpec
import com.mkz.rpg.effect.adapters.storage.InMemoryEffectRepository
import com.mkz.rpg.effect.domain.EffectMother.decreaseHealthEffect
import com.mkz.rpg.effect.usecases.queries.SearchEffectById
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class CalculateImmediateDamageTest {
    private val abilityRepository = InMemoryAbilityRepository()
    private val effectRepository = InMemoryEffectRepository()
    private val calculateImmediateDamage =
        CalculateImmediateDamage(
            abilityRepository = abilityRepository,
            searchEffectById = SearchEffectById(effectRepository),
        )

    @Test
    fun `should sum power of immediately applied effects when ability has a mix of immediate and non immediate effects`() {
        // Given
        val immediateEffect = decreaseHealthEffect(id = "effect-1", damage = 4, applicationType = "IMMEDIATELY").also(effectRepository::create).toDto()
        val onTurnStartedEffect = decreaseHealthEffect(id = "effect-2", damage = 10).also(effectRepository::create).toDto()
        val ability =
            ability(
                effectSpecs =
                    listOf(
                        effectSpec(effectId = immediateEffect.id),
                        effectSpec(effectId = onTurnStartedEffect.id),
                    ),
            )
        val abilityDto = ability.toDto()
        abilityRepository.create(ability)
        // When
        val damage = calculateImmediateDamage(abilityDto.id)
        // Then
        damage shouldBe 4
    }

    @Test
    fun `should return zero when ability does not exist`() {
        // Given
        val unknownAbilityId =
            com.mkz.rpg.ability.domain.AbilityMother
                .id()
        // When
        val damage = calculateImmediateDamage(unknownAbilityId)
        // Then
        damage shouldBe 0
    }
}
