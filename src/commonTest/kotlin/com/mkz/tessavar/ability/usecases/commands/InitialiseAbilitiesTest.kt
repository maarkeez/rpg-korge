package com.mkz.tessavar.ability.usecases.commands

import com.mkz.tessavar.ability.adapters.storage.InMemoryAbilityRepository
import com.mkz.tessavar.ability.domain.Ability
import com.mkz.tessavar.ability.domain.AbilityError.AbilityEffectDoesNotExist
import com.mkz.tessavar.ability.domain.AbilityEvent.AbilityCreated
import com.mkz.tessavar.ability.domain.AbilityMother.ability
import com.mkz.tessavar.ability.domain.AbilityMother.abilityEffect
import com.mkz.tessavar.ability.usecases.services.AbilityCreation
import com.mkz.tessavar.ability.usecases.services.FakeAbilityLoader
import com.mkz.tessavar.effect.adapters.storage.InMemoryEffectRepository
import com.mkz.tessavar.effect.domain.EffectMother.damageEffect
import com.mkz.tessavar.effect.usecases.queries.SearchEffectById
import com.mkz.tessavar.shared.domain.FakeEventBus
import com.mkz.tessavar.shared.domain.shouldHavePublishedEvents
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class InitialiseAbilitiesTest {
    private val abilityRepository = InMemoryAbilityRepository()
    private val eventBus = FakeEventBus()
    private val effectRepository = InMemoryEffectRepository()

    private fun initialiseAbilities(loadedAbilities: List<Ability.Dto>) =
        InitialiseAbilities(FakeAbilityLoader(loadedAbilities), AbilityCreation(abilityRepository, SearchEffectById(effectRepository)), eventBus)

    @Test
    fun `should create every loaded ability when abilities are initialised`() {
        // Given
        effectRepository.create(damageEffect(id = "damage"))
        val sword = ability(id = "sword", abilityEffects = listOf(abilityEffect(effectId = "damage"))).toDto()
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
        val sword = ability(id = "sword", abilityEffects = listOf(abilityEffect(effectId = "missing-effect"))).toDto()
        val initialiseAbilities = initialiseAbilities(loadedAbilities = listOf(sword))
        // When
        val initialisation = { initialiseAbilities() }
        // Then
        shouldThrow<AbilityEffectDoesNotExist> { initialisation() }
    }
}
