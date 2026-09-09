package screen

import korlibs.image.text.TextAlignment
import korlibs.korge.style.styles
import korlibs.korge.style.textAlignment
import korlibs.korge.ui.UIContainer
import korlibs.korge.ui.uiText
import korlibs.korge.view.setText
import korlibs.math.geom.Size

class UnitNameView(
    size: Size,
) : UIContainer(size) {
    private val unitNameLabel = uiText("", size = size)

    init {
        addChild(unitNameLabel)
    }

    fun display(unitName: String) {
        unitNameLabel.setText(unitName)
        visible = true
    }

    fun alignTextToRight() {
        unitNameLabel.styles.textAlignment = TextAlignment.RIGHT
    }

    fun hide() {
        visible = false
    }
}
