package com.mkz.tessavar.screen.adapters.presentation

import com.mkz.tessavar.screen.adapters.presentation.HudStyle.panel
import com.mkz.tessavar.screen.adapters.presentation.QuestViewParts.chevron
import com.mkz.tessavar.screen.adapters.presentation.QuestViewParts.fitted
import com.mkz.tessavar.screen.adapters.presentation.QuestViewParts.pinGlyph
import com.mkz.tessavar.screen.adapters.presentation.QuestViewParts.progressBar
import com.mkz.tessavar.screen.adapters.presentation.QuestViewParts.repeatablePill
import com.mkz.tessavar.screen.adapters.presentation.QuestViewParts.ringOf
import com.mkz.tessavar.screen.adapters.presentation.QuestViewParts.targetSlot
import com.mkz.tessavar.screen.adapters.presentation.QuestViewParts.tickDisc
import com.mkz.tessavar.screen.adapters.presentation.QuestViewParts.widthOf
import korlibs.image.color.Colors
import korlibs.korge.input.onClick
import korlibs.korge.input.onMouseDrag
import korlibs.korge.view.Container
import korlibs.korge.view.GraphicsRenderer
import korlibs.korge.view.ShapeView
import korlibs.korge.view.Text
import korlibs.korge.view.clipContainer
import korlibs.korge.view.container
import korlibs.korge.view.foreachDescendant
import korlibs.korge.view.roundRect
import korlibs.korge.view.solidRect
import korlibs.korge.view.text
import korlibs.korge.view.xy
import korlibs.math.geom.RectCorners
import korlibs.math.geom.Size
import kotlin.math.abs

class QuestJournalView(
    private val resourcesLoader: GameSceneResourcesLoader,
    private val messages: Messages,
) : Container() {
    companion object {
        const val WIDTH = 378.0
        const val HEIGHT = 300.0

        private const val PADDING = 12.0
        private const val CONTENT_WIDTH = WIDTH - 2 * PADDING
        private const val TITLE_Y = 9.0
        private const val TITLE_SIZE = 16.0
        private const val ACTIVE_Y = 12.0
        private const val ACTIVE_SIZE = 12.0
        private const val LIST_Y = 34.0
        private const val LIST_HEIGHT = 261.0
        private const val FOOTER_Y = LIST_Y + LIST_HEIGHT
        private const val FOOTER_HEIGHT = HEIGHT - FOOTER_Y
        private const val ROW_HEIGHT = 63.0
        private const val ROW_SPACING = 3.0
        private const val TARGET_X = 8.0
        private const val TARGET_SIZE = 50.0
        private const val TARGET_Y = (ROW_HEIGHT - TARGET_SIZE) / 2
        private const val TARGET_TICK_OFFSET = 36.0
        private const val TARGET_TICK_SIZE = 18.0
        private const val TEXT_X = 68.0
        private const val SECTION_Y = 6.0
        private const val SECTION_SIZE = 10.0
        private const val NAME_Y = 19.0
        private const val NAME_SIZE = 14.0
        private const val TASK_Y = 36.0
        private const val TASK_SIZE = 12.0
        private const val BAR_Y = 53.0
        private const val BAR_HEIGHT = 5.0
        private const val END_PADDING = 10.0
        private const val END_SPACING = 8.0
        private const val CHEVRON_SIZE = 10.0
        private const val COUNT_SIZE = 13.0
        private const val PILL_HEIGHT = 18.0
        private const val PIN_SIZE = 14.0
        private const val PIN_TAP_WIDTH = 32.0
        private const val SCROLLBAR_X = 370.0
        private const val SCROLLBAR_WIDTH = 3.0
        private const val DRAG_THRESHOLD = 4.0
    }

    interface Delegate {
        fun questJournalSelected(questId: String)

        fun questJournalPinSelected(questId: String)
    }

    internal class Row(
        val questId: String,
        val view: Container,
        val body: Container,
        val pin: Container?,
        val isPinned: Boolean,
        val sectionText: Text,
        val nameText: Text,
        val taskText: Text,
        val countText: Text?,
        val repeatablePillView: PillView?,
        val progressBarView: Container?,
    )

    private var delegate: Delegate? = null
    private var contentHeight = 0.0
    private var hasDragged = false

    internal var rows: List<Row> = emptyList()

    private val activeText: Text
    private val rowsView: Container
    private val scrollbar: Container

    init {
        visible = false
        panel(Size(WIDTH, HEIGHT)).mouseEnabled = true
        rowsView = clipContainer(Size(WIDTH, LIST_HEIGHT)).xy(0.0, LIST_Y).container()
        scrollbar = container()
        solidRect(Size(CONTENT_WIDTH + 2, LIST_Y - 1), HudStyle.PANEL) {
            xy(PADDING - 1, 1.0)
            mouseEnabled = true
        }
        solidRect(Size(CONTENT_WIDTH + 2, FOOTER_HEIGHT - 1), HudStyle.PANEL) {
            xy(PADDING - 1, FOOTER_Y)
            mouseEnabled = true
        }
        text(messages[UiMessage.QUEST_JOURNAL_TITLE], textSize = TITLE_SIZE, color = Colors.WHITE) { xy(PADDING, TITLE_Y) }
        activeText = text("", textSize = ACTIVE_SIZE, color = HudStyle.FAINT)
        var draggedY = 0.0
        onMouseDrag { event ->
            if (event.start) {
                draggedY = 0.0
                hasDragged = false
            }
            val dy = event.localDXY(rowsView).y
            if (abs(dy) > DRAG_THRESHOLD) hasDragged = true
            moveRowsTo(rowsView.y + dy - draggedY)
            draggedY = dy
        }
    }

    fun setDelegate(delegate: Delegate) {
        this.delegate = delegate
    }

    fun display(quests: List<QuestInfo>) {
        rowsView.removeChildren()
        rows = quests.mapIndexed { index, quest -> rowsView.drawRow(quest, index * (ROW_HEIGHT + ROW_SPACING)) }
        rowsView.foreachDescendant { view -> (view as? ShapeView)?.renderer = GraphicsRenderer.SYSTEM }
        contentHeight = (quests.size * (ROW_HEIGHT + ROW_SPACING) - ROW_SPACING).coerceAtLeast(0.0)
        val active = quests.count { quest -> !quest.isCompleted }
        activeText.text = messages.plural(UiMessage.QUEST_JOURNAL_ACTIVE, active, "quests" to active)
        activeText.xy(WIDTH - PADDING - widthOf(activeText.text, ACTIVE_SIZE), ACTIVE_Y)
        scrollbar.removeChildren()
        if (contentHeight > LIST_HEIGHT) {
            scrollbar.roundRect(Size(SCROLLBAR_WIDTH, LIST_HEIGHT * LIST_HEIGHT / contentHeight), RectCorners(SCROLLBAR_WIDTH / 2), fill = HudStyle.STROKE)
            scrollbar.xy(SCROLLBAR_X, LIST_Y)
        }
        moveRowsTo(rowsView.y)
        visible = true
    }

    fun hide() {
        visible = false
    }

    fun scrollTo(pixels: Double) = moveRowsTo(-pixels)

    private fun moveRowsTo(y: Double) {
        rowsView.y = y.coerceIn(minOf(0.0, LIST_HEIGHT - contentHeight), 0.0)
        scrollbar.y = LIST_Y + if (contentHeight > 0.0) -rowsView.y / contentHeight * LIST_HEIGHT else 0.0
        rows.forEach { row ->
            val top = rowsView.y + row.view.y
            row.view.visible = top + ROW_HEIGHT > 0.0 && top < LIST_HEIGHT
        }
    }

    private fun Container.drawRow(
        quest: QuestInfo,
        rowY: Double,
    ): Row {
        val task = quest.currentTask
        val isCounted = !quest.isCompleted && task.requiredDefeats > 1
        lateinit var row: Row
        container {
            xy(PADDING, rowY)
            val rowView = this
            val body = container()
            with(body) {
                panel(Size(CONTENT_WIDTH, ROW_HEIGHT), fill = HudStyle.PANEL_RAISED, radius = HudStyle.SLOT_RADIUS)
                targetSlot(task, resourcesLoader, TARGET_X, TARGET_Y, TARGET_SIZE, portraitScale = 1.0, ring = if (quest.isCompleted) HudStyle.ALLY else ringOf(task))
                if (quest.isCompleted) tickDisc(TARGET_X + TARGET_TICK_OFFSET, TARGET_Y + TARGET_TICK_OFFSET, TARGET_TICK_SIZE)
                var right = CONTENT_WIDTH - END_PADDING - CHEVRON_SIZE
                chevron(right, (ROW_HEIGHT - CHEVRON_SIZE) / 2, CHEVRON_SIZE, HudStyle.MUTED)
                val countText =
                    if (isCounted) {
                        val count = "${task.defeatsCounted}/${task.requiredDefeats}"
                        right -= END_SPACING + widthOf(count, COUNT_SIZE)
                        val countX = right
                        text(count, textSize = COUNT_SIZE, color = HudStyle.SELECTION) { xy(countX, (ROW_HEIGHT - COUNT_SIZE) / 2 - 1) }
                    } else {
                        null
                    }
                val pill =
                    if (quest.isRepeatable) {
                        repeatablePill(0.0, (ROW_HEIGHT - PILL_HEIGHT) / 2, messages).also { pill ->
                            right -= END_SPACING + pill.width
                            pill.x = right
                        }
                    } else {
                        null
                    }
                val pin =
                    if (quest.isCompleted) {
                        null
                    } else {
                        right -= END_SPACING + PIN_SIZE
                        rowView.drawPin(quest, right)
                    }
                val textWidth = right - END_SPACING - TEXT_X
                val sectionText = text(sectionOf(quest), textSize = SECTION_SIZE, color = HudStyle.FAINT) { xy(TEXT_X, SECTION_Y) }
                val nameText = text(fitted(quest.name, NAME_SIZE, textWidth), textSize = NAME_SIZE, color = if (quest.isCompleted) HudStyle.MUTED else Colors.WHITE) { xy(TEXT_X, NAME_Y) }
                val taskLine = if (quest.isCompleted) messages[UiMessage.QUEST_TRACKER_COMPLETED] else task.text
                val taskText = text(fitted(taskLine, TASK_SIZE, textWidth), textSize = TASK_SIZE, color = HudStyle.MUTED) { xy(TEXT_X, TASK_Y) }
                val bar = if (isCounted) container { progressBar(TEXT_X, BAR_Y, textWidth, BAR_HEIGHT, task.defeatsCounted, task.requiredDefeats) } else null
                onClick { if (!hasDragged) delegate?.questJournalSelected(quest.id) }
                row = Row(quest.id, rowView, body, pin, quest.isPinned, sectionText, nameText, taskText, countText, pill, bar)
            }
        }
        return row
    }

    private fun Container.drawPin(
        quest: QuestInfo,
        pinX: Double,
    ): Container =
        container {
            xy(pinX - (PIN_TAP_WIDTH - PIN_SIZE) / 2, 0.0)
            solidRect(Size(PIN_TAP_WIDTH, ROW_HEIGHT), Colors.TRANSPARENT)
            pinGlyph((PIN_TAP_WIDTH - PIN_SIZE) / 2, (ROW_HEIGHT - PIN_SIZE) / 2, PIN_SIZE, if (quest.isPinned) HudStyle.SELECTION else HudStyle.FAINT)
            onClick { if (!hasDragged) delegate?.questJournalPinSelected(quest.id) }
        }

    private fun sectionOf(quest: QuestInfo): String =
        messages[
            when {
                quest.isCompleted -> UiMessage.QUEST_JOURNAL_COMPLETED
                quest.isMain -> UiMessage.QUEST_DETAIL_MAIN_QUEST
                else -> UiMessage.QUEST_DETAIL_SIDE_QUEST
            },
        ]
}
