package com.mkz.rpg.localization.usecases.queries

import com.mkz.rpg.localization.domain.Localization
import com.mkz.rpg.localization.domain.LocalizationRepository

class SearchLanguages(
    private val localizationRepository: LocalizationRepository,
) {
    operator fun invoke(): List<Localization.Dto.LanguageDto> = localizationRepository.search()?.languages().orEmpty()
}
