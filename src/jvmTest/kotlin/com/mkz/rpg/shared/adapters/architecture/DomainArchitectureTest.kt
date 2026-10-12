package com.mkz.rpg.shared.adapters.architecture

import com.lemonappdev.konsist.api.Konsist
import io.kotest.assertions.assertSoftly
import io.kotest.assertions.withClue
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

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
        val allClasses = Konsist.scopeFromProduction().classes()

        val aggregateRoots =
            allClasses.filter { it.isTopLevel }.mapNotNull { declaration ->
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

        assertSoftly {
            aggregateRoots.forEach { aggregateRoot ->
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
}
