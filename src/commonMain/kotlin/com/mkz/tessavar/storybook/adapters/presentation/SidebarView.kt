package com.mkz.tessavar.storybook.adapters.presentation

import com.mkz.tessavar.STORYBOOK_WINDOW_SIZE
import com.mkz.tessavar.storybook.domain.Storybook.Dto.ComponentDto
import korlibs.image.color.Colors
import korlibs.image.color.RGBA
import korlibs.korge.input.onClick
import korlibs.korge.input.onMouseDrag
import korlibs.korge.input.onOut
import korlibs.korge.input.onOver
import korlibs.korge.input.onScroll
import korlibs.korge.ui.UIContainer
import korlibs.korge.ui.UITextInput
import korlibs.korge.view.ClipContainer
import korlibs.korge.view.Container
import korlibs.korge.view.RoundRect
import korlibs.korge.view.Text
import korlibs.korge.view.addTo
import korlibs.korge.view.clipContainer
import korlibs.korge.view.container
import korlibs.korge.view.position
import korlibs.korge.view.roundRect
import korlibs.korge.view.solidRect
import korlibs.korge.view.text
import korlibs.math.geom.RectCorners
import korlibs.math.geom.Size

open class SidebarView : UIContainer(Size(WIDTH, STORYBOOK_WINDOW_SIZE.height)) {
    companion object {
        const val WIDTH = 260.0
        val BACKGROUND_COLOR = RGBA(0x1A, 0x1A, 0x1A)

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
        private const val SEARCH_GAP = 4.0
        private const val SEARCH_WIDTH = WIDTH - 2 * PADDING - StorybookButton.HEIGHT - SEARCH_GAP
        private const val SEARCH_TEXT_PADDING = 5.0

        private const val HEADER_HEIGHT = TITLE_HEIGHT + StorybookButton.HEIGHT + GROUP_GAP
        private val LIST_HEIGHT = STORYBOOK_WINDOW_SIZE.height - HEADER_HEIGHT
    }

    interface Delegate {
        fun storySelected(storyId: String)
    }

    private var delegate: Delegate? = null

    internal val title: Text
    internal val searchInput: UITextInput
    internal val searchPlaceholder: Text
    internal val clearSearchButton: StorybookButton
    internal val list: ClipContainer
    internal val catalogue: Container
    internal var groupTexts: List<Text> = emptyList()
        private set
    internal var componentTexts: List<Text> = emptyList()
        private set
    internal var noStoriesText: Text? = null
        private set

    internal var storyRows: List<StoryRow> = emptyList()
        private set

    private var contentHeight = 0.0

    private var unsearchedComponents: List<ComponentDto> = emptyList()

    private var selectedStoryId: String? = null

    private var scrolledStoryId: String? = null

    init {
        solidRect(Size(WIDTH, STORYBOOK_WINDOW_SIZE.height), BACKGROUND_COLOR)
        list = clipContainer(Size(WIDTH, LIST_HEIGHT)).position(0.0, HEADER_HEIGHT)
        catalogue = list.container()
        var draggedY = 0.0
        onMouseDrag { event ->
            if (event.start) draggedY = 0.0
            val dy = event.localDXY(catalogue).y
            scrollTo(catalogue.y + dy - draggedY)
            draggedY = dy
        }
        onScroll { event -> if (!event.isCtrlDown) scrollTo(catalogue.y - event.scrollDeltaYPixels) }
        solidRect(Size(WIDTH, HEADER_HEIGHT), BACKGROUND_COLOR)
        title = text("Storybook", textSize = 24.0, color = Colors.WHITE).position(PADDING, PADDING)
        searchInput = UITextInput(size = Size(SEARCH_WIDTH, StorybookButton.HEIGHT)).addTo(this).position(PADDING, TITLE_HEIGHT)
        searchInput.padding = searchInput.padding.copy(left = SEARCH_TEXT_PADDING)
        searchPlaceholder =
            searchInput.text("Search stories", textSize = searchInput.textSize, color = GROUP_COLOR).position(SEARCH_TEXT_PADDING, searchInput.padding.top).also { text ->
                text.mouseEnabled = false
            }
        clearSearchButton =
            StorybookButton("×", Size(StorybookButton.HEIGHT, StorybookButton.HEIGHT)) { searchInput.text = "" }
                .addTo(this)
                .position(PADDING + SEARCH_WIDTH + SEARCH_GAP, TITLE_HEIGHT)
        searchInput.controller.onTextUpdated { searchChanged() }
        searchChanged()
    }

    open fun setDelegate(delegate: Delegate) {
        this.delegate = delegate
    }

    open fun display(components: List<ComponentDto>) {
        unsearchedComponents = components
        list(CatalogueSearch.search(components, searchInput.text))
        scrollTo(catalogue.y)
    }

    open fun selectStory(storyId: String) {
        selectedStoryId = storyId
        storyRows.forEach { row -> row.isSelected = row.storyId == storyId }
        if (storyId == scrolledStoryId) return
        scrolledStoryId = storyId
        scrollToWhenOutOfSight(storyId)
    }

    private fun searchChanged() {
        val isEmpty = searchInput.text.isEmpty()
        searchPlaceholder.visible = isEmpty
        clearSearchButton.visible = !isEmpty
        list(CatalogueSearch.search(unsearchedComponents, searchInput.text))
        scrollTo(0.0)
        if (isEmpty) selectedStoryId?.let(::scrollToWhenOutOfSight)
    }

    private fun list(components: List<ComponentDto>) {
        catalogue.removeChildren()
        val groups = mutableListOf<Text>()
        val titles = mutableListOf<Text>()
        val rows = mutableListOf<StoryRow>()
        var y = 0.0
        noStoriesText =
            if (components.isEmpty() && unsearchedComponents.isNotEmpty()) {
                catalogue.text("No stories match", textSize = STORY_TEXT_SIZE, color = GROUP_COLOR).position(PADDING, 0.0)
            } else {
                null
            }
        components.groupBy(ComponentDto::group).forEach { (group, groupComponents) ->
            groups += catalogue.text(group.uppercase(), textSize = 13.0, color = GROUP_COLOR).position(PADDING, y)
            y += GROUP_HEIGHT
            groupComponents.forEach { component ->
                titles += catalogue.text(component.title, textSize = 16.0, color = Colors.WHITE).position(PADDING, y)
                y += COMPONENT_HEIGHT
                component.stories.forEach { story ->
                    rows += StoryRow(story.id, story.name).addTo(catalogue).position(STORY_INDENT, y)
                    rows.last().isSelected = story.id == selectedStoryId
                    y += STORY_HEIGHT
                }
            }
            y += GROUP_GAP
        }
        groupTexts = groups
        componentTexts = titles
        storyRows = rows
        contentHeight = y
    }

    private fun scrollToWhenOutOfSight(storyId: String) {
        storyRows.firstOrNull { row -> row.storyId == storyId }?.let { row ->
            val isInSight = row.y + catalogue.y >= 0.0 && row.y + STORY_HEIGHT + catalogue.y <= LIST_HEIGHT
            if (!isInSight) scrollTo(LIST_HEIGHT / 2 - (row.y + STORY_HEIGHT / 2))
        }
    }

    private fun scrollTo(y: Double) {
        catalogue.y = y.coerceIn(minOf(0.0, LIST_HEIGHT - contentHeight), 0.0)
    }

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
