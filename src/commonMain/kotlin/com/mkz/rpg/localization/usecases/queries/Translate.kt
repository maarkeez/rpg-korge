package com.mkz.rpg.localization.usecases.queries

import com.mkz.rpg.localization.domain.LocalizationError.LocalizationNotFound
import com.mkz.rpg.localization.domain.LocalizationRepository

class Translate(
    private val localizationRepository: LocalizationRepository,
) {
    operator fun invoke(
        key: String,
        arguments: Map<String, String> = emptyMap(),
    ): String = (localizationRepository.search() ?: throw LocalizationNotFound()).translate(key, arguments)
}
