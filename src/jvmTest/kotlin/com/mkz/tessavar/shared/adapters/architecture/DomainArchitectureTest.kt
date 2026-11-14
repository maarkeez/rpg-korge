package com.mkz.tessavar.shared.adapters.architecture

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.declaration.KoClassDeclaration
import com.lemonappdev.konsist.api.declaration.KoFileDeclaration
import com.lemonappdev.konsist.api.provider.KoParentProvider
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
                .filter { it != "com.mkz.tessavar.shared.domain" }
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

                withClue("Aggregate root '$aggregateName' must declare a primary constructor.") {
                    aggregateRoot.primaryConstructor.shouldNotBeNull()
                }

                withClue("Aggregate root '$aggregateName' must have a private primary constructor.") {
                    aggregateRoot.primaryConstructor?.hasPrivateModifier shouldBe true
                }

                withClue("Aggregate root '$aggregateName' must be annotated with @ConsistentCopyVisibility.") {
                    aggregateRoot.hasAnnotationOf(ConsistentCopyVisibility::class).shouldBeTrue()
                }

                val nestedClasses = aggregateRoot.classes(includeNested = false, includeLocal = false)

                val dtoClasses =
                    nestedClasses.filter { it.name == "Dto" }

                withClue("Aggregate root '$aggregateName' must declare exactly one nested Dto class.") {
                    dtoClasses.shouldHaveSize(1)
                }

                dtoClasses.singleOrNull()?.let { dto ->
                    withClue("Aggregate root '$aggregateName.Dto' must be public.") {
                        dto.hasPublicOrDefaultModifier.shouldBeTrue()
                    }

                    dataDeclarations(dto).forEach { declaration ->
                        val declarationName = declaration.fullyQualifiedName.substringAfter("${aggregateRoot.packagee?.fullyQualifiedName}.")
                        withClue("Aggregate root '$declarationName' must not declare methods: the methods of its companion object are its factories.") {
                            declaration.functionNames.shouldBeEmpty()
                        }
                        withClue("Aggregate root '$declarationName' must not declare properties with a getter.") {
                            declaration.getterNames.shouldBeEmpty()
                        }
                    }

                    val packagePrefix = "${aggregateRoot.packagee?.fullyQualifiedName}."
                    withClue(
                        "Aggregate root '$aggregateName.Dto' must only declare data classes and enum classes: a choice between shapes is an enum with optional fields, never a sealed class or interface.",
                    ) {
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

                    (listOf(dto) + dto.classes(includeNested = true, includeLocal = false)).forEach { declaration ->
                        val declarationName = declaration.fullyQualifiedName.removePrefix(packagePrefix)
                        withClue("Aggregate root '$declarationName' must not declare default values: they belong to its factories.") {
                            declaration.primaryConstructor
                                ?.parameters
                                .orEmpty()
                                .filter { it.defaultValue != null }
                                .map { it.name }
                                .shouldBeEmpty()
                        }
                    }
                }

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
    fun `should pull the events of its own subdomain when it is an aggregate root`() {
        val roots = aggregateRoots()

        withClue("Expected the 23 aggregate roots of the game.") {
            roots.shouldHaveSize(23)
        }

        assertSoftly {
            roots.forEach { aggregateRoot ->
                val aggregateName = aggregateRoot.name
                val allowedReturnTypes =
                    setOf(
                        "Pair<Set<${aggregateName}Event>, $aggregateName>",
                        "Pair<Set<DomainEvent>, $aggregateName>",
                    )
                val pullFunctions =
                    aggregateRoot
                        .functions(includeNested = false, includeLocal = false)
                        .filter { it.name.startsWith("pull") && it.hasPublicOrDefaultModifier }

                withClue("Aggregate root '$aggregateName' must declare pullEvents() and no other public function starting with pull, found ${pullFunctions.map { it.name }}.") {
                    pullFunctions.map { it.name } shouldBe listOf("pullEvents")
                }

                pullFunctions.filter { it.name == "pullEvents" }.forEach { pullEvents ->
                    withClue(
                        "Aggregate root '$aggregateName' must write out the return type of pullEvents() as one of $allowedReturnTypes: " +
                            "the events of its own subdomain, or DomainEvent when it also requests another subdomain to act.",
                    ) {
                        (pullEvents.returnType?.text in allowedReturnTypes).shouldBeTrue()
                    }
                    withClue("Aggregate root '$aggregateName' must declare pullEvents() without parameters.") {
                        pullEvents.parameters.shouldBeEmpty()
                    }
                }
            }
        }
    }

    @Test
    fun `should take and give only its aggregate root or primitives when it is a repository`() {
        val primitives = setOf("String", "Int", "Long", "Double", "Float", "Boolean")
        val repositories =
            Konsist
                .scopeFromProduction()
                .interfaces()
                .filter { it.isTopLevel && it.packagee?.fullyQualifiedName?.endsWith(".domain") == true && it.name.endsWith("Repository") }

        withClue("Expected a repository interface in the domain packages.") {
            repositories.isNotEmpty().shouldBeTrue()
        }

        assertSoftly {
            repositories.forEach { repository ->
                val aggregateName =
                    repository.packagee!!
                        .fullyQualifiedName
                        .substringBeforeLast(".domain")
                        .substringAfterLast(".")
                        .replaceFirstChar { it.uppercase() }
                val allowedParameterTypes = primitives + primitives.map { "$it?" } + aggregateName
                val allowedReturnTypes = setOf(null, "Unit", aggregateName, "$aggregateName?", "List<$aggregateName>")

                repository.functions(includeNested = false, includeLocal = false).forEach { function ->
                    val functionName = "${repository.name}.${function.name}"
                    withClue("Repository function '$functionName' must take only its aggregate root '$aggregateName' or primitives $primitives.") {
                        function.parameters
                            .map { it.type.text }
                            .filterNot { it in allowedParameterTypes }
                            .shouldBeEmpty()
                    }
                    withClue("Repository function '$functionName' must return $aggregateName, nullable or a List of it, or nothing, but returns ${function.returnType?.text}.") {
                        (function.returnType?.text in allowedReturnTypes).shouldBeTrue()
                    }
                }
            }
        }
    }

    @Test
    fun `should be the event of its own domain package, a top-level sealed interface, when it extends DomainEvent`() {
        val scope = Konsist.scopeFromProduction()
        val events =
            scope
                .interfaces(includeNested = true)
                .filter {
                    it.extendsDomainEvent()
                }.map { EventDeclaration(it.fullyQualifiedName, it.name, it.packagee?.fullyQualifiedName, it.isTopLevel, it.hasSealedModifier) } +
                scope
                    .classes(includeNested = true, includeLocal = true)
                    .filter {
                        it.extendsDomainEvent()
                    }.map { EventDeclaration(it.fullyQualifiedName, it.name, it.packagee?.fullyQualifiedName, it.isTopLevel, false) } +
                scope.objects(includeNested = true).filter { it.extendsDomainEvent() }.map { EventDeclaration(it.fullyQualifiedName, it.name, it.packagee?.fullyQualifiedName, it.isTopLevel, false) }

        withClue("Expected the event of every domain package to extend DomainEvent.") {
            events.isNotEmpty().shouldBeTrue()
        }

        assertSoftly {
            events.forEach { event ->
                val packageName = event.packageName.orEmpty()
                val isSubdomainPackage = packageName.endsWith(".domain") && packageName != "com.mkz.tessavar.shared.domain"
                withClue("'${event.fullyQualifiedName}' extends DomainEvent, so it must be declared in the domain package of a subdomain.") {
                    isSubdomainPackage.shouldBeTrue()
                }
                if (isSubdomainPackage) {
                    withClue("'${event.fullyQualifiedName}' extends DomainEvent, so it must be named '${subdomainOf(packageName)}Event', after its domain package.") {
                        event.name shouldBe "${subdomainOf(packageName)}Event"
                    }
                }
                withClue("'${event.fullyQualifiedName}' extends DomainEvent, so it must be a top-level sealed interface.") {
                    (event.isTopLevel && event.isSealedInterface).shouldBeTrue()
                }
            }
        }
    }

    @Test
    fun `should extend DomainEvent when it is the event of a domain package`() {
        val scope = Konsist.scopeFromProduction()
        val declarations =
            scope.interfaces().filter { it.isTopLevel }.map { it.fullyQualifiedName to it.extendsDomainEvent() } +
                scope.classes().filter { it.isTopLevel }.map { it.fullyQualifiedName to it.extendsDomainEvent() } +
                scope.objects().filter { it.isTopLevel }.map { it.fullyQualifiedName to it.extendsDomainEvent() }

        val subdomainEvents =
            declarations.filter { (fullyQualifiedName, _) ->
                val packageName = fullyQualifiedName.substringBeforeLast(".")
                packageName.endsWith(".domain") &&
                    packageName != "com.mkz.tessavar.shared.domain" &&
                    fullyQualifiedName.substringAfterLast(".") == "${subdomainOf(packageName)}Event"
            }

        withClue("Expected the event of every domain package.") {
            subdomainEvents.isNotEmpty().shouldBeTrue()
        }

        withClue("The event of every domain package must extend DomainEvent, so the event bus can publish it.") {
            subdomainEvents.filterNot { (_, extendsDomainEvent) -> extendsDomainEvent }.map { (fullyQualifiedName, _) -> fullyQualifiedName }.shouldBeEmpty()
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
            if (packageName == "com.mkz.tessavar.shared.domain") {
                return@mapNotNull null
            }
            val expectedAggregateRootName = packageName.substringBeforeLast(".domain").substringAfterLast(".").replaceFirstChar { it.uppercase() }
            declaration.takeIf { it.name == expectedAggregateRootName }
        }

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

    private fun KoParentProvider.extendsDomainEvent(): Boolean = parents.any { parent -> parent.name.substringAfterLast(".") == "DomainEvent" }

    private fun subdomainOf(domainPackageName: String): String = domainPackageName.substringBeforeLast(".domain").substringAfterLast(".").replaceFirstChar { it.uppercase() }

    private class EventDeclaration(
        val fullyQualifiedName: String,
        val name: String,
        val packageName: String?,
        val isTopLevel: Boolean,
        val isSealedInterface: Boolean,
    )

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
        fun receiverFullyQualifiedName(): String {
            val type = receiver.substringBefore("<").removeSuffix("?")
            val firstSegment = type.substringBefore(".")
            val imported = file.imports.firstOrNull { it.name.substringAfterLast(".") == firstSegment }?.name
            val packageName = imported?.substringBeforeLast(".") ?: file.packagee?.fullyQualifiedName
            return listOfNotNull(packageName, type).joinToString(".")
        }
    }
}
