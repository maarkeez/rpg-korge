package com.mkz.rpg.screen

import com.mkz.rpg.shared.adapters.presentation.PIXEL_SCALE
import com.mkz.rpg.shared.adapters.presentation.UiPalette
import korlibs.image.text.TextAlignment
import korlibs.korge.style.styles
import korlibs.korge.style.textAlignment
import korlibs.korge.style.textColor
import korlibs.korge.style.textSize
import korlibs.korge.ui.UIContainer
import korlibs.korge.ui.uiText
import korlibs.korge.view.SolidRect
import korlibs.korge.view.image
import korlibs.math.geom.Size

/**
 * Up to [MAX_ROWS] statuses side by side. Each entry shows its pixel-art icon at x[ICON_SCALE] with the short name and
 * the turns left stacked beside it. On-death statuses show a hollow ring and "On death".
 */
class StatusListView(
    size: Size,
    private val sprites: SpriteRegistry = SpriteRegistry(),
) : UIContainer(size) {
    data class Status(
        val effectId: String,
        /** `null` for on-death effects, which never run out by time. */
        val turnsLeft: Int?,
    ) {
        val shortName: String get() = effectId.replace('-', ' ').replaceFirstChar { it.uppercase() }
        val isOnDeath: Boolean get() = turnsLeft == null
        val turnsLabel: String get() =
            if (turnsLeft == null) {
                "On death"
            } else if (turnsLeft == 1) {
                "1 turn"
            } else {
                "$turnsLeft turns"
            }
    }

    companion object {
        const val STATUS_ROW = "STATUS_ROW"
        const val STATUS_TURNS_LABEL = "STATUS_TURNS_LABEL"
        const val STATUS_ON_DEATH_GLYPH = "STATUS_ON_DEATH_GLYPH"
        const val STATUS_OVERFLOW_LABEL = "STATUS_OVERFLOW_LABEL"
        const val MAX_ROWS = 3
        const val ROW_HEIGHT = 34
        const val ICON_SCALE = 2
        private const val ART_SIZE = 16
        private const val ICON_SIZE = ART_SIZE * ICON_SCALE
        private const val LINE_HEIGHT = 16.0
        private const val TEXT_SIZE = 12.0
        private const val TEXT_GAP = 4
    }

    private val entryWidth: Double get() = width / MAX_ROWS

    val rowCount: Int get() = children.count { it.name == STATUS_ROW }

    fun display(statuses: List<Status>) {
        removeChildren()
        val shown = if (statuses.size > MAX_ROWS) statuses.take(MAX_ROWS - 1) else statuses
        shown.forEachIndexed { index, status -> addRow(index, status) }
        if (statuses.size > MAX_ROWS) {
            val hidden = statuses.size - shown.size
            uiText("+$hidden more", size = Size(entryWidth, ROW_HEIGHT.toDouble())) {
                name = STATUS_OVERFLOW_LABEL
                x = shown.size * entryWidth
                styles.textSize = TEXT_SIZE
                styles.textColor = UiPalette.textMuted
                styles.textAlignment = TextAlignment.MIDDLE_LEFT
            }
        }
        visible = statuses.isNotEmpty()
    }

    private fun addRow(
        index: Int,
        status: Status,
    ) {
        val row = UIContainer(Size(entryWidth, ROW_HEIGHT.toDouble()))
        row.name = STATUS_ROW
        row.x = index * entryWidth
        addChild(row)
        row.image(sprites.effect(status.effectId)) {
            smoothing = false
            scale = ICON_SCALE.toDouble()
            y = (ROW_HEIGHT - ICON_SIZE) / 2.0
        }
        val textX = (ICON_SIZE + TEXT_GAP).toDouble()
        val textWidth = entryWidth - textX
        row.uiText(status.shortName, size = Size(textWidth, LINE_HEIGHT)) {
            x = textX
            styles.textSize = TEXT_SIZE
            styles.textColor = UiPalette.textPrimary
            styles.textAlignment = TextAlignment.MIDDLE_LEFT
        }
        val glyphWidth = if (status.isOnDeath) addOnDeathGlyph(row, textX) else 0.0
        row.uiText(status.turnsLabel, size = Size(textWidth - glyphWidth, LINE_HEIGHT)) {
            name = STATUS_TURNS_LABEL
            x = textX + glyphWidth
            y = ROW_HEIGHT - LINE_HEIGHT
            styles.textSize = TEXT_SIZE
            styles.textColor = if (status.isOnDeath) UiPalette.danger else UiPalette.conditional
            styles.textAlignment = TextAlignment.MIDDLE_LEFT
        }
    }

    /** The same 3x3 art pixel ring used by the on-death pip on the map. Returns the width it takes, gap included. */
    private fun addOnDeathGlyph(
        row: UIContainer,
        x: Double,
    ): Double {
        val ring =
            korlibs.korge.view
                .Container()
                .also { it.name = STATUS_ON_DEATH_GLYPH }
        row.addChild(ring)
        ring.x = x
        ring.y = ROW_HEIGHT - LINE_HEIGHT + (LINE_HEIGHT - 3 * PIXEL_SCALE) / 2.0
        listOf(0 to 0, 1 to 0, 2 to 0, 0 to 1, 2 to 1, 0 to 2, 1 to 2, 2 to 2).forEach { (dx, dy) ->
            ring.addChild(
                SolidRect(Size(PIXEL_SCALE, PIXEL_SCALE), UiPalette.danger).also {
                    it.x = (dx * PIXEL_SCALE).toDouble()
                    it.y = (dy * PIXEL_SCALE).toDouble()
                },
            )
        }
        return 3.0 * PIXEL_SCALE + TEXT_GAP
    }
}
