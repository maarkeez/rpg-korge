package com.mkz.rpg.localization.adapters.resources

import com.mkz.rpg.localization.adapters.resources.PoFile.Entry
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class PoFileTest {
    @Test
    fun `Parse should read the header fields from the empty message`() {
        // Given
        val content =
            """
            msgid ""
            msgstr ""
            "Language: es\n"
            "Plural-Forms: nplurals=2; plural=(n != 1);\n"
            """.trimIndent()
        // When
        val file = PoFile.parse(content)
        // Then
        file.header shouldBe mapOf("Language" to "es", "Plural-Forms" to "nplurals=2; plural=(n != 1);")
        file.entries.shouldBeEmpty()
    }

    @Test
    fun `Parse should read the entries with their context, text and translation, skipping the comments`() {
        // Given
        val content =
            """
            # A translator comment
            #. An extracted comment
            #: src/MenuView.kt:12
            msgctxt "ui.shop.buy"
            msgid "Buy"
            msgstr "Comprar"

            msgid "Without context"
            msgstr ""
            """.trimIndent()
        // When
        val file = PoFile.parse(content)
        // Then
        file.entries.shouldContainExactly(
            Entry(context = "ui.shop.buy", id = "Buy", idPlural = null, translations = listOf("Comprar"), flags = emptySet()),
            Entry(context = null, id = "Without context", idPlural = null, translations = listOf(""), flags = emptySet()),
        )
    }

    @Test
    fun `Parse should read the plural forms of a plural entry`() {
        // Given
        val content =
            """
            msgctxt "ui.remainingTurns.vanishes"
            msgid "Vanishes in {turns} turn"
            msgid_plural "Vanishes in {turns} turns"
            msgstr[0] "Desaparece en {turns} turno"
            msgstr[1] "Desaparece en {turns} turnos"
            """.trimIndent()
        // When
        val entry = PoFile.parse(content).entries.single()
        // Then
        entry.idPlural shouldBe "Vanishes in {turns} turns"
        entry.translations.shouldContainExactly("Desaparece en {turns} turno", "Desaparece en {turns} turnos")
    }

    @Test
    fun `Parse should join the strings of a field spanning several lines and unescape them`() {
        // Given
        val content =
            """
            msgctxt "ui.long"
            msgid ""
            "A \"long\" "
            "text\twith\\escapes\n"
            msgstr ""
            "Un texto\r"
            """.trimIndent()
        // When
        val entry = PoFile.parse(content).entries.single()
        // Then
        entry.id shouldBe "A \"long\" text\twith\\escapes\n"
        entry.translations.shouldContainExactly("Un texto\r")
    }

    @Test
    fun `Parse should read the flags of an entry`() {
        // Given
        val content =
            """
            #, fuzzy, c-format
            msgctxt "ui.shop.buy"
            msgid "Buy"
            msgstr "Comprar"
            """.trimIndent()
        // When
        val entry = PoFile.parse(content).entries.single()
        // Then
        entry.flags shouldBe setOf("fuzzy", "c-format")
    }

    @Test
    fun `Parse should skip the obsolete entries and the byte order mark`() {
        // Given
        val content = "\uFEFF" + "#~ msgctxt \"ui.old\"\n#~ msgid \"Old\"\n#~ msgstr \"Viejo\"\nmsgctxt \"ui.new\"\nmsgid \"New\"\nmsgstr \"Nuevo\"\n"
        // When
        val file = PoFile.parse(content)
        // Then
        file.entries.map(Entry::context).shouldContainExactly("ui.new")
    }

    @Test
    fun `IsTranslated should be false when a form is empty or the entry is fuzzy`() {
        // Given
        val entry = Entry(context = "k", id = "a", idPlural = "b", translations = listOf("x", "y"), flags = emptySet())
        // Then
        entry.isTranslated shouldBe true
        entry.copy(translations = listOf("x", "")).isTranslated shouldBe false
        entry.copy(translations = emptyList()).isTranslated shouldBe false
        entry.copy(flags = setOf("fuzzy")).isTranslated shouldBe false
    }

    @Test
    fun `Parse should fail with the line when the file is not valid`() {
        val invalidFiles =
            mapOf(
                "msgid \"a\"\nmsgstr \"b\"\nunknown \"c\"" to "Line 3: Unexpected 'unknown \"c\"'",
                "msgid \"a\nmsgstr \"b\"" to "Line 1: Unterminated string \"a",
                "msgid \"a\"b\"\nmsgstr \"b\"" to "Line 1: Unescaped quote in \"a\"b\"",
                "msgid \"\\x\"\nmsgstr \"b\"" to "Line 1: Unknown escape in \"\\x\"",
                "\"a\"" to "Line 1: String without a keyword",
                "msgstr \"b\"" to "Line 1: msgstr without msgid",
                "msgid \"a\"\nmsgid \"b\"" to "Line 2: Unexpected msgid",
                "msgid \"a\"\nmsgctxt \"k\"" to "Line 2: Unexpected msgctxt",
                "msgid \"a\"\nmsgstr \"b\"\nmsgid_plural \"c\"" to "Line 3: Unexpected msgid_plural",
                "msgid \"a\"\nmsgid_plural \"b\"\nmsgstr \"c\"" to "Line 3: A plural message needs msgstr[n]",
                "msgid \"a\"\nmsgstr[0] \"b\"" to "Line 2: A singular message needs msgstr",
                "msgid \"a\"\nmsgid_plural \"b\"\nmsgstr[1] \"c\"" to "Line 3: Expected msgstr[0]",
                "msgid \"a\"" to "End of file: Entry 'a' without msgstr",
                "msgctxt \"k\"\nmsgstr \"b\"" to "Line 2: msgstr without msgid",
                "msgctxt \"k\"\nmsgid \"a\"\nmsgstr \"b\"\nmsgctxt \"k\"\nmsgid \"a\"\nmsgstr \"c\"" to "End of file: Duplicated entry 'k'",
                "#, fuzzy\nmsgctxt \"k\"" to "End of file: Entry without msgid",
            )
        invalidFiles.forEach { (content, message) ->
            shouldThrow<IllegalArgumentException> { PoFile.parse(content) }.message shouldBe message
        }
    }
}
