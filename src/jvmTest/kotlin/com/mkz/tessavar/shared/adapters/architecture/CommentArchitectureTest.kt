package com.mkz.tessavar.shared.adapters.architecture

import io.kotest.assertions.withClue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldNotBeEmpty
import java.io.File
import kotlin.test.Test

class CommentArchitectureTest {
    private val sourceRoot = File("src")
    private val productionSourceSets = setOf("commonMain", "jsMain", "jvmMain")

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
    fun `Source files should have no comment other than the markers`() {
        // When
        val violations = fileComments.filterNot { fileComment -> CommentScanner.isMarker(fileComment.comment) }.map(FileComment::describe)
        // Then
        sourceFiles.shouldNotBeEmpty()
        withClue("Comments that are not allowed:\n${violations.joinToString("\n")}\nSay it in a name or a test name instead.") {
            violations.shouldBeEmpty()
        }
    }

    @Test
    fun `Production files should have no Given, When or Then marker`() {
        // Given
        val productionComments = fileComments.filter { fileComment -> fileComment.sourceFile.sourceSet in productionSourceSets }
        // When
        val violations = productionComments.filter { fileComment -> CommentScanner.isMarker(fileComment.comment) }.map(FileComment::describe)
        // Then
        withClue("Markers in production code:\n${violations.joinToString("\n")}") {
            violations.shouldBeEmpty()
        }
    }

    private fun sourceFileOf(file: File): SourceFile =
        SourceFile(
            path = file.path,
            sourceSet = file.relativeTo(sourceRoot).invariantSeparatorsPath.substringBefore("/"),
            text = file.readText(),
        )

    private data class SourceFile(
        val path: String,
        val sourceSet: String,
        val text: String,
    )

    private data class FileComment(
        val sourceFile: SourceFile,
        val comment: Comment,
    ) {
        fun describe(): String = "${sourceFile.path}:${comment.line}: ${comment.text.lineSequence().first()}"
    }
}
