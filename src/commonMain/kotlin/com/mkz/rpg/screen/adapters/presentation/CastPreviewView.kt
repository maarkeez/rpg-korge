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

    internal val rowViews = mutableListOf<RecipientPreviewView>()

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
