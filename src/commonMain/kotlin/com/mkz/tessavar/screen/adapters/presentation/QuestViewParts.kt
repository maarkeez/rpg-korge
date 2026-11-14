package com.mkz.tessavar.screen.adapters.presentation

import com.mkz.tessavar.screen.adapters.presentation.HudStyle.panel
import korlibs.image.color.Colors
import korlibs.image.color.RGBA
import korlibs.korge.view.Container
import korlibs.korge.view.Text
import korlibs.korge.view.circle
import korlibs.korge.view.image
import korlibs.korge.view.roundRect
import korlibs.korge.view.solidRect
import korlibs.korge.view.text
import korlibs.korge.view.xy
import korlibs.math.geom.Angle
import korlibs.math.geom.RectCorners
import korlibs.math.geom.Size
import korlibs.math.geom.degrees

internal object QuestViewParts {
    const val CAPTION_SIZE = 11.0
    const val REWARD_ROW_HEIGHT = 28.0
    const val LINE_SPACING = 3.0

    private const val MAXIMUM_SEGMENTS = 10
    private const val SEGMENT_GAP = 2.0
    private const val REWARD_GAP = 5.0
    private const val RING_THICKNESS = 2.0
    private const val ELLIPSIS = "…"
    private const val PILL_TEXT_SIZE = 10.0

    fun widthOf(
        content: String,
        textSize: Double,
    ): Double = Text(content, textSize = textSize).width

    fun fitted(
        content: String,
        textSize: Double,
        width: Double,
    ): String {
        if (widthOf(content, textSize) <= width) return content
        var cut = content
        while (cut.isNotEmpty() && widthOf(cut + ELLIPSIS, textSize) > width) cut = cut.dropLast(1)
        return cut.trimEnd() + ELLIPSIS
    }

    fun Container.wrappedText(
        content: String,
        x: Double,
        y: Double,
        width: Double,
        textSize: Double,
        color: RGBA,
        isCentered: Boolean = false,
        maximumLines: Int = Int.MAX_VALUE,
    ): Double {
        val lines = mutableListOf<String>()
        content.split(" ").forEach { word ->
            val line = lines.lastOrNull()
            if (line != null && widthOf("$line $word", textSize) <= width) {
                lines[lines.lastIndex] = "$line $word"
            } else {
                lines += word
            }
        }
        if (lines.size > maximumLines) {
            lines[maximumLines - 1] = fitted("${lines[maximumLines - 1]} ${lines[maximumLines]}", textSize, width)
            lines.subList(maximumLines, lines.size).clear()
        }
        lines.forEachIndexed { index, line ->
            text(line, textSize = textSize, color = color) {
                xy(if (isCentered) x + (width - this.width) / 2 else x, y + index * (textSize + LINE_SPACING))
            }
        }
        return lines.size * (textSize + LINE_SPACING) - LINE_SPACING
    }

    fun Container.tick(
        x: Double,
        y: Double,
        side: Double,
        color: RGBA,
    ) {
        arm(x + side * 0.18, y + side * 0.5, side * 0.34, side * 0.15, 45.degrees, color)
        arm(x + side * 0.4, y + side * 0.76, side * 0.62, side * 0.15, (-45).degrees, color)
    }

    fun Container.tickDisc(
        x: Double,
        y: Double,
        side: Double,
    ) {
        circle(radius = side / 2, fill = HudStyle.ALLY) { xy(x, y) }
        tick(x + side * 0.16, y + side * 0.14, side * 0.68, Colors.WHITE)
    }

    fun Container.chevron(
        x: Double,
        y: Double,
        side: Double,
        color: RGBA,
    ) {
        arm(x + side * 0.36, y + side * 0.14, side * 0.46, side * 0.14, 45.degrees, color)
        arm(x + side * 0.68, y + side * 0.46, side * 0.46, side * 0.14, 135.degrees, color)
    }

    fun Container.taskGlyph(
        kind: QuestInfo.Kind,
        x: Double,
        y: Double,
        side: Double,
        color: RGBA,
    ) {
        when (kind) {
            QuestInfo.Kind.DEFEAT -> {
                val tick = side * 0.3
                val thickness = side * 0.14
                val middle = (side - thickness) / 2
                solidRect(Size(thickness, tick), color) { xy(x + middle, y) }
                solidRect(Size(thickness, tick), color) { xy(x + middle, y + side - tick) }
                solidRect(Size(tick, thickness), color) { xy(x, y + middle) }
                solidRect(Size(tick, thickness), color) { xy(x + side - tick, y + middle) }
                circle(radius = side * 0.11, fill = color) { xy(x + side * 0.39, y + side * 0.39) }
            }
            QuestInfo.Kind.TALK -> {
                roundRect(Size(side, side * 0.68), RectCorners(side * 0.2), fill = color) { xy(x, y + side * 0.06) }
                arm(x + side * 0.24, y + side * 0.66, side * 0.3, side * 0.22, 60.degrees, color)
            }
            QuestInfo.Kind.VISIT -> {
                solidRect(Size(side * 0.14, side), color) { xy(x + side * 0.16, y) }
                solidRect(Size(side * 0.62, side * 0.42), color) { xy(x + side * 0.3, y + side * 0.04) }
            }
        }
    }

    fun Container.targetSlot(
        task: QuestInfo.Task,
        resourcesLoader: GameSceneResourcesLoader,
        x: Double,
        y: Double,
        side: Double,
        portraitScale: Double,
        ring: RGBA,
    ) {
        roundRect(Size(side, side), RectCorners(HudStyle.SLOT_RADIUS), fill = ring) { xy(x, y) }
        roundRect(Size(side - 2 * RING_THICKNESS, side - 2 * RING_THICKNESS), RectCorners(HudStyle.SLOT_RADIUS - RING_THICKNESS), fill = HudStyle.PANEL_RAISED) {
            xy(x + RING_THICKNESS, y + RING_THICKNESS)
        }
        val bitmap = task.portraitPath?.let(resourcesLoader::bitmapOrNull)
        if (bitmap == null) {
            taskGlyph(task.kind, x + side * 0.25, y + side * 0.25, side * 0.5, ring)
        } else {
            image(bitmap) {
                smoothing = false
                scale = portraitScale
                xy(x + (side - bitmap.width * portraitScale) / 2, y + (side - bitmap.height * portraitScale) / 2)
            }
        }
    }

    fun ringOf(task: QuestInfo.Task): RGBA =
        when (task.kind) {
            QuestInfo.Kind.DEFEAT -> HudStyle.ENEMY
            QuestInfo.Kind.TALK -> HudStyle.ALLY
            QuestInfo.Kind.VISIT -> HudStyle.SELECTION
        }

    fun Container.progressBar(
        x: Double,
        y: Double,
        width: Double,
        height: Double,
        progress: Int,
        count: Int,
    ) {
        val corners = RectCorners(height / 2)
        if (count in 2..MAXIMUM_SEGMENTS) {
            val segment = (width - SEGMENT_GAP * (count - 1)) / count
            repeat(count) { index ->
                roundRect(Size(segment, height), corners, fill = if (index < progress) HudStyle.SELECTION else HudStyle.TRACK) { xy(x + index * (segment + SEGMENT_GAP), y) }
            }
        } else {
            roundRect(Size(width, height), corners, fill = HudStyle.TRACK) { xy(x, y) }
            if (progress > 0) roundRect(Size(width * progress / count, height), corners, fill = HudStyle.SELECTION) { xy(x, y) }
        }
    }

    fun Container.rewardRow(
        quest: QuestInfo,
        x: Double,
        y: Double,
        resourcesLoader: GameSceneResourcesLoader,
        messages: Messages,
    ): Double {
        var cursor = x
        if (quest.gold > 0) {
            val pill = PillView(color = HudStyle.SELECTION, glyph = PillView.Glyph.GOLD)
            pill.display(messages.gold(quest.gold))
            addChild(pill)
            pill.xy(cursor, y + (REWARD_ROW_HEIGHT - pill.pillHeight) / 2)
            cursor += pill.width + REWARD_GAP
        }
        quest.items.forEach { item ->
            itemSlot(item, cursor, y, resourcesLoader)
            cursor += REWARD_ROW_HEIGHT + REWARD_GAP
        }
        return cursor - x - REWARD_GAP
    }

    fun Container.pinGlyph(
        x: Double,
        y: Double,
        side: Double,
        color: RGBA,
    ) {
        solidRect(Size(side * 0.44, side * 0.46), color) { xy(x + side * 0.28, y) }
        solidRect(Size(side * 0.8, side * 0.14), color) { xy(x + side * 0.1, y + side * 0.46) }
        solidRect(Size(side * 0.14, side * 0.4), color) { xy(x + side * 0.43, y + side * 0.6) }
    }

    fun Container.exclamationGlyph(
        x: Double,
        y: Double,
        side: Double,
        color: RGBA,
    ) {
        solidRect(Size(side * 0.2, side * 0.58), color) { xy(x + side * 0.4, y) }
        solidRect(Size(side * 0.2, side * 0.2), color) { xy(x + side * 0.4, y + side * 0.72) }
    }

    fun Container.repeatablePill(
        x: Double,
        y: Double,
        messages: Messages,
    ): PillView {
        val pill = PillView(color = HudStyle.MANA, textSize = PILL_TEXT_SIZE)
        pill.display(messages[UiMessage.QUEST_REPEATABLE])
        addChild(pill)
        pill.xy(x, y)
        return pill
    }

    fun Container.itemSlot(
        item: QuestInfo.RewardItem,
        x: Double,
        y: Double,
        resourcesLoader: GameSceneResourcesLoader,
    ) {
        panel(Size(REWARD_ROW_HEIGHT, REWARD_ROW_HEIGHT), fill = HudStyle.PANEL_RAISED, radius = HudStyle.SLOT_RADIUS).xy(x, y)
        resourcesLoader.bitmapOrNull("lootableItem/${item.id}.png")?.let { bitmap ->
            image(bitmap) {
                smoothing = false
                xy(x + (REWARD_ROW_HEIGHT - bitmap.width) / 2, y + (REWARD_ROW_HEIGHT - bitmap.height) / 2)
            }
        }
    }

    private fun Container.arm(
        x: Double,
        y: Double,
        length: Double,
        thickness: Double,
        angle: Angle,
        color: RGBA,
    ) {
        solidRect(Size(length, thickness), color) {
            rotation = angle
            xy(x, y)
        }
    }
}
