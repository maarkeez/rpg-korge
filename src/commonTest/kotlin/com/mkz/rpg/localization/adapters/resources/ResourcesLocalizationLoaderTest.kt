package com.mkz.rpg.localization.adapters.resources

import com.mkz.rpg.localization.domain.Localization.Dto.CatalogDto
import com.mkz.rpg.localization.domain.Localization.Dto.MessageDto
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import korlibs.io.file.std.MemoryVfsMix
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class ResourcesLocalizationLoaderTest {
    private val source =
        """
        msgid ""
        msgstr ""
        "Plural-Forms: nplurals=2; plural=(n != 1);\n"

        msgctxt "ui.shop.buy"
        msgid "Buy"
        msgstr ""

        msgctxt "ui.remainingTurns.vanishes"
        msgid "Vanishes in {turns} turn"
        msgid_plural "Vanishes in {turns} turns"
        msgstr[0] ""
        msgstr[1] ""
        """.trimIndent()

    private val spanish =
        """
        msgid ""
        msgstr ""
        "Plural-Forms: nplurals=2; plural=(n != 1);\n"

        msgctxt "ui.shop.buy"
        msgid "Buy"
        msgstr "Comprar"

        msgctxt "ui.remainingTurns.vanishes"
        msgid "Vanishes in {turns} turn"
        msgid_plural "Vanishes in {turns} turns"
        msgstr[0] "Desaparece en {turns} turno"
        msgstr[1] ""
        """.trimIndent()

    @Test
    fun `should load the source catalog in English, and a translation per po file sorted by code`() =
        runTest {
            // Given
            val localizationRoot =
                MemoryVfsMix(
                    "localization/messages.pot" to source,
                    "localization/pt_BR.po" to spanish,
                    "localization/es.po" to spanish,
                    "localization/README.md" to "Not a catalog",
                )["localization"]
            val loader = ResourcesLocalizationLoader(localizationRoot = localizationRoot)
            // When
            loader.initResources()
            // Then
            val localization = loader.loadLocalization()
            localization.selectedLanguageCode shouldBe "en"
            localization.source shouldBe
                CatalogDto(
                    languageCode = "en",
                    pluralForms = "nplurals=2; plural=(n != 1);",
                    messages =
                        listOf(
                            MessageDto(key = "ui.shop.buy", forms = listOf("Buy")),
                            MessageDto(key = "ui.remainingTurns.vanishes", forms = listOf("Vanishes in {turns} turn", "Vanishes in {turns} turns")),
                        ),
                )
            localization.translations.map(CatalogDto::languageCode).shouldContainExactly("es", "pt_BR")
        }

    @Test
    fun `should only keep the translated entries of a translation`() {
        // When
        val localization = ResourcesLocalizationLoader.localizationOf(mapOf("messages.pot" to source, "es.po" to spanish))
        // Then
        localization.translations.single() shouldBe
            CatalogDto(languageCode = "es", pluralForms = "nplurals=2; plural=(n != 1);", messages = listOf(MessageDto(key = "ui.shop.buy", forms = listOf("Comprar"))))
    }

    @Test
    fun `should fail with the file when a translation has no plural forms`() {
        // When
        val error = shouldThrow<IllegalStateException> { ResourcesLocalizationLoader.localizationOf(mapOf("messages.pot" to source, "es.po" to "msgid \"\"\nmsgstr \"\"")) }
        // Then
        error.message shouldBe "Resource 'localization/es.po' is invalid: 'Plural-Forms' is required"
    }

    @Test
    fun `should fail with the file when a catalog does not parse`() {
        // When
        val error = shouldThrow<IllegalStateException> { ResourcesLocalizationLoader.localizationOf(mapOf("messages.pot" to "msgid \"a\"")) }
        // Then
        error.message shouldBe "Resource 'localization/messages.pot' is invalid: End of file: Entry 'a' without msgstr"
    }

    @Test
    fun `should fail when there is no source catalog`() =
        runTest {
            // Given
            val loader = ResourcesLocalizationLoader(localizationRoot = MemoryVfsMix()["localization"])
            // When
            val error = shouldThrow<IllegalStateException> { loader.initResources() }
            // Then
            error.message shouldContain "localization/messages.pot"
        }

    @Test
    fun `should fail when the localization is loaded before the resources are read`() {
        shouldThrow<IllegalStateException> { ResourcesLocalizationLoader().loadLocalization() }
    }
}
