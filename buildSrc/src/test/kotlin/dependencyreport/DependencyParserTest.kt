package dependencyreport

import dependencyreport.KotlinSourceFileMother.BASE_PACKAGE
import dependencyreport.KotlinSourceFileMother.kotlinFile
import dependencyreport.KotlinSourceFileMother.subdomainFile
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.maps.shouldContainExactly
import kotlin.test.Test

class DependencyParserTest {
    private val parser = DependencyParser(BASE_PACKAGE)

    @Test
    fun `should depend on a subdomain when a file imports one of its declarations`() {
        // Given
        val battleFile = subdomainFile("battle", imports = listOf("battleUnit.domain.BattleUnitEvent"), layer = "adapters", name = "OnBattleUnitDefeated")
        val battleUnitFile = subdomainFile("battleUnit")
        // When
        val graph = parser.parse(listOf(battleFile, battleUnitFile))
        // Then
        graph.edges.flatMap { it.imports }.shouldContainExactly(
            ImportDependency(
                sourceFile = "com/mkz/rpg/battle/adapters/OnBattleUnitDefeated.kt",
                sourceSubdomain = "battle",
                sourceLayer = "adapters",
                targetSubdomain = "battleUnit",
                importedName = "battleUnit.domain.BattleUnitEvent",
            ),
        )
    }

    @Test
    fun `should not depend on a subdomain when a file imports a declaration of its own subdomain`() {
        // Given
        val battleFile = subdomainFile("battle", imports = listOf("battle.usecases.commands.LeaveBattle"), layer = "adapters")
        // When
        val graph = parser.parse(listOf(battleFile))
        // Then
        graph.edges.shouldBeEmpty()
    }

    @Test
    fun `should ignore an import when it is not declared in the base package`() {
        // Given
        val battleFile = kotlinFile("$BASE_PACKAGE.battle.domain", imports = listOf("korlibs.korge.view.Stage", "kotlin.random.Random"))
        // When
        val graph = parser.parse(listOf(battleFile))
        // Then
        graph.edges.shouldBeEmpty()
    }

    @Test
    fun `should ignore a file when it is declared directly in the base package`() {
        // Given
        val mainFile = kotlinFile(BASE_PACKAGE, imports = listOf("$BASE_PACKAGE.screen.adapters.presentation.MenuScene"))
        val screenFile = subdomainFile("screen", layer = "adapters")
        // When
        val graph = parser.parse(listOf(mainFile, screenFile))
        // Then
        graph.subdomains.shouldContainExactly("screen")
        graph.edges.shouldBeEmpty()
    }

    @Test
    fun `should ignore an import when it is declared directly in the base package`() {
        // Given
        val screenFile = subdomainFile("screen", imports = listOf("GAME_WINDOW_SIZE"), layer = "adapters")
        // When
        val graph = parser.parse(listOf(screenFile))
        // Then
        graph.edges.shouldBeEmpty()
    }

    @Test
    fun `should count a nested subdomain as part of its parent subdomain when it is declared inside it`() {
        // Given
        val hudFile =
            kotlinFile(
                "$BASE_PACKAGE.screen.adapters.presentation.battlefieldHud.usecases.commands",
                imports = listOf("$BASE_PACKAGE.screen.adapters.presentation.BattlefieldView", "$BASE_PACKAGE.battle.usecases.queries.SearchBattle"),
            )
        val screenFile = subdomainFile("screen", layer = "adapters")
        val battleFile = subdomainFile("battle")
        // When
        val graph = parser.parse(listOf(hudFile, screenFile, battleFile))
        // Then
        graph.fileCountBySubdomain.shouldContainExactly(mapOf("battle" to 1, "screen" to 2))
        graph.edges.map { it.from to it.to }.shouldContainExactly("screen" to "battle")
    }

    @Test
    fun `should read the imported name when the import has an alias`() {
        // Given
        val battleFile = subdomainFile("battle", imports = listOf("battleUnit.domain.BattleUnit as Unit"))
        val battleUnitFile = subdomainFile("battleUnit")
        // When
        val graph = parser.parse(listOf(battleFile, battleUnitFile))
        // Then
        graph.edges.flatMap { it.imports }.map { it.importedName }.shouldContainExactly("battleUnit.domain.BattleUnit")
    }

    @Test
    fun `should ignore an import when it names a package that is not a subdomain`() {
        // Given
        val battleFile = subdomainFile("battle", imports = listOf("unknown.domain.Unknown"))
        // When
        val graph = parser.parse(listOf(battleFile))
        // Then
        graph.edges.shouldBeEmpty()
    }
}
