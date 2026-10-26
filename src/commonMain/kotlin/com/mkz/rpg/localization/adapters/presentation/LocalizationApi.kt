package com.mkz.rpg.localization.adapters.presentation

import com.mkz.rpg.localization.adapters.resources.ResourcesLocalizationLoader
import com.mkz.rpg.localization.adapters.storage.InMemoryLocalizationRepository
import com.mkz.rpg.localization.domain.LocalizationRepository
import com.mkz.rpg.localization.usecases.commands.InitialiseLocalization
import com.mkz.rpg.localization.usecases.commands.SelectLanguage
import com.mkz.rpg.localization.usecases.queries.SearchLanguages
import com.mkz.rpg.localization.usecases.queries.Translate
import com.mkz.rpg.localization.usecases.queries.TranslatePlural
import com.mkz.rpg.localization.usecases.services.LocalizationLoader
import com.mkz.rpg.shared.domain.EventBus

/** The text of the game in every language. Only the presentation layer may use it: the other layers hold ids, never text. */
class LocalizationApi(
    eventBus: EventBus,
    private val localizationLoader: LocalizationLoader = ResourcesLocalizationLoader(),
) {
    // Storage
    private val localizationRepository: LocalizationRepository = InMemoryLocalizationRepository()

    // Commands
    val initialiseLocalization = InitialiseLocalization(localizationLoader, localizationRepository, eventBus)
    val selectLanguage = SelectLanguage(localizationRepository, eventBus)

    // Queries
    val translate = Translate(localizationRepository)
    val translatePlural = TranslatePlural(localizationRepository)
    val searchLanguages = SearchLanguages(localizationRepository)

    /** Reads the catalogs and initialises the localization, in English. */
    suspend fun init() {
        localizationLoader.initResources()
        initialiseLocalization()
    }
}
