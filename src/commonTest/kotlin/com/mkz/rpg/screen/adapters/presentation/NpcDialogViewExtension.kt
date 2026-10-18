package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.shared.adapters.presentation.clickOn
import com.mkz.rpg.shared.adapters.presentation.isShown
import korlibs.korge.tests.ViewsForTesting

// Page object of [NpcDialogView], used by the tests to read and tap the dialog as the player does.

fun NpcDialogView.isDialogShown(): Boolean = isShown()

fun NpcDialogView.displayedName(): String = nameText.plainText

fun NpcDialogView.displayedLine(): String = lineText.plainText

fun NpcDialogView.hasPortrait(): Boolean = portrait != null

/** The portrait, scaled as displayed: its width and height. */
fun NpcDialogView.portraitSize(): Pair<Double, Double> = portrait?.let { it.scaledWidth to it.scaledHeight } ?: (0.0 to 0.0)

/** Whether the portrait is on the left of the name and the line. */
fun NpcDialogView.isPortraitLeftOfText(): Boolean = portrait?.let { it.x + it.scaledWidth <= nameText.x && it.x + it.scaledWidth <= lineText.x } == true

/** Whether the name is on top of the line. */
fun NpcDialogView.isNameAboveLine(): Boolean = nameText.y + nameText.height <= lineText.y

/** Taps the dialog, as the player. */
fun NpcDialogView.tapDialog() = tap()

/** Taps the dialog where its line is written, as the player. */
suspend fun ViewsForTesting.clickDialog(npcDialogView: NpcDialogView) = clickOn(npcDialogView.lineText)
