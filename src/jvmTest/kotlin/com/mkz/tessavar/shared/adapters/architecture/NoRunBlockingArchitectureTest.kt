package com.mkz.tessavar.shared.adapters.architecture

import com.mkz.tessavar.ability.domain.Ability
import com.tngtech.archunit.core.domain.JavaClasses
import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.lang.ArchRule
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses
import io.kotest.matchers.comparables.shouldBeGreaterThan
import java.io.File
import kotlin.test.Test

class NoRunBlockingArchitectureTest {
    private val blockingBuildersMissingOnJs = "kotlinx.coroutines.BuildersKt"

    private val productionClasses: JavaClasses =
        run {
            val codeSourceLocation = Ability::class.java.protectionDomain.codeSource.location
            ClassFileImporter().importPath(File(codeSourceLocation.toURI()).toPath())
        }

    @Test
    fun `should not use runBlocking when the project is multiplatform`() {
        // Given
        val rule: ArchRule =
            noClasses()
                .should()
                .dependOnClassesThat()
                .haveFullyQualifiedName(blockingBuildersMissingOnJs)
        // When
        rule.check(productionClasses)
        // Then
        val productionClassCount = productionClasses.size
        productionClassCount.shouldBeGreaterThan(0)
    }
}
