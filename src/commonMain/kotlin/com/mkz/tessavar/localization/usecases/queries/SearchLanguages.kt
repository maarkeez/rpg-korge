package com.mkz.tessavar.localization.usecases.queries

import com.mkz.tessavar.localization.domain.Localization
import com.mkz.tessavar.localization.domain.LocalizationRepository

class SearchLanguages(
    private val localizationRepository: LocalizationRepository,
) {
    operator fun invoke(): List<Localization.Dto.LanguageDto> = localizationRepository.search()?.languages().orEmpty()
}
