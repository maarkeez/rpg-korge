package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.VirtualPadView.Direction
import com.mkz.rpg.shared.adapters.presentation.clickOn
import com.mkz.rpg.shared.adapters.presentation.isShown
import korlibs.korge.tests.ViewsForTesting
import korlibs.korge.ui.UIButton

fun VirtualPadView.isPadShown(): Boolean = isShown()

fun VirtualPadView.heldDirection(): Direction? = heldDirection

fun VirtualPadView.arrow(direction: Direction): UIButton =
    when (direction) {
        Direction.NORTH -> northArrow
        Direction.EAST -> eastArrow
        Direction.SOUTH -> southArrow
        Direction.WEST -> westArrow
    }

fun VirtualPadView.aButton(): UIButton = aButton

fun VirtualPadView.bButton(): UIButton = bButton

fun VirtualPadView.startButton(): UIButton = startButton

fun VirtualPadView.pressAButton() = pressA()

fun VirtualPadView.pressBButton() = pressB()

fun VirtualPadView.pressStartButton() = pressStart()

suspend fun ViewsForTesting.clickA(virtualPadView: VirtualPadView) = clickOn(virtualPadView.aButton)

suspend fun ViewsForTesting.clickB(virtualPadView: VirtualPadView) = clickOn(virtualPadView.bButton)

suspend fun ViewsForTesting.clickStart(virtualPadView: VirtualPadView) = clickOn(virtualPadView.startButton)

suspend fun ViewsForTesting.clickArrow(
    virtualPadView: VirtualPadView,
    direction: Direction,
) = clickOn(virtualPadView.arrow(direction))

fun VirtualPadView.holdArrow(direction: Direction) = hold(direction)

fun VirtualPadView.releaseArrow() = release()
