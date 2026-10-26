package com.mkz.rpg.localization.usecases.queries

import com.mkz.rpg.localization.adapters.storage.InMemoryLocalizationRepository
import com.mkz.rpg.localization.domain.LocalizationError.LocalizationNotFound
import com.mkz.rpg.localization.domain.LocalizationMother.localization
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class TranslateTest {
    private val localizationRepository = InMemoryLocalizationRepository()
    private val translate = Translate(localizationRepository)

    @Test
    fun `should return the message in the selected language`() {
        // Given
        localizationRepository.save(localization(selectedLanguageCode = "es"))
        // When
        val text = translate("ui.battleInfo.round", mapOf("round" to "2"))
        // Then
        text shouldBe "Ronda 2"
    }

    @Test
    fun `should fail when the localization is not initialised`() {
        shouldThrow<LocalizationNotFound> { translate("ui.shop.buy") }
    }
}
