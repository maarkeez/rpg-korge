package screen

import battleunit.domain.BattleUnit
import korlibs.image.color.Colors
import korlibs.image.color.RGBA
import korlibs.image.text.TextAlignment
import korlibs.korge.style.styles
import korlibs.korge.style.textAlignment
import korlibs.korge.style.textColor
import korlibs.korge.ui.UIButton
import korlibs.korge.ui.uiBackgroundColor
import korlibs.korge.ui.uiButton
import korlibs.korge.ui.uiHorizontalStack
import korlibs.korge.ui.uiProgressBar
import korlibs.korge.ui.uiSelectedColor
import korlibs.korge.ui.uiSpacing
import korlibs.korge.ui.uiText
import korlibs.korge.ui.uiVerticalStack
import korlibs.korge.view.Container
import korlibs.korge.view.align.centerXOn
import korlibs.korge.view.container
import korlibs.korge.view.setText
import korlibs.korge.view.text
import korlibs.math.geom.*

class BattleUnitInfoView: Container() {

    private lateinit var battleUnitAvatar: UIButton

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
                    uiText("Goblin", size = Size(width = 281.5, height = 14))
                    uiText("Movements left: 2    Remaining casts: 1", size = Size(width = 281.5, height = 14)) {}
                    uiSpacing(Size(0, 15))
                    container {
                        val healthBar =
                            uiProgressBar(size = Size(281.5, 16), current = 75f, maximum = 100f).also { progressBar ->
                                progressBar.styles.uiSelectedColor = RGBA(255, 55, 95)
                                progressBar.styles.uiBackgroundColor = Colors.DIMGREY
                            }
                        val healthLabel = text(
                            text = "75 / 100",
                            textSize = 14.0,
                            color = Colors.WHITE
                        )
                        healthLabel.centerXOn(healthBar)
                        healthLabel.y = (healthBar.height - healthLabel.height) / 2 + 1
                    }

                    uiSpacing(Size(0, 4))
                    container {
                        val manaBar =
                            uiProgressBar(size = Size(281.5, 16), current = 40f, maximum = 100f).also { progressBar ->
                                progressBar.styles.uiSelectedColor = RGBA(0, 145, 255)
                                progressBar.styles.uiBackgroundColor = Colors.DIMGREY
                            }
                        val manaLabel = text(
                            text = "40 / 100",
                            textSize = 14.0,
                            color = Colors.WHITE
                        )
                        manaLabel.centerXOn(manaBar)
                        manaLabel.y = (manaBar.height - manaLabel.height) / 2 + 1
                    }
                }
            }
            uiHorizontalStack(padding = 2.0) {
                repeat(6) {
                    uiButton().also { button ->
                        button.size = Size(width = 48.75, height = 48.75)
                        button.bgColorOut = Colors.WHITE
                        button.bgColorOver = Colors.LIGHTSKYBLUE
                        button.background.borderColor = Colors.LIGHTGRAY
                    }
                }
            }
        }


    init {
        addChild(battleUnitInfoLayout)
    }

    fun display(battleUnit: BattleUnit.Dto) {
        battleUnitAvatar.setText("*")
    }

    fun clear() {
        battleUnitAvatar.setText("")
    }
}
