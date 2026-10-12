package com.mkz.rpg.shared.adapters.architecture

import com.mkz.rpg.ability.domain.Ability
import com.tngtech.archunit.core.domain.JavaClasses
import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.lang.ArchRule
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses
import io.kotest.matchers.comparables.shouldBeGreaterThan
import java.io.File
import kotlin.test.Test

class NoRunBlockingArchitectureTest {
    private val productionClasses: JavaClasses =
        run {
            val codeSourceLocation = Ability::class.java.protectionDomain.codeSource.location
            ClassFileImporter().importPath(File(codeSourceLocation.toURI()).toPath())
        }

    // kotlinx.coroutines.BuildersKt is the file facade that contains the blocking
    // builders (runBlocking, runInterruptible) and is not available on every
    // Kotlin Multiplatform target (e.g.: JS).
    @Test
    fun `should not use runBlocking when the project is multiplatform`() {
        // Given
        val rule: ArchRule =
            noClasses()
                .should()
                .dependOnClassesThat()
                .haveFullyQualifiedName("kotlinx.coroutines.BuildersKt")
        // When
        rule.check(productionClasses)
        // Then
        val productionClassCount = productionClasses.size
        productionClassCount.shouldBeGreaterThan(0)
    }
}
