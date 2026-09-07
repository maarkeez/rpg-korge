package screen

import korlibs.image.text.TextAlignment
import korlibs.korge.style.styles
import korlibs.korge.style.textAlignment
import korlibs.korge.style.textSize
import korlibs.korge.ui.uiText
import korlibs.korge.view.Container
import korlibs.math.geom.*

class BattleInfoView(container: Container) {
    private val label = container.uiText(
        text = "",
        size = Size(width = 390, height = 50)
    ){
        this.styles.textAlignment = TextAlignment.MIDDLE_CENTER
        this.styles.textSize = 32.0
    }

    init {
        container.addChild(label)
    }

    fun displayBattleInfo(playerName: String, round: Int){
        label.text = "$playerName turn - Round: $round"
    }

    fun displayPlayerWin(playerName: String) {
        label.text = "$playerName wins!"
    }
}
