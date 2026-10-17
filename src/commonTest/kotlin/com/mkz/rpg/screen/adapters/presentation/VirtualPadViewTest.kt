package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.exploration.domain.Exploration.Dto.DirectionDto
import com.mkz.rpg.shared.adapters.presentation.nextFrame
import com.mkz.rpg.shared.adapters.presentation.sharedViewsTest
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

    private fun newVirtualPadView() = VirtualPadView().also { pad -> pad.setOnDirectionHeld { direction -> heldDirections += direction } }

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
