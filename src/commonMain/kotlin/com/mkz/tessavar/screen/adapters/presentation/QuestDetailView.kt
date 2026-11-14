package com.mkz.tessavar.screen.adapters.presentation

import com.mkz.tessavar.screen.adapters.presentation.HudStyle.panel
import com.mkz.tessavar.screen.adapters.presentation.QuestViewParts.fitted
import com.mkz.tessavar.screen.adapters.presentation.QuestViewParts.progressBar
import com.mkz.tessavar.screen.adapters.presentation.QuestViewParts.repeatablePill
import com.mkz.tessavar.screen.adapters.presentation.QuestViewParts.rewardRow
import com.mkz.tessavar.screen.adapters.presentation.QuestViewParts.ringOf
import com.mkz.tessavar.screen.adapters.presentation.QuestViewParts.targetSlot
import com.mkz.tessavar.screen.adapters.presentation.QuestViewParts.taskGlyph
import com.mkz.tessavar.screen.adapters.presentation.QuestViewParts.tickDisc
import com.mkz.tessavar.screen.adapters.presentation.QuestViewParts.widthOf
import com.mkz.tessavar.screen.adapters.presentation.QuestViewParts.wrappedText
import korlibs.image.color.Colors
import korlibs.korge.view.Container
import korlibs.korge.view.Text
import korlibs.korge.view.container
import korlibs.korge.view.text
import korlibs.korge.view.xy
import korlibs.math.geom.Size

class QuestDetailView(
    private val resourcesLoader: GameSceneResourcesLoader,
    private val messages: Messages,
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
        private const val BANNER_Y = 141.0
        private const val BANNER_Y_WITHOUT_TASKS_DONE = 160.0
        private const val BANNER_HEIGHT = 52.0
        private const val TASK_X = 24.0
        private const val TASK_TEXT_X = 48.0
        private const val TASK_RIGHT = WIDTH - 2 * PADDING
        private const val TASK_SIZE = 16.0
        private const val SINGLE_TASK_OFFSET = 18.0
        private const val COUNTED_TASK_OFFSET = 10.0
        private const val COUNT_SPACING = 8.0
        private const val BAR_OFFSET = 34.0
        private const val BAR_WIDTH = 330.0
        private const val BAR_HEIGHT = 7.0
        private const val TASKS_DONE_Y = 217.0
        private const val TASKS_DONE_SIZE = 14.0
        private const val REWARD_ROW_Y = 256.0
        private const val REWARD_CAPTION_Y = 263.0
        private const val REWARD_SPACING = 8.0
    }

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

    fun display(quest: QuestInfo) {
        removeChildren()
        panel(Size(WIDTH, HEIGHT))
        drawLeader(quest)
        drawTask(quest)
        drawTasksDone(quest)
        drawReward(quest)
        visible = true
    }

    fun hide() {
        visible = false
    }

    private fun drawLeader(quest: QuestInfo) {
        val task = quest.currentTask
        targetSlot(task, resourcesLoader, PADDING, PADDING, TARGET_SIZE, portraitScale = 3.0, ring = if (quest.isCompleted) HudStyle.ALLY else ringOf(task))
        targetTick = if (quest.isCompleted) container { tickDisc(TARGET_BOTTOM - 20, TARGET_BOTTOM - 20, TARGET_TICK_SIZE) } else null
        val caption =
            messages[
                when {
                    quest.isCompleted -> UiMessage.QUEST_DETAIL_COMPLETED
                    quest.isMain -> UiMessage.QUEST_DETAIL_MAIN_QUEST
                    else -> UiMessage.QUEST_DETAIL_SIDE_QUEST
                },
            ]
        captionText = text(caption, textSize = QuestViewParts.CAPTION_SIZE, color = if (quest.isCompleted) HudStyle.ALLY else HudStyle.FAINT) { xy(TEXT_X, PADDING - 2) }
        repeatablePillView = if (quest.isRepeatable) repeatablePill(TEXT_X + widthOf(caption, QuestViewParts.CAPTION_SIZE) + PILL_SPACING, PILL_Y, messages) else null
        var nameHeight = 0.0
        nameView = container { nameHeight = wrappedText(quest.name, TEXT_X, NAME_Y, TEXT_WIDTH, NAME_SIZE, Colors.WHITE, maximumLines = NAME_MAXIMUM_LINES) }
        val descriptionY = NAME_Y + nameHeight + DESCRIPTION_SPACING
        val descriptionLines = ((TARGET_BOTTOM - descriptionY + QuestViewParts.LINE_SPACING) / (DESCRIPTION_SIZE + QuestViewParts.LINE_SPACING)).toInt()
        descriptionView = container { wrappedText(quest.description, TEXT_X, descriptionY, TEXT_WIDTH, DESCRIPTION_SIZE, HudStyle.MUTED, maximumLines = descriptionLines) }
    }

    private fun drawTask(quest: QuestInfo) {
        val task = quest.currentTask
        val bannerY = if (quest.hasTasksDoneDisplayed()) BANNER_Y else BANNER_Y_WITHOUT_TASKS_DONE
        panel(Size(CONTENT_WIDTH, BANNER_HEIGHT), fill = HudStyle.PANEL_RAISED).xy(PADDING, bannerY)
        val isCounted = !quest.isCompleted && task.requiredDefeats > 1
        val taskY = bannerY + if (isCounted) COUNTED_TASK_OFFSET else SINGLE_TASK_OFFSET
        if (quest.isCompleted) tickDisc(TASK_X, taskY, TASK_SIZE) else taskGlyph(task.kind, TASK_X, taskY, TASK_SIZE, HudStyle.SELECTION)
        val count = if (isCounted) "${task.defeatsCounted}/${task.requiredDefeats}" else null
        val countWidth = count?.let { value -> widthOf(value, TASK_SIZE) } ?: 0.0
        val taskWidth = TASK_RIGHT - TASK_TEXT_X - if (isCounted) countWidth + COUNT_SPACING else 0.0
        taskText = text(fitted(task.text, TASK_SIZE, taskWidth), textSize = TASK_SIZE, color = if (quest.isCompleted) HudStyle.MUTED else Colors.WHITE) { xy(TASK_TEXT_X, taskY) }
        countText = count?.let { value -> text(value, textSize = TASK_SIZE, color = HudStyle.SELECTION) { xy(TASK_RIGHT - countWidth, taskY) } }
        progressBarView = if (isCounted) container { progressBar(TASK_X, bannerY + BAR_OFFSET, BAR_WIDTH, BAR_HEIGHT, task.defeatsCounted, task.requiredDefeats) } else null
    }

    private fun drawTasksDone(quest: QuestInfo) {
        if (!quest.hasTasksDoneDisplayed()) {
            tasksDoneText = null
            return
        }
        val tasksDone = quest.doneTasks.size
        tickDisc(PADDING + 2, TASKS_DONE_Y + 1, 12.0)
        tasksDoneText = text(messages.plural(UiMessage.QUEST_DETAIL_TASKS_DONE, tasksDone, "tasks" to tasksDone), textSize = TASKS_DONE_SIZE, color = HudStyle.MUTED) { xy(PADDING + 20, TASKS_DONE_Y) }
    }

    private fun drawReward(quest: QuestInfo) {
        val caption = messages[UiMessage.QUEST_DETAIL_REWARD]
        text(caption, textSize = QuestViewParts.CAPTION_SIZE, color = HudStyle.FAINT) { xy(PADDING, REWARD_CAPTION_Y) }
        rewardView = container { rewardRow(quest, PADDING + widthOf(caption, QuestViewParts.CAPTION_SIZE) + REWARD_SPACING, REWARD_ROW_Y, resourcesLoader, messages) }
    }

    private fun QuestInfo.hasTasksDoneDisplayed() = doneTasks.isNotEmpty() && !isCompleted
}
