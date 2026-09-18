package com.mkz.rpg.shared.adapters.architecture

import com.lemonappdev.konsist.api.Konsist
import org.assertj.core.api.SoftAssertions
import org.junit.jupiter.api.Disabled
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

        SoftAssertions.assertSoftly { softly ->
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

                softly
                    .assertThat(unexpectedNames)
                    .withFailMessage(
                        "Package '%s' has unexpected declaration(s) %s. Allowed names for this subdomain are %s.",
                        domainPackageName,
                        unexpectedNames,
                        expected,
                    ).isEmpty()
            }
        }
    }

    // TODO: Refactor code to met the new architecture rule
    @Test
    @Disabled("Requires refactor before it pass")
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

        SoftAssertions.assertSoftly { softly ->
            aggregateRoots.forEach { aggregateRoot ->
                val aggregateName = aggregateRoot.name

                // Aggregate root must have a private primary constructor.
                softly
                    .assertThat(aggregateRoot.primaryConstructor)
                    .withFailMessage(
                        "Aggregate root '%s' must declare a primary constructor.",
                        aggregateName,
                    ).isNotNull

                softly
                    .assertThat(aggregateRoot.primaryConstructor?.hasPrivateModifier)
                    .withFailMessage(
                        "Aggregate root '%s' must have a private primary constructor.",
                        aggregateName,
                    ).isTrue

                // Aggregate root must be annotated with @ConsistentCopyVisibility.
                softly
                    .assertThat(
                        aggregateRoot.hasAnnotationOf(ConsistentCopyVisibility::class),
                    ).withFailMessage(
                        "Aggregate root '%s' must be annotated with @ConsistentCopyVisibility.",
                        aggregateName,
                    ).isTrue

                val nestedClasses = aggregateRoot.classes(includeNested = false, includeLocal = false)

                // Aggregate root must declare exactly one Dto nested class.
                val dtoClasses =
                    nestedClasses.filter { it.name == "Dto" }

                softly
                    .assertThat(dtoClasses)
                    .withFailMessage(
                        "Aggregate root '%s' must declare exactly one nested Dto class.",
                        aggregateName,
                    ).hasSize(1)

                // Dto must be public.
                dtoClasses.singleOrNull()?.let { dto ->
                    softly
                        .assertThat(dto.hasPublicOrDefaultModifier)
                        .withFailMessage(
                            "Aggregate root '%s.Dto' must be public.",
                            aggregateName,
                        ).isTrue
                }
                // TODO: Assert it contains a public method called `toDto()` with return type `<subdomain>.Dto`

                // All other nested classes must be private.
                nestedClasses
                    .filterNot { it.name == "Dto" }
                    .forEach { nestedClass ->
                        softly
                            .assertThat(nestedClass.hasPrivateModifier)
                            .withFailMessage(
                                "Nested class '%s.%s' must be private.",
                                aggregateName,
                                nestedClass.name,
                            ).isTrue
                    }
            }
        }
    }
}
