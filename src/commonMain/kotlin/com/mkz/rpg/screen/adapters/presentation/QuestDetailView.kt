package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.HudStyle.panel
import com.mkz.rpg.screen.adapters.presentation.QuestViewParts.fitted
import com.mkz.rpg.screen.adapters.presentation.QuestViewParts.progressBar
import com.mkz.rpg.screen.adapters.presentation.QuestViewParts.repeatablePill
import com.mkz.rpg.screen.adapters.presentation.QuestViewParts.rewardRow
import com.mkz.rpg.screen.adapters.presentation.QuestViewParts.ringOf
import com.mkz.rpg.screen.adapters.presentation.QuestViewParts.targetSlot
import com.mkz.rpg.screen.adapters.presentation.QuestViewParts.taskGlyph
import com.mkz.rpg.screen.adapters.presentation.QuestViewParts.tickDisc
import com.mkz.rpg.screen.adapters.presentation.QuestViewParts.widthOf
import com.mkz.rpg.screen.adapters.presentation.QuestViewParts.wrappedText
import korlibs.image.color.Colors
import korlibs.korge.view.Container
import korlibs.korge.view.Text
import korlibs.korge.view.container
import korlibs.korge.view.solidRect
import korlibs.korge.view.text
import korlibs.korge.view.xy
import korlibs.math.geom.Size

/**
 * A quest in full, in place of the pad, led by the portrait of who its current task is about: its name and description
 * beside it, the current task under them, the tasks done as a count, and the reward, with a bottom row just for the
 * buttons, so a large reward never slides under them. It stays open once completed, so the story can be read again.
 */
class QuestDetailView(
    private val resourcesLoader: BattleSceneResourcesLoader,
    private val texts: Texts,
) : Container() {
    companion object {
        const val WIDTH = 378.0
        const val HEIGHT = 300.0

        private const val PADDING = 12.0
        private const val CONTENT_WIDTH = WIDTH - 2 * PADDING
        private const val TARGET_SIZE = 104.0
        private const val TARGET_BOTTOM = PADDING + TARGET_SIZE
        private const val TARGET_TICK_SIZE = 26.0
        private const val TEXT_X = 128.0
        private const val TEXT_WIDTH = WIDTH - PADDING - TEXT_X
        private const val NAME_Y = 26.0
        private const val NAME_SIZE = 17.0
        private const val NAME_MAXIMUM_LINES = 2
        private const val DESCRIPTION_SIZE = 13.0
        private const val DESCRIPTION_SPACING = 5.0
        private const val PILL_SPACING = 8.0
        private const val PILL_Y = 7.0
        private const val BANNER_Y = 124.0
        private const val BANNER_HEIGHT = 52.0
        private const val TASK_X = 24.0
        private const val TASK_TEXT_X = 48.0
        private const val TASK_RIGHT = WIDTH - 2 * PADDING
        private const val TASK_SIZE = 16.0
        private const val SINGLE_TASK_Y = 142.0
        private const val COUNTED_TASK_Y = 134.0
        private const val COUNT_SPACING = 8.0
        private const val BAR_Y = 158.0
        private const val BAR_WIDTH = 330.0
        private const val BAR_HEIGHT = 7.0
        private const val TASKS_DONE_Y = 182.0
        private const val TASKS_DONE_SIZE = 14.0
        private const val REWARD_ROW_Y = 208.0
        private const val REWARD_CAPTION_Y = 215.0
        private const val REWARD_SPACING = 8.0
        private const val DIVIDER_Y = 248.0
        private const val BACK_X = PADDING
        private const val BACK_Y = 256.0
        private const val BACK_WIDTH = 72.0
    }

    interface Delegate {
        fun questDetailBackSelected()
    }

    private var delegate: Delegate? = null

    internal var captionText: Text? = null
    internal var repeatablePillView: PillView? = null
    internal var nameView: Container? = null
    internal var descriptionView: Container? = null
    internal var targetTick: Container? = null
    internal var taskText: Text? = null
    internal var countText: Text? = null
    internal var progressBarView: Container? = null
    internal var tasksDoneText: Text? = null
    internal var rewardView: Container? = null
    internal var backButton: CancelButton? = null

    fun setDelegate(delegate: Delegate) {
        this.delegate = delegate
    }

    /** Draws the quest from scratch. */
    fun display(quest: QuestInfo) {
        removeChildren()
        panel(Size(WIDTH, HEIGHT))
        drawLeader(quest)
        drawTask(quest)
        drawTasksDone(quest)
        drawReward(quest)
        drawButtons()
        visible = true
    }

    fun hide() {
        visible = false
    }

    // The target, and beside it the caption, the name, and as many lines of the description as fit down to its bottom.
    private fun drawLeader(quest: QuestInfo) {
        val task = quest.currentTask
        targetSlot(task, resourcesLoader, PADDING, PADDING, TARGET_SIZE, portraitScale = 3.0, ring = if (quest.isCompleted) HudStyle.ALLY else ringOf(task))
        targetTick = if (quest.isCompleted) container { tickDisc(TARGET_BOTTOM - 20, TARGET_BOTTOM - 20, TARGET_TICK_SIZE) } else null
        val caption =
            texts[
                when {
                    quest.isCompleted -> UiText.QUEST_DETAIL_COMPLETED
                    quest.isMain -> UiText.QUEST_DETAIL_MAIN_QUEST
                    else -> UiText.QUEST_DETAIL_SIDE_QUEST
                },
            ]
        captionText = text(caption, textSize = QuestViewParts.CAPTION_SIZE, color = if (quest.isCompleted) HudStyle.ALLY else HudStyle.FAINT) { xy(TEXT_X, PADDING - 2) }
        repeatablePillView = if (quest.isRepeatable) repeatablePill(TEXT_X + widthOf(caption, QuestViewParts.CAPTION_SIZE) + PILL_SPACING, PILL_Y, texts) else null
        var nameHeight = 0.0
        nameView = container { nameHeight = wrappedText(quest.name, TEXT_X, NAME_Y, TEXT_WIDTH, NAME_SIZE, Colors.WHITE, maximumLines = NAME_MAXIMUM_LINES) }
        val descriptionY = NAME_Y + nameHeight + DESCRIPTION_SPACING
        val descriptionLines = ((TARGET_BOTTOM - descriptionY + QuestViewParts.LINE_SPACING) / (DESCRIPTION_SIZE + QuestViewParts.LINE_SPACING)).toInt()
        descriptionView = container { wrappedText(quest.description, TEXT_X, descriptionY, TEXT_WIDTH, DESCRIPTION_SIZE, HudStyle.MUTED, maximumLines = descriptionLines) }
    }

    // The current task, in a banner, with its count and bar only when it counts above 1.
    private fun drawTask(quest: QuestInfo) {
        val task = quest.currentTask
        panel(Size(CONTENT_WIDTH, BANNER_HEIGHT), fill = HudStyle.PANEL_RAISED).xy(PADDING, BANNER_Y)
        val isCounted = !quest.isCompleted && task.count > 1
        val taskY = if (isCounted) COUNTED_TASK_Y else SINGLE_TASK_Y
        if (quest.isCompleted) tickDisc(TASK_X, taskY, TASK_SIZE) else taskGlyph(task.kind, TASK_X, taskY, TASK_SIZE, HudStyle.SELECTION)
        val count = if (isCounted) "${task.progress}/${task.count}" else null
        val countWidth = count?.let { value -> widthOf(value, TASK_SIZE) } ?: 0.0
        val taskWidth = TASK_RIGHT - TASK_TEXT_X - if (isCounted) countWidth + COUNT_SPACING else 0.0
        taskText = text(fitted(task.text, TASK_SIZE, taskWidth), textSize = TASK_SIZE, color = if (quest.isCompleted) HudStyle.MUTED else Colors.WHITE) { xy(TASK_TEXT_X, taskY) }
        countText = count?.let { value -> text(value, textSize = TASK_SIZE, color = HudStyle.SELECTION) { xy(TASK_RIGHT - countWidth, taskY) } }
        progressBarView = if (isCounted) container { progressBar(TASK_X, BAR_Y, BAR_WIDTH, BAR_HEIGHT, task.progress, task.count) } else null
    }

    private fun drawTasksDone(quest: QuestInfo) {
        val tasksDone = quest.doneTasks.size
        if (tasksDone == 0 || quest.isCompleted) {
            tasksDoneText = null
            return
        }
        tickDisc(PADDING + 2, TASKS_DONE_Y + 1, 12.0)
        tasksDoneText = text(texts.plural(UiText.QUEST_DETAIL_TASKS_DONE, tasksDone, "tasks" to tasksDone), textSize = TASKS_DONE_SIZE, color = HudStyle.MUTED) { xy(PADDING + 20, TASKS_DONE_Y) }
    }

    private fun drawReward(quest: QuestInfo) {
        val caption = texts[UiText.QUEST_DETAIL_REWARD]
        text(caption, textSize = QuestViewParts.CAPTION_SIZE, color = HudStyle.FAINT) { xy(PADDING, REWARD_CAPTION_Y) }
        rewardView = container { rewardRow(quest, PADDING + widthOf(caption, QuestViewParts.CAPTION_SIZE) + REWARD_SPACING, REWARD_ROW_Y, resourcesLoader, texts) }
    }

    private fun drawButtons() {
        solidRect(Size(CONTENT_WIDTH, 1.0), HudStyle.STROKE) { xy(PADDING, DIVIDER_Y) }
        backButton =
            CancelButton(texts[UiText.QUEST_DETAIL_BACK], BACK_WIDTH) { delegate?.questDetailBackSelected() }.also { button ->
                addChild(button)
                button.xy(BACK_X, BACK_Y)
            }
    }
}
