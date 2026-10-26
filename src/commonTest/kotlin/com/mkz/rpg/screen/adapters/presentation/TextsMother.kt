package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.localization.adapters.presentation.LocalizationApi
import com.mkz.rpg.localization.adapters.resources.ResourcesLocalizationLoader
import com.mkz.rpg.localization.adapters.resources.TestCatalogs
import com.mkz.rpg.localization.usecases.services.FakeLocalizationLoader
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus

object TextsMother {
    /** The texts of the real catalogs, in English unless the [languageCode] says otherwise, e.g.: `es`. */
    fun texts(languageCode: String = ResourcesLocalizationLoader.SOURCE_LANGUAGE_CODE): Texts = Texts(localizationApi(languageCode))

    fun localizationApi(languageCode: String = ResourcesLocalizationLoader.SOURCE_LANGUAGE_CODE): LocalizationApi {
        val localization = ResourcesLocalizationLoader.localizationOf(TestCatalogs.files)
        val localizationApi = LocalizationApi(InMemoryEventBus(), FakeLocalizationLoader(localization))
        localizationApi.initialiseLocalization()
        if (languageCode != localization.selectedLanguageCode) localizationApi.selectLanguage(languageCode)
        return localizationApi
    }
}
