package com.mkz.rpg.localization.domain

import com.mkz.rpg.localization.domain.Localization.Dto.CatalogDto
import com.mkz.rpg.localization.domain.Localization.Dto.LanguageDto
import com.mkz.rpg.localization.domain.Localization.Dto.MessageDto
import com.mkz.rpg.localization.domain.LocalizationError.EmptyLanguageCode
import com.mkz.rpg.localization.domain.LocalizationError.InvalidPluralRule
import com.mkz.rpg.localization.domain.LocalizationError.LanguageNotFound
import com.mkz.rpg.localization.domain.LocalizationError.TooManyPluralForms
import com.mkz.rpg.localization.domain.LocalizationEvent.LanguageSelected
import com.mkz.rpg.localization.domain.LocalizationEvent.LocalizationInitialised
import com.mkz.rpg.localization.domain.LocalizationMother.ENGLISH_PLURAL_RULE
import com.mkz.rpg.localization.domain.LocalizationMother.english
import com.mkz.rpg.localization.domain.LocalizationMother.localization
import com.mkz.rpg.localization.domain.LocalizationMother.polish
import com.mkz.rpg.localization.domain.LocalizationMother.spanish
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class LocalizationTest {
    @Test
    fun `Create should record the languages and the selected one`() {
        // When
        val (events, _) = localization(translationCatalogs = listOf(spanish(), polish())).pullEvents()
        // Then
        events.shouldContainExactly(LocalizationInitialised(languageCodes = listOf("en", "es", "pl"), selectedLanguageCode = "en"))
    }

    @Test
    fun `Create should fail when the selected language has no catalog`() {
        // When
        val error = shouldThrow<LanguageNotFound> { localization(selectedLanguageCode = "fr") }
        // Then
        error.message shouldBe "Language 'fr' not found"
    }

    @Test
    fun `Create should fail when a catalog has no language code`() {
        shouldThrow<EmptyLanguageCode> { localization(translationCatalogs = listOf(spanish().copy(languageCode = ""))) }
    }

    @Test
    fun `Create should fail when the plural rule of a catalog is not valid`() {
        listOf("", "nplurals=2;", "plural=(n != 1);", "nplurals=0; plural=0;", "nplurals=2; plural=(n !! 1);", "nplurals=2; plural=(n != 1;", "nplurals=2; plural=n $ 1;")
            .forEach { pluralRule -> shouldThrow<InvalidPluralRule> { localization(translationCatalogs = listOf(spanish().copy(pluralRule = pluralRule))) } }
    }

    @Test
    fun `Create should fail when a message has more plural forms than the plural rule of its catalog`() {
        // Given
        val japanese = CatalogDto(languageCode = "ja", pluralRule = "nplurals=1; plural=0;", messages = listOf(MessageDto(key = "ui.turns", forms = listOf("a", "b"))))
        // When
        val error = shouldThrow<TooManyPluralForms> { localization(translationCatalogs = listOf(japanese)) }
        // Then
        error.message shouldBe "Message 'ui.turns' of language 'ja' has more than 1 plural forms"
    }

    @Test
    fun `Translate should return the source message when the source language is selected`() {
        localization().translate("ui.shop.buy") shouldBe "Buy"
    }

    @Test
    fun `Translate should return the message of the selected language`() {
        localization(selectedLanguageCode = "es").translate("ui.shop.buy") shouldBe "Comprar"
    }

    @Test
    fun `Translate should fall back to the source message when the selected language has no translation`() {
        localization(selectedLanguageCode = "es").translate("ui.shop.sell") shouldBe "Sell"
    }

    @Test
    fun `Translate should return the key when the source has no message for it`() {
        localization(selectedLanguageCode = "es").translate("ui.unknown") shouldBe "ui.unknown"
    }

    @Test
    fun `Translate should replace the placeholders by their arguments`() {
        localization(selectedLanguageCode = "es").translate("ui.banner.round", mapOf("round" to "3")) shouldBe "Ronda 3"
    }

    @Test
    fun `Translate should leave a placeholder as it is when it has no argument`() {
        localization().translate("ui.banner.round", mapOf("turn" to "3")) shouldBe "Round {round}"
    }

    @Test
    fun `Translate should return the singular when the message is a plural`() {
        localization().translate("ui.remainingSummonDuration.vanishes") shouldBe "Vanishes in {turns} turn"
    }

    @Test
    fun `TranslatePlural should choose the form by the plural rule of the selected language`() {
        // Given
        val localization = localization(translationCatalogs = listOf(polish()), selectedLanguageCode = "pl")
        // When
        val texts = listOf(1, 2, 5, 12, 22, 25).map { count -> localization.translatePlural("ui.remainingSummonDuration.vanishes", count, mapOf("turns" to "$count")) }
        // Then
        texts.shouldContainExactly("Znika za 1 turę", "Znika za 2 tury", "Znika za 5 tur", "Znika za 12 tur", "Znika za 22 tury", "Znika za 25 tur")
    }

    @Test
    fun `TranslatePlural should choose the English form when the source language is selected`() {
        // Given
        val localization = localization()
        // When
        val texts = listOf(0, 1, 2).map { count -> localization.translatePlural("ui.remainingSummonDuration.vanishes", count, mapOf("turns" to "$count")) }
        // Then
        texts.shouldContainExactly("Vanishes in 0 turns", "Vanishes in 1 turn", "Vanishes in 2 turns")
    }

    @Test
    fun `TranslatePlural should fall back to the source when the form chosen is missing in the selected language`() {
        // Given
        val french =
            CatalogDto(
                languageCode = "fr",
                pluralRule = "nplurals=2; plural=(n > 1);",
                messages = listOf(MessageDto(key = "ui.remainingSummonDuration.vanishes", forms = listOf("Disparaît dans {turns} tour"))),
            )
        // When
        val text = localization(translationCatalogs = listOf(french), selectedLanguageCode = "fr").translatePlural("ui.remainingSummonDuration.vanishes", 3)
        // Then
        text shouldBe "Vanishes in {turns} turns"
    }

    @Test
    fun `TranslatePlural should evaluate the operators of the plural rule`() {
        // Given
        val formulas =
            mapOf(
                "n * 2 - 1 + n / 2" to 6,
                "n % 0 + n / 0" to 0,
                "!(n == 3) || n < 2" to 0,
                "n <= 3 && n >= 3 && n > 2" to 1,
                "(n != 3) ? 1 : 2 ? 3 : 4" to 3,
            )
        // When
        val results =
            formulas.mapValues { (formula, _) ->
                val catalog = CatalogDto(languageCode = "xx", pluralRule = "nplurals=7; plural=$formula;", messages = listOf(MessageDto(key = "k", forms = (0..6).map(Int::toString))))
                localization(translationCatalogs = listOf(catalog), selectedLanguageCode = "xx").translatePlural("k", 3).toInt()
            }
        // Then
        results shouldBe formulas
    }

    @Test
    fun `SelectLanguage should translate into the selected language`() {
        // Given
        val localization = localization()
        // When
        val selected = localization.selectLanguage("es")
        // Then
        selected.translate("ui.shop.buy") shouldBe "Comprar"
    }

    @Test
    fun `SelectLanguage should record the language selected`() {
        // Given
        val (_, localization) = localization().pullEvents()
        // When
        val (events, _) = localization.selectLanguage("es").pullEvents()
        // Then
        events.shouldContainExactly(LanguageSelected(languageCode = "es"))
    }

    @Test
    fun `SelectLanguage should fail when the language has no catalog`() {
        shouldThrow<LanguageNotFound> { localization().selectLanguage("fr") }
    }

    @Test
    fun `Languages should return the source catalog first, then the translation catalogs by code, named in their language`() {
        // When
        val languages = localization(translationCatalogs = listOf(polish(), spanish())).languages()
        // Then
        languages.shouldContainExactly(LanguageDto(code = "en", name = "English"), LanguageDto(code = "es", name = "Español"), LanguageDto(code = "pl", name = "English"))
    }

    @Test
    fun `Languages should name a language by its code when no catalog names it`() {
        // When
        val languages = localization(sourceCatalog = english().copy(messages = emptyList()), translationCatalogs = emptyList()).languages()
        // Then
        languages.shouldContainExactly(LanguageDto(code = "en", name = "en"))
    }

    @Test
    fun `PullEvents should clear the events`() {
        // Given
        val (_, localization) = localization().pullEvents()
        // When
        val (events, _) = localization.pullEvents()
        // Then
        events.shouldBeEmpty()
    }

    @Test
    fun `ToDto should return the catalogs and the selected language`() {
        // Given
        val dto = Localization.Dto(sourceCatalog = english(), translationCatalogs = listOf(spanish()), selectedLanguageCode = "es")
        // When
        val result = Localization.create(dto).toDto()
        // Then
        result shouldBe dto
        result.sourceCatalog.pluralRule shouldBe ENGLISH_PLURAL_RULE
    }
}
