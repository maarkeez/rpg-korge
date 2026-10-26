package com.mkz.rpg.localization.usecases.commands

import com.mkz.rpg.localization.adapters.storage.InMemoryLocalizationRepository
import com.mkz.rpg.localization.domain.Localization
import com.mkz.rpg.localization.domain.LocalizationEvent.LocalizationInitialised
import com.mkz.rpg.localization.domain.LocalizationMother.english
import com.mkz.rpg.localization.domain.LocalizationMother.spanish
import com.mkz.rpg.localization.usecases.services.FakeLocalizationLoader
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class InitialiseLocalizationTest {
    private val localizationRepository = InMemoryLocalizationRepository()
    private val eventBus = FakeEventBus()
    private val dto = Localization.Dto(source = english(), translations = listOf(spanish()), selectedLanguageCode = "en")
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
