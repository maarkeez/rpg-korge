package screen

import korlibs.korge.ui.*
import korlibs.korge.view.*
import korlibs.math.geom.*

class UnitNameView(size: Size): UIContainer(size) {

    private val unitNameLabel = uiText("", size = size)

    init {
        addChild(unitNameLabel)
    }

    fun display(unitName: String) {
        unitNameLabel.setText(unitName)
        visible = true
    }

    fun hide() {
        visible = false
    }
}
