package com.mkz.rpg.shared.adapters.architecture

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.declaration.KoFileDeclaration
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.SoftAssertions
import org.junit.jupiter.api.Test

class PackageStructureArchitectureTest {
    // Each subdomain lives in `com.mkz.rpg.<subdomain>`.
    // `battlefieldHud` is a presentation-only subdomain nested in the `screen` presentation adapter.
    private val subdomainRoots =
        listOf(
            """com\.mkz\.rpg\.[^.]+""",
            """com\.mkz\.rpg\.screen\.adapters\.presentation\.battlefieldHud""",
        )

    private val layerPackages =
        listOf(
            """adapters\.[^.]+""",
            """domain""",
            """usecases\.(commands|queries|services)""",
        )

    private val allowedPackage =
        Regex("""(${subdomainRoots.joinToString("|")})\.(${layerPackages.joinToString("|")})""")

    @Test
    fun `should reside in a layer package of a subdomain when is a production file`() {
        // Given
        val productionFiles = Konsist.scopeFromProduction().files
        // When
        val misplacedFiles = productionFiles.filterNot { it.isEntryPoint() || it.resideInAllowedPackage() }
        // Then
        assertThat(productionFiles).isNotEmpty
        SoftAssertions.assertSoftly { softly ->
            misplacedFiles.forEach { file ->
                softly
                    .fail<Unit>(
                        "File '%s' is declared in package '%s'. Allowed packages are " +
                            "'<subdomain>.adapters.<any name>', '<subdomain>.domain' and " +
                            "'<subdomain>.usecases.commands|queries|services'.",
                        file.path,
                        file.packagee?.fullyQualifiedName,
                    )
            }
        }
    }

    private fun KoFileDeclaration.isEntryPoint(): Boolean = packagee?.fullyQualifiedName == "com.mkz.rpg" && nameWithExtension == "Main.kt"

    private fun KoFileDeclaration.resideInAllowedPackage(): Boolean = packagee?.fullyQualifiedName?.let { allowedPackage.matches(it) } ?: false
}
