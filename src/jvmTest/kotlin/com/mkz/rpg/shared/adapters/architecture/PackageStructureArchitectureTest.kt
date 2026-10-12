package com.mkz.rpg.shared.adapters.architecture

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.declaration.KoFileDeclaration
import io.kotest.assertions.withClue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldNotBeEmpty
import kotlin.test.Test

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
        productionFiles.shouldNotBeEmpty()
        val violations =
            misplacedFiles.map { file ->
                "File '${file.path}' is declared in package '${file.packagee?.fullyQualifiedName}'. Allowed packages are " +
                    "'<subdomain>.adapters.<any name>', '<subdomain>.domain' and " +
                    "'<subdomain>.usecases.commands|queries|services'."
            }
        withClue(violations.joinToString("\n")) { violations.shouldBeEmpty() }
    }

    private fun KoFileDeclaration.isEntryPoint(): Boolean = packagee?.fullyQualifiedName == "com.mkz.rpg" && nameWithExtension == "Main.kt"

    private fun KoFileDeclaration.resideInAllowedPackage(): Boolean = packagee?.fullyQualifiedName?.let { allowedPackage.matches(it) } ?: false
}
