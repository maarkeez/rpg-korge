package com.mkz.rpg.localization.usecases.queries

import com.mkz.rpg.localization.domain.Localization
import com.mkz.rpg.localization.domain.LocalizationRepository

/** The languages the game can be played in, the source one first, each named in its own language. None before it is initialised. */
class SearchLanguages(
    private val localizationRepository: LocalizationRepository,
) {
    operator fun invoke(): List<Localization.Dto.LanguageDto> = localizationRepository.search()?.languages().orEmpty()
}
