package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.HudStyle.panel
import com.mkz.rpg.screen.adapters.presentation.QuestViewParts.fitted
import com.mkz.rpg.screen.adapters.presentation.QuestViewParts.rewardRow
import com.mkz.rpg.screen.adapters.presentation.QuestViewParts.ringOf
import com.mkz.rpg.screen.adapters.presentation.QuestViewParts.targetSlot
import com.mkz.rpg.screen.adapters.presentation.QuestViewParts.taskGlyph
import com.mkz.rpg.screen.adapters.presentation.QuestViewParts.tickDisc
import korlibs.image.color.Colors
import korlibs.korge.animate.alpha
import korlibs.korge.animate.animator
import korlibs.korge.animate.wait
import korlibs.korge.ui.UIContainer
import korlibs.korge.view.Container
import korlibs.korge.view.RoundRect
import korlibs.korge.view.Text
import korlibs.korge.view.container
import korlibs.korge.view.text
import korlibs.korge.view.xy
import korlibs.math.geom.Size
import korlibs.time.milliseconds
import korlibs.time.seconds

open class QuestAnnouncementView(
    private val resourcesLoader: GameSceneResourcesLoader,
    private val messages: Messages,
) : UIContainer(Size(SCREEN_WIDTH, SCREEN_HEIGHT)) {
    companion object {
        const val SCREEN_WIDTH = 390.0
        const val SCREEN_HEIGHT = 844.0

        val HOLD_DURATION = 4.seconds
        val FADE_DURATION = 500.milliseconds

        private const val ANNOUNCEMENT_X = 12.0
        private const val ANNOUNCEMENT_Y = 54.0
        private const val ANNOUNCEMENT_WIDTH = 366.0
        private const val ANNOUNCEMENT_HEIGHT = 76.0
        private const val ANNOUNCEMENT_RADIUS = 12.0
        private const val TARGET_X = 10.0
        private const val TARGET_Y = 10.0
        private const val TARGET_SIZE = 56.0
        private const val TARGET_TICK_AT = 52.0
        private const val TARGET_TICK_SIZE = 18.0
        private const val TEXT_X = 76.0
        private const val TITLE_Y = 8.0
        private const val NAME_Y = 22.0
        private const val NAME_SIZE = 15.0
        private const val RIGHT_PADDING = 10.0
        private const val GOAL_Y = 49.0
        private const val GOAL_GLYPH_SIZE = 14.0
        private const val GOAL_TEXT_X = 96.0
        private const val GOAL_TEXT_SIZE = 13.0
        private const val REWARD_Y = 42.0
    }

    enum class Moment { STARTED, COMPLETED }

    internal var background: RoundRect? = null
    internal var titleText: Text? = null
    internal var nameText: Text? = null
    internal var taskText: Text? = null
    internal var targetTick: Container? = null
    internal var rewardView: Container? = null

    init {
        visible = false
    }

    open suspend fun announce(
        moment: Moment,
        quest: QuestInfo,
    ) {
        display(moment, quest)
        val fade =
            animator {
                wait(HOLD_DURATION)
                alpha(this@QuestAnnouncementView, 0.0, time = FADE_DURATION)
            }
        fade.awaitComplete()
        visible = false
    }

    fun display(
        moment: Moment,
        quest: QuestInfo,
    ) {
        removeChildren()
        val isCompleted = moment == Moment.COMPLETED
        container {
            xy(ANNOUNCEMENT_X, ANNOUNCEMENT_Y)
            background = panel(Size(ANNOUNCEMENT_WIDTH, ANNOUNCEMENT_HEIGHT), radius = ANNOUNCEMENT_RADIUS, stroke = if (isCompleted) HudStyle.ALLY else HudStyle.SELECTION)
            drawHeader(isCompleted, quest)
            when (moment) {
                Moment.STARTED -> drawGoal(quest)
                Moment.COMPLETED -> drawReward(quest)
            }
        }
        alpha = 1.0
        visible = true
    }

    private fun Container.drawHeader(
        isCompleted: Boolean,
        quest: QuestInfo,
    ) {
        val task = quest.currentTask
        targetSlot(task, resourcesLoader, TARGET_X, TARGET_Y, TARGET_SIZE, portraitScale = 1.0, ring = if (isCompleted) HudStyle.ALLY else ringOf(task))
        targetTick = if (isCompleted) container { tickDisc(TARGET_TICK_AT, TARGET_TICK_AT, TARGET_TICK_SIZE) } else null
        val title = messages[if (isCompleted) UiMessage.QUEST_ANNOUNCEMENT_COMPLETED else UiMessage.QUEST_ANNOUNCEMENT_STARTED]
        titleText = text(title, textSize = QuestViewParts.CAPTION_SIZE, color = if (isCompleted) HudStyle.ALLY else HudStyle.SELECTION) { xy(TEXT_X, TITLE_Y) }
        nameText = text(fitted(quest.name, NAME_SIZE, ANNOUNCEMENT_WIDTH - TEXT_X - RIGHT_PADDING), textSize = NAME_SIZE, color = Colors.WHITE) { xy(TEXT_X, NAME_Y) }
    }

    private fun Container.drawGoal(quest: QuestInfo) {
        val task = quest.currentTask
        taskGlyph(task.kind, TEXT_X, GOAL_Y, GOAL_GLYPH_SIZE, HudStyle.SELECTION)
        taskText = text(fitted(task.text, GOAL_TEXT_SIZE, ANNOUNCEMENT_WIDTH - GOAL_TEXT_X - RIGHT_PADDING), textSize = GOAL_TEXT_SIZE, color = HudStyle.MUTED) { xy(GOAL_TEXT_X, GOAL_Y - 1) }
        rewardView = null
    }

    private fun Container.drawReward(quest: QuestInfo) {
        rewardView = container { rewardRow(quest, TEXT_X, REWARD_Y, resourcesLoader, messages) }
        taskText = null
    }
}
