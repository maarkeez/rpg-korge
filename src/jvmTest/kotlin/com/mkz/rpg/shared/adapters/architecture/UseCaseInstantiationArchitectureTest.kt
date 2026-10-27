package com.mkz.rpg.shared.adapters.architecture

import com.lemonappdev.konsist.api.Konsist
import io.kotest.assertions.assertSoftly
import io.kotest.assertions.withClue
import io.kotest.matchers.collections.shouldBeEmpty
import kotlin.test.Test

/**
 * The use cases of a subdomain (its commands, queries and services) must only be instantiated in its presentation Api,
 * `<subdomain>.adapters.presentation.<Subdomain>Api`, which injects them where they are needed (see
 * docs/architecture/package-structure.md).
 */
class UseCaseInstantiationArchitectureTest {
    private val useCasePackages = listOf("..usecases.commands..", "..usecases.queries..", "..usecases.services..")

    @Test
    fun `should only be instantiated in the Api of their subdomain when they are use cases`() {
        val scope = Konsist.scopeFromProduction()
        val useCases =
            scope
                .classes()
                .filter { useCase -> useCasePackages.any(useCase::resideInPackage) }
                .filter { it.isTopLevel }

        assertSoftly {
            useCases.forEach { useCase ->
                val useCasePackage = useCase.packagee?.fullyQualifiedName ?: return@forEach
                val subdomainPackage = useCasePackage.substringBeforeLast(".usecases.")
                // Compared ignoring case, as a package is lowercase where its Api is not, e.g.: `battlesetup` and `BattleSetupApi`.
                val apiName = subdomainPackage.substringAfterLast(".").replaceFirstChar(Char::uppercase) + "Api"
                val apiPackage = "$subdomainPackage.adapters.presentation"
                // A constructor call, not a declaration nor a call on another receiver, e.g.: `BattlefieldCreation(`.
                val call = Regex("""(?<![\w.]|class |fun )${useCase.name}\(""")

                val instantiations =
                    scope.files
                        // Only the files that see the use case, so a homonym declared elsewhere does not count.
                        .filter { file -> file.packagee?.fullyQualifiedName == useCasePackage || file.imports.any { it.name == useCase.fullyQualifiedName } }
                        .filterNot { file -> file.packagee?.fullyQualifiedName == apiPackage && file.name.equals(apiName, ignoreCase = true) }
                        .flatMap { file -> call.findAll(file.text).map { match -> "${file.name}.kt:${file.text.lineNumberAt(match.range.first)}" } }

                withClue("Use case '${useCase.name}' must only be instantiated in '$apiName', found in: $instantiations") {
                    instantiations.shouldBeEmpty()
                }
            }
        }
    }

    private fun String.lineNumberAt(index: Int) = substring(0, index).count { it == '\n' } + 1
}
