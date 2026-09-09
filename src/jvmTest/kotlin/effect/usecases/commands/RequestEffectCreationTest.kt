package effect.usecases.commands

import effect.adapters.storage.InMemoryEffectRepository
import effect.domain.EffectEvent
import effect.domain.EffectMother.effect
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import shared.domain.FakeEventBus
import shared.domain.assertThat

class RequestEffectCreationTest {
    private val effectRepository = InMemoryEffectRepository()
    private val eventBus = _root_ide_package_.shared.domain.FakeEventBus()
    private val requestEffectCreation =
        RequestEffectCreation(
            effectRepository = effectRepository,
            eventBus = eventBus,
        )

    @Test
    fun `should create effect when it does not exist`() {
        // Given
        val effect =
            _root_ide_package_.effect.domain.EffectMother
                .effect()
                .toDto()
        // When
        requestEffectCreation(effectDto = effect)
        // Then
        val storedEffect = effectRepository.searchById(effect.id)?.toDto()
        assertThat(storedEffect).isEqualTo(effect)
        _root_ide_package_.shared.domain
            .assertThat(eventBus)
            .hasPublishedEvents(EffectEvent.EffectCreated(effect.id))
    }

    @Test
    fun `should not create effect when it already exists`() {
        // Given
        val existingEffect =
            _root_ide_package_.effect.domain.EffectMother
                .effect(power = 3)
        val duplicateEffect =
            _root_ide_package_.effect.domain.EffectMother
                .effect(existingEffect.toDto().id, power = 10)
        effectRepository.create(existingEffect)
        // When
        requestEffectCreation(effectDto = duplicateEffect.toDto())
        // Then
        val storedEffect = effectRepository.searchById(existingEffect.toDto().id)?.toDto()
        assertThat(storedEffect).isEqualTo(existingEffect.toDto())
        _root_ide_package_.shared.domain
            .assertThat(eventBus)
            .hasPublishedEvents()
    }
}
