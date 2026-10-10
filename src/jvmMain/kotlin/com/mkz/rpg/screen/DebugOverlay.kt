package com.mkz.rpg.screen

import korlibs.image.color.RGBA
import korlibs.korge.ui.UIContainer
import korlibs.korge.view.Text
import korlibs.korge.view.roundRect
import korlibs.korge.view.text
import korlibs.math.geom.RectCorners
import korlibs.math.geom.Size

internal class DebugOverlay(
    size: Size = Size(width = 220, height = 150),
) : UIContainer(size) {
    private val background =
        roundRect(size, radius = RectCorners(3)) {
            color = RGBA(15, 15, 15, 220)
        }

    val label: Text =
        text(
            text = "",
            textSize = 11.0,
            color = RGBA(170, 255, 170),
            autoScaling = false,
        )

    init {
        label.size = size
        visible = false
    }

    fun toggle() {
        visible = !visible
    }

    fun update(
        fps: Double,
        frameMs: Double,
        seed: Long?,
        round: Int?,
        currentPlayer: String?,
        selectedUnitId: String?,
        queueDepth: Int,
        lastEvents: List<String>,
    ) {
        label.text =
            buildString {
                appendLine("FPS ${fps.toInt()} | ${frameMs.toInt()}ms")
                appendLine("seed $seed")
                appendLine("round ${round ?: "-"} | ${currentPlayer ?: "-"}")
                appendLine("unit ${selectedUnitId ?: "-"}")
                appendLine("queue $queueDepth")
                lastEvents.forEach { appendLine("  $it") }
            }
    }
}
