package com.mkz.rpg.localization.usecases.queries

import com.mkz.rpg.localization.adapters.storage.InMemoryLocalizationRepository
import com.mkz.rpg.localization.domain.Localization.Dto.LanguageDto
import com.mkz.rpg.localization.domain.LocalizationMother.localization
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import kotlin.test.Test

class SearchLanguagesTest {
    private val localizationRepository = InMemoryLocalizationRepository()
    private val searchLanguages = SearchLanguages(localizationRepository)

    @Test
    fun `should return the languages when the localization is initialised`() {
        // Given
        localizationRepository.save(localization())
        // When
        val languages = searchLanguages()
        // Then
        languages.shouldContainExactly(LanguageDto(code = "en", name = "English"), LanguageDto(code = "es", name = "Español"))
    }

    @Test
    fun `should return no language when the localization is not initialised`() {
        searchLanguages().shouldBeEmpty()
    }
}
