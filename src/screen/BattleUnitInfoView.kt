package screen

import battleunit.domain.*
import korlibs.image.bitmap.*
import korlibs.image.color.*
import korlibs.image.format.*
import korlibs.io.file.std.*
import korlibs.korge.input.*
import korlibs.korge.ui.*
import korlibs.korge.view.*
import korlibs.korge.view.align.*
import korlibs.korge.view.filter.*
import korlibs.math.geom.*
import unit.domain.*

class BattleUnitInfoView: Container() {

    private lateinit var unitNameLabel: UIText
    private lateinit var remainingTurnActionsLabel: UIText
    private lateinit var abilityButtons: Array<UIButton?>
    private lateinit var heal: Bitmap
    private lateinit var sword: Bitmap
    private lateinit var abilitySelection: Bitmap
    private var delegate: Delegate? = null
    private lateinit var healthBarView: HealthBarView
    private lateinit var manaBarView: ManaBarView
    private lateinit var unitPortraitView: UnitPortraitView

    suspend fun loadAssets() {
        unitPortraitView.loadAssets()
        heal = resourcesVfs["ability/heal.png"].readBitmap()
        sword = resourcesVfs["ability/sword.png"].readBitmap()
        abilitySelection = resourcesVfs["ability/ability_selection.png"].readBitmap()
    }

    private val battleUnitInfoLayout = uiVerticalStack(padding = 5.0) {
            uiHorizontalStack {
                unitPortraitView = UnitPortraitView(Size(width = 97.5, height = 97.5))
                addChild(unitPortraitView)

                uiSpacing(Size(10, 0))
                uiVerticalStack(padding = 1.0) {
                    unitNameLabel = uiText("", size = Size(width = 281.5, height = 14))
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
                abilityButtons = arrayOfNulls(6)
                repeat(6) { index ->
                    val abilityButton = uiButton().also { button ->
                        button.size = Size(width = 48.75, height = 48.75)
                        button.bgColorOut = Colors.TRANSPARENT
                        button.bgColorOver = Colors.TRANSPARENT
                        button.background.borderColor = Colors.TRANSPARENT
                        button.background.bgColor = Colors.TRANSPARENT
                    }
                    abilityButtons[index] = abilityButton
                }
            }
        }


    init {
        visible = false
        addChild(battleUnitInfoLayout)
    }

    fun setDelegate(delegate: Delegate) {
        this.delegate = delegate
    }

    fun display(battleUnit: BattleUnit.Dto, unit: Unit.Dto) {
        abilityButtons.forEach { it?.visible = false }
        visible = true
        // Avatar
        unitPortraitView.display(battleUnit.unitId)
        // Name
        unitNameLabel.setText(unit.name)
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
            abilityButtons[index]?.also { abilityButton ->
                abilityButton.findViewByName("ability")?.removeFromParent()
                abilityButton.findViewByName("abilitySelection")?.removeFromParent()
                abilityButton.visible = true
                val isInCooldown = battleUnit.abilityCooldowns[abilityId]!! > 0
                val canUseAbility = canCast && !isInCooldown
                when(abilityId) {
                    "heal" -> heal
                    "sword" -> sword
                    else -> null
                }?.let { avatarBitmap ->
                    abilityButton.image(avatarBitmap) {
                        name = "ability"
                        smoothing = false
                        scale = 3.0
                        centerOn(abilityButton)
                        if(!canUseAbility){
                            filter = darkFilter()
                        }
                    }
                }
                abilityButton.onClick {
                    abilityButton.image(abilitySelection){
                        name = "abilitySelection"
                        smoothing = false
                        scale = 3.0
                        centerOn(abilityButton)
                    }
                    delegate?.abilitySelected(abilityId = abilityId)
                }
            }
        }
    }

    private fun darkFilter(): ColorMatrixFilter = ColorMatrixFilter(
        Matrix4.fromRows(
            0.5f, 0f, 0f, 0f,
            0f, 0.5f, 0f, 0f,
            0f, 0f, 0.5f, 0f,
            0f, 0f, 0f, 1f
        )
    )

    fun hide() {
        visible = false
    }

    interface Delegate {
        fun abilitySelected(abilityId: String)
    }
}
