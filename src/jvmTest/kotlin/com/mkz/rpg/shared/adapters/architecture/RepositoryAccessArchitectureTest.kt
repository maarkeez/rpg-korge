package com.mkz.rpg.shared.adapters.architecture

import com.lemonappdev.konsist.api.Konsist
import io.kotest.assertions.withClue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldNotBeEmpty
import kotlin.test.Test

/**
 * The domain declares a repository, its storage adapter implements it, and only its use cases call it: the Api of its
 * subdomain only builds it and hands it to them. So no adapter reads or writes an aggregate around the use cases (e.g.:
 * a snapshottable restoring a save). The tests are not checked: they arrange their state through the repositories. See
 * docs/architecture/package-structure.md.
 */
class RepositoryAccessArchitectureTest {
    private val rootPackage = "com.mkz.rpg"

    // The snapshottables still calling their repository, until they take their snapshot through use cases: the list can only shrink.
    private val allowedSnapshottables =
        setOf(
            "$rootPackage.battleUnit.adapters.save",
            "$rootPackage.battlefield.adapters.save",
            "$rootPackage.npc.adapters.save",
            "$rootPackage.lootDrop.adapters.save",
            "$rootPackage.inventory.adapters.save",
            "$rootPackage.cpuWanderBrain.adapters.save",
        )

    private val files = Konsist.scopeFromProduction().files

    // A repository is a `…Repository` declared in a domain package, e.g.: `party.domain.PartyRepository`.
    private val repositoryImports: List<RepositoryImport> =
        files.flatMap { file ->
            val packageName = file.packagee?.fullyQualifiedName ?: return@flatMap emptyList()
            file.imports
                .map { import -> import.name }
                .filter { import -> import.startsWith("$rootPackage.") && import.substringAfterLast(".").endsWith("Repository") && import.substringBeforeLast(".").endsWith(".domain") }
                .map { import -> RepositoryImport(repository = import, moduleRoot = import.substringBeforeLast(".").removeSuffix(".domain"), packageName = packageName, fileName = file.name) }
        }

    private val apiFiles = files.filter { file -> file.packagee?.fullyQualifiedName?.endsWith(".adapters.presentation") == true && file.name.endsWith("Api") }

    @Test
    fun `should only be imported by the domain, the use cases, the storage and the Api of its subdomain when it is a repository`() {
        // When
        val forbiddenImports =
            repositoryImports
                .filterNot(::isAllowed)
                .map { import -> "${import.repository} (${import.packageName}.${import.fileName})" }
                .sorted()
        // Then
        repositoryImports.shouldNotBeEmpty()
        withClue("Repositories imported out of the domain, the use cases, the storage and the Api of their subdomain: $forbiddenImports. Call a use case instead.") {
            forbiddenImports.shouldBeEmpty()
        }
    }

    @Test
    fun `should not be called by the Api of its subdomain when it is a repository`() {
        // When
        val calls =
            apiFiles.flatMap { file ->
                file.text
                    .lines()
                    .filterNot { line -> line.trimStart().startsWith("import ") }
                    .filter { line -> repositoryCall.containsMatchIn(line) }
                    .map { line -> "${line.trim()} (${file.name})" }
            }
        // Then
        apiFiles.shouldNotBeEmpty()
        withClue("Apis calling a repository: $calls. Only build it and hand it to the use cases.") {
            calls.shouldBeEmpty()
        }
    }

    private data class RepositoryImport(
        val repository: String,
        val moduleRoot: String,
        val packageName: String,
        val fileName: String,
    )

    // A call on a repository, e.g.: `partyRepository.search()` or `InMemoryPartyRepository().apply { … }`.
    private val repositoryCall = Regex("""Repository(\(\))?\??\.\w""")

    private fun isAllowed(import: RepositoryImport): Boolean {
        val packageName = import.packageName
        val root = import.moduleRoot
        return listOf("$root.domain", "$root.usecases", "$root.adapters.storage").any { allowed -> packageName == allowed || packageName.startsWith("$allowed.") } ||
            (packageName == "$root.adapters.presentation" && import.fileName.endsWith("Api")) ||
            packageName in allowedSnapshottables
    }
}
