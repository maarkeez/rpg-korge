package effect.usecases.queries

import effect.adapters.storage.*
import effect.domain.*
import effect.domain.EffectMother.effect
import org.assertj.core.api.Assertions.assertThat
import org.junit.*

class SearchEffectByIdTest {
    private val effectRepository = InMemoryEffectRepository()
    private val searchEffectById = SearchEffectById(effectRepository)

    @Test
    fun `should return effect when it exists`() {
        // Given
        val effect = effect()
        val effectDto = effect.toDto()
        effectRepository.create(effect)
        // When
        val storedEffect = searchEffectById(effectDto.id)
        // Then
        assertThat(storedEffect).isEqualTo(effectDto)
    }

    @Test
    fun `should return null when effect does not exist`() {
        // Given
        val unknownEffectId = EffectMother.effectId()
        // When
        val storedEffect = searchEffectById(unknownEffectId)
        // Then
        assertThat(storedEffect).isNull()
    }
}
