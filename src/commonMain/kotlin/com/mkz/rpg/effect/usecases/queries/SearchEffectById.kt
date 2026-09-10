package com.mkz.rpg.effect.usecases.queries

import com.mkz.rpg.effect.domain.Effect
import com.mkz.rpg.effect.domain.EffectRepository

class SearchEffectById(
    private val effectRepository: EffectRepository,
) {
    operator fun invoke(id: String): Effect.Dto? = effectRepository.searchById(id)?.toDto()
}
