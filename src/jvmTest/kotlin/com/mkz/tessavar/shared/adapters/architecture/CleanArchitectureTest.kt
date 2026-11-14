package com.mkz.tessavar.shared.adapters.architecture

import com.mkz.tessavar.ability.domain.Ability
import com.tngtech.archunit.core.domain.JavaClasses
import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.lang.ArchRule
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses
import io.kotest.matchers.comparables.shouldBeGreaterThan
import java.io.File
import kotlin.test.Test

class CleanArchitectureTest {
    private val productionClasses: JavaClasses =
        run {
            val codeSourceLocation = Ability::class.java.protectionDomain.codeSource.location
            ClassFileImporter().importPath(File(codeSourceLocation.toURI()).toPath())
        }

    private val externalPackages =
        arrayOf(
            "kotlin",
            "kotlin..",
            "java",
            "java..",
            "kotlinx",
            "kotlinx..",
            "org.jetbrains",
            "org.jetbrains..",
        )

    @Test
    fun `should only depend on domain classes when class is declared in a domain folder`() {
        // Given
        val rule: ArchRule =
            noClasses()
                .that()
                .resideInAPackage("..domain..")
                .should()
                .dependOnClassesThat()
                .resideOutsideOfPackages("..domain..", *externalPackages)
        // When
        rule.check(productionClasses)
        // Then
        val domainClassCount = productionClasses.count { it.getPackageName().split(".").contains("domain") }
        domainClassCount.shouldBeGreaterThan(0)
    }

    @Test
    fun `should only depend on usecases and domain classes when class is declared in a usecases folder`() {
        // Given
        val rule: ArchRule =
            noClasses()
                .that()
                .resideInAPackage("..usecases..")
                .should()
                .dependOnClassesThat()
                .resideOutsideOfPackages("..usecases..", "..domain..", *externalPackages)
        // When
        rule.check(productionClasses)
        // Then
        val useCaseClassCount = productionClasses.count { it.getPackageName().split(".").contains("usecases") }
        useCaseClassCount.shouldBeGreaterThan(0)
    }

    @Test
    fun `should allow adapters to depend on any layer when class is declared in an adapters folder`() {
        // Given
        val adapterClassCount = productionClasses.count { it.getPackageName().split(".").contains("adapters") }
        // When
        // Then
        adapterClassCount.shouldBeGreaterThan(0)
    }
}
