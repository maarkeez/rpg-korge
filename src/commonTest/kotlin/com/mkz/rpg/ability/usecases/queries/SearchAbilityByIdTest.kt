package com.mkz.rpg.ability.usecases.queries

import com.mkz.rpg.ability.adapters.storage.InMemoryAbilityRepository
import com.mkz.rpg.ability.domain.AbilityMother.ability
import com.mkz.rpg.ability.domain.AbilityMother.id
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class SearchAbilityByIdTest {
    private val abilityRepository = InMemoryAbilityRepository()
    private val searchAbilityById = SearchAbilityById(abilityRepository)

    @Test
    fun `should return ability when it exists`() {
        // Given
        val ability = ability()
        val abilityDto = ability.toDto()
        abilityRepository.create(ability)
        // When
        val storedAbility = searchAbilityById(abilityDto.id)
        // Then
        storedAbility shouldBe abilityDto
    }

    @Test
    fun `should return null when ability does not exist`() {
        // Given
        val unknownAbilityId = id()
        // When
        val storedAbility = searchAbilityById(unknownAbilityId)
        // Then
        storedAbility.shouldBeNull()
    }
}
