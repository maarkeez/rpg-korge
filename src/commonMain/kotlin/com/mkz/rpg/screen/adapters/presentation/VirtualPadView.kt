package com.mkz.rpg.screen.adapters.presentation

import korlibs.image.color.Colors
import korlibs.image.color.RGBA
import korlibs.korge.input.onClick
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
import korlibs.math.interpolation.Ratio

/**
 * The pad the player explores with, laid out as a Game Boy Color: the north, east, south and west arrows on the left,
 * the round B and A buttons on the right, A higher and further right than B, and the Start pill centred at the bottom,
 * between both.
 *
 * Holding an arrow keeps walking the party towards it: the held listener is called when the arrow is pressed, and on
 * every frame while it is held. The pressed listener is called once before, so a tap is known even when it is released
 * before it can be walked. Tapping A, B or Start calls its listener once (e.g.: to talk, to open the party formation, or
 * to pause the game).
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

        const val BUTTON_SIZE = 64.0
        const val BUTTON_TEXT_SIZE = 28.0

        // From the FamiCube palette.
        private val BUTTON_COLOR = RGBA(135, 22, 70)
        private val BUTTON_PRESSED_COLOR = RGBA(207, 60, 113)
        private val BUTTON_BORDER_COLOR = RGBA(79, 21, 7)
        private val BUTTON_LABEL_COLOR = RGBA(255, 255, 255)

        // The cross of arrows is centred on the left part of the pad, and the buttons on its right part.
        private const val ARROWS_CENTER_RATIO = 0.3
        private const val BUTTONS_CENTER_RATIO = 0.8

        // How far each button is from the centre of the buttons, A up and to the right, B down and to the left.
        private const val BUTTON_OFFSET_X = 38.0
        private const val BUTTON_OFFSET_Y = 24.0

        const val START_WIDTH = 72.0
        const val START_HEIGHT = 22.0
        private const val START_TEXT_SIZE = 12.0

        // Start is centred between the arrows and the buttons, at the bottom of the pad.
        private const val START_CENTER_RATIO = 0.55
        private const val START_BOTTOM_MARGIN = 10.0
        private val START_COLOR = RGBA(76, 76, 76)
        private val START_PRESSED_COLOR = RGBA(110, 110, 110)
    }

    /** An arrow of the pad, with the tile offsets of the direction it points to. */
    enum class Direction(
        val rowOffset: Int,
        val columnOffset: Int,
    ) {
        NORTH(rowOffset = -1, columnOffset = 0),
        EAST(rowOffset = 0, columnOffset = 1),
        SOUTH(rowOffset = 1, columnOffset = 0),
        WEST(rowOffset = 0, columnOffset = -1),
    }

    private var onDirectionPressed: ((Direction) -> Unit)? = null
    private var onDirectionHeld: ((Direction) -> Unit)? = null
    private var onAPressed: (() -> Unit)? = null
    private var onBPressed: (() -> Unit)? = null
    private var onStartPressed: (() -> Unit)? = null
    internal var heldDirection: Direction? = null
        private set

    // The arrows form a cross, centred on the left part of the pad.
    private val centerX = size.width * ARROWS_CENTER_RATIO - ARROW_SIZE / 2
    private val centerY = (size.height - ARROW_SIZE) / 2
    internal val northArrow = arrow(Direction.NORTH, x = centerX, y = centerY - ARROW_SIZE)
    internal val eastArrow = arrow(Direction.EAST, x = centerX + ARROW_SIZE, y = centerY)
    internal val southArrow = arrow(Direction.SOUTH, x = centerX, y = centerY + ARROW_SIZE)
    internal val westArrow = arrow(Direction.WEST, x = centerX - ARROW_SIZE, y = centerY)

    private val buttonsCenterX = size.width * BUTTONS_CENTER_RATIO
    private val buttonsCenterY = size.height / 2
    internal val bButton = button("B", centerX = buttonsCenterX - BUTTON_OFFSET_X, centerY = buttonsCenterY + BUTTON_OFFSET_Y) { pressB() }
    internal val aButton = button("A", centerX = buttonsCenterX + BUTTON_OFFSET_X, centerY = buttonsCenterY - BUTTON_OFFSET_Y) { pressA() }

    internal val startButton =
        uiButton(size = Size(START_WIDTH, START_HEIGHT), label = "START") {
            position(size.width * START_CENTER_RATIO - START_WIDTH / 2, size.height - START_BOTTOM_MARGIN - START_HEIGHT)
            radiusRatio = Ratio.ONE
            bgColorOut = START_COLOR
            bgColorOver = START_PRESSED_COLOR
            background.borderColor = Colors.LIGHTGRAY
            textColor = BUTTON_LABEL_COLOR
            textSize = START_TEXT_SIZE
            onClick { pressStart() }
        }

    init {
        addUpdater { heldDirection?.let { direction -> onDirectionHeld?.invoke(direction) } }
    }

    open fun setOnDirectionPressed(listener: (Direction) -> Unit) {
        onDirectionPressed = listener
    }

    open fun setOnDirectionHeld(listener: (Direction) -> Unit) {
        onDirectionHeld = listener
    }

    open fun setOnAPressed(listener: () -> Unit) {
        onAPressed = listener
    }

    open fun setOnBPressed(listener: () -> Unit) {
        onBPressed = listener
    }

    open fun setOnStartPressed(listener: () -> Unit) {
        onStartPressed = listener
    }

    internal fun pressA() {
        onAPressed?.invoke()
    }

    internal fun pressB() {
        onBPressed?.invoke()
    }

    internal fun pressStart() {
        onStartPressed?.invoke()
    }

    internal fun hold(direction: Direction) {
        heldDirection = direction
        onDirectionPressed?.invoke(direction)
        onDirectionHeld?.invoke(direction)
    }

    internal fun release() {
        heldDirection = null
    }

    private fun arrow(
        direction: Direction,
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

    private fun button(
        label: String,
        centerX: Double,
        centerY: Double,
        onPressed: () -> Unit,
    ): UIButton =
        uiButton(size = Size(BUTTON_SIZE, BUTTON_SIZE), label = label) {
            position(centerX - BUTTON_SIZE / 2, centerY - BUTTON_SIZE / 2)
            radiusRatio = Ratio.ONE
            bgColorOut = BUTTON_COLOR
            bgColorOver = BUTTON_PRESSED_COLOR
            background.borderColor = BUTTON_BORDER_COLOR
            textColor = BUTTON_LABEL_COLOR
            textSize = BUTTON_TEXT_SIZE
            onClick { onPressed() }
        }

    // The tip of the triangle points towards the direction, from the centre of the arrow.
    private fun triangle(direction: Direction): Triple<Point, Point, Point> {
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
