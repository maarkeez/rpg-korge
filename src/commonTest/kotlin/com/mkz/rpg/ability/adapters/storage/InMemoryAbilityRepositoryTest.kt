package com.mkz.rpg.ability.adapters.storage

import com.mkz.rpg.ability.domain.AbilityMother
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class InMemoryAbilityRepositoryTest {
    private val abilityRepository = InMemoryAbilityRepository()

    @Test
    fun `Create should store the ability when the ability is created`() {
        // Given
        val ability = AbilityMother.ability()
        // When
        abilityRepository.create(ability)
        // Then
        val storedAbility = abilityRepository.searchById(ability.toDto().id)
        storedAbility shouldBe ability
    }

    @Test
    fun `Create should replace the stored ability when an ability with the same id is created`() {
        // Given
        val ability = AbilityMother.ability(id = "ability-1")
        abilityRepository.create(ability)
        val updatedAbility = AbilityMother.ability(id = "ability-1")
        // When
        abilityRepository.create(updatedAbility)
        // Then
        val storedAbility = abilityRepository.searchById("ability-1")
        storedAbility shouldBe updatedAbility
    }

    @Test
    fun `SearchById should return the ability when the ability is stored`() {
        // Given
        val ability = AbilityMother.ability()
        abilityRepository.create(ability)
        // When
        val storedAbility = abilityRepository.searchById(ability.toDto().id)
        // Then
        storedAbility shouldBe ability
    }

    @Test
    fun `SearchById should return null when the ability is not stored`() {
        // When
        val storedAbility = abilityRepository.searchById("ability-unknown")
        // Then
        storedAbility.shouldBeNull()
    }
}
