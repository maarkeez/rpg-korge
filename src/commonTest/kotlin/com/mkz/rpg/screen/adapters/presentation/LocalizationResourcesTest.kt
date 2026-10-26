package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.localization.adapters.resources.PoFile
import com.mkz.rpg.localization.adapters.resources.ResourcesLocalizationLoader
import com.mkz.rpg.localization.adapters.resources.ResourcesLocalizationLoader.Companion.SOURCE_FILE_NAME
import com.mkz.rpg.localization.adapters.resources.ResourcesLocalizationLoader.Companion.TRANSLATION_EXTENSION
import com.mkz.rpg.localization.adapters.resources.TestCatalogs
import com.mkz.rpg.localization.domain.Localization
import com.mkz.rpg.shared.adapters.resources.ResourcesBundle
import io.kotest.assertions.withClue
import io.kotest.matchers.collections.shouldBeEmpty
import korlibs.image.font.CharacterSet
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

/**
 * The catalogs under resources/localization must match the text the screen displays: every key it translates is in
 * messages.pot, messages.pot has no other key, and every translation matches messages.pot. Translations may be missing.
 * The screen translates the UI texts, and the game data from its ids: the name of each resource (e.g.: `unit.bat.name`
 * for resources/unit/bat.toml), and the line of each npc.
 */
class LocalizationResourcesTest {
    private val source = PoFile.parse(TestCatalogs.files.getValue(SOURCE_FILE_NAME))
    private val sourceEntries = source.entries.associateBy { entry -> entry.context }
    private val translations =
        TestCatalogs.files
            .filterKeys { fileName -> fileName.endsWith(TRANSLATION_EXTENSION) }
            .mapValues { (_, content) -> PoFile.parse(content) }

    // The game data folders whose resources are named, and the fields translated for each, e.g.: `npc.elder.line`.
    private val gameDataFields: Map<String, List<String>> =
        mapOf(
            "ability" to listOf("name"),
            "lootableItem" to listOf("name"),
            "npc" to listOf("name", "line"),
            "player" to listOf("name"),
            "terrain" to listOf("name"),
            "unit" to listOf("name"),
        )

    // The characters the font draws, and the line breaks.
    private val drawnCharacters: Set<Int> = CharacterSet.LATIN_ALL.codePoints.toSet() + '\n'.code

    private val placeholder = Regex("""\{([A-Za-z][A-Za-z0-9]*)\}""")

    @Test
    fun `should load every catalog`() {
        ResourcesLocalizationLoader.localizationOf(TestCatalogs.files)
    }

    @Test
    fun `should have every UI text in the source catalog, plural when the UI text is`() {
        val missing =
            UiText.entries
                .filter { text -> sourceEntries[text.key]?.let { entry -> (entry.idPlural != null) == text.isPlural } != true }
                .map { text -> "${text.key}${if (text.isPlural) " (plural)" else ""}" }
        withClue("UI texts missing from $SOURCE_FILE_NAME, or not plural when the UI text is: $missing") { missing.shouldBeEmpty() }
    }

    @Test
    fun `should have every game data text in the source catalog`() =
        runTest {
            val missing = gameDataKeys().filter { key -> key !in sourceEntries }
            withClue("Game data texts missing from $SOURCE_FILE_NAME: $missing") { missing.shouldBeEmpty() }
        }

    @Test
    fun `should only have the keys the screen translates in the source catalog`() =
        runTest {
            val keys = UiText.entries.map(UiText::key).toSet() + Localization.LANGUAGE_NAME_KEY + gameDataKeys()
            val orphans = source.entries.map { entry -> entry.context ?: entry.id }.filter { key -> key !in keys }
            withClue("Entries of $SOURCE_FILE_NAME the screen does not translate, or without msgctxt: $orphans") { orphans.shouldBeEmpty() }
        }

    @Test
    fun `should only have the source entries in the translations`() {
        val stale =
            translations.flatMap { (fileName, file) ->
                file.entries.filter { entry -> sourceEntries[entry.context]?.id != entry.id }.map { entry -> "$fileName: ${entry.context ?: entry.id}" }
            }
        withClue("Entries missing from $SOURCE_FILE_NAME, or with another English text: merge it into them, e.g.: msgmerge. $stale") { stale.shouldBeEmpty() }
    }

    @Test
    fun `should have the placeholders of the source in every translation`() {
        val mismatches =
            translations.flatMap { (fileName, file) ->
                file.entries.filter(PoFile.Entry::isTranslated).flatMap { entry ->
                    val expected = placeholders(entry.id) + placeholders(entry.idPlural.orEmpty())
                    entry.translations.filter { translation -> placeholders(translation) != expected }.map { translation -> "$fileName: ${entry.context} '$translation' instead of $expected" }
                }
            }
        withClue("Translations with other placeholders than their English text: $mismatches") { mismatches.shouldBeEmpty() }
    }

    @Test
    fun `should have one form per plural of its language in every plural translation`() {
        val mismatches =
            translations.flatMap { (fileName, file) ->
                val pluralCount =
                    file.header["Plural-Forms"]?.let {
                        Regex("""nplurals\s*=\s*(\d+)""")
                            .find(it)
                            ?.groupValues
                            ?.get(1)
                            ?.toInt()
                    }
                file.entries
                    .filter { entry -> entry.idPlural != null && entry.translations.size != pluralCount }
                    .map { entry -> "$fileName: ${entry.context} has ${entry.translations.size} forms instead of $pluralCount" }
            }
        withClue("Plural translations with another number of forms than their language: $mismatches") { mismatches.shouldBeEmpty() }
    }

    @Test
    fun `should only use the characters the font draws`() {
        val texts = source.entries.flatMap { entry -> listOfNotNull(entry.id, entry.idPlural) } + translations.values.flatMap { file -> file.entries.flatMap(PoFile.Entry::translations) }
        val undrawn =
            texts
                .flatMap { text -> text.filter { character -> character.code !in drawnCharacters }.map { character -> "'$character' in '$text'" } }
                .distinct()
        withClue("Characters the font does not draw: $undrawn") { undrawn.shouldBeEmpty() }
    }

    private suspend fun gameDataKeys(): Set<String> =
        gameDataFields
            .flatMap { (folder, fields) ->
                ResourcesBundle.root[folder]
                    .listNames()
                    .filter { fileName -> fileName.endsWith(".toml") }
                    .flatMap { fileName -> fields.map { field -> "$folder.${fileName.removeSuffix(".toml")}.$field" } }
            }.toSet()

    private fun placeholders(text: String): Set<String> = placeholder.findAll(text).map { match -> match.groupValues[1] }.toSet()
}
