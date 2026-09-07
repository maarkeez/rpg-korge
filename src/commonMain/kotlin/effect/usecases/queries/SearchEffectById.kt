package effect.usecases.queries

import effect.domain.Effect
import effect.domain.EffectRepository

class SearchEffectById(
    private val effectRepository: EffectRepository,
) {
    operator fun invoke(id: String): Effect.Dto? = effectRepository.searchById(id)?.toDto()
}
