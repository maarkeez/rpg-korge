package battle.adapters.presentation

import korlibs.image.color.Colors
import korlibs.image.color.RGBA
import korlibs.image.color.RGBA.Companion.invoke
import korlibs.image.text.TextAlignment
import korlibs.korge.input.onClick
import korlibs.korge.style.styles
import korlibs.korge.style.textAlignment
import korlibs.korge.style.textSize
import korlibs.korge.ui.uiButton
import korlibs.korge.ui.uiText
import korlibs.korge.view.Container
import korlibs.math.geom.*

class FinishTurnView(container: Container) {

    private var delegate: Delegate? = null
    private val button = container.uiButton("Finish turn")
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
        container.addChild(button)
    }

    fun setDelegate(delegate: Delegate) {
        this.delegate = delegate
    }

    interface Delegate {
        fun finishTurn()
    }
}
