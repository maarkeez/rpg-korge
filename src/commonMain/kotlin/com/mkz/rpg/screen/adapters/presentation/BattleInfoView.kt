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

open class BattleInfoView(
    private val texts: Texts,
) : UIContainer(Size(width = 390, height = 33)) {
    companion object {
        // FamiCube palette colours of each player turn.
        val HUMAN_TURN_COLOR = RGBA(0x00, 0x84, 0xFF)
        val CPU_TURN_COLOR = RGBA(0xE0, 0x3C, 0x28)

        // When a turn starts, the header lines grow to this scale and shrink back.
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
    internal var winner: UIText? = null
    internal val layout =
        uiVerticalStack {
            addChild(round)
            addChild(playerTurn)
        }

    private var displayedRound: Int? = null

    /** Displays that the player is exploring, out of the battles. */
    open fun displayExploring() {
        this.round.text = texts[UiText.BATTLE_INFO_EXPLORING]
        this.playerTurn.text = ""
        displayedRound = null
    }

    /** Displays and pops that a battle starts, e.g.: when the CPU engages the exploring player. */
    open suspend fun announceBattle() {
        this.round.text = texts[UiText.BATTLE_INFO_BATTLE]
        this.playerTurn.text = ""
        displayedRound = null
        pop(listOf(this.round))
    }

    /** Displays and pops that the battle is over, e.g.: when no CPU battle unit is left in it. */
    open suspend fun announceBattleEnd() {
        this.round.text = texts[UiText.BATTLE_INFO_BATTLE_OVER]
        this.playerTurn.text = ""
        displayedRound = null
        pop(listOf(this.round))
    }

    open fun displayBattleInfo(
        playerName: String,
        round: Int,
        turnColor: RGBA,
    ) {
        this.round.text = texts[UiText.BATTLE_INFO_ROUND, "round" to round]
        this.playerTurn.text = texts[UiText.BATTLE_INFO_PLAYER_TURN, "player" to playerName]
        this.playerTurn.styles.textColor = turnColor
        displayedRound = round
    }

    /** Displays the battle info and pops the player turn, and the round when it has changed. */
    open suspend fun announceTurn(
        playerName: String,
        round: Int,
        turnColor: RGBA,
    ) {
        val isNewRound = round != displayedRound
        displayBattleInfo(playerName, round, turnColor)
        pop(if (isNewRound) listOf(this.round, playerTurn) else listOf(playerTurn))
    }

    // The lines grow to the pop scale and shrink back.
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

    open fun displayPlayerWin(playerName: String) {
        this.playerTurn.removeFromParent()
        this.round.removeFromParent()

        winner =
            layout.uiText(
                text = texts[UiText.BATTLE_INFO_PLAYER_WINS, "player" to playerName],
                size = Size(width = width, height = 33),
            ) {
                this.styles.textAlignment = TextAlignment.MIDDLE_CENTER
                this.styles.textSize = 30.0
            }
    }
}
