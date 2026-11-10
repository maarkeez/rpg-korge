package com.mkz.rpg.localization.adapters.presentation

import com.mkz.rpg.localization.adapters.storage.InMemoryLocalizationRepository
import com.mkz.rpg.localization.domain.LocalizationRepository
import com.mkz.rpg.localization.usecases.commands.InitialiseLocalization
import com.mkz.rpg.localization.usecases.commands.SelectLanguage
import com.mkz.rpg.localization.usecases.queries.SearchLanguages
import com.mkz.rpg.localization.usecases.queries.Translate
import com.mkz.rpg.localization.usecases.queries.TranslatePlural
import com.mkz.rpg.localization.usecases.services.LocalizationLoader
import com.mkz.rpg.shared.domain.EventBus

class LocalizationApi(
    eventBus: EventBus,
    private val localizationLoader: LocalizationLoader,
) {
    private val localizationRepository: LocalizationRepository = InMemoryLocalizationRepository()

    val initialiseLocalization = InitialiseLocalization(localizationLoader, localizationRepository, eventBus)
    val selectLanguage = SelectLanguage(localizationRepository, eventBus)

    val translate = Translate(localizationRepository)
    val translatePlural = TranslatePlural(localizationRepository)
    val searchLanguages = SearchLanguages(localizationRepository)

    suspend fun init() {
        localizationLoader.initResources()
        initialiseLocalization()
    }
}
