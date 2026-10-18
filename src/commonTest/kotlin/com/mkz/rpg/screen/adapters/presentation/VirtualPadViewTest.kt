package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.exploration.domain.Exploration.Dto.DirectionDto
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
    private val heldDirections = mutableListOf<DirectionDto>()
    private var aPresses = 0

    private fun newVirtualPadView() =
        VirtualPadView().also { pad ->
            pad.setOnDirectionHeld { direction -> heldDirections += direction }
            pad.setOnAPressed { aPresses++ }
        }

    @Test
    fun `VirtualPadView should lay the arrows out as a cross when it is displayed`() =
        sharedViewsTest {
            // When
            val pad = newVirtualPadView()
            // Then
            pad.arrow(DirectionDto.NORTH).y shouldBeLessThan pad.arrow(DirectionDto.WEST).y
            pad.arrow(DirectionDto.SOUTH).y shouldBeGreaterThan pad.arrow(DirectionDto.EAST).y
            pad.arrow(DirectionDto.WEST).x shouldBeLessThan pad.arrow(DirectionDto.NORTH).x
            pad.arrow(DirectionDto.EAST).x shouldBeGreaterThan pad.arrow(DirectionDto.SOUTH).x
            pad.arrow(DirectionDto.NORTH).x shouldBe pad.arrow(DirectionDto.SOUTH).x
            pad.arrow(DirectionDto.WEST).y shouldBe pad.arrow(DirectionDto.EAST).y
        }

    @Test
    fun `VirtualPadView should lay the arrows out on the left, and the B and A buttons on the right, A higher and further right`() =
        sharedViewsTest {
            // When
            val pad = newVirtualPadView()
            // Then
            pad.arrow(DirectionDto.EAST).let { east -> east.x + east.width } shouldBeLessThan pad.bButton().x
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
    fun `ClickB should do nothing yet when B is tapped`() =
        sharedViewsTest {
            // Given
            val pad = newVirtualPadView()
            addChild(pad)
            // When
            clickB(pad)
            // Then
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
            clickArrow(pad, DirectionDto.WEST)
            // Then
            heldDirections.shouldNotBeEmpty()
            heldDirections.toSet().shouldContainExactly(DirectionDto.WEST)
            pad.heldDirection().shouldBeNull()
        }

    @Test
    fun `HoldArrow should keep walking towards the arrow on every frame while it is held`() =
        sharedViewsTest {
            // Given
            val pad = newVirtualPadView()
            addChild(pad)
            // When
            pad.holdArrow(DirectionDto.NORTH)
            repeat(3) { nextFrame() }
            // Then
            heldDirections.shouldHaveSize(4)
            heldDirections.toSet().shouldContainExactly(DirectionDto.NORTH)
        }

    @Test
    fun `ReleaseArrow should stop walking when the arrow is released`() =
        sharedViewsTest {
            // Given
            val pad = newVirtualPadView()
            addChild(pad)
            pad.holdArrow(DirectionDto.SOUTH)
            // When
            pad.releaseArrow()
            repeat(3) { nextFrame() }
            // Then
            heldDirections.shouldContainExactly(DirectionDto.SOUTH)
        }
}
