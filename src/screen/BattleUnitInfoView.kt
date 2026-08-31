package screen

import battleunit.domain.BattleUnit
import korlibs.image.bitmap.Bitmap
import unit.domain.Unit
import korlibs.image.color.Colors
import korlibs.image.color.RGBA
import korlibs.image.format.BitmapNativeImage
import korlibs.image.format.readBitmap
import korlibs.image.text.TextAlignment
import korlibs.io.file.std.resourcesVfs
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
import korlibs.korge.view.align.centerOn
import korlibs.korge.view.align.centerXOn
import korlibs.korge.view.container
import korlibs.korge.view.filter.ColorMatrixFilter
import korlibs.korge.view.filter.filter
import korlibs.korge.view.image
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
    private lateinit var knightPortrait: Bitmap
    private lateinit var ratPortrait: Bitmap
    private lateinit var heal: Bitmap
    private lateinit var sword: Bitmap
    private var delegate: Delegate? = null

    suspend fun loadAssets() {
        knightPortrait = resourcesVfs["unit/knight_portrait.png"].readBitmap()
        ratPortrait = resourcesVfs["unit/rat_portrait.png"].readBitmap()
        heal = resourcesVfs["ability/heal.png"].readBitmap()
        sword = resourcesVfs["ability/sword.png"].readBitmap()
    }

    private val battleUnitInfoLayout = uiVerticalStack(padding = 5.0) {
            uiHorizontalStack {
                battleUnitAvatar = uiButton("").also { button ->
                    button.size = Size(width = 97.5, height = 97.5)
                    button.bgColorOut = Colors.WHITE
                    button.bgColorOver = Colors.LIGHTSKYBLUE
                    button.background.borderColor = Colors.LIGHTGRAY
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
        battleUnitAvatar.findViewByName("avatar")?.removeFromParent()
        when(battleUnit.unitId) {
            "knight" -> knightPortrait
            "rat" -> ratPortrait
            else -> null
        }?.let { avatarBitmap ->
            battleUnitAvatar.image(avatarBitmap) {
                name = "avatar"
                smoothing = false
                scale = 3.0
                centerOn(battleUnitAvatar)
            }
        }
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
                abilityButton.findViewByName("ability")?.removeFromParent()
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
                abilityButton.onClick { delegate?.abilitySelected(abilityId = abilityId) }
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
