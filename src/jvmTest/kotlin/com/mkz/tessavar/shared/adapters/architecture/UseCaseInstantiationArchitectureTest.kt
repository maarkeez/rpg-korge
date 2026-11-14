package com.mkz.tessavar.shared.adapters.architecture

import com.lemonappdev.konsist.api.Konsist
import io.kotest.assertions.assertSoftly
import io.kotest.assertions.withClue
import io.kotest.matchers.collections.shouldBeEmpty
import kotlin.test.Test

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
                val apiName = subdomainPackage.substringAfterLast(".").replaceFirstChar(Char::uppercase) + "Api"
                val apiPackage = "$subdomainPackage.adapters.presentation"
                val call = Regex("""(?<![\w.]|class |fun )${useCase.name}\(""")

                val instantiations =
                    scope.files
                        .filter { file -> file.packagee?.fullyQualifiedName == useCasePackage || file.imports.any { it.name == useCase.fullyQualifiedName } }
                        .filterNot { file -> file.packagee?.fullyQualifiedName == apiPackage && file.name == apiName }
                        .flatMap { file -> call.findAll(file.text).map { match -> "${file.name}.kt:${file.text.lineNumberAt(match.range.first)}" } }

                withClue("Use case '${useCase.name}' must only be instantiated in '$apiName', found in: $instantiations") {
                    instantiations.shouldBeEmpty()
                }
            }
        }
    }

    private fun String.lineNumberAt(index: Int) = substring(0, index).count { it == '\n' } + 1
}
