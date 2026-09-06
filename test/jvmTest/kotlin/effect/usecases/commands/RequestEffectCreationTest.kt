package effect.usecases.commands

import effect.adapters.storage.*
import effect.domain.EffectMother.effect
import org.assertj.core.api.Assertions.assertThat
import org.junit.*
import org.mockito.kotlin.*
import shared.domain.*

class RequestEffectCreationTest {
    private val effectRepository = InMemoryEffectRepository()
    private val eventBus: EventBus = mock()
    private val requestEffectCreation = RequestEffectCreation(
        effectRepository = effectRepository,
        eventBus = eventBus
    )

    @Test
    fun `should create effect when it does not exist`() {
        // Given
        val effect = effect().toDto()
        // When
        requestEffectCreation(effectDto = effect)
        // Then
        val storedEffect = effectRepository.searchById(effect.id)?.toDto()
        assertThat(storedEffect).isEqualTo(effect)
    }

    @Test
    fun `should not create effect when it already exists`() {
        // Given
        val existingEffect = effect(power = 3)
        val duplicateEffect = effect(existingEffect.toDto().id, power = 10)
        effectRepository.create(existingEffect)
        // When
        requestEffectCreation(effectDto = duplicateEffect.toDto())
        // Then
        val storedEffect = effectRepository.searchById(existingEffect.toDto().id)?.toDto()
        assertThat(storedEffect).isEqualTo(existingEffect.toDto())
    }
}
