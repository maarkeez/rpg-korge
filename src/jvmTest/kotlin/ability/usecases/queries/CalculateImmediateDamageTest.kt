package ability.usecases.queries

import ability.adapters.storage.*
import ability.domain.*
import ability.domain.AbilityMother.ability
import effect.domain.*
import effect.usecases.queries.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*

class CalculateImmediateDamageTest {
    private val abilityRepository = InMemoryAbilityRepository()
    private val searchEffectById: SearchEffectById = mock()
    private val calculateImmediateDamage = CalculateImmediateDamage(
        abilityRepository = abilityRepository,
        searchEffectById = searchEffectById
    )

    @Test
    fun `should sum power of immediately applied effects when ability has a mix of immediate and non immediate effects`() {
        // Given
        val immediateEffect = EffectMother.effect(id= "effect-1", power = 4, applicationType = "IMMEDIATELY").toDto()
        val onTurnStartedEffect = EffectMother.effect(id= "effect-2", power = 10).toDto()
        val ability = ability(effects = listOf(immediateEffect.id, onTurnStartedEffect.id))
        val abilityDto = ability.toDto()
        abilityRepository.create(ability)
        whenever(searchEffectById(immediateEffect.id)).thenReturn(immediateEffect)
        whenever(searchEffectById(onTurnStartedEffect.id)).thenReturn(onTurnStartedEffect)
        // When
        val damage = calculateImmediateDamage(abilityDto.id)
        // Then
        assertThat(damage).isEqualTo(4)
    }

    @Test
    fun `should return zero when ability does not exist`() {
        // Given
        val unknownAbilityId = AbilityMother.id()
        // When
        val damage = calculateImmediateDamage(unknownAbilityId)
        // Then
        assertThat(damage).isEqualTo(0)
    }
}
