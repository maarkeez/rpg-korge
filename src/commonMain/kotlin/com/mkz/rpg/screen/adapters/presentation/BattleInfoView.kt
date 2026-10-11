package com.mkz.rpg.screen.adapters.presentation

import korlibs.image.color.RGBA
import korlibs.image.text.TextAlignment
import korlibs.korge.style.styles
import korlibs.korge.style.textAlignment
import korlibs.korge.style.textColor
import korlibs.korge.style.textSize
import korlibs.korge.ui.UIContainer
import korlibs.korge.ui.UIText
import korlibs.korge.ui.uiText
import korlibs.korge.ui.uiVerticalStack
import korlibs.math.geom.Size

class BattleInfoView : UIContainer(Size(width = 390, height = 33)) {
    internal val round =
        uiText(
            text = "",
            size = Size(width = width, height = 18),
        ) {
            this.styles.textAlignment = TextAlignment.MIDDLE_LEFT
            this.styles.textSize = 18.0
        }
    internal val playerTurn =
        uiText(
            text = "",
            size = Size(width = width, height = 15),
        ) {
            this.styles.textAlignment = TextAlignment.MIDDLE_LEFT
            this.styles.textSize = 15.0
            this.styles.textColor = RGBA(0, 136, 255)
        }
    internal var winner: UIText? = null
    internal val layout =
        uiVerticalStack {
            addChild(round)
            addChild(playerTurn)
        }

    fun displayBattleInfo(
        playerName: String,
        round: Int,
    ) {
        this.round.text = "Round $round"
        this.playerTurn.text = "$playerName turn"
    }

    fun displayPlayerWin(playerName: String) {
        this.playerTurn.removeFromParent()
        this.round.removeFromParent()

        winner =
            layout.uiText(
                text = "$playerName wins!",
                size = Size(width = width, height = 33),
            ) {
                this.styles.textAlignment = TextAlignment.MIDDLE_CENTER
                this.styles.textSize = 30.0
            }
    }
}
