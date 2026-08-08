package screen

import battlefield.domain.Battlefield
import korlibs.image.color.Colors
import korlibs.korge.ui.uiButton
import korlibs.korge.ui.uiGridFill
import korlibs.korge.view.Container
import korlibs.math.geom.*

class BattlefieldView(container: Container) {
    private val battlefieldGrid = container.uiGridFill(
        size= Size(width=390, height=390),
        spacing = Spacing(2.0,2.0),
        cols = 0,
        rows = 0)
    init {
        container.addChild(battlefieldGrid)
    }

    fun displayBattlefield(dto: Battlefield.Dto) {
        battlefieldGrid.rows = dto.rows
        battlefieldGrid.cols = dto.columns
        for (n in 0 until dto.rows *  dto.columns) {
            battlefieldGrid.uiButton().also { button ->
                button.bgColorOut = Colors.WHITE
                button.bgColorOver = Colors.LIGHTSKYBLUE
                button.background.borderColor = Colors.LIGHTGRAY
            }
        }
    }

}
