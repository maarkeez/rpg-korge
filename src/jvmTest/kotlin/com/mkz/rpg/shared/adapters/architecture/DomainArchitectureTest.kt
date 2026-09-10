package com.mkz.rpg.shared.adapters.architecture

import com.lemonappdev.konsist.api.Konsist
import org.assertj.core.api.SoftAssertions
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
}
