package com.mkz.rpg.localization.usecases.queries

import com.mkz.rpg.localization.domain.LocalizationError.LocalizationNotFound
import com.mkz.rpg.localization.domain.LocalizationRepository

/** The plural form of the message of a key for a [count] in the selected language, e.g.: `Vanishes in 3 turns`. */
class TranslatePlural(
    private val localizationRepository: LocalizationRepository,
) {
    operator fun invoke(
        key: String,
        count: Int,
        arguments: Map<String, String> = emptyMap(),
    ): String = (localizationRepository.search() ?: throw LocalizationNotFound()).translatePlural(key, count, arguments)
}
