package screen

import battlefield.domain.Battlefield
import korlibs.image.color.Colors
import korlibs.image.text.TextAlignment
import korlibs.korge.input.onClick
import korlibs.korge.style.styles
import korlibs.korge.style.textAlignment
import korlibs.korge.style.textColor
import korlibs.korge.ui.UIButton
import korlibs.korge.ui.uiBackgroundColor
import korlibs.korge.ui.uiButton
import korlibs.korge.ui.uiGridFill
import korlibs.korge.ui.uiText
import korlibs.korge.view.Container
import korlibs.korge.view.position
import korlibs.korge.view.setText
import korlibs.math.geom.*

class BattlefieldView: Container() {

    private var delegate: Delegate? = null

    private val battlefieldGrid = uiGridFill(
        size= Size(width=390, height=390),
        spacing = Spacing(2.0,2.0),
        cols = 0,
        rows = 0)


    init {
        addChild(battlefieldGrid)
    }

    fun setDelegate(delegate: Delegate) {
        this.delegate = delegate
    }

    fun displayBattlefield(dto: Battlefield.Dto) {
        battlefieldGrid.rows = dto.rows
        battlefieldGrid.cols = dto.columns
        for (row in 0 until dto.rows) {
            for (column in 0 until dto.columns) {
                battlefieldGrid.uiButton(label = "").also { button ->
                    button.bgColorOut = Colors.WHITE
                    button.bgColorOver = Colors.LIGHTSKYBLUE
                    button.background.borderColor = Colors.LIGHTGRAY
                    button.name = tileName(row, column)
                    button.uiText("", button.size){
                        button.styles {
                            textColor = Colors.BLACK
                            textAlignment = TextAlignment.MIDDLE_CENTER
                        }
                    }
                    button.onClick {
                        delegate?.tileSelected(row, column)
                    }
                }
            }
        }
    }

    private fun tileName(row: Int, column: Int): String = "row-$row-column-$column"

    fun displayHumanBattlefieldUnit(row: Int, column: Int) {
        val tileButton = battlefieldGrid.findViewByName(tileName(row, column))
        tileButton.setText("H")
    }

    fun displayCPUBattlefieldUnit(row: Int, column: Int) {
        val tileButton = battlefieldGrid.findViewByName(tileName(row, column))
        tileButton.setText("CPU")
    }

    interface Delegate {
        fun tileSelected(row: Int, column: Int)
    }

    fun displayPotentialMovement(row: Int, column: Int) {
        val tileButton = battlefieldGrid.findViewByName(tileName(row, column)) as UIButton
        tileButton.background.bgColor = Colors.GREEN
        tileButton.bgColorOut = Colors.GREEN
        tileButton.bgColorOver = Colors.DARKGREEN
    }

    fun resetTiles() {
        battlefieldGrid.children.forEach {
            val tileButton = it as UIButton
            tileButton.bgColorOut = Colors.WHITE
            tileButton.bgColorOver = Colors.LIGHTSKYBLUE
            tileButton.background.borderColor = Colors.LIGHTGRAY
        }
    }

}
