package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.shared.adapters.presentation.clickOn
import com.mkz.rpg.shared.adapters.presentation.isShown
import korlibs.korge.tests.ViewsForTesting

// Page object of [TalkView], used by the tests to read and tap the dialog as the player does.

fun TalkView.isDialogShown(): Boolean = isShown()

fun TalkView.displayedName(): String = nameText.plainText

fun TalkView.displayedLine(): String = lineText.plainText

fun TalkView.hasPortrait(): Boolean = portrait != null

/** The portrait, scaled as displayed: its width and height. */
fun TalkView.portraitSize(): Pair<Double, Double> = portrait?.let { it.scaledWidth to it.scaledHeight } ?: (0.0 to 0.0)

/** Whether the portrait is on the left of the name and the line. */
fun TalkView.isPortraitLeftOfText(): Boolean = portrait?.let { it.x + it.scaledWidth <= nameText.x && it.x + it.scaledWidth <= lineText.x } == true

/** Whether the name is on top of the line. */
fun TalkView.isNameAboveLine(): Boolean = nameText.y + nameText.height <= lineText.y

/** How tall the line can be. */
fun TalkView.lineHeight(): Double = lineText.height

/** Taps the dialog, as the player. */
fun TalkView.tapDialog() = tap()

/** Taps the dialog where its line is written, as the player. */
suspend fun ViewsForTesting.clickDialog(talkView: TalkView) = clickOn(talkView.lineText)
