package com.mkz.tessavar.localization.usecases.commands

import com.mkz.tessavar.localization.adapters.storage.InMemoryLocalizationRepository
import com.mkz.tessavar.localization.domain.Localization
import com.mkz.tessavar.localization.domain.LocalizationEvent.LocalizationInitialised
import com.mkz.tessavar.localization.domain.LocalizationMother.english
import com.mkz.tessavar.localization.domain.LocalizationMother.spanish
import com.mkz.tessavar.localization.usecases.services.FakeLocalizationLoader
import com.mkz.tessavar.shared.domain.FakeEventBus
import com.mkz.tessavar.shared.domain.shouldHavePublishedEvents
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class InitialiseLocalizationTest {
    private val localizationRepository = InMemoryLocalizationRepository()
    private val eventBus = FakeEventBus()
    private val dto = Localization.Dto(sourceCatalog = english(), translationCatalogs = listOf(spanish()), selectedLanguageCode = "en")
    private val initialiseLocalization = InitialiseLocalization(FakeLocalizationLoader(dto), localizationRepository, eventBus)

    @Test
    fun `should store the localization when the catalogs are loaded`() {
        // When
        initialiseLocalization()
        // Then
        localizationRepository.search()?.toDto() shouldBe dto
    }

    @Test
    fun `should publish the localization initialised when it is stored`() {
        // When
        initialiseLocalization()
        // Then
        eventBus.shouldHavePublishedEvents(LocalizationInitialised(languageCodes = listOf("en", "es"), selectedLanguageCode = "en"))
    }
}
