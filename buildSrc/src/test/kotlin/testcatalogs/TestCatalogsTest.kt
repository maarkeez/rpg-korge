package testcatalogs

import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import java.io.File
import java.nio.file.Files
import kotlin.test.Test

class TestCatalogsTest {
    private val localizationRoot: File = Files.createTempDirectory("localization").toFile()

    @Test
    fun `should hold every catalog by file name sorted when writes the source`() {
        // Given
        localizationRoot.resolve("messages.pot").writeText("msgid \"\"\nmsgstr \"\"")
        localizationRoot.resolve("es.po").writeText("msgid \"Hi\"")
        // When
        val source = TestCatalogs.source(localizationRoot, "com.example", "Catalogs")
        // Then
        source shouldContain "package com.example"
        source shouldContain "object Catalogs {"
        (source.indexOf("\"es.po\" to") < source.indexOf("\"messages.pot\" to")) shouldBe true
        source shouldContain "                    \"msgid \\\"\\\"\",\n                    \"msgstr \\\"\\\"\",\n"
    }

    @Test
    fun `should leave out the files that are not catalogs when writes the source`() {
        // Given
        localizationRoot.resolve("messages.pot").writeText("")
        localizationRoot.resolve("README.md").writeText("")
        // When
        val source = TestCatalogs.source(localizationRoot, "com.example", "Catalogs")
        // Then
        source shouldNotContain "README.md"
    }

    @Test
    fun `should escape the backslashes and dollars when writes the source`() {
        // Given
        localizationRoot.resolve("messages.pot").writeText("msgid \"a\\nb \$gold\"")
        // When
        val source = TestCatalogs.source(localizationRoot, "com.example", "Catalogs")
        // Then
        source shouldContain "\"msgid \\\"a\\\\nb \\\$gold\\\"\","
    }
}
