package com.mkz.rpg.ability.usecases.commands

import com.mkz.rpg.ability.adapters.storage.InMemoryAbilityRepository
import com.mkz.rpg.ability.domain.Ability
import com.mkz.rpg.ability.domain.AbilityError.AbilityEffectDoesNotExist
import com.mkz.rpg.ability.domain.AbilityEvent.AbilityCreated
import com.mkz.rpg.ability.domain.AbilityMother.ability
import com.mkz.rpg.ability.domain.AbilityMother.effectSpec
import com.mkz.rpg.ability.usecases.services.FakeAbilityLoader
import com.mkz.rpg.effect.adapters.storage.InMemoryEffectRepository
import com.mkz.rpg.effect.domain.EffectMother.decreaseHealthEffect
import com.mkz.rpg.effect.usecases.queries.SearchEffectById
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class InitialiseAbilitiesTest {
    private val abilityRepository = InMemoryAbilityRepository()
    private val eventBus = FakeEventBus()
    private val effectRepository = InMemoryEffectRepository()

    private fun initialiseAbilities(loadedAbilities: List<Ability.Dto>) = InitialiseAbilities(FakeAbilityLoader(loadedAbilities), abilityRepository, SearchEffectById(effectRepository), eventBus)

    @Test
    fun `should create every loaded ability when abilities are initialised`() {
        // Given
        effectRepository.create(decreaseHealthEffect(id = "damage"))
        val sword = ability(id = "sword", effectSpecs = listOf(effectSpec(effectId = "damage"))).toDto()
        val initialiseAbilities = initialiseAbilities(loadedAbilities = listOf(sword))
        // When
        initialiseAbilities()
        // Then
        abilityRepository.searchById("sword")?.toDto() shouldBe sword
        eventBus.shouldHavePublishedEvents(AbilityCreated("sword"))
    }

    @Test
    fun `should fail when an ability effect does not exist`() {
        // Given
        val sword = ability(id = "sword", effectSpecs = listOf(effectSpec(effectId = "missing-effect"))).toDto()
        val initialiseAbilities = initialiseAbilities(loadedAbilities = listOf(sword))
        // When
        val initialisation = { initialiseAbilities() }
        // Then
        shouldThrow<AbilityEffectDoesNotExist> { initialisation() }
    }
}
