package com.mkz.tessavar.localization.adapters.presentation

import com.mkz.tessavar.localization.adapters.storage.InMemoryLocalizationRepository
import com.mkz.tessavar.localization.domain.LocalizationRepository
import com.mkz.tessavar.localization.usecases.commands.InitialiseLocalization
import com.mkz.tessavar.localization.usecases.commands.SelectLanguage
import com.mkz.tessavar.localization.usecases.queries.SearchLanguages
import com.mkz.tessavar.localization.usecases.queries.Translate
import com.mkz.tessavar.localization.usecases.queries.TranslatePlural
import com.mkz.tessavar.localization.usecases.services.LocalizationLoader
import com.mkz.tessavar.shared.domain.EventBus

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
