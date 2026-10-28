package com.mkz.rpg.storybook.adapters.presentation

import com.mkz.rpg.storybook.domain.Storybook.Dto.ActionDto
import korlibs.image.color.Colors
import korlibs.korge.view.Container
import korlibs.korge.view.Text
import korlibs.korge.view.addTo
import korlibs.korge.view.container
import korlibs.korge.view.position
import korlibs.korge.view.text

/**
 * The callbacks the view on show made, under the controls, e.g.: `shopSlotSelected(3)`, the newest on top, as many as
 * fit, and how many there are in the title. `Clear` asks to empty them. With none, a hint tells where they come from.
 */
open class ActionsView : Container() {
    companion object {
        const val WIDTH = 480.0
        const val TITLE_HEIGHT = 40.0
        const val LINE_HEIGHT = 20.0
        const val VISIBLE_ACTIONS = 5
        const val HEIGHT = TITLE_HEIGHT + VISIBLE_ACTIONS * LINE_HEIGHT
        private const val TEXT_SIZE = 15.0
        const val EMPTY_HINT = "Tap the view in the phone: the callbacks it makes, if any, are listed here."
    }

    interface Delegate {
        fun actionsCleared()
    }

    private var delegate: Delegate? = null

    internal val title: Text
    internal val clearButton: StorybookButton
    internal val lines: Container
    internal val emptyHint: Text

    // The newest first, only those that fit.
    internal var actionTexts: List<Text> = emptyList()
        private set

    init {
        title = text(titleOf(0), textSize = 22.0, color = Colors.WHITE)
        val clearSize = StorybookButton.sizeFor("Clear")
        clearButton = StorybookButton("Clear", clearSize) { delegate?.actionsCleared() }.addTo(this).position(WIDTH - clearSize.width, 0.0)
        lines = container().position(0.0, TITLE_HEIGHT)
        emptyHint = text(EMPTY_HINT, textSize = TEXT_SIZE, color = StoryInfoView.SECONDARY_COLOR).position(0.0, TITLE_HEIGHT)
    }

    open fun setDelegate(delegate: Delegate) {
        this.delegate = delegate
    }

    /** Lists the newest actions that fit, the newest first, each as its name and its arguments, e.g.: `shopSlotSelected(3)`. */
    open fun display(actions: List<ActionDto>) {
        title.text = titleOf(actions.size)
        emptyHint.visible = actions.isEmpty()
        lines.removeChildren()
        actionTexts =
            actions.take(VISIBLE_ACTIONS).mapIndexed { index, action ->
                lines.text("${action.name}(${action.arguments.joinToString(", ")})", textSize = TEXT_SIZE, color = StoryInfoView.SECONDARY_COLOR).position(0.0, index * LINE_HEIGHT)
            }
    }

    // e.g.: `Actions (3)`.
    private fun titleOf(count: Int): String = "Actions ($count)"
}
