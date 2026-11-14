package com.mkz.tessavar.localization.usecases.queries

import com.mkz.tessavar.localization.adapters.storage.InMemoryLocalizationRepository
import com.mkz.tessavar.localization.domain.LocalizationError.LocalizationNotFound
import com.mkz.tessavar.localization.domain.LocalizationMother.localization
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class TranslatePluralTest {
    private val localizationRepository = InMemoryLocalizationRepository()
    private val translatePlural = TranslatePlural(localizationRepository)

    @Test
    fun `should return the plural form of the count in the selected language`() {
        // Given
        localizationRepository.save(localization(selectedLanguageCode = "es"))
        // When
        val text = translatePlural("ui.remainingSummonDuration.vanishes", 2, mapOf("turns" to "2"))
        // Then
        text shouldBe "Desaparece en 2 turnos"
    }

    @Test
    fun `should fail when the localization is not initialised`() {
        shouldThrow<LocalizationNotFound> { translatePlural("ui.remainingSummonDuration.vanishes", 2) }
    }
}
