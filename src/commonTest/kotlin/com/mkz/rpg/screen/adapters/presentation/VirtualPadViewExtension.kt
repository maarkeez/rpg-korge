package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.exploration.domain.Exploration.Dto.DirectionDto
import com.mkz.rpg.shared.adapters.presentation.clickOn
import com.mkz.rpg.shared.adapters.presentation.isShown
import korlibs.korge.tests.ViewsForTesting
import korlibs.korge.ui.UIButton

// Page object of [VirtualPadView], used by the tests to read and press the arrows as the player does.

fun VirtualPadView.isPadShown(): Boolean = isShown()

fun VirtualPadView.heldDirection(): DirectionDto? = heldDirection

fun VirtualPadView.arrow(direction: DirectionDto): UIButton =
    when (direction) {
        DirectionDto.NORTH -> northArrow
        DirectionDto.EAST -> eastArrow
        DirectionDto.SOUTH -> southArrow
        DirectionDto.WEST -> westArrow
    }

fun VirtualPadView.aButton(): UIButton = aButton

fun VirtualPadView.bButton(): UIButton = bButton

/** The label written on the button, e.g.: `A`. */
fun UIButton.label(): String = text

/** Presses A, as the player tapping it. */
fun VirtualPadView.pressAButton() = pressA()

/** Taps the A button, as the player. */
suspend fun ViewsForTesting.clickA(virtualPadView: VirtualPadView) = clickOn(virtualPadView.aButton)

/** Taps the B button, as the player. */
suspend fun ViewsForTesting.clickB(virtualPadView: VirtualPadView) = clickOn(virtualPadView.bButton)

/** Presses and releases the arrow, as a tap of the player. */
suspend fun ViewsForTesting.clickArrow(
    virtualPadView: VirtualPadView,
    direction: DirectionDto,
) = clickOn(virtualPadView.arrow(direction))

/** Holds the arrow, as the player keeping it pressed. */
fun VirtualPadView.holdArrow(direction: DirectionDto) = hold(direction)

/** Releases the held arrow. */
fun VirtualPadView.releaseArrow() = release()
