package effect.usecases.commands

import effect.domain.EffectPublisher
import effect.domain.EffectRepository

class RequestEffectCreation(
    private val effectRepository: EffectRepository,
    private val effectPublisher: EffectPublisher,
) {
    operator fun invoke() {
        // TODO: Implement
    }
}
