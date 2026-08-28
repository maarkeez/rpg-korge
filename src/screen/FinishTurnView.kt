package screen

import korlibs.image.color.Colors
import korlibs.image.color.RGBA
import korlibs.korge.input.onClick
import korlibs.korge.ui.uiButton
import korlibs.korge.view.Container
import korlibs.math.geom.*

class FinishTurnView: Container() {

    private var delegate: Delegate? = null
    private val button = uiButton("Finish turn")
        .also { button ->
            button.size = Size(width= 390, height=48.75)
            button.bgColorOut = RGBA(0, 136, 255)
            button.bgColorOver = RGBA(30, 110, 244)
            button.background.borderColor = Colors.LIGHTGRAY

            button.onClick {
                println("Finish turn clicked!")
                delegate?.finishTurn()
            }
        }

    init {
        addChild(button)
    }

    fun setDelegate(delegate: Delegate) {
        this.delegate = delegate
    }

    fun hide() {
        visible = false
    }

    interface Delegate {
        fun finishTurn()
    }
}
