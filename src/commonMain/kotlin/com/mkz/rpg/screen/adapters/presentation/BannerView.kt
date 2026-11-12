package com.mkz.rpg.screen.adapters.presentation

import korlibs.image.color.RGBA
import korlibs.image.text.TextAlignment
import korlibs.korge.animate.animator
import korlibs.korge.animate.scaleTo
import korlibs.korge.style.styles
import korlibs.korge.style.textAlignment
import korlibs.korge.style.textColor
import korlibs.korge.style.textSize
import korlibs.korge.ui.UIContainer
import korlibs.korge.ui.UIText
import korlibs.korge.ui.uiText
import korlibs.korge.ui.uiVerticalStack
import korlibs.math.geom.Size
import korlibs.time.milliseconds

open class BannerView(
    private val messages: Messages,
) : UIContainer(Size(width = 390, height = 33)) {
    companion object {
        val HUMAN_TURN_COLOR = RGBA(0x00, 0x84, 0xFF)
        val CPU_TURN_COLOR = RGBA(0xE0, 0x3C, 0x28)

        const val POP_SCALE = 1.3
        val POP_DURATION = 400.milliseconds
    }

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
            this.styles.textColor = HUMAN_TURN_COLOR
        }
    internal var gameOver: UIText? = null
    internal val layout =
        uiVerticalStack {
            addChild(round)
            addChild(playerTurn)
        }

    private var displayedRound: Int? = null

    open fun displayExploring() {
        this.round.text = messages[UiMessage.BANNER_EXPLORING]
        this.playerTurn.text = ""
        displayedRound = null
    }

    open suspend fun announceBattle() {
        this.round.text = messages[UiMessage.BANNER_BATTLE]
        this.playerTurn.text = ""
        displayedRound = null
        pop(listOf(this.round))
    }

    open suspend fun announceBattleFinished() {
        this.round.text = messages[UiMessage.BANNER_BATTLE_FINISHED]
        this.playerTurn.text = ""
        displayedRound = null
        pop(listOf(this.round))
    }

    open fun displayBanner(
        playerId: String,
        round: Int,
        turnColor: RGBA,
    ) {
        this.round.text = messages[UiMessage.BANNER_ROUND, "round" to round]
        this.playerTurn.text = messages[UiMessage.BANNER_PLAYER_TURN, "player" to messages.playerName(playerId)]
        this.playerTurn.styles.textColor = turnColor
        displayedRound = round
    }

    open suspend fun announcePlayerTurn(
        playerId: String,
        round: Int,
        turnColor: RGBA,
    ) {
        val isNewRound = round != displayedRound
        displayBanner(playerId, round, turnColor)
        pop(if (isNewRound) listOf(this.round, playerTurn) else listOf(playerTurn))
    }

    private suspend fun pop(poppingLines: List<UIText>) {
        val popAnimator =
            animator(parallel = true) {
                poppingLines.forEach { line ->
                    sequence {
                        scaleTo(line, POP_SCALE, time = POP_DURATION / 2)
                        scaleTo(line, 1.0, time = POP_DURATION / 2)
                    }
                }
            }
        popAnimator.awaitComplete()
    }

    open fun displayGameOver() {
        this.playerTurn.removeFromParent()
        this.round.removeFromParent()

        gameOver =
            layout.uiText(
                text = messages[UiMessage.BANNER_GAME_OVER],
                size = Size(width = width, height = 33),
            ) {
                this.styles.textAlignment = TextAlignment.MIDDLE_CENTER
                this.styles.textSize = 30.0
            }
    }
}
