package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.HudStyle.panel
import com.mkz.rpg.screen.adapters.presentation.QuestViewParts.fitted
import com.mkz.rpg.screen.adapters.presentation.QuestViewParts.itemSlot
import com.mkz.rpg.screen.adapters.presentation.QuestViewParts.rewardRow
import com.mkz.rpg.screen.adapters.presentation.QuestViewParts.ringOf
import com.mkz.rpg.screen.adapters.presentation.QuestViewParts.targetSlot
import com.mkz.rpg.screen.adapters.presentation.QuestViewParts.taskGlyph
import com.mkz.rpg.screen.adapters.presentation.QuestViewParts.tickDisc
import com.mkz.rpg.screen.adapters.presentation.QuestViewParts.wrappedText
import korlibs.image.color.Colors
import korlibs.korge.ui.UIContainer
import korlibs.korge.view.Container
import korlibs.korge.view.RoundRect
import korlibs.korge.view.Text
import korlibs.korge.view.container
import korlibs.korge.view.solidRect
import korlibs.korge.view.text
import korlibs.korge.view.xy
import korlibs.math.geom.Size

/**
 * A card over the dimmed screen, until Continue is tapped, announcing that the main quest started, with its goal, or
 * completed, with its reward. The dim takes every tap, so what is under it is not pressed.
 */
open class QuestAnnouncementView(
    private val resourcesLoader: BattleSceneResourcesLoader,
    private val texts: Texts,
) : UIContainer(Size(SCREEN_WIDTH, SCREEN_HEIGHT)) {
    companion object {
        const val SCREEN_WIDTH = 390.0
        const val SCREEN_HEIGHT = 844.0

        private const val DIM_ALPHA = 0.62
        private const val CARD_WIDTH = 330.0
        private const val CARD_RADIUS = 14.0
        private const val PADDING = 16.0
        private const val CONTENT_WIDTH = CARD_WIDTH - 2 * PADDING
        private const val TARGET_SIZE = 72.0
        private const val TARGET_TICK_SIZE = 22.0
        private const val TITLE_SIZE = 12.0
        private const val NAME_SIZE = 18.0
        private const val DESCRIPTION_SIZE = 12.0
        private const val TASK_ROW_HEIGHT = 34.0
        private const val TASK_GLYPH_SIZE = 14.0
        private const val TASK_TEXT_X = 32.0
        private const val TEXT_SIZE = 13.0
        private const val ITEM_SPACING = 4.0
        private const val ITEM_NAME_SPACING = 8.0
        private const val CONTINUE_HEIGHT = 32.0
    }

    enum class Moment { STARTED, COMPLETED }

    interface Delegate {
        fun questAnnouncementContinued()
    }

    private var delegate: Delegate? = null

    internal var card: Container? = null
    internal var background: RoundRect? = null
    internal var titleText: Container? = null
    internal var nameView: Container? = null
    internal var descriptionView: Container? = null
    internal var taskText: Text? = null
    internal var targetTick: Container? = null
    internal var rewardView: Container? = null
    internal var itemNames: List<Text> = emptyList()
    internal var continueButton: ConfirmButton? = null

    init {
        visible = false
    }

    fun setDelegate(delegate: Delegate) {
        this.delegate = delegate
    }

    /** Draws the card and shows it. */
    open fun display(
        moment: Moment,
        quest: QuestInfo,
    ) {
        removeChildren()
        // A view only takes the taps when it is mouse enabled, so the pad under it is not pressed.
        solidRect(Size(SCREEN_WIDTH, SCREEN_HEIGHT), Colors.BLACK.withAd(DIM_ALPHA)) { mouseEnabled = true }
        card =
            container {
                val cardBackground = panel(Size(CARD_WIDTH, CONTINUE_HEIGHT), radius = CARD_RADIUS)
                background = cardBackground
                var cursor = drawHeader(moment, quest)
                cursor =
                    when (moment) {
                        Moment.STARTED -> drawGoal(quest, cursor)
                        Moment.COMPLETED -> drawReward(quest, cursor)
                    }
                continueButton =
                    ConfirmButton(texts[UiText.QUEST_ANNOUNCEMENT_CONTINUE], CONTENT_WIDTH) { delegate?.questAnnouncementContinued() }.also { button ->
                        addChild(button)
                        button.xy(PADDING, cursor)
                    }
                val cardHeight = cursor + CONTINUE_HEIGHT + PADDING
                cardBackground.size = Size(CARD_WIDTH, cardHeight)
                xy((SCREEN_WIDTH - CARD_WIDTH) / 2, (SCREEN_HEIGHT - cardHeight) / 2)
            }
        visible = true
    }

    open fun hide() {
        visible = false
    }

    // The target, the title and the name, all centred. Returns where what comes under them starts.
    private fun Container.drawHeader(
        moment: Moment,
        quest: QuestInfo,
    ): Double {
        val task = quest.currentTask
        val isCompleted = moment == Moment.COMPLETED
        val targetX = (CARD_WIDTH - TARGET_SIZE) / 2
        targetSlot(task, resourcesLoader, targetX, PADDING, TARGET_SIZE, portraitScale = 2.0, ring = if (isCompleted) HudStyle.ALLY else ringOf(task))
        targetTick = if (isCompleted) container { tickDisc(targetX + TARGET_SIZE - 16, PADDING + TARGET_SIZE - 16, TARGET_TICK_SIZE) } else null
        var cursor = PADDING + TARGET_SIZE + 12
        val title = texts[if (isCompleted) UiText.QUEST_ANNOUNCEMENT_COMPLETED else UiText.QUEST_ANNOUNCEMENT_STARTED]
        var titleHeight = 0.0
        val titleY = cursor
        titleText = container { titleHeight = wrappedText(title, PADDING, titleY, CONTENT_WIDTH, TITLE_SIZE, if (isCompleted) HudStyle.ALLY else HudStyle.SELECTION, isCentered = true) }
        cursor += titleHeight + 6
        var nameHeight = 0.0
        val nameY = cursor
        nameView = container { nameHeight = wrappedText(quest.name, PADDING, nameY, CONTENT_WIDTH, NAME_SIZE, Colors.WHITE, isCentered = true) }
        return cursor + nameHeight + 10
    }

    // The description and the current task. Returns where Continue goes.
    private fun Container.drawGoal(
        quest: QuestInfo,
        top: Double,
    ): Double {
        val task = quest.currentTask
        var descriptionHeight = 0.0
        descriptionView = container { descriptionHeight = wrappedText(quest.description, PADDING, top, CONTENT_WIDTH, DESCRIPTION_SIZE, HudStyle.MUTED, isCentered = true) }
        val taskY = top + descriptionHeight + 12
        panel(Size(CONTENT_WIDTH, TASK_ROW_HEIGHT), fill = HudStyle.PANEL_RAISED, radius = HudStyle.SLOT_RADIUS).xy(PADDING, taskY)
        taskGlyph(task.kind, PADDING + 10, taskY + 10, TASK_GLYPH_SIZE, HudStyle.SELECTION)
        taskText = text(fitted(task.text, TEXT_SIZE, CONTENT_WIDTH - TASK_TEXT_X - 10), textSize = TEXT_SIZE, color = Colors.WHITE) { xy(PADDING + TASK_TEXT_X, taskY + 9) }
        rewardView = null
        itemNames = emptyList()
        return taskY + TASK_ROW_HEIGHT + 14
    }

    // The gold, and each item in a row with its name. Returns where Continue goes.
    private fun Container.drawReward(
        quest: QuestInfo,
        top: Double,
    ): Double {
        solidRect(Size(CONTENT_WIDTH, 1.0), HudStyle.STROKE) { xy(PADDING, top) }
        text(texts[UiText.QUEST_ANNOUNCEMENT_REWARD], textSize = QuestViewParts.CAPTION_SIZE, color = HudStyle.FAINT) { xy(PADDING, top + 10) }
        var cursor = top + 28
        if (quest.gold > 0) {
            val goldY = cursor
            rewardView = container { rewardRow(quest.copy(items = emptyList()), PADDING, goldY, resourcesLoader, texts) }
            cursor += QuestViewParts.REWARD_ROW_HEIGHT + ITEM_SPACING
        } else {
            rewardView = null
        }
        itemNames =
            quest.items.map { item ->
                itemSlot(item, PADDING, cursor, resourcesLoader)
                val nameY = cursor + (QuestViewParts.REWARD_ROW_HEIGHT - TEXT_SIZE) / 2 - 1
                cursor += QuestViewParts.REWARD_ROW_HEIGHT + ITEM_SPACING
                text(item.name, textSize = TEXT_SIZE, color = Colors.WHITE) { xy(PADDING + QuestViewParts.REWARD_ROW_HEIGHT + ITEM_NAME_SPACING, nameY) }
            }
        descriptionView = null
        taskText = null
        return cursor + 8
    }
}
