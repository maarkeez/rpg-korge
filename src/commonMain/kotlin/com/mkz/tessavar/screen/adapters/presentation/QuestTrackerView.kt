package com.mkz.tessavar.screen.adapters.presentation

import com.mkz.tessavar.screen.adapters.presentation.HudStyle.panel
import com.mkz.tessavar.screen.adapters.presentation.QuestViewParts.chevron
import com.mkz.tessavar.screen.adapters.presentation.QuestViewParts.fitted
import com.mkz.tessavar.screen.adapters.presentation.QuestViewParts.pinGlyph
import com.mkz.tessavar.screen.adapters.presentation.QuestViewParts.taskGlyph
import com.mkz.tessavar.screen.adapters.presentation.QuestViewParts.tickDisc
import com.mkz.tessavar.screen.adapters.presentation.QuestViewParts.widthOf
import korlibs.image.color.Colors
import korlibs.korge.animate.animator
import korlibs.korge.animate.scaleTo
import korlibs.korge.input.onClick
import korlibs.korge.ui.UIContainer
import korlibs.korge.view.Container
import korlibs.korge.view.RoundRect
import korlibs.korge.view.Text
import korlibs.korge.view.container
import korlibs.korge.view.text
import korlibs.korge.view.xy
import korlibs.math.geom.Size
import korlibs.time.milliseconds

open class QuestTrackerView(
    private val messages: Messages,
) : UIContainer(Size(390, 33)) {
    companion object {
        const val POP_SCALE = 1.15
        val POP_DURATION = 400.milliseconds

        const val MAXIMUM_TEXT_WIDTH = 200.0

        private const val RIGHT_EDGE = 384.0
        private const val TOP = 1.0
        private const val HEIGHT = 30.0
        private const val TEXT_SIZE = 13.0
        private const val GLYPH_SIZE = 14.0
        private const val PIN_SIZE = 13.0
        private const val PIN_SPACING = 3.0
        private const val CHEVRON_SIZE = 10.0
        private const val LEFT_PADDING = 10.0
        private const val RIGHT_PADDING = 8.0
        private const val TEXT_SPACING = 6.0
        private const val CHEVRON_SPACING = 4.0
    }

    interface Delegate {
        fun questTrackerSelected()
    }

    private var delegate: Delegate? = null
    private var quest: QuestInfo? = null
    private var isEmpty = false
    private var isTappable = true

    internal val capsule: Container =
        container {
            xy(RIGHT_EDGE, TOP + HEIGHT / 2)
            onClick { if (isTappable) delegate?.questTrackerSelected() }
        }
    internal var background: RoundRect? = null
    internal var taskText: Text? = null
    internal var countText: Text? = null
    internal var pin: Container? = null
    internal var glyph: Container? = null
    internal var tick: Container? = null
    internal var chevronView: Container? = null

    fun setDelegate(delegate: Delegate) {
        this.delegate = delegate
    }

    open fun display(quest: QuestInfo) {
        this.quest = quest
        isEmpty = false
        draw(quest)
    }

    open fun displayEmpty() {
        quest = null
        isEmpty = true
        drawEmpty()
    }

    open fun clear() {
        quest = null
        isEmpty = false
        capsule.removeChildren()
        background = null
        taskText = null
        countText = null
        pin = null
        glyph = null
        tick = null
        chevronView = null
    }

    open fun setTappable(isTappable: Boolean) {
        this.isTappable = isTappable
        quest?.let(::draw)
        if (isEmpty) drawEmpty()
    }

    open fun hide() {
        visible = false
    }

    open suspend fun pop() {
        val popAnimator =
            animator {
                scaleTo(capsule, POP_SCALE, time = POP_DURATION / 2)
                scaleTo(capsule, 1.0, time = POP_DURATION / 2)
            }
        popAnimator.awaitComplete()
    }

    private fun drawEmpty() {
        val label = fitted(messages[UiMessage.QUEST_TRACKER_EMPTY], TEXT_SIZE, MAXIMUM_TEXT_WIDTH)
        val labelWidth = widthOf(label, TEXT_SIZE)
        val chevronWidth = if (isTappable) CHEVRON_SPACING + CHEVRON_SIZE else 0.0
        val capsuleWidth = LEFT_PADDING + labelWidth + chevronWidth + RIGHT_PADDING
        val textY = (HEIGHT - TEXT_SIZE) / 2 - 1
        capsule.removeChildren()
        capsule.container {
            xy(-capsuleWidth, -HEIGHT / 2)
            background = panel(Size(capsuleWidth, HEIGHT), radius = HEIGHT / 2)
            pin = null
            glyph = null
            tick = null
            countText = null
            taskText = text(label, textSize = TEXT_SIZE, color = HudStyle.MUTED) { xy(LEFT_PADDING, textY) }
            chevronView = if (isTappable) container { chevron(capsuleWidth - RIGHT_PADDING - CHEVRON_SIZE, (HEIGHT - CHEVRON_SIZE) / 2, CHEVRON_SIZE, HudStyle.MUTED) } else null
        }
    }

    private fun draw(quest: QuestInfo) {
        val task = quest.currentTask
        val label = fitted(if (quest.isCompleted) messages[UiMessage.QUEST_TRACKER_COMPLETED] else task.text, TEXT_SIZE, MAXIMUM_TEXT_WIDTH)
        val count = if (!quest.isCompleted && task.requiredDefeats > 1) "${task.defeatsCounted}/${task.requiredDefeats}" else null
        val labelWidth = widthOf(label, TEXT_SIZE)
        val countWidth = count?.let { value -> TEXT_SPACING + widthOf(value, TEXT_SIZE) } ?: 0.0
        val chevronWidth = if (isTappable) CHEVRON_SPACING + CHEVRON_SIZE else 0.0
        val isPinShown = quest.isPinned && !quest.isMain
        val glyphX = LEFT_PADDING + if (isPinShown) PIN_SIZE + PIN_SPACING else 0.0
        val textX = glyphX + GLYPH_SIZE + TEXT_SPACING
        val capsuleWidth = textX + labelWidth + countWidth + chevronWidth + RIGHT_PADDING
        val glyphY = (HEIGHT - GLYPH_SIZE) / 2
        val textY = (HEIGHT - TEXT_SIZE) / 2 - 1
        capsule.removeChildren()
        capsule.container {
            xy(-capsuleWidth, -HEIGHT / 2)
            background = panel(Size(capsuleWidth, HEIGHT), radius = HEIGHT / 2)
            pin = if (isPinShown) container { pinGlyph(LEFT_PADDING, (HEIGHT - PIN_SIZE) / 2, PIN_SIZE, HudStyle.SELECTION) } else null
            glyph = if (quest.isCompleted) null else container { taskGlyph(task.kind, glyphX, glyphY, GLYPH_SIZE, HudStyle.SELECTION) }
            tick = if (quest.isCompleted) container { tickDisc(glyphX, glyphY, GLYPH_SIZE) } else null
            taskText = text(label, textSize = TEXT_SIZE, color = if (quest.isCompleted) HudStyle.MUTED else Colors.WHITE) { xy(textX, textY) }
            countText = count?.let { value -> text(value, textSize = TEXT_SIZE, color = HudStyle.SELECTION) { xy(textX + labelWidth + TEXT_SPACING, textY) } }
            chevronView = if (isTappable) container { chevron(capsuleWidth - RIGHT_PADDING - CHEVRON_SIZE, (HEIGHT - CHEVRON_SIZE) / 2, CHEVRON_SIZE, HudStyle.MUTED) } else null
        }
    }
}
