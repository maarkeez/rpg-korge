package com.mkz.rpg.ability.usecases.commands

import com.mkz.rpg.ability.adapters.storage.InMemoryAbilityRepository
import com.mkz.rpg.ability.domain.AbilityError.AbilitySummonDurationMissing
import com.mkz.rpg.ability.domain.AbilityError.AbilitySummonDurationNotAllowed
import com.mkz.rpg.ability.domain.AbilityEvent
import com.mkz.rpg.ability.domain.AbilityMother.ability
import com.mkz.rpg.ability.domain.AbilityMother.abilityEffect
import com.mkz.rpg.ability.usecases.services.AbilityCreation
import com.mkz.rpg.effect.adapters.storage.InMemoryEffectRepository
import com.mkz.rpg.effect.domain.EffectMother.damageEffect
import com.mkz.rpg.effect.domain.EffectMother.summonEffect
import com.mkz.rpg.effect.usecases.queries.SearchEffectById
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class RequestAbilityCreationTest {
    private val abilityRepository = InMemoryAbilityRepository()
    private val effectRepository = InMemoryEffectRepository()
    private val eventBus = FakeEventBus()
    private val requestAbilityCreation =
        RequestAbilityCreation(
            abilityCreation = AbilityCreation(abilityRepository, SearchEffectById(effectRepository)),
            eventBus = eventBus,
        )

    @Test
    fun `should create ability`() {
        // Given
        val effect = damageEffect()
        effectRepository.create(effect)
        val ability = ability(abilityEffects = listOf(abilityEffect(effectId = effect.toDto().id))).toDto()
        // When
        requestAbilityCreation(abilityDto = ability)
        // Then
        val storedAbility = abilityRepository.searchById(ability.id)?.toDto()
        storedAbility shouldBe ability
        eventBus.shouldHavePublishedEvents(AbilityEvent.AbilityCreated(ability.id))
    }

    @Test
    fun `should create an ability deploying a battle unit with its summon duration`() {
        // Given
        val effect = summonEffect()
        effectRepository.create(effect)
        val ability = ability(abilityEffects = listOf(abilityEffect(effectId = effect.toDto().id)), summonDuration = 5).toDto()
        // When
        requestAbilityCreation(abilityDto = ability)
        // Then
        abilityRepository.searchById(ability.id)?.toDto()?.summonDuration shouldBe 5
    }

    @Test
    fun `should fail when an ability deploying a battle unit has no summon duration`() {
        // Given
        val effect = summonEffect()
        effectRepository.create(effect)
        val ability = ability(abilityEffects = listOf(abilityEffect(effectId = effect.toDto().id)), summonDuration = null).toDto()
        // When
        val creation = { requestAbilityCreation(abilityDto = ability) }
        // Then
        shouldThrow<AbilitySummonDurationMissing> { creation() }
        abilityRepository.searchById(ability.id).shouldBeNull()
    }

    @Test
    fun `should fail when an ability deploying no battle unit has a summon duration`() {
        // Given
        val effect = damageEffect()
        effectRepository.create(effect)
        val ability = ability(abilityEffects = listOf(abilityEffect(effectId = effect.toDto().id)), summonDuration = 5).toDto()
        // When
        val creation = { requestAbilityCreation(abilityDto = ability) }
        // Then
        shouldThrow<AbilitySummonDurationNotAllowed> { creation() }
        abilityRepository.searchById(ability.id).shouldBeNull()
    }
}
