package screen

import battleunit.domain.BattleUnit
import unit.domain.Unit
import korlibs.image.color.Colors
import korlibs.image.color.RGBA
import korlibs.image.text.TextAlignment
import korlibs.korge.input.onClick
import korlibs.korge.style.styles
import korlibs.korge.style.textAlignment
import korlibs.korge.style.textColor
import korlibs.korge.ui.UIButton
import korlibs.korge.ui.UIProgressBar
import korlibs.korge.ui.UIText
import korlibs.korge.ui.uiBackgroundColor
import korlibs.korge.ui.uiButton
import korlibs.korge.ui.uiHorizontalStack
import korlibs.korge.ui.uiProgressBar
import korlibs.korge.ui.uiSelectedColor
import korlibs.korge.ui.uiSpacing
import korlibs.korge.ui.uiText
import korlibs.korge.ui.uiVerticalStack
import korlibs.korge.view.Container
import korlibs.korge.view.Text
import korlibs.korge.view.align.centerXOn
import korlibs.korge.view.container
import korlibs.korge.view.setText
import korlibs.korge.view.text
import korlibs.math.geom.*

class BattleUnitInfoView: Container() {

    private lateinit var battleUnitAvatar: UIButton
    private lateinit var unitNameLabel: UIText
    private lateinit var remainingTurnActionsLabel: UIText
    private lateinit var healthLabel: Text
    private lateinit var healthBar: UIProgressBar
    private lateinit var manaLabel: Text
    private lateinit var manaBar: UIProgressBar
    private lateinit var abilityButtons: Array<UIButton?>
    private var delegate: Delegate? = null

    private val battleUnitInfoLayout = uiVerticalStack(padding = 5.0) {
            uiHorizontalStack {
                battleUnitAvatar = uiButton("").also { button ->
                    button.size = Size(width = 97.5, height = 97.5)
                    button.bgColorOut = Colors.WHITE
                    button.bgColorOver = Colors.LIGHTSKYBLUE
                    button.background.borderColor = Colors.LIGHTGRAY
                    button.uiText("", button.size){
                        button.styles {
                            textColor = Colors.BLACK
                            textAlignment = TextAlignment.MIDDLE_CENTER
                        }
                    }
                }
                uiSpacing(Size(10, 0))
                uiVerticalStack(padding = 1.0) {
                    unitNameLabel = uiText("", size = Size(width = 281.5, height = 14))
                    remainingTurnActionsLabel = uiText("", size = Size(width = 281.5, height = 14)) {}
                    uiSpacing(Size(0, 15))
                    container {
                        healthBar =
                            uiProgressBar(size = Size(281.5, 16), current = 75f, maximum = 100f).also { progressBar ->
                                progressBar.styles.uiSelectedColor = RGBA(255, 55, 95)
                                progressBar.styles.uiBackgroundColor = Colors.DIMGREY
                            }
                        healthLabel = text(
                            text = "",
                            textSize = 14.0,
                            color = Colors.WHITE
                        )
                        healthLabel.centerXOn(healthBar)
                        healthLabel.y = (healthBar.height - healthLabel.height) / 2 + 1
                    }

                    uiSpacing(Size(0, 4))
                    container {
                        manaBar =
                            uiProgressBar(size = Size(281.5, 16), current = 40f, maximum = 100f).also { progressBar ->
                                progressBar.styles.uiSelectedColor = RGBA(0, 145, 255)
                                progressBar.styles.uiBackgroundColor = Colors.DIMGREY
                            }
                        manaLabel = text(
                            text = "",
                            textSize = 14.0,
                            color = Colors.WHITE
                        )
                        manaLabel.centerXOn(manaBar)
                        manaLabel.y = (manaBar.height - manaLabel.height) / 2 + 1
                    }
                }
            }
            uiHorizontalStack(padding = 2.0) {
                abilityButtons = arrayOfNulls(6)
                repeat(6) { index ->
                    val abilityButton = uiButton().also { button ->
                        button.size = Size(width = 48.75, height = 48.75)
                        button.bgColorOut = Colors.WHITE
                        button.bgColorOver = Colors.LIGHTSKYBLUE
                        button.background.borderColor = Colors.LIGHTGRAY
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
        battleUnitAvatar.setText("*")
        // Name
        unitNameLabel.setText(unit.name)
        // Remaining turn actions
        remainingTurnActionsLabel.setText("Movements left: ${battleUnit.remainingTurnActions.remainingSteps}    Remaining casts: ${battleUnit.remainingTurnActions.remainingCasts}")
        // Health
        healthLabel.setText("${battleUnit.remainingHealthPoints} / ${unit.healthPoints}")
        healthBar.current = (battleUnit.remainingHealthPoints.toDouble() / unit.healthPoints.toDouble()) * 100
        // Mana
        manaLabel.setText("${battleUnit.remainingManaPoints} / ${unit.manaPoints}")
        manaBar.current = (battleUnit.remainingManaPoints.toDouble() / unit.manaPoints.toDouble()) * 100
        // Abilities
        val canCast = battleUnit.remainingTurnActions.remainingCasts > 0
        battleUnit.abilityCooldowns.keys.forEachIndexed { index, abilityId ->
            abilityButtons[index]?.also { abilityButton ->
                abilityButton.visible = true
                val isInCooldown = battleUnit.abilityCooldowns[abilityId]!! > 0
                if(canCast && !isInCooldown) {
                    abilityButton.bgColorOut = Colors.WHITE
                    abilityButton.bgColorOver = Colors.LIGHTSKYBLUE
                }else{
                    abilityButton.bgColorOut = Colors.DIMGRAY
                    abilityButton.bgColorOver = Colors.DIMGRAY
                }
                abilityButton.onClick { delegate?.abilitySelected(abilityId = abilityId) }
            }
        }
    }

    fun hide() {
        visible = false
    }

    interface Delegate {
        fun abilitySelected(abilityId: String)
    }
}
