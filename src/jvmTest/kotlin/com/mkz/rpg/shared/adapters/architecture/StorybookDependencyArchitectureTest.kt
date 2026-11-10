package com.mkz.rpg.shared.adapters.architecture

import com.lemonappdev.konsist.api.Konsist
import io.kotest.assertions.withClue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldNotBeEmpty
import kotlin.test.Test

class StorybookDependencyArchitectureTest {
    private val rootPackage = "com.mkz.rpg"
    private val storybookPackage = "$rootPackage.storybook"
    private val fullyAllowedSubdomains = setOf("shared", "localization", "storybook")

    private val files = Konsist.scopeFromProduction().files

    @Test
    fun `should only import the presentation of other subdomains when it is the storybook subdomain`() {
        // Given
        val storybookFiles = files.filter { file -> file.packagee?.fullyQualifiedName?.startsWith("$storybookPackage.") == true }
        // When
        val forbiddenImports =
            storybookFiles.flatMap { file ->
                file.imports
                    .map { import -> import.name }
                    .filterNot(::isAllowedForStorybook)
                    .map { import -> "$import (${file.name})" }
            }
        // Then
        storybookFiles.shouldNotBeEmpty()
        withClue("The storybook imports from other subdomains out of their presentation package: $forbiddenImports. Feed the views plain data instead.") {
            forbiddenImports.shouldBeEmpty()
        }
    }

    @Test
    fun `should not be imported by other subdomains when it is the storybook subdomain`() {
        // Given
        val otherFiles = files.filterNot { file -> file.packagee?.fullyQualifiedName?.let { it == rootPackage || it.startsWith("$storybookPackage.") } == true }
        // When
        val importingFiles = otherFiles.filter { file -> file.imports.any { import -> import.name.startsWith("$storybookPackage.") } }.map { file -> file.path }
        // Then
        withClue("Files of other subdomains importing the storybook: $importingFiles. Only the entry point (Main.kt) may start it.") {
            importingFiles.shouldBeEmpty()
        }
    }

    private fun isAllowedForStorybook(import: String): Boolean {
        val relativeName = import.removePrefix("$rootPackage.").takeIf { it != import && it.contains(".") } ?: return true
        val subdomain = relativeName.substringBefore(".")
        if (subdomain in fullyAllowedSubdomains) return true
        val declaration = relativeName.removePrefix("$subdomain.adapters.presentation.").takeIf { it != relativeName } ?: return false
        return !declaration.contains(".") || declaration.first().isUpperCase()
    }
}
