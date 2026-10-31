package com.mkz.rpg.screen.adapters.presentation

import korlibs.image.color.Colors
import korlibs.image.color.RGBA
import korlibs.korge.ui.UIContainer
import korlibs.korge.ui.uiHorizontalStack
import korlibs.korge.ui.uiSpacing
import korlibs.korge.ui.uiVerticalStack
import korlibs.korge.view.Container
import korlibs.math.geom.Size

open class BattleUnitInfoView(
    resourcesLoader: BattleSceneResourcesLoader,
    private val texts: Texts,
) : Container() {
    companion object {
        // FamiCube palette colours of the remaining turn actions.
        val MOVEMENT_POINTS_COLOR = Colors.WHITE
        val CAST_POINTS_COLOR = RGBA(0xFF, 0xBB, 0x31)

        private const val TURN_ACTIONS_SPACING = 16.0

        // Space between the info rows, e.g.: between the health and the mana bars.
        private const val ROWS_SPACING = 4
    }

    /** The steps and casts the battle unit has left this turn, out of the ones it has each turn. */
    data class TurnActions(
        val remainingSteps: Int,
        val maximumSteps: Int,
        val remainingCasts: Int,
        val maximumCasts: Int,
    )

    /** An ability of the battle unit, and why it can or can not be cast. */
    data class Ability(
        val abilityId: String,
        // The turns left until it can be cast again, e.g.: 1 when it can be cast on the next turn.
        val cooldownTurnsLeft: Int,
        val hasEnoughMana: Boolean,
        // Told on the button while there is not enough mana.
        val manaCost: Int,
    )

    internal lateinit var unitNameView: UnitNameView
    internal val movementPointsView = PointsView(MOVEMENT_POINTS_COLOR)
    internal val castPointsView = PointsView(CAST_POINTS_COLOR)
    internal lateinit var remainingTurnsView: RemainingTurnsView
    internal lateinit var abilityButtons: Array<AbilityButtonView>
    internal lateinit var bagButton: BagButtonView
    internal lateinit var effectsView: EffectsView
    private lateinit var healthBarView: HealthBarView
    private lateinit var manaBarView: ManaBarView
    internal lateinit var unitPortraitView: UnitPortraitView
    internal lateinit var abilityInfoView: AbilityInfoView

    private val battleUnitInfoLayout =
        uiVerticalStack(padding = 5.0) {
            uiHorizontalStack {
                // The portrait at 3x, 4 pixels in its slot.
                unitPortraitView = UnitPortraitView(Size(width = 104, height = 104), resourcesLoader)
                addChild(unitPortraitView)

                uiSpacing(Size(10, 0))
                uiVerticalStack(padding = 1.0) {
                    uiHorizontalStack(padding = 1.0) {
                        uiVerticalStack(width = 237.5) {
                            unitNameView = UnitNameView(size = Size(width = this.width, height = 14))
                            addChild(unitNameView)

                            uiSpacing(Size(0, ROWS_SPACING))
                            val remainingTurnActionsView = UIContainer(Size(width = this.width, height = 14))
                            remainingTurnActionsView.addChild(movementPointsView)
                            remainingTurnActionsView.addChild(castPointsView)
                            remainingTurnsView = RemainingTurnsView(Size(width = this.width, height = 14), texts)
                            remainingTurnActionsView.addChild(remainingTurnsView)
                            addChild(remainingTurnActionsView)
                        }
                        bagButton = BagButtonView(Size(width = 29, height = 29), resourcesLoader)
                        addChild(bagButton)
                    }

                    // Minus the space added above the remaining turn actions (the spacing and the stack padding),
                    // so the health bar stays where it was.
                    uiSpacing(Size(0, 15 - ROWS_SPACING - 1))
                    healthBarView = HealthBarView(Size(275, 16))
                    addChild(healthBarView)

                    uiSpacing(Size(0, ROWS_SPACING))
                    manaBarView = ManaBarView(Size(275, 16))
                    addChild(manaBarView)

                    uiSpacing(Size(0, ROWS_SPACING))
                    effectsView = EffectsView(resourcesLoader)
                    addChild(effectsView)
                }
            }
            uiHorizontalStack(padding = 2.0) {
                val abilityButtonSize = Size(width = 54, height = 54)
                abilityButtons = Array(6) { AbilityButtonView(abilityButtonSize, resourcesLoader) }
                abilityButtons.forEach(::addChild)
            }
            uiSpacing(Size(0, ROWS_SPACING))
            abilityInfoView = AbilityInfoView(AbilityInfoView.WIDTH, resourcesLoader, texts)
            addChild(abilityInfoView)
        }

    init {
        visible = false
        addChild(battleUnitInfoLayout)
    }

    open fun setDelegate(delegate: AbilityButtonView.Delegate) {
        abilityButtons.forEach { abilityButton -> abilityButton.setDelegate(delegate) }
    }

    /**
     * Displays the battle unit with the turn actions it has left, and the turns left before it vanishes if it is a
     * summon (none for the others). No ability is selected, so no ability info is displayed.
     */
    open fun display(
        battleUnit: BattleUnitInfo,
        turnActions: TurnActions,
        remainingTurns: Int?,
        abilities: List<Ability>,
    ) {
        abilityButtons.forEach(AbilityButtonView::hide)
        abilityInfoView.hide()
        // Avatar, ringed with the team's colour
        unitPortraitView.display(battleUnit.unitId, ring = if (battleUnit.isAlly) HudStyle.ALLY else HudStyle.ENEMY)
        // Name
        unitNameView.display(unitName = texts.unitName(battleUnit.unitId))
        // Remaining turn actions, out of their maximum
        movementPointsView.display(remaining = turnActions.remainingSteps, maximum = turnActions.maximumSteps)
        castPointsView.display(remaining = turnActions.remainingCasts, maximum = turnActions.maximumCasts)
        castPointsView.x = movementPointsView.width + TURN_ACTIONS_SPACING
        // Turns left, only for the summons, after the casts
        if (remainingTurns != null) remainingTurnsView.display(remainingTurns) else remainingTurnsView.hide()
        remainingTurnsView.x = castPointsView.x + castPointsView.width + TURN_ACTIONS_SPACING
        // Health
        healthBarView.display(
            remaining = battleUnit.remainingHealthPoints,
            maximum = battleUnit.maximumHealthPoints,
        )
        // Mana
        manaBarView.display(
            remaining = battleUnit.remainingManaPoints,
            maximum = battleUnit.maximumManaPoints,
        )
        // Abilities, telling apart the cooldowns from the lack of mana. No casts left darkens none, as the cast points
        // already tell it.
        abilities.forEachIndexed { index, ability ->
            abilityButtons[index].display(
                abilityId = ability.abilityId,
                cooldownTurnsLeft = ability.cooldownTurnsLeft,
                hasEnoughMana = ability.hasEnoughMana,
                manaCost = ability.manaCost,
            )
        }
        // Effects
        effectsView.display(effectIds = battleUnit.effectIds)
        visible = true
    }

    /** Displays the bag, which opens the inventory of the battle unit. Only the battle units of the human player have one, the summoned ones excluded. */
    open fun displayBag(onBagSelected: () -> kotlin.Unit) {
        bagButton.display(onBagSelected)
    }

    open fun hideBag() {
        bagButton.hide()
    }

    open fun displayAbilitySelected(index: Int) {
        abilityButtons.forEach(AbilityButtonView::unselect)
        abilityButtons[index].select()
    }

    /** Displays what the selected ability does, under the ability buttons. */
    open fun displayAbilityInfo(abilityInfo: AbilityInfoView.AbilityInfo) {
        abilityInfoView.display(abilityInfo)
    }

    open fun hideAbilityInfo() {
        abilityInfoView.hide()
    }

    open fun hide() {
        visible = false
    }
}
