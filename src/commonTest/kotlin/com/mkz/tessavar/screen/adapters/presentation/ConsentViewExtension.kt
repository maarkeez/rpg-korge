package com.mkz.tessavar.screen.adapters.presentation

import com.mkz.tessavar.shared.adapters.presentation.clickOn
import com.mkz.tessavar.shared.adapters.presentation.descendants
import com.mkz.tessavar.shared.adapters.presentation.isShown
import korlibs.korge.tests.ViewsForTesting
import korlibs.korge.view.Stage

fun Stage.consentView(): ConsentView? = descendants().filterIsInstance<ConsentView>().firstOrNull()

fun ConsentView.isConsentShown(): Boolean = isShown()

fun ConsentView.consentTitle(): String = titleText.text

fun ConsentView.consentBody(): String = bodyText.plainText

fun ConsentView.consentLabels(): List<String> = listOf(readMoreText.text, declineButton.text, acceptButton.text)

suspend fun ViewsForTesting.clickAccept(consentView: ConsentView) = clickOn(consentView.acceptButton)

suspend fun ViewsForTesting.clickDecline(consentView: ConsentView) = clickOn(consentView.declineButton)

suspend fun ViewsForTesting.clickReadMore(consentView: ConsentView) = clickOn(consentView.readMoreText)
