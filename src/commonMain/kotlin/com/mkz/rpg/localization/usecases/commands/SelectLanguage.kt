package com.mkz.rpg.localization.usecases.commands

import com.mkz.rpg.localization.domain.LocalizationError.LocalizationNotFound
import com.mkz.rpg.localization.domain.LocalizationRepository
import com.mkz.rpg.shared.domain.EventBus

/** Translates the messages into another language from now on, e.g.: `es`. */
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
