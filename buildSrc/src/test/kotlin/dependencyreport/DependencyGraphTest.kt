package dependencyreport

import dependencyreport.KotlinSourceFileMother.BASE_PACKAGE
import dependencyreport.KotlinSourceFileMother.subdomainFile
import io.kotest.assertions.assertSoftly
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class DependencyGraphTest {
    private val parser = DependencyParser(BASE_PACKAGE)

    @Test
    fun `Edges should weight a dependency by its imports when a subdomain imports another one several times`() {
        // Given
        val files =
            listOf(
                subdomainFile("battle", imports = listOf("unit.domain.Unit", "unit.domain.UnitEvent"), name = "Battle"),
                subdomainFile("battle", imports = listOf("unit.domain.Unit"), name = "BattleEvent"),
                subdomainFile("unit"),
            )
        // When
        val graph = parser.parse(files)
        // Then
        val edge = graph.edges.single()
        assertSoftly {
            edge.weight shouldBe 3
            edge.fileCount shouldBe 2
        }
    }

    @Test
    fun `Edges should be sorted by subdomain when they are read in any order`() {
        // Given
        val files =
            listOf(
                subdomainFile("unit", imports = listOf("effect.domain.Effect")),
                subdomainFile("battle", imports = listOf("unit.domain.Unit", "effect.domain.Effect")),
                subdomainFile("effect"),
            )
        // When
        val graph = parser.parse(files)
        // Then
        graph.edges.map { it.from to it.to }.shouldContainExactly("battle" to "effect", "battle" to "unit", "unit" to "effect")
    }

    @Test
    fun `Metrics should count the distinct subdomains and the imports when subdomains depend on each other`() {
        // Given
        val files =
            listOf(
                subdomainFile("battle", imports = listOf("unit.domain.Unit", "unit.domain.UnitEvent", "effect.domain.Effect")),
                subdomainFile("screen", imports = listOf("battle.domain.Battle")),
                subdomainFile("unit"),
                subdomainFile("effect"),
            )
        // When
        val graph = parser.parse(files)
        // Then
        graph.metrics.single { it.subdomain == "battle" } shouldBe SubdomainMetrics(subdomain = "battle", fanIn = 1, fanOut = 2, importsIn = 1, importsOut = 3)
    }

    @Test
    fun `Metrics should compute the instability from the fan-in and the fan-out when a subdomain has dependencies`() {
        // Given
        val files =
            listOf(
                subdomainFile("battle", imports = listOf("unit.domain.Unit", "effect.domain.Effect")),
                subdomainFile("screen", imports = listOf("battle.domain.Battle")),
                subdomainFile("unit"),
                subdomainFile("effect"),
            )
        // When
        val graph = parser.parse(files)
        // Then
        val instabilityBySubdomain = graph.metrics.associate { it.subdomain to it.instability }
        assertSoftly {
            instabilityBySubdomain["battle"] shouldBe 0.67
            instabilityBySubdomain["screen"] shouldBe 1.0
            instabilityBySubdomain["unit"] shouldBe 0.0
        }
    }

    @Test
    fun `Metrics should leave the instability undefined when a subdomain has no dependencies`() {
        // Given
        val files = listOf(subdomainFile("battle"))
        // When
        val graph = parser.parse(files)
        // Then
        graph.metrics.single().instability.shouldBeNull()
    }

    @Test
    fun `Cycles should find the subdomains that can reach each other when they depend on each other indirectly`() {
        // Given
        val files =
            listOf(
                subdomainFile("battle", imports = listOf("unit.domain.Unit")),
                subdomainFile("unit", imports = listOf("effect.domain.Effect")),
                subdomainFile("effect", imports = listOf("battle.domain.Battle", "terrain.domain.Terrain")),
                subdomainFile("terrain"),
                subdomainFile("screen", imports = listOf("battle.domain.Battle")),
            )
        // When
        val graph = parser.parse(files)
        // Then
        graph.cycles shouldBe listOf(listOf("battle", "effect", "unit"))
    }

    @Test
    fun `Cycles should be empty when the subdomains do not depend on each other`() {
        // Given
        val files =
            listOf(
                subdomainFile("battle", imports = listOf("unit.domain.Unit", "effect.domain.Effect")),
                subdomainFile("unit", imports = listOf("effect.domain.Effect")),
                subdomainFile("effect"),
            )
        // When
        val graph = parser.parse(files)
        // Then
        assertSoftly {
            graph.cycles.shouldBeEmpty()
            graph.mutualDependencies.shouldBeEmpty()
        }
    }

    @Test
    fun `Cycles should be listed separately when two groups of subdomains can reach each other`() {
        // Given
        val files =
            listOf(
                subdomainFile("battle", imports = listOf("unit.domain.Unit")),
                subdomainFile("unit", imports = listOf("battle.domain.Battle", "effect.domain.Effect")),
                subdomainFile("effect", imports = listOf("terrain.domain.Terrain")),
                subdomainFile("terrain", imports = listOf("effect.domain.Effect")),
            )
        // When
        val graph = parser.parse(files)
        // Then
        graph.cycles shouldBe listOf(listOf("battle", "unit"), listOf("effect", "terrain"))
    }

    @Test
    fun `MutualDependencies should list the pairs of subdomains when they depend directly on each other`() {
        // Given
        val files =
            listOf(
                subdomainFile("unit", imports = listOf("battle.domain.Battle")),
                subdomainFile("battle", imports = listOf("unit.domain.Unit", "effect.domain.Effect")),
                subdomainFile("effect", imports = listOf("unit.domain.Unit")),
            )
        // When
        val graph = parser.parse(files)
        // Then
        graph.mutualDependencies.shouldContainExactly("battle" to "unit")
    }
}
