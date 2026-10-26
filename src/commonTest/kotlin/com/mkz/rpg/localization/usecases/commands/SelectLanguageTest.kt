package com.mkz.rpg.localization.usecases.commands

import com.mkz.rpg.localization.adapters.storage.InMemoryLocalizationRepository
import com.mkz.rpg.localization.domain.LocalizationError.LanguageNotFound
import com.mkz.rpg.localization.domain.LocalizationError.LocalizationNotFound
import com.mkz.rpg.localization.domain.LocalizationEvent.LanguageSelected
import com.mkz.rpg.localization.domain.LocalizationMother.localization
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class SelectLanguageTest {
    private val localizationRepository = InMemoryLocalizationRepository()
    private val eventBus = FakeEventBus()
    private val selectLanguage = SelectLanguage(localizationRepository, eventBus)

    @Test
    fun `should store the language selected when the localization has its catalog`() {
        // Given
        localizationRepository.save(localization().pullEvents().second)
        // When
        selectLanguage("es")
        // Then
        localizationRepository.search()?.toDto()?.selectedLanguageCode shouldBe "es"
    }

    @Test
    fun `should publish the language selected when it is stored`() {
        // Given
        localizationRepository.save(localization().pullEvents().second)
        // When
        selectLanguage("es")
        // Then
        eventBus.shouldHavePublishedEvents(LanguageSelected(languageCode = "es"))
    }

    @Test
    fun `should fail and publish nothing when the language has no catalog`() {
        // Given
        localizationRepository.save(localization().pullEvents().second)
        // When
        shouldThrow<LanguageNotFound> { selectLanguage("fr") }
        // Then
        localizationRepository.search()?.toDto()?.selectedLanguageCode shouldBe "en"
        eventBus.publishedEvents.shouldBeEmpty()
    }

    @Test
    fun `should fail when the localization is not initialised`() {
        shouldThrow<LocalizationNotFound> { selectLanguage("es") }
    }
}
