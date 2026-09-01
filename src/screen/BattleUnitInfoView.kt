package screen

import battleunit.domain.*
import korlibs.image.bitmap.*
import korlibs.image.format.*
import korlibs.io.file.std.*
import korlibs.korge.ui.*
import korlibs.korge.view.*
import korlibs.korge.view.align.*
import korlibs.korge.view.filter.*
import korlibs.math.geom.*
import unit.domain.*

class BattleUnitInfoView: Container() {

    private lateinit var unitNameView: UnitNameView
    private lateinit var remainingTurnActionsLabel: UIText
    private lateinit var abilityButtons: Array<AbilityButtonView>
    private lateinit var healthBarView: HealthBarView
    private lateinit var manaBarView: ManaBarView
    private lateinit var unitPortraitView: UnitPortraitView

    suspend fun loadAssets() {
        unitPortraitView.loadAssets()
        abilityButtons.forEach { abilityButton -> abilityButton.loadAssets() }
    }

    private val battleUnitInfoLayout = uiVerticalStack(padding = 5.0) {
            uiHorizontalStack {
                unitPortraitView = UnitPortraitView(Size(width = 97.5, height = 97.5))
                addChild(unitPortraitView)

                uiSpacing(Size(10, 0))
                uiVerticalStack(padding = 1.0) {
                    unitNameView = UnitNameView(size = Size(width = 281.5, height = 14))
                    addChild(unitNameView)

                    remainingTurnActionsLabel = uiText("", size = Size(width = 281.5, height = 14)) {}
                    uiSpacing(Size(0, 15))
                    healthBarView =  HealthBarView(Size(281.5, 16))
                    addChild(healthBarView)

                    uiSpacing(Size(0, 4))
                    manaBarView = ManaBarView(Size(281.5, 16))
                    addChild(manaBarView)
                }
            }
            uiHorizontalStack(padding = 2.0) {
                val abilityButtonSize = Size(width = 48.75, height = 48.75)
                abilityButtons = arrayOf(
                    AbilityButtonView(abilityButtonSize),
                    AbilityButtonView(abilityButtonSize),
                    AbilityButtonView(abilityButtonSize),
                    AbilityButtonView(abilityButtonSize),
                    AbilityButtonView(abilityButtonSize),
                    AbilityButtonView(abilityButtonSize),
                )
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

    fun display(battleUnit: BattleUnit.Dto, unit: Unit.Dto) {
        abilityButtons.forEach(AbilityButtonView::hide)
        // Avatar
        unitPortraitView.display(battleUnit.unitId)
        // Name
        unitNameView.display(unitName = unit.name)
        // Remaining turn actions
        remainingTurnActionsLabel.setText("Movements left: ${battleUnit.remainingTurnActions.remainingSteps}    Remaining casts: ${battleUnit.remainingTurnActions.remainingCasts}")
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
