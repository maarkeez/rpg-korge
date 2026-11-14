package com.mkz.tessavar.screen.adapters.presentation

import com.mkz.tessavar.shared.adapters.presentation.clickOn
import com.mkz.tessavar.shared.adapters.presentation.isShown
import korlibs.korge.tests.ViewsForTesting

fun TalkView.isDialogShown(): Boolean = isShown()

fun TalkView.displayedName(): String = nameText.plainText

fun TalkView.displayedLine(): String = lineText.plainText

fun TalkView.hasPortrait(): Boolean = portrait != null

fun TalkView.portraitSize(): Pair<Double, Double> = portrait?.let { it.scaledWidth to it.scaledHeight } ?: (0.0 to 0.0)

fun TalkView.isPortraitLeftOfText(): Boolean = portrait?.let { it.x + it.scaledWidth <= nameText.x && it.x + it.scaledWidth <= lineText.x } == true

fun TalkView.isNameAboveLine(): Boolean = nameText.y + nameText.height <= lineText.y

fun TalkView.lineHeight(): Double = lineText.height

fun TalkView.tapDialog() = tap()

suspend fun ViewsForTesting.clickDialog(talkView: TalkView) = clickOn(talkView.lineText)
