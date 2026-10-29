package com.mkz.rpg.shared.adapters.architecture

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.declaration.KoClassDeclaration

/**
 * The views of the game: the top-level classes of an `adapters.presentation` package extending a KorGE view, directly or
 * through another view of the game, but the presenters and the views of the Storybook itself. The architecture tests of
 * the views share them.
 */
internal object GameViews {
    const val STORYBOOK_PRESENTATION_PACKAGE = "com.mkz.rpg.storybook.adapters.presentation"

    // The KorGE views the views of the game extend, directly or through another view of the game (e.g.: `HealthBarView`
    // extends `BarView`, which extends `UIContainer`). Konsist only knows the parents declared in the project.
    private val korgeViews = setOf("View", "Container", "FixedSizeContainer", "UIView", "UIContainer", "UIButton", "Image", "SolidRect", "RoundRect", "Text", "UIText")

    private val classes = Konsist.scopeFromProduction().classes()
    private val parentsByClass: Map<String, List<String>> = classes.associate { declaration -> declaration.name to declaration.parents.map { parent -> parent.name } }

    val declarations: List<KoClassDeclaration> =
        classes
            .filter { declaration -> declaration.isTopLevel }
            .filter { declaration -> declaration.packagee?.fullyQualifiedName?.endsWith(".adapters.presentation") == true }
            .filterNot { declaration -> declaration.packagee?.fullyQualifiedName == STORYBOOK_PRESENTATION_PACKAGE }
            .filterNot { declaration -> declaration.name.endsWith("Presenter") }
            .filter { declaration -> isView(declaration.name) }

    val names: List<String> = declarations.map { declaration -> declaration.name }.distinct()

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
