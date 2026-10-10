package com.mkz.rpg.screen

import com.mkz.rpg.battleUnit.domain.BattleUnit
import com.mkz.rpg.shared.adapters.presentation.PIXEL_SCALE
import com.mkz.rpg.unit.domain.Unit
import korlibs.korge.ui.uiHorizontalStack
import korlibs.korge.ui.uiSpacing
import korlibs.korge.ui.uiVerticalStack
import korlibs.korge.view.Container
import korlibs.korge.view.image
import korlibs.math.geom.Size

class BattleUnitInfoView(
    private val sprites: SpriteRegistry = SpriteRegistry(),
) : Container() {
    private lateinit var unitNameView: UnitNameView
    private lateinit var turnPipsView: TurnPipsView
    private lateinit var abilityButtons: Array<AbilityButtonView>
    private val statusListView = StatusListView(Size(width = 390.0, height = StatusListView.MAX_ROWS * StatusListView.ROW_HEIGHT.toDouble()), sprites)
    private val readOnlyAbilities = Container()
    private lateinit var healthBarView: HealthBarView
    private lateinit var manaBarView: ManaBarView
    private lateinit var unitPortraitView: UnitPortraitView

    suspend fun loadAssets() {
        unitPortraitView.loadAssets()
        abilityButtons.forEach { abilityButton -> abilityButton.loadAssets() }
        sprites.load()
    }

    private val battleUnitInfoLayout =
        uiVerticalStack(padding = 5.0) {
            uiHorizontalStack {
                unitPortraitView = UnitPortraitView(Size(width = 97.5, height = 97.5), sprites)
                addChild(unitPortraitView)

                uiSpacing(Size(10, 0))
                uiVerticalStack(padding = 1.0) {
                    unitNameView = UnitNameView(size = Size(width = 281.5, height = 14))
                    addChild(unitNameView)

                    turnPipsView = TurnPipsView(Size(width = 281.5, height = 14))
                    addChild(turnPipsView)
                    uiSpacing(Size(0, 15))
                    healthBarView = HealthBarView(Size(281.5, 16))
                    addChild(healthBarView)

                    uiSpacing(Size(0, 4))
                    manaBarView = ManaBarView(Size(281.5, 16))
                    addChild(manaBarView)
                }
            }
            uiHorizontalStack(padding = 2.0) {
                val abilityButtonSize = Size(width = 48.75, height = 48.75)
                abilityButtons =
                    arrayOf(
                        AbilityButtonView(abilityButtonSize, sprites),
                        AbilityButtonView(abilityButtonSize, sprites),
                        AbilityButtonView(abilityButtonSize, sprites),
                        AbilityButtonView(abilityButtonSize, sprites),
                        AbilityButtonView(abilityButtonSize, sprites),
                        AbilityButtonView(abilityButtonSize, sprites),
                    )
                abilityButtons.forEach(::addChild)
            }
        }

    init {
        visible = false
        addChild(battleUnitInfoLayout)
        readOnlyAbilities.y = ABILITY_ROW_Y
        addChild(readOnlyAbilities)
        statusListView.y = STATUS_LIST_Y
        addChild(statusListView)
    }

    private companion object {
        const val MAX_CASTS = 1
        const val ART_SIZE = 16
        const val ABILITY_ROW_Y = 102.5
        const val ABILITY_BUTTON_SIZE = 48.75
        const val STATUS_LIST_Y = ABILITY_ROW_Y + ABILITY_BUTTON_SIZE + 4.0
    }

    /** Statuses shown in the list, for tests. */
    val displayedStatusRows: Int get() = statusListView.rowCount

    /** Ability buttons that can start a cast. Always zero for units the player can't command. */
    val visibleAbilityButtonCount: Int get() = abilityButtons.count { it.visible }

    val readOnlyAbilityIconCount: Int get() = readOnlyAbilities.numChildren

    fun setDelegate(delegate: AbilityButtonView.Delegate) {
        abilityButtons.forEach { abilityButton -> abilityButton.setDelegate(delegate) }
    }

    fun display(
        battleUnit: BattleUnit.Dto,
        unit: Unit.Dto,
        interactive: Boolean = true,
    ) {
        abilityButtons.forEach(AbilityButtonView::hide)
        readOnlyAbilities.removeChildren()
        // Avatar
        unitPortraitView.display(battleUnit.unitId)
        // Name
        unitNameView.display(unitName = unit.name)
        // Remaining turn actions
        turnPipsView.display(
            remainingSteps = battleUnit.remainingTurnActions.remainingSteps,
            maximumSteps = unit.movementRange,
            remainingCasts = battleUnit.remainingTurnActions.remainingCasts,
            maximumCasts = MAX_CASTS,
        )
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
            if (interactive) {
                val isInCooldown = battleUnit.abilityCooldowns[abilityId]!! > 0
                val canUseAbility = canCast && !isInCooldown
                abilityButtons[index].display(abilityId = abilityId, canCast = canUseAbility)
            } else {
                // Inspected units can't be commanded: show their abilities as plain icons, never as buttons.
                readOnlyAbilities.image(sprites.ability(abilityId)) {
                    smoothing = false
                    scale = PIXEL_SCALE.toDouble()
                    x = index * ABILITY_BUTTON_SIZE + (ABILITY_BUTTON_SIZE - ART_SIZE * PIXEL_SCALE) / 2
                    y = (ABILITY_BUTTON_SIZE - ART_SIZE * PIXEL_SCALE) / 2
                }
            }
        }
        // Statuses
        val onTurnStarted =
            battleUnit.ongoingEffects.onTurnStarted.distinct().map { effectId ->
                StatusListView.Status(effectId, battleUnit.ongoingEffects.onTurnStartedTurnsLeft[effectId] ?: 0)
            }
        val onDefeated = battleUnit.ongoingEffects.onDefeatedEffects.map { effectId -> StatusListView.Status(effectId, null) }
        statusListView.display(onTurnStarted + onDefeated)
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
