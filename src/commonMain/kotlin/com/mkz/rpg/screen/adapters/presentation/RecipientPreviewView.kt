package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.HudStyle.panel
import korlibs.image.color.Colors
import korlibs.korge.view.Container
import korlibs.korge.view.Text
import korlibs.korge.view.text
import korlibs.korge.view.xy
import korlibs.math.geom.Size

class RecipientPreviewView(
    resourcesLoader: GameSceneResourcesLoader,
    private val messages: Messages,
) : Container() {
    companion object {
        const val WIDTH = 382.0
        const val HEIGHT = 44.0
        private const val ROW_RADIUS = 8.0
        private const val PORTRAIT_SIZE = 40.0
        private const val CONTENT_X = 50.0
        private const val NAME_TEXT_SIZE = 13.0
        private const val CHANGE_TEXT_SIZE = 18.0
        private const val KO_TEXT_SIZE = 10.0
        private const val SPACING = 6.0
        private const val RIGHT = WIDTH - 10
        private const val HEALTH_BAR_WIDTH = 262.0
        private const val HEALTH_BAR_HEIGHT = 7.0
        private const val MANA_BAR_HEIGHT = 4.0
    }

    init {
        panel(Size(WIDTH, HEIGHT), radius = ROW_RADIUS)
    }

    internal val unitPortraitView = UnitPortraitView(Size(PORTRAIT_SIZE, PORTRAIT_SIZE), resourcesLoader, portraitScale = 1.0).also { view -> addChild(view.apply { xy(2.0, 2.0) }) }
    internal val nameText: Text = text("", textSize = NAME_TEXT_SIZE, color = Colors.WHITE) { xy(CONTENT_X, 5.0) }
    internal val koPill = PillView(color = HudStyle.ENEMY, solid = true, textSize = KO_TEXT_SIZE).also(::addChild)
    internal val effectsView = EffectsView(resourcesLoader).also(::addChild)
    internal val healthBarView = HealthBarPreviewView(Size(HEALTH_BAR_WIDTH, HEALTH_BAR_HEIGHT)).also { view -> addChild(view.apply { xy(CONTENT_X, 26.0) }) }
    private val manaBarView = ManaBarView(Size(HEALTH_BAR_WIDTH, MANA_BAR_HEIGHT), showValue = false).also { view -> addChild(view.apply { xy(CONTENT_X, 36.0) }) }
    internal val changeText: Text = text("", textSize = CHANGE_TEXT_SIZE, color = Colors.WHITE) { y = 4.0 }

    fun display(
        battleUnit: BattleUnitInfo,
        remainingHealth: Int,
        newEffectIds: List<String>,
    ) {
        val remainingAfter = remainingHealth.coerceAtLeast(0)
        val isDefeated = remainingAfter == 0
        unitPortraitView.display(
            battleUnit.unitId,
            ring =
                when {
                    isDefeated -> HudStyle.DEFEATED
                    battleUnit.isHumanPlayers -> HudStyle.ALLY
                    else -> HudStyle.ENEMY
                },
        )
        nameText.text = messages.unitName(battleUnit.unitId)
        nameText.color = if (isDefeated) HudStyle.MUTED else Colors.WHITE
        var x = nameText.x + nameText.width + SPACING
        if (isDefeated) {
            koPill.display(messages[UiMessage.CAST_PREVIEW_DEFEATED])
            koPill.xy(x, 4.0)
            x += koPill.width + SPACING
        } else {
            koPill.hide()
        }
        effectsView.display(effectIds = battleUnit.effectIds, newEffectIds = newEffectIds)
        effectsView.xy(x, 2.0)
        healthBarView.display(remainingBefore = battleUnit.remainingHealth, remainingAfter = remainingAfter, maximum = battleUnit.maximumHealth)
        manaBarView.size = Size(healthBarView.barWidth, MANA_BAR_HEIGHT)
        manaBarView.display(remaining = battleUnit.remainingMana, maximum = battleUnit.maximumMana)
        val change = remainingAfter - battleUnit.remainingHealth
        changeText.text = if (change > 0) "+$change" else "$change"
        changeText.color =
            when {
                change > 0 -> HudStyle.GAIN
                change < 0 -> HudStyle.ENEMY
                else -> HudStyle.MUTED
            }
        changeText.x = RIGHT - changeText.width
        visible = true
    }

    fun hide() {
        visible = false
    }
}
