package screen

import korlibs.image.color.*
import korlibs.korge.style.*
import korlibs.korge.ui.*
import korlibs.korge.view.*
import korlibs.korge.view.align.centerXOn
import korlibs.math.geom.*

class ManaBarView(size: Size) : UIContainer(size) {

    private val progressBar: UIProgressBar = uiProgressBar(size = size, current = 100f, maximum = 100f).also { progressBar ->
        progressBar.styles.uiSelectedColor = RGBA(0, 145, 255)
        progressBar.styles.uiBackgroundColor = Colors.DIMGREY
    }
    private val label = text(
        text = "",
        textSize = 14.0,
        color = Colors.WHITE
    )

    init {
        addChild(progressBar)
        addChild(label)
    }

    fun display(remainingManaPoints: Int, maxManaPoints: Int) {
        val healthPercentage = (remainingManaPoints.toDouble() / maxManaPoints.toDouble()) * 100
        label.setText("$remainingManaPoints / $maxManaPoints")
        progressBar.current = healthPercentage
        label.centerXOn(progressBar)
        label.y = (progressBar.height - label.height) / 2 + 1
        visible = true
    }

    fun hide() {
        visible = false
    }
}
