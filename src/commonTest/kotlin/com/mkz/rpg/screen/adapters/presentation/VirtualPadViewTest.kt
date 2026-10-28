package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.VirtualPadView.Direction
import com.mkz.rpg.shared.adapters.presentation.nextFrame
import com.mkz.rpg.shared.adapters.presentation.sharedViewsTest
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.doubles.shouldBeGreaterThan
import io.kotest.matchers.doubles.shouldBeLessThan
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import korlibs.korge.tests.ViewsForTesting
import kotlin.test.Test

class VirtualPadViewTest : ViewsForTesting() {
    private val pressedDirections = mutableListOf<Direction>()
    private val heldDirections = mutableListOf<Direction>()
    private var aPresses = 0
    private var bPresses = 0

    private fun newVirtualPadView() =
        VirtualPadView().also { pad ->
            pad.setOnDirectionPressed { direction -> pressedDirections += direction }
            pad.setOnDirectionHeld { direction -> heldDirections += direction }
            pad.setOnAPressed { aPresses++ }
            pad.setOnBPressed { bPresses++ }
        }

    @Test
    fun `VirtualPadView should lay the arrows out as a cross when it is displayed`() =
        sharedViewsTest {
            // When
            val pad = newVirtualPadView()
            // Then
            pad.arrow(Direction.NORTH).y shouldBeLessThan pad.arrow(Direction.WEST).y
            pad.arrow(Direction.SOUTH).y shouldBeGreaterThan pad.arrow(Direction.EAST).y
            pad.arrow(Direction.WEST).x shouldBeLessThan pad.arrow(Direction.NORTH).x
            pad.arrow(Direction.EAST).x shouldBeGreaterThan pad.arrow(Direction.SOUTH).x
            pad.arrow(Direction.NORTH).x shouldBe pad.arrow(Direction.SOUTH).x
            pad.arrow(Direction.WEST).y shouldBe pad.arrow(Direction.EAST).y
        }

    @Test
    fun `VirtualPadView should lay the arrows out on the left, and the B and A buttons on the right, A higher and further right`() =
        sharedViewsTest {
            // When
            val pad = newVirtualPadView()
            // Then
            pad.arrow(Direction.EAST).let { east -> east.x + east.width } shouldBeLessThan pad.bButton().x
            pad.aButton().x shouldBeGreaterThan pad.bButton().x
            pad.aButton().y shouldBeLessThan pad.bButton().y
            pad.aButton().let { a -> a.x + a.width } shouldBeLessThan pad.width
            pad.aButton().label() shouldBe "A"
            pad.bButton().label() shouldBe "B"
        }

    @Test
    fun `ClickA should call the A listener once when A is tapped`() =
        sharedViewsTest {
            // Given
            val pad = newVirtualPadView()
            addChild(pad)
            // When
            clickA(pad)
            // Then
            aPresses shouldBe 1
            heldDirections.shouldBeEmpty()
        }

    @Test
    fun `ClickB should call the B listener once when B is tapped`() =
        sharedViewsTest {
            // Given
            val pad = newVirtualPadView()
            addChild(pad)
            // When
            clickB(pad)
            // Then
            bPresses shouldBe 1
            aPresses shouldBe 0
            heldDirections.shouldBeEmpty()
        }

    @Test
    fun `ClickArrow should walk towards the arrow while it is tapped and stop once it is released`() =
        sharedViewsTest {
            // Given
            val pad = newVirtualPadView()
            addChild(pad)
            // When
            clickArrow(pad, Direction.WEST)
            // Then
            heldDirections.shouldNotBeEmpty()
            heldDirections.toSet().shouldContainExactly(Direction.WEST)
            pad.heldDirection().shouldBeNull()
        }

    @Test
    fun `HoldArrow should keep walking towards the arrow on every frame while it is held`() =
        sharedViewsTest {
            // Given
            val pad = newVirtualPadView()
            addChild(pad)
            // When
            pad.holdArrow(Direction.NORTH)
            repeat(3) { nextFrame() }
            // Then
            heldDirections.shouldHaveSize(4)
            heldDirections.toSet().shouldContainExactly(Direction.NORTH)
        }

    @Test
    fun `HoldArrow should report the press of the arrow once, then hold it on every frame`() =
        sharedViewsTest {
            // Given
            val pad = newVirtualPadView()
            addChild(pad)
            // When
            pad.holdArrow(Direction.EAST)
            repeat(3) { nextFrame() }
            // Then
            pressedDirections.shouldContainExactly(Direction.EAST)
            heldDirections.shouldHaveSize(4)
        }

    @Test
    fun `ReleaseArrow should stop walking when the arrow is released`() =
        sharedViewsTest {
            // Given
            val pad = newVirtualPadView()
            addChild(pad)
            pad.holdArrow(Direction.SOUTH)
            // When
            pad.releaseArrow()
            repeat(3) { nextFrame() }
            // Then
            heldDirections.shouldContainExactly(Direction.SOUTH)
        }
}
