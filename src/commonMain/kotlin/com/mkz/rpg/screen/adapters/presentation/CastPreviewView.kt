package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.HudStyle.panel
import korlibs.image.color.Colors
import korlibs.korge.view.Container
import korlibs.korge.view.Image
import korlibs.korge.view.Text
import korlibs.korge.view.container
import korlibs.korge.view.image
import korlibs.korge.view.text
import korlibs.korge.view.xy
import korlibs.math.geom.Size

/**
 * The forecast of a cast: the ability and its caster on a line, with the mana it spends, then a row per battle unit the
 * cast changes, all in sight: the caster first, when the cast changes its health or its effects (e.g.: a heal), then
 * the recipients. A cast reaches at most the caster and its 4 adjacent tiles, so the 5 rows fit in the HUD.
 */
open class CastPreviewView(
    private val resourcesLoader: GameSceneResourcesLoader,
    private val messages: Messages,
) : Container() {
    companion object {
        private const val WIDTH = 390.0
        private const val MARGIN = 4.0
        private const val HEADER_HEIGHT = 52.0
        private const val ICON_SLOT_SIZE = 40.0
        private const val ICON_SCALE = 2.0
        private const val NAME_X = MARGIN + 54
        private const val NAME_TEXT_SIZE = 15.0
        private const val CASTER_TEXT_SIZE = 11.0
        private const val MANA_TEXT_SIZE = 11.0
        internal const val ROWS_Y = HEADER_HEIGHT + 8
        internal const val ROW_PITCH = RecipientPreviewView.HEIGHT + 4
    }

    /** A battle unit receiving the cast, the health it would have left and the effects that would stay on it. */
    data class Recipient(
        val battleUnit: BattleUnitInfo,
        val remainingHealth: Int,
        val newEffectIds: List<String> = emptyList(),
    )

    private val header: Container =
        container {
            panel(Size(WIDTH - 2 * MARGIN, HEADER_HEIGHT)).xy(MARGIN, 0.0)
            panel(Size(ICON_SLOT_SIZE, ICON_SLOT_SIZE), fill = HudStyle.PANEL_RAISED, radius = HudStyle.SLOT_RADIUS).xy(MARGIN + 6, 6.0)
        }
    internal var abilityIcon: Image? = null
    internal val abilityNameText: Text = header.text("", textSize = NAME_TEXT_SIZE, color = Colors.WHITE) { xy(NAME_X, 8.0) }
    internal val casterText: Text = header.text("", textSize = CASTER_TEXT_SIZE, color = HudStyle.MUTED) { xy(NAME_X, 28.0) }
    internal val manaPill = PillView(color = HudStyle.MANA, glyph = PillView.Glyph.MANA, textSize = MANA_TEXT_SIZE).also(header::addChild)

    // A row per battle unit the cast changes. Grows to the largest number of rows displayed, the ones not needed are hidden.
    internal val rowViews = mutableListOf<RecipientPreviewView>()

    // The row of the caster, or null when the cast does not change it.
    internal var casterRowView: RecipientPreviewView? = null

    init {
        visible = false
    }

    open fun display(
        abilityId: String,
        casterBattleUnit: BattleUnitInfo,
        manaCost: Int,
        recipients: List<Recipient>,
        casterRemainingHealth: Int = casterBattleUnit.remainingHealth,
        casterNewEffectIds: List<String> = emptyList(),
    ) {
        displayHeader(abilityId, casterBattleUnit, manaCost)

        // The caster gets a row only when the cast changes it, e.g.: a heal.
        val isCasterChanged = casterRemainingHealth != casterBattleUnit.remainingHealth || casterNewEffectIds.isNotEmpty()
        val rows =
            buildList {
                if (isCasterChanged) add(Recipient(casterBattleUnit, casterRemainingHealth, casterNewEffectIds))
                addAll(recipients)
            }
        while (rowViews.size < rows.size) {
            val rowView = RecipientPreviewView(resourcesLoader, messages)
            rowView.xy(MARGIN, ROWS_Y + rowViews.size * ROW_PITCH)
            addChild(rowView)
            rowViews += rowView
        }
        rowViews.forEachIndexed { index, rowView ->
            val row = rows.getOrNull(index)
            if (row == null) {
                rowView.hide()
            } else {
                rowView.display(battleUnit = row.battleUnit, remainingHealth = row.remainingHealth, newEffectIds = row.newEffectIds)
            }
        }
        casterRowView = if (isCasterChanged) rowViews.first() else null
        visible = true
    }

    open fun hide() {
        visible = false
    }

    // The ability's icon at 2x in its slot, its name, its caster, and the mana it spends, left out for a free ability.
    private fun displayHeader(
        abilityId: String,
        casterBattleUnit: BattleUnitInfo,
        manaCost: Int,
    ) {
        abilityIcon?.removeFromParent()
        abilityIcon =
            resourcesLoader.bitmapOrNull("ability/$abilityId.png")?.let { bitmap ->
                header.image(bitmap) {
                    smoothing = false
                    scale = ICON_SCALE
                    xy(MARGIN + 6 + (ICON_SLOT_SIZE - bitmap.width * ICON_SCALE) / 2, 6 + (ICON_SLOT_SIZE - bitmap.height * ICON_SCALE) / 2)
                }
            }
        abilityNameText.text = messages.abilityName(abilityId)
        casterText.text = messages[UiMessage.CAST_PREVIEW_BY, "unit" to messages.unitName(casterBattleUnit.unitId)]
        if (manaCost > 0) {
            manaPill.display(messages[UiMessage.CAST_PREVIEW_MANA, "mana" to manaCost])
            manaPill.xy(WIDTH - MARGIN - 10 - manaPill.width, (HEADER_HEIGHT - manaPill.pillHeight) / 2)
        } else {
            manaPill.hide()
        }
    }
}
