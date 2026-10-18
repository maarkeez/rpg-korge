package com.mkz.rpg.ability.usecases.commands

import com.mkz.rpg.ability.adapters.storage.InMemoryAbilityRepository
import com.mkz.rpg.ability.domain.AbilityError.AbilityTurnsDurationMissing
import com.mkz.rpg.ability.domain.AbilityError.AbilityTurnsDurationNotAllowed
import com.mkz.rpg.ability.domain.AbilityEvent
import com.mkz.rpg.ability.domain.AbilityMother.ability
import com.mkz.rpg.ability.domain.AbilityMother.effectSpec
import com.mkz.rpg.effect.adapters.storage.InMemoryEffectRepository
import com.mkz.rpg.effect.domain.EffectMother.decreaseHealthEffect
import com.mkz.rpg.effect.domain.EffectMother.deployBattleUnitEffect
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
            abilityRepository = abilityRepository,
            searchEffectById = SearchEffectById(effectRepository),
            eventBus = eventBus,
        )

    @Test
    fun `should create ability`() {
        // Given
        val effect = decreaseHealthEffect()
        effectRepository.create(effect)
        val ability = ability(effectSpecs = listOf(effectSpec(effectId = effect.toDto().id))).toDto()
        // When
        requestAbilityCreation(abilityDto = ability)
        // Then
        val storedAbility = abilityRepository.searchById(ability.id)?.toDto()
        storedAbility shouldBe ability
        eventBus.shouldHavePublishedEvents(AbilityEvent.AbilityCreated(ability.id))
    }

    @Test
    fun `should create an ability deploying a battle unit with its turns duration`() {
        // Given
        val effect = deployBattleUnitEffect()
        effectRepository.create(effect)
        val ability = ability(effectSpecs = listOf(effectSpec(effectId = effect.toDto().id)), turnsDuration = 5).toDto()
        // When
        requestAbilityCreation(abilityDto = ability)
        // Then
        abilityRepository.searchById(ability.id)?.toDto()?.turnsDuration shouldBe 5
    }

    @Test
    fun `should fail when an ability deploying a battle unit has no turns duration`() {
        // Given
        val effect = deployBattleUnitEffect()
        effectRepository.create(effect)
        val ability = ability(effectSpecs = listOf(effectSpec(effectId = effect.toDto().id)), turnsDuration = null).toDto()
        // When
        val creation = { requestAbilityCreation(abilityDto = ability) }
        // Then
        shouldThrow<AbilityTurnsDurationMissing> { creation() }
        abilityRepository.searchById(ability.id).shouldBeNull()
    }

    @Test
    fun `should fail when an ability deploying no battle unit has a turns duration`() {
        // Given
        val effect = decreaseHealthEffect()
        effectRepository.create(effect)
        val ability = ability(effectSpecs = listOf(effectSpec(effectId = effect.toDto().id)), turnsDuration = 5).toDto()
        // When
        val creation = { requestAbilityCreation(abilityDto = ability) }
        // Then
        shouldThrow<AbilityTurnsDurationNotAllowed> { creation() }
        abilityRepository.searchById(ability.id).shouldBeNull()
    }
}
