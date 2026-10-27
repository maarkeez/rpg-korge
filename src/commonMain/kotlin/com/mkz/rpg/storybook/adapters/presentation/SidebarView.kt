package com.mkz.rpg.storybook.adapters.presentation

import com.mkz.rpg.STORYBOOK_WINDOW_SIZE
import com.mkz.rpg.storybook.domain.Storybook.Dto.ComponentDto
import korlibs.image.color.Colors
import korlibs.image.color.RGBA
import korlibs.korge.input.onClick
import korlibs.korge.input.onOut
import korlibs.korge.input.onOver
import korlibs.korge.ui.UIContainer
import korlibs.korge.view.Container
import korlibs.korge.view.RoundRect
import korlibs.korge.view.Text
import korlibs.korge.view.addTo
import korlibs.korge.view.container
import korlibs.korge.view.position
import korlibs.korge.view.roundRect
import korlibs.korge.view.solidRect
import korlibs.korge.view.text
import korlibs.math.geom.RectCorners
import korlibs.math.geom.Size

/**
 * The catalogue, on the left of the Storybook: the components, grouped by their subdomain, each with its stories below
 * it. Tapping a story shows it, and the story on show is highlighted.
 */
open class SidebarView : UIContainer(Size(WIDTH, STORYBOOK_WINDOW_SIZE.height)) {
    companion object {
        const val WIDTH = 260.0
        val BACKGROUND_COLOR = RGBA(0x1A, 0x1A, 0x1A)

        // Storybook's own blue, so the story on show stands out as in Storybook.
        val SELECTED_COLOR = RGBA(0x1E, 0xA7, 0xFD)
        val HOVER_COLOR = RGBA(0x33, 0x33, 0x33)
        val GROUP_COLOR = RGBA(0x99, 0x99, 0x99)

        private const val PADDING = 16.0
        private const val TITLE_HEIGHT = 56.0
        private const val GROUP_HEIGHT = 24.0
        private const val COMPONENT_HEIGHT = 26.0
        private const val STORY_HEIGHT = 28.0
        private const val STORY_INDENT = 28.0
        private const val STORY_TEXT_PADDING = 10.0
        private const val STORY_TEXT_SIZE = 15.0
        private const val GROUP_GAP = 12.0
    }

    interface Delegate {
        fun storySelected(storyId: String)
    }

    private var delegate: Delegate? = null

    internal val title: Text
    internal val catalogue: Container
    internal var groupTexts: List<Text> = emptyList()
        private set
    internal var componentTexts: List<Text> = emptyList()
        private set

    // In the order they are listed.
    internal var storyRows: List<StoryRow> = emptyList()
        private set

    init {
        solidRect(Size(WIDTH, STORYBOOK_WINDOW_SIZE.height), BACKGROUND_COLOR)
        title = text("Storybook", textSize = 24.0, color = Colors.WHITE).position(PADDING, PADDING)
        catalogue = container().position(0.0, TITLE_HEIGHT)
    }

    open fun setDelegate(delegate: Delegate) {
        this.delegate = delegate
    }

    /** Lists the components, in their order, under the group of the first one of each group, e.g.: `SCREEN`. */
    open fun display(components: List<ComponentDto>) {
        catalogue.removeChildren()
        val groups = mutableListOf<Text>()
        val titles = mutableListOf<Text>()
        val rows = mutableListOf<StoryRow>()
        var y = 0.0
        components.groupBy(ComponentDto::group).forEach { (group, groupComponents) ->
            groups += catalogue.text(group.uppercase(), textSize = 13.0, color = GROUP_COLOR).position(PADDING, y)
            y += GROUP_HEIGHT
            groupComponents.forEach { component ->
                titles += catalogue.text(component.title, textSize = 16.0, color = Colors.WHITE).position(PADDING, y)
                y += COMPONENT_HEIGHT
                component.stories.forEach { story ->
                    rows += StoryRow(story.id, story.name).addTo(catalogue).position(STORY_INDENT, y)
                    y += STORY_HEIGHT
                }
            }
            y += GROUP_GAP
        }
        groupTexts = groups
        componentTexts = titles
        storyRows = rows
    }

    /** Highlights the story on show, and only that one. */
    open fun selectStory(storyId: String) {
        storyRows.forEach { row -> row.isSelected = row.storyId == storyId }
    }

    /** A story of the list, highlighted while it is on show or under the mouse, that shows it when tapped. */
    internal inner class StoryRow(
        val storyId: String,
        name: String,
    ) : Container() {
        internal val background: RoundRect = roundRect(Size(WIDTH - STORY_INDENT - PADDING, STORY_HEIGHT), RectCorners(5.0), fill = Colors.WHITE)
        internal val label: Text = text(name, textSize = STORY_TEXT_SIZE, color = Colors.WHITE).position(STORY_TEXT_PADDING, (STORY_HEIGHT - STORY_TEXT_SIZE) / 2)
        private var isOver = false

        internal var isSelected = false
            set(value) {
                field = value
                paint()
            }

        init {
            paint()
            onClick { delegate?.storySelected(storyId) }
            onOver {
                isOver = true
                paint()
            }
            onOut {
                isOver = false
                paint()
            }
        }

        private fun paint() {
            background.color =
                when {
                    isSelected -> SELECTED_COLOR
                    isOver -> HOVER_COLOR
                    else -> BACKGROUND_COLOR
                }
        }
    }
}
