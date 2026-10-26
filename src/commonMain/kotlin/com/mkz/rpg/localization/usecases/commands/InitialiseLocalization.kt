package com.mkz.rpg.localization.usecases.commands

import com.mkz.rpg.localization.domain.Localization
import com.mkz.rpg.localization.domain.LocalizationRepository
import com.mkz.rpg.localization.usecases.services.LocalizationLoader
import com.mkz.rpg.shared.domain.EventBus

/** Creates the localization from the loaded catalogs, in the source language. */
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
