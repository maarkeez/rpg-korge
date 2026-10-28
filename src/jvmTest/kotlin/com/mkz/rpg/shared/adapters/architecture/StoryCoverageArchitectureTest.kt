package com.mkz.rpg.shared.adapters.architecture

import com.lemonappdev.konsist.api.Konsist
import io.kotest.assertions.withClue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldNotBeEmpty
import kotlin.test.Test

/**
 * Every view of the game has its stories in the Storybook: a `<View>Stories` binding in the presentation package of the
 * storybook subdomain, listed in `StorybookApi`. The views that have none yet are allowed, and only those: the list
 * can only shrink. The views of the Storybook itself are left out. See docs/architecture/package-structure.md.
 */
class StoryCoverageArchitectureTest {
    private val storybookPresentationPackage = "com.mkz.rpg.storybook.adapters.presentation"

    // The KorGE views the views of the game extend, directly or through another view of the game (e.g.: `HealthBarView`
    // extends `BarView`, which extends `UIContainer`). Konsist only knows the parents declared in the project.
    private val korgeViews = setOf("View", "Container", "FixedSizeContainer", "UIView", "UIContainer", "UIButton", "Image", "SolidRect", "RoundRect", "Text", "UIText")

    // The views that have no stories yet. Remove a view once it has its stories.
    private val viewsWithoutStories =
        setOf(
            "AttackPreviewView",
            "BattleHudView",
            "BattleInfoView",
            "BattleUnitInfoView",
            "BattlefieldView",
            "CancelAndConfirmView",
            "CancelButton",
            "ConfirmButton",
            "FinishTurnView",
            "FormationRowView",
            "FormationView",
            "GiveItemView",
            "InventoryFullView",
            "InventoryView",
            "ItemActionsView",
            "MapBuilderCallToActionView",
            "MapBuilderHudView",
            "MapBuilderInfoView",
            "MapBuilderMenuView",
            "MenuView",
            "NpcDialogView",
            "PlayerCallToActionView",
            "ReceiverPreviewView",
            "VirtualPadView",
        )

    private val classes = Konsist.scopeFromProduction().classes()
    private val parentsByClass: Map<String, List<String>> = classes.associate { declaration -> declaration.name to declaration.parents.map { parent -> parent.name } }
    private val bindings: List<String> =
        Konsist
            .scopeFromProduction()
            .objects()
            .filter { declaration -> declaration.packagee?.fullyQualifiedName == storybookPresentationPackage && declaration.name.endsWith("Stories") }
            .map { declaration -> declaration.name }

    // The views of the presentation packages of the game, but the Storybook's own.
    private val views: List<String> =
        classes
            .filter { declaration -> declaration.isTopLevel }
            .filter { declaration -> declaration.packagee?.fullyQualifiedName?.endsWith(".adapters.presentation") == true }
            .filterNot { declaration -> declaration.packagee?.fullyQualifiedName == storybookPresentationPackage }
            .filterNot { declaration -> declaration.name.endsWith("Presenter") }
            .map { declaration -> declaration.name }
            .filter(::isView)
            .distinct()

    @Test
    fun `should have its stories when it is a view of the game`() {
        // When
        val viewsMissingStories = views.filterNot { view -> "${view}Stories" in bindings } - viewsWithoutStories
        // Then
        views.shouldNotBeEmpty()
        withClue("Views without stories: $viewsMissingStories. Add a `<View>Stories` binding in $storybookPresentationPackage, listed in StorybookApi.") {
            viewsMissingStories.shouldBeEmpty()
        }
    }

    @Test
    fun `should only allow the views that have no stories yet when they are allowed to have none`() {
        // When
        val viewsWithStories = viewsWithoutStories.filter { view -> "${view}Stories" in bindings }
        val unknownViews = viewsWithoutStories - views.toSet()
        // Then
        withClue("Views allowed to have no stories that have them: $viewsWithStories. Remove them from the allowed views.") {
            viewsWithStories.shouldBeEmpty()
        }
        withClue("Views allowed to have no stories that are not views of the game: $unknownViews. Remove them from the allowed views.") {
            unknownViews.shouldBeEmpty()
        }
    }

    @Test
    fun `should bind a view and be listed in StorybookApi when it is a story binding`() {
        // Given
        val storybookApi = Konsist.scopeFromProduction().files.single { file -> file.packagee?.fullyQualifiedName == storybookPresentationPackage && file.name == "StorybookApi" }
        // When
        val bindingsOfNoView = bindings.filterNot { binding -> binding.removeSuffix("Stories") in views }
        val unlistedBindings = bindings.filterNot { binding -> Regex("""\b$binding\b""").containsMatchIn(storybookApi.text) }
        // Then
        bindings.shouldNotBeEmpty()
        withClue("Story bindings named after no view of the game: $bindingsOfNoView. Name them `<View>Stories`.") {
            bindingsOfNoView.shouldBeEmpty()
        }
        withClue("Story bindings not listed in StorybookApi.STORY_BINDINGS: $unlistedBindings. The Storybook and its smoke test would miss them.") {
            unlistedBindings.shouldBeEmpty()
        }
    }

    // Whether it extends a KorGE view, directly or through other classes of the project.
    private fun isView(name: String): Boolean {
        val visited = mutableSetOf<String>()
        val pending = ArrayDeque(listOf(name))
        while (pending.isNotEmpty()) {
            val current = pending.removeFirst()
            if (!visited.add(current)) continue
            val parents = parentsByClass[current].orEmpty()
            if (parents.any { parent -> parent in korgeViews }) return true
            pending.addAll(parents)
        }
        return false
    }
}
