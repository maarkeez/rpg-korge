package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.battleUnit.domain.BattleUnit
import com.mkz.rpg.unit.domain.Unit
import korlibs.image.color.Colors
import korlibs.image.color.RGBA
import korlibs.korge.ui.UIContainer
import korlibs.korge.ui.uiHorizontalStack
import korlibs.korge.ui.uiSpacing
import korlibs.korge.ui.uiVerticalStack
import korlibs.korge.view.Container
import korlibs.math.geom.Size

class BattleUnitInfoView(
    resourcesLoader: BattleSceneResourcesLoader,
) : Container() {
    companion object {
        // FamiCube palette colours of the remaining turn actions.
        val MOVEMENT_POINTS_COLOR = Colors.WHITE
        val CAST_POINTS_COLOR = RGBA(0xFF, 0xBB, 0x31)

        private const val TURN_ACTIONS_SPACING = 16.0

        // Space between the info rows, e.g.: between the health and the mana bars.
        private const val ROWS_SPACING = 4
    }

    internal lateinit var unitNameView: UnitNameView
    internal val movementPointsView = PointsView(MOVEMENT_POINTS_COLOR)
    internal val castPointsView = PointsView(CAST_POINTS_COLOR)
    internal lateinit var abilityButtons: Array<AbilityButtonView>
    internal lateinit var effectsView: EffectsView
    private lateinit var healthBarView: HealthBarView
    private lateinit var manaBarView: ManaBarView
    internal lateinit var unitPortraitView: UnitPortraitView

    private val battleUnitInfoLayout =
        uiVerticalStack(padding = 5.0) {
            uiHorizontalStack {
                unitPortraitView = UnitPortraitView(Size(width = 97.5, height = 97.5), resourcesLoader)
                addChild(unitPortraitView)

                uiSpacing(Size(10, 0))
                uiVerticalStack(padding = 1.0) {
                    unitNameView = UnitNameView(size = Size(width = 281.5, height = 14))
                    addChild(unitNameView)

                    uiSpacing(Size(0, ROWS_SPACING))
                    val remainingTurnActionsView = UIContainer(Size(width = 281.5, height = 14))
                    remainingTurnActionsView.addChild(movementPointsView)
                    remainingTurnActionsView.addChild(castPointsView)
                    addChild(remainingTurnActionsView)
                    // Minus the space added above the remaining turn actions (the spacing and the stack padding),
                    // so the health bar stays where it was.
                    uiSpacing(Size(0, 15 - ROWS_SPACING - 1))
                    healthBarView = HealthBarView(Size(281.5, 16))
                    addChild(healthBarView)

                    uiSpacing(Size(0, ROWS_SPACING))
                    manaBarView = ManaBarView(Size(281.5, 16))
                    addChild(manaBarView)

                    uiSpacing(Size(0, ROWS_SPACING))
                    effectsView = EffectsView(resourcesLoader)
                    addChild(effectsView)
                }
            }
            uiHorizontalStack(padding = 2.0) {
                val abilityButtonSize = Size(width = 48.75, height = 48.75)
                abilityButtons = Array(6) { AbilityButtonView(abilityButtonSize, resourcesLoader) }
                abilityButtons.forEach(::addChild)
            }
        }

    init {
        visible = false
        addChild(battleUnitInfoLayout)
    }

    fun setDelegate(delegate: AbilityButtonView.Delegate) {
        abilityButtons.forEach { abilityButton -> abilityButton.setDelegate(delegate) }
    }

    fun display(
        battleUnit: BattleUnit.Dto,
        unit: Unit.Dto,
    ) {
        abilityButtons.forEach(AbilityButtonView::hide)
        // Avatar
        unitPortraitView.display(battleUnit.unitId)
        // Name
        unitNameView.display(unitName = unit.name)
        // Remaining turn actions, out of their maximum
        val remainingTurnActions = battleUnit.remainingTurnActions
        movementPointsView.display(remaining = remainingTurnActions.remainingSteps, maximum = remainingTurnActions.maximumSteps)
        castPointsView.display(remaining = remainingTurnActions.remainingCasts, maximum = remainingTurnActions.maximumCasts)
        castPointsView.x = movementPointsView.width + TURN_ACTIONS_SPACING
        // Health
        healthBarView.display(
            remaining = battleUnit.remainingHealthPoints,
            maximum = unit.healthPoints,
        )
        // Mana
        manaBarView.display(
            remaining = battleUnit.remainingManaPoints,
            maximum = unit.manaPoints,
        )
        // Abilities
        val canCast = battleUnit.remainingTurnActions.remainingCasts > 0
        battleUnit.abilityCooldowns.keys.forEachIndexed { index, abilityId ->
            val abilityButton = abilityButtons[index]
            val isInCooldown = battleUnit.abilityCooldowns[abilityId]!! > 0
            val canUseAbility = canCast && !isInCooldown
            abilityButton.display(abilityId = abilityId, canCast = canUseAbility)
        }
        // Effects
        effectsView.display(effectIds = battleUnit.ongoingEffects.onTurnStarted + battleUnit.ongoingEffects.onDefeatedEffects)
        visible = true
    }

    fun displayAbilitySelected(index: Int) {
        abilityButtons.forEach(AbilityButtonView::unselect)
        abilityButtons[index].select()
    }

    fun hide() {
        visible = false
    }
}
