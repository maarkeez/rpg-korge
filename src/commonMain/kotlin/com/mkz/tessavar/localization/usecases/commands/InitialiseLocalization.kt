package com.mkz.tessavar.localization.usecases.commands

import com.mkz.tessavar.localization.domain.Localization
import com.mkz.tessavar.localization.domain.LocalizationRepository
import com.mkz.tessavar.localization.usecases.services.LocalizationLoader
import com.mkz.tessavar.shared.domain.EventBus

class InitialiseLocalization(
    private val localizationLoader: LocalizationLoader,
    private val localizationRepository: LocalizationRepository,
    private val eventBus: EventBus,
) {
    operator fun invoke() {
        val (events, localization) = Localization.create(localizationLoader.loadLocalization()).pullEvents()
        localizationRepository.save(localization)
        eventBus.publish(events)
    }
}
