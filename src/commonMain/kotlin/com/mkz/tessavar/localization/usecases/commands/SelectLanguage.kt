package com.mkz.tessavar.localization.usecases.commands

import com.mkz.tessavar.localization.domain.LocalizationError.LocalizationNotFound
import com.mkz.tessavar.localization.domain.LocalizationRepository
import com.mkz.tessavar.shared.domain.EventBus

class SelectLanguage(
    private val localizationRepository: LocalizationRepository,
    private val eventBus: EventBus,
) {
    operator fun invoke(languageCode: String) {
        val localization = localizationRepository.search() ?: throw LocalizationNotFound()
        val (events, updatedLocalization) = localization.selectLanguage(languageCode).pullEvents()
        localizationRepository.save(updatedLocalization)
        eventBus.publish(events)
    }
}
