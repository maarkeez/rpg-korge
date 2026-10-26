package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.battleUnit.domain.BattleUnit
import com.mkz.rpg.unit.domain.Unit
import korlibs.korge.ui.UIContainer
import korlibs.korge.ui.UIText
import korlibs.korge.ui.uiHorizontalStack
import korlibs.korge.ui.uiSpacing
import korlibs.korge.ui.uiText
import korlibs.korge.ui.uiVerticalStack
import korlibs.korge.view.Container
import korlibs.korge.view.setText
import korlibs.math.geom.Size

/** A battle unit receiving the previewed cast, with the health it would have left. A row of [AttackPreviewView]. */
class ReceiverPreviewView(
    resourcesLoader: BattleSceneResourcesLoader,
    private val texts: Texts,
) : Container() {
    internal lateinit var counterLabel: UIText
    private lateinit var unitNameView: UnitNameView
    internal lateinit var unitPortraitView: UnitPortraitView
    private lateinit var healthBarView: HealthBarPreviewView
    private lateinit var manaBarView: ManaBarView
    private lateinit var effectsView: EffectsView

    init {
        uiHorizontalStack {
            uiVerticalStack(padding = 1.0) {
                // The counter shares the line of the name, which is aligned to the right.
                val nameLine = UIContainer(Size(width = 281.5, height = 14))
                unitNameView = UnitNameView(size = nameLine.size)
                unitNameView.alignTextToRight()
                nameLine.addChild(unitNameView)
                counterLabel = nameLine.uiText("", size = nameLine.size)
                addChild(nameLine)

                uiSpacing(Size(0, 15))
                healthBarView = HealthBarPreviewView(Size(281.5, 16))
                addChild(healthBarView)

                uiSpacing(Size(0, 4))
                manaBarView = ManaBarView(Size(281.5, 16))
                addChild(manaBarView)

                uiSpacing(Size(0, 4))
                effectsView = EffectsView(resourcesLoader)
                addChild(effectsView)
            }
            uiSpacing(Size(10, 0))
            unitPortraitView = UnitPortraitView(Size(width = 97.5, height = 97.5), resourcesLoader)
            addChild(unitPortraitView)
        }
    }

    /** Displays the receiver, being the [position] out of [count] receivers. The counter is only displayed when there are several. */
    fun display(
        battleUnit: BattleUnit.Dto,
        unit: Unit.Dto,
        remainingHealthPoints: Int,
        newEffectIds: List<String>,
        position: Int,
        count: Int,
    ) {
        unitPortraitView.display(battleUnit.unitId)
        unitNameView.display(unitName = texts.unitName(unit.id))
        healthBarView.display(
            remainingBefore = battleUnit.remainingHealthPoints,
            remainingAfter = remainingHealthPoints,
            maximum = battleUnit.maximumHealthPoints,
        )
        manaBarView.display(
            remaining = battleUnit.remainingManaPoints,
            maximum = battleUnit.maximumManaPoints,
        )
        effectsView.display(
            effectIds = battleUnit.ongoingEffects.onTurnStarted + battleUnit.ongoingEffects.onDefeatedEffects,
            newEffectIds = newEffectIds,
        )
        counterLabel.setText("$position/$count")
        counterLabel.visible = count > 1
        visible = true
    }

    fun hide() {
        visible = false
    }
}
