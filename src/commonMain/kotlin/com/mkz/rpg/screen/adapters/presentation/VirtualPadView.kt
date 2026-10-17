package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.exploration.domain.Exploration.Dto.DirectionDto
import korlibs.image.color.Colors
import korlibs.image.color.RGBA
import korlibs.korge.input.onDown
import korlibs.korge.input.onUp
import korlibs.korge.input.onUpOutside
import korlibs.korge.ui.UIButton
import korlibs.korge.ui.UIContainer
import korlibs.korge.ui.uiButton
import korlibs.korge.view.addUpdater
import korlibs.korge.view.graphics
import korlibs.korge.view.position
import korlibs.math.geom.Point
import korlibs.math.geom.Size

/**
 * The north, east, south and west arrows the player walks the party with while exploring, as on a SNES pad. Holding an
 * arrow keeps walking towards it: the listener is called when the arrow is pressed, and on every frame while it is held.
 */
open class VirtualPadView(
    size: Size = Size(390, 300),
) : UIContainer(size) {
    companion object {
        const val ARROW_SIZE = 72.0

        // Half the side of the triangle drawn on each arrow.
        private const val ARROW_HALF_SIDE = 16.0
        private val ARROW_COLOR = RGBA(0, 136, 255)
        private val ARROW_HELD_COLOR = RGBA(30, 110, 244)
    }

    private var onDirectionHeld: ((DirectionDto) -> Unit)? = null
    internal var heldDirection: DirectionDto? = null
        private set

    // The arrows form a cross, centred on the pad.
    private val centerX = (size.width - ARROW_SIZE) / 2
    private val centerY = (size.height - ARROW_SIZE) / 2
    internal val northArrow = arrow(DirectionDto.NORTH, x = centerX, y = centerY - ARROW_SIZE)
    internal val eastArrow = arrow(DirectionDto.EAST, x = centerX + ARROW_SIZE, y = centerY)
    internal val southArrow = arrow(DirectionDto.SOUTH, x = centerX, y = centerY + ARROW_SIZE)
    internal val westArrow = arrow(DirectionDto.WEST, x = centerX - ARROW_SIZE, y = centerY)

    init {
        addUpdater { heldDirection?.let { direction -> onDirectionHeld?.invoke(direction) } }
    }

    open fun setOnDirectionHeld(listener: (DirectionDto) -> Unit) {
        onDirectionHeld = listener
    }

    internal fun hold(direction: DirectionDto) {
        heldDirection = direction
        onDirectionHeld?.invoke(direction)
    }

    internal fun release() {
        heldDirection = null
    }

    private fun arrow(
        direction: DirectionDto,
        x: Double,
        y: Double,
    ): UIButton =
        uiButton(size = Size(ARROW_SIZE, ARROW_SIZE)) {
            position(x, y)
            bgColorOut = ARROW_COLOR
            bgColorOver = ARROW_HELD_COLOR
            background.borderColor = Colors.LIGHTGRAY
            graphics {
                fill(Colors.WHITE) {
                    val (tip, firstCorner, secondCorner) = triangle(direction)
                    moveTo(tip)
                    lineTo(firstCorner)
                    lineTo(secondCorner)
                    close()
                }
            }
            onDown { hold(direction) }
            onUp { release() }
            onUpOutside { release() }
        }

    // The tip of the triangle points towards the direction, from the centre of the arrow.
    private fun triangle(direction: DirectionDto): Triple<Point, Point, Point> {
        val center = ARROW_SIZE / 2
        val tip = Point(center + direction.columnOffset * ARROW_HALF_SIDE, center + direction.rowOffset * ARROW_HALF_SIDE)
        val baseX = center - direction.columnOffset * ARROW_HALF_SIDE
        val baseY = center - direction.rowOffset * ARROW_HALF_SIDE
        // The base is across the direction: horizontal for north and south, vertical for east and west.
        val firstCorner = Point(baseX + direction.rowOffset * ARROW_HALF_SIDE, baseY + direction.columnOffset * ARROW_HALF_SIDE)
        val secondCorner = Point(baseX - direction.rowOffset * ARROW_HALF_SIDE, baseY - direction.columnOffset * ARROW_HALF_SIDE)
        return Triple(tip, firstCorner, secondCorner)
    }
}
