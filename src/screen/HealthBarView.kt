package screen

import korlibs.image.color.*
import korlibs.korge.style.*
import korlibs.korge.ui.*
import korlibs.korge.view.*
import korlibs.korge.view.align.centerXOn
import korlibs.math.geom.*

class HealthBarView(size: Size) : UIContainer(size) {

    private val healthBar: UIProgressBar = uiProgressBar(size = size, current = 100f, maximum = 100f).also { progressBar ->
        progressBar.styles.uiSelectedColor = RGBA(255, 55, 95)
        progressBar.styles.uiBackgroundColor = Colors.DIMGREY
    }
    private val healthLabel = text(
        text = "",
        textSize = 14.0,
        color = Colors.WHITE
    )

    init {
        addChild(healthBar)
        addChild(healthLabel)
    }

    fun display(remainingHealthPoints: Int, maxHealthPoints: Int) {
        val healthPercentage = (remainingHealthPoints.toDouble() / maxHealthPoints.toDouble()) * 100
        healthLabel.setText("$remainingHealthPoints / $maxHealthPoints")
        healthBar.current = healthPercentage
        healthLabel.centerXOn(healthBar)
        healthLabel.y = (healthBar.height - healthLabel.height) / 2 + 1
        visible = true
    }

    fun hide() {
        visible = false
    }
}
