package com.mkz.rpg.shared.adapters.architecture

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.declaration.KoClassDeclaration
import com.lemonappdev.konsist.api.declaration.KoFileDeclaration
import io.kotest.assertions.assertSoftly
import io.kotest.assertions.withClue
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class DomainArchitectureTest {
    @Test
    fun `should be named as Domain, DomainEvent, DomainRepository or DomainError when it is a domain package top level class`() {
        val allDeclarations = Konsist.scopeFromProduction().classes() + Konsist.scopeFromProduction().interfaces()

        val domainPackages =
            allDeclarations
                .mapNotNull { it.packagee?.fullyQualifiedName }
                .filter { it.substringAfterLast(".") == "domain" }
                .filter { it != "com.mkz.rpg.shared.domain" }
                .distinct()

        assertSoftly {
            domainPackages.forEach { domainPackageName ->
                val subdomain =
                    domainPackageName
                        .substringBeforeLast(".domain")
                        .substringAfterLast(".")
                        .replaceFirstChar { it.uppercase() }

                val expected =
                    setOf(
                        subdomain,
                        "${subdomain}Error",
                        "${subdomain}Event",
                        "${subdomain}Repository",
                    )

                val actual =
                    allDeclarations
                        .filter { it.packagee?.fullyQualifiedName == domainPackageName && it.isTopLevel }
                        .map { it.name }
                        .toSet()

                val unexpectedNames = actual - expected

                withClue("Package '$domainPackageName' has unexpected declaration(s) $unexpectedNames. Allowed names for this subdomain are $expected.") {
                    unexpectedNames.shouldBeEmpty()
                }
            }
        }
    }

    @Test
    fun `should follow domain encapsulation rules when is the aggregate root`() {
        assertSoftly {
            aggregateRoots().forEach { aggregateRoot ->
                val aggregateName = aggregateRoot.name

                // Aggregate root must have a private primary constructor.
                withClue("Aggregate root '$aggregateName' must declare a primary constructor.") {
                    aggregateRoot.primaryConstructor.shouldNotBeNull()
                }

                withClue("Aggregate root '$aggregateName' must have a private primary constructor.") {
                    aggregateRoot.primaryConstructor?.hasPrivateModifier shouldBe true
                }

                // Aggregate root must be annotated with @ConsistentCopyVisibility.
                withClue("Aggregate root '$aggregateName' must be annotated with @ConsistentCopyVisibility.") {
                    aggregateRoot.hasAnnotationOf(ConsistentCopyVisibility::class).shouldBeTrue()
                }

                val nestedClasses = aggregateRoot.classes(includeNested = false, includeLocal = false)

                // Aggregate root must declare exactly one Dto nested class.
                val dtoClasses =
                    nestedClasses.filter { it.name == "Dto" }

                withClue("Aggregate root '$aggregateName' must declare exactly one nested Dto class.") {
                    dtoClasses.shouldHaveSize(1)
                }

                // Dto must be public.
                dtoClasses.singleOrNull()?.let { dto ->
                    withClue("Aggregate root '$aggregateName.Dto' must be public.") {
                        dto.hasPublicOrDefaultModifier.shouldBeTrue()
                    }

                    // Dto and the declarations nested in it are plain data: companion object methods are their factories.
                    dataDeclarations(dto).forEach { declaration ->
                        val declarationName = declaration.fullyQualifiedName.substringAfter("${aggregateRoot.packagee?.fullyQualifiedName}.")
                        withClue("Aggregate root '$declarationName' must not declare methods.") {
                            declaration.functionNames.shouldBeEmpty()
                        }
                        withClue("Aggregate root '$declarationName' must not declare properties with a getter.") {
                            declaration.getterNames.shouldBeEmpty()
                        }
                    }

                    // Dto and the declarations nested in it are data classes or enum classes: a choice between shapes is an
                    // enum type with optional fields, never a sealed class or interface.
                    val packagePrefix = "${aggregateRoot.packagee?.fullyQualifiedName}."
                    withClue("Aggregate root '$aggregateName.Dto' must only declare data classes and enum classes.") {
                        (listOf(dto) + dto.classes(includeNested = true, includeLocal = false))
                            .filterNot { it.hasDataModifier || it.hasEnumModifier }
                            .map { it.fullyQualifiedName.removePrefix(packagePrefix) }
                            .shouldBeEmpty()
                    }
                    withClue("Aggregate root '$aggregateName.Dto' must not declare interfaces.") {
                        dto.interfaces(includeNested = true).map { it.fullyQualifiedName.removePrefix(packagePrefix) }.shouldBeEmpty()
                    }
                    withClue("Aggregate root '$aggregateName.Dto' must not declare objects other than companion objects.") {
                        dto
                            .objects(includeNested = true)
                            .filterNot { it.hasCompanionModifier }
                            .map { it.fullyQualifiedName.removePrefix(packagePrefix) }
                            .shouldBeEmpty()
                    }
                }
                // TODO: Assert it contains a public method called `toDto()` with return type `<subdomain>.Dto`

                // All other nested classes must be private.
                nestedClasses
                    .filterNot { it.name == "Dto" }
                    .forEach { nestedClass ->
                        withClue("Nested class '$aggregateName.${nestedClass.name}' must be private.") {
                            nestedClass.hasPrivateModifier.shouldBeTrue()
                        }
                    }
            }
        }
    }

    @Test
    fun `should not declare extensions on the Dto of an aggregate root`() {
        val dtoNames =
            aggregateRoots()
                .flatMap { aggregateRoot -> aggregateRoot.classes(includeNested = false, includeLocal = false).filter { it.name == "Dto" } }
                .flatMap { dto -> dataDeclarations(dto).map { it.fullyQualifiedName } }
                .toSet()

        val scope = Konsist.scopeFromProduction()
        val extensions =
            scope.functions(includeNested = true, includeLocal = true).mapNotNull { function ->
                function.receiverType?.let { receiverType -> Extension(function.name, receiverType.sourceType, function.containingFile) }
            } +
                scope.properties(includeNested = true, includeLocal = true).mapNotNull { property ->
                    property.receiverType?.let { receiverType -> Extension(property.name, receiverType.sourceType, property.containingFile) }
                }

        val dtoExtensions =
            extensions
                .filter { extension -> extension.receiverFullyQualifiedName() in dtoNames }
                .map { extension -> "${extension.receiver}.${extension.name} in ${extension.file.name}" }

        withClue("Extensions must not be declared on the Dto of an aggregate root, nor on a declaration nested in it.") {
            dtoExtensions.shouldBeEmpty()
        }
    }

    private fun aggregateRoots(): List<KoClassDeclaration> =
        Konsist.scopeFromProduction().classes().filter { it.isTopLevel }.mapNotNull { declaration ->
            val packageName = declaration.packagee?.fullyQualifiedName ?: return@mapNotNull null
            if (!packageName.endsWith(".domain")) {
                return@mapNotNull null
            }
            if (packageName == "com.mkz.rpg.shared.domain") {
                return@mapNotNull null
            }
            val expectedAggregateRootName = packageName.substringBeforeLast(".domain").substringAfterLast(".").replaceFirstChar { it.uppercase() }
            declaration.takeIf { it.name == expectedAggregateRootName }
        }

    // The Dto, with the classes and interfaces nested in it at any depth. Companion objects are left out: their methods
    // are the factories of the Dto.
    private fun dataDeclarations(dto: KoClassDeclaration): List<DataDeclaration> =
        (listOf(dto) + dto.classes(includeNested = true, includeLocal = false)).map { declaration ->
            DataDeclaration(
                fullyQualifiedName = declaration.fullyQualifiedName,
                functionNames = declaration.functions(includeNested = false, includeLocal = false).map { it.name },
                getterNames = declaration.properties(includeNested = false, includeLocal = false).filter { it.hasGetter }.map { it.name },
            )
        } +
            dto.interfaces(includeNested = true).map { declaration ->
                DataDeclaration(
                    fullyQualifiedName = declaration.fullyQualifiedName,
                    functionNames = declaration.functions(includeNested = false, includeLocal = false).map { it.name },
                    getterNames = declaration.properties(includeNested = false, includeLocal = false).filter { it.hasGetter }.map { it.name },
                )
            }

    private class DataDeclaration(
        val fullyQualifiedName: String,
        val functionNames: List<String>,
        val getterNames: List<String>,
    )

    private class Extension(
        val name: String,
        val receiver: String,
        val file: KoFileDeclaration,
    ) {
        // Konsist does not resolve a nested receiver type (`Npc.Dto` stays `Npc.Dto`), so its first segment is resolved
        // with the imports of the file, or else with its package.
        fun receiverFullyQualifiedName(): String {
            val type = receiver.substringBefore("<").removeSuffix("?")
            val firstSegment = type.substringBefore(".")
            val imported = file.imports.firstOrNull { it.name.substringAfterLast(".") == firstSegment }?.name
            val packageName = imported?.substringBeforeLast(".") ?: file.packagee?.fullyQualifiedName
            return listOfNotNull(packageName, type).joinToString(".")
        }
    }
}
