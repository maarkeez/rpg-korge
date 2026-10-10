package com.mkz.rpg.screen

import com.mkz.rpg.shared.adapters.presentation.PIXEL_SCALE
import com.mkz.rpg.shared.adapters.presentation.UiPalette
import korlibs.image.text.TextAlignment
import korlibs.korge.style.styles
import korlibs.korge.style.textAlignment
import korlibs.korge.style.textColor
import korlibs.korge.ui.UIContainer
import korlibs.korge.ui.uiText
import korlibs.korge.view.SolidRect
import korlibs.korge.view.image
import korlibs.math.geom.Size

/** One row per status: icon, short name and turns left. On-death statuses show a hollow ring and "On death". */
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
        const val ROW_HEIGHT = 18
        private const val ICON_SIZE = 16
    }

    val rowCount: Int get() = children.count { it.name == STATUS_ROW }

    fun display(statuses: List<Status>) {
        removeChildren()
        val shown = if (statuses.size > MAX_ROWS) statuses.take(MAX_ROWS - 1) else statuses
        shown.forEachIndexed { index, status -> addRow(index, status) }
        if (statuses.size > MAX_ROWS) {
            val hidden = statuses.size - shown.size
            uiText("+$hidden more", size = Size(width, ROW_HEIGHT)) {
                name = STATUS_OVERFLOW_LABEL
                y = (shown.size * ROW_HEIGHT).toDouble()
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
        val row = UIContainer(Size(width, ROW_HEIGHT))
        row.name = STATUS_ROW
        row.y = (index * ROW_HEIGHT).toDouble()
        addChild(row)
        row.image(sprites.effect(status.effectId)) {
            smoothing = false
            size = Size(ICON_SIZE, ICON_SIZE)
            y = (ROW_HEIGHT - ICON_SIZE) / 2.0
        }
        row.uiText(status.shortName, size = Size(width / 2, ROW_HEIGHT)) {
            x = (ICON_SIZE + 6).toDouble()
            styles.textColor = UiPalette.textPrimary
            styles.textAlignment = TextAlignment.MIDDLE_LEFT
        }
        if (status.isOnDeath) addOnDeathGlyph(row)
        row.uiText(status.turnsLabel, size = Size(width / 2 - ICON_SIZE, ROW_HEIGHT)) {
            name = STATUS_TURNS_LABEL
            x = width / 2 + ICON_SIZE
            styles.textColor = if (status.isOnDeath) UiPalette.danger else UiPalette.conditional
            styles.textAlignment = TextAlignment.MIDDLE_LEFT
        }
    }

    /** The same 3x3 art pixel ring used by the on-death pip on the map. */
    private fun addOnDeathGlyph(row: UIContainer) {
        val ring =
            korlibs.korge.view
                .Container()
                .also { it.name = STATUS_ON_DEATH_GLYPH }
        row.addChild(ring)
        ring.x = (width / 2 + 1)
        ring.y = (ROW_HEIGHT - 3 * PIXEL_SCALE) / 2.0
        listOf(0 to 0, 1 to 0, 2 to 0, 0 to 1, 2 to 1, 0 to 2, 1 to 2, 2 to 2).forEach { (dx, dy) ->
            ring.addChild(
                SolidRect(Size(PIXEL_SCALE, PIXEL_SCALE), UiPalette.danger).also {
                    it.x = (dx * PIXEL_SCALE).toDouble()
                    it.y = (dy * PIXEL_SCALE).toDouble()
                },
            )
        }
    }
}
