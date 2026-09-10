package shared.adapters.architecture

import ability.domain.Ability
import com.tngtech.archunit.core.domain.JavaClasses
import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.lang.ArchRule
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.File

class CleanArchitectureTest {
    private val productionClasses: JavaClasses =
        run {
            val codeSourceLocation = Ability::class.java.protectionDomain.codeSource.location
            ClassFileImporter().importPath(File(codeSourceLocation.toURI()).toPath())
        }

    // Kotlin stdlib, JDK, kotlinx and JetBrains annotations are part of the compiled bytecode
    // and are not considered architecture dependencies.
    // Note: in ArchUnit package identifiers '*' matches a single package level, '..' matches all subpackages.
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
        assertThat(domainClassCount).isGreaterThan(0)
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
        assertThat(useCaseClassCount).isGreaterThan(0)
    }

    @Test
    fun `should allow adapters to depend on any layer when class is declared in an adapters folder`() {
        // Given
        // The adapters layer is the outermost layer and is intentionally unrestricted:
        // it may depend on domain classes, usecases and any external library
        // (presentation, storage, events, ...). There is no ArchRule to check.
        val adapterClassCount = productionClasses.count { it.getPackageName().split(".").contains("adapters") }
        // When
        // no dependency restrictions apply to the adapters layer
        // Then
        assertThat(adapterClassCount).isGreaterThan(0)
    }
}
