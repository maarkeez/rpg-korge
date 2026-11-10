package com.mkz.rpg.shared.adapters.architecture

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.declaration.KoClassDeclaration

internal object GameViews {
    const val STORYBOOK_PRESENTATION_PACKAGE = "com.mkz.rpg.storybook.adapters.presentation"

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
