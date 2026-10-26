package com.mkz.rpg.localization.adapters.storage

import com.mkz.rpg.localization.domain.LocalizationMother.localization
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class InMemoryLocalizationRepositoryTest {
    private val localizationRepository = InMemoryLocalizationRepository()

    @Test
    fun `Save should store the localization`() {
        // Given
        val localization = localization()
        // When
        localizationRepository.save(localization)
        // Then
        localizationRepository.search() shouldBe localization
    }

    @Test
    fun `Save should replace the stored localization`() {
        // Given
        localizationRepository.save(localization())
        val selected = localization().selectLanguage("es")
        // When
        localizationRepository.save(selected)
        // Then
        localizationRepository.search() shouldBe selected
    }

    @Test
    fun `Search should return null when no localization is stored`() {
        localizationRepository.search().shouldBeNull()
    }
}
