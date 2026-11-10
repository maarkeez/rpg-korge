package com.mkz.rpg.shared.adapters.architecture

import io.kotest.assertions.withClue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldNotBeEmpty
import java.io.File
import kotlin.test.Test

class CommentArchitectureTest {
    private val rootPackage = "com.mkz.rpg"
    private val sourceRoot = File("src")
    private val productionSourceSets = setOf("commonMain", "jsMain", "jvmMain")

    private val subdomainsWithComments: Set<String> =
        setOf(
            "ability",
            "battle",
            "battlefield",
            "battleUnit",
            "building",
            "cpuBattleBrain",
            "cpuWanderBrain",
            "gameSetup",
            "inventory",
            "lootableItem",
            "lootDrop",
            "lootTable",
            "npc",
            "party",
            "quest",
            "screen",
            "storybook",
            "unit",
            "worldBuilder",
        )

    private val sourceFiles: List<SourceFile> =
        sourceRoot
            .walkTopDown()
            .filter { file -> file.isFile && file.extension == "kt" }
            .sortedBy { file -> file.path }
            .map(::sourceFileOf)
            .toList()

    private val fileComments: List<FileComment> =
        sourceFiles.flatMap { sourceFile -> CommentScanner.comments(sourceFile.text).map { comment -> FileComment(sourceFile, comment) } }

    @Test
    fun `CommentScanner should find line comments, block comments and KDoc with their line`() {
        // Given
        val text =
            listOf(
                "val health = 30 // trailing",
                "/** KDoc */",
                "/*",
                " block /* nested */",
                "*/",
                "fun `should heal when it's a turn`() {",
                "    // Given",
                "}",
            ).joinToString("\n")
        // When
        val comments = CommentScanner.comments(text)
        // Then
        comments shouldContainExactly
            listOf(
                Comment(1, "// trailing"),
                Comment(2, "/** KDoc */"),
                Comment(3, "/*\n block /* nested */\n*/"),
                Comment(7, "// Given"),
            )
    }

    @Test
    fun `CommentScanner should not find a comment when the slashes are in a string, a raw string or a char`() {
        // Given
        val text =
            listOf(
                "val url = \"https://maarkeez.github.io/rpg-korge/storybook/\"",
                "val quote = \"\\\" // still the string\"",
                "val raw = \"\"\"/* still */ // the raw string\"\"\"",
                "val slash = '/'",
                "val template = \"\${\"//\"} and \${listOf(\"/*\").first()}\"",
                "fun `should read // when it is a test name`() = Unit",
            ).joinToString("\n")
        // When
        val comments = CommentScanner.comments(text)
        // Then
        comments.shouldBeEmpty()
    }

    @Test
    fun `Source files should have no comment other than the markers when their subdomain is not listed`() {
        // Given
        val checkedComments = fileComments.filter { fileComment -> fileComment.sourceFile.subdomain !in subdomainsWithComments }
        // When
        val violations = checkedComments.filterNot { fileComment -> CommentScanner.isMarker(fileComment.comment) }.map(FileComment::describe)
        // Then
        sourceFiles.shouldNotBeEmpty()
        withClue("Comments that are not allowed:\n${violations.joinToString("\n")}\nSay it in a name or a test name instead.") {
            violations.shouldBeEmpty()
        }
    }

    @Test
    fun `Production files should have no Given, When or Then marker when their subdomain is not listed`() {
        // Given
        val productionComments =
            fileComments.filter { fileComment -> fileComment.sourceFile.sourceSet in productionSourceSets && fileComment.sourceFile.subdomain !in subdomainsWithComments }
        // When
        val violations = productionComments.filter { fileComment -> CommentScanner.isMarker(fileComment.comment) }.map(FileComment::describe)
        // Then
        withClue("Markers in production code:\n${violations.joinToString("\n")}") {
            violations.shouldBeEmpty()
        }
    }

    @Test
    fun `Listed subdomains should still have a comment, so the list can only shrink`() {
        // Given
        val subdomainsStillCommented =
            fileComments
                .filter { fileComment -> !CommentScanner.isMarker(fileComment.comment) || fileComment.sourceFile.sourceSet in productionSourceSets }
                .mapNotNull { fileComment -> fileComment.sourceFile.subdomain }
                .toSet()
        // When
        val cleanSubdomains = subdomainsWithComments.filter { subdomain -> subdomain !in subdomainsStillCommented }.sorted()
        // Then
        withClue("Subdomains with no comment left: $cleanSubdomains. Remove them from subdomainsWithComments.") {
            cleanSubdomains.shouldBeEmpty()
        }
    }

    private fun sourceFileOf(file: File): SourceFile {
        val text = file.readText()
        val packageName =
            Regex("""^package\s+([\w.]+)""", RegexOption.MULTILINE)
                .find(text)
                ?.groupValues
                ?.get(1)
                .orEmpty()
        return SourceFile(
            path = file.path,
            sourceSet = file.relativeTo(sourceRoot).invariantSeparatorsPath.substringBefore("/"),
            subdomain = subdomainOf(packageName),
            text = text,
        )
    }

    private fun subdomainOf(packageName: String): String? =
        packageName
            .removePrefix("$rootPackage.")
            .takeIf { it != packageName }
            ?.substringBefore(".")

    private data class SourceFile(
        val path: String,
        val sourceSet: String,
        val subdomain: String?,
        val text: String,
    )

    private data class FileComment(
        val sourceFile: SourceFile,
        val comment: Comment,
    ) {
        fun describe(): String = "${sourceFile.path}:${comment.line}: ${comment.text.lineSequence().first()}"
    }
}
