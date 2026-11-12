package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.shared.adapters.presentation.clickOn
import com.mkz.rpg.shared.adapters.presentation.descendants
import com.mkz.rpg.shared.adapters.presentation.isShown
import korlibs.korge.tests.ViewsForTesting
import korlibs.korge.view.Stage

fun Stage.privacyNoticeView(): PrivacyNoticeView? = descendants().filterIsInstance<PrivacyNoticeView>().firstOrNull()

fun PrivacyNoticeView.isPrivacyNoticeShown(): Boolean = isShown()

fun PrivacyNoticeView.privacyNoticeTitle(): String = titleText.text

fun PrivacyNoticeView.privacyNoticeBody(): String = bodyText.plainText

suspend fun ViewsForTesting.clickPrivacyNoticeBack(privacyNoticeView: PrivacyNoticeView) = clickOn(privacyNoticeView.backButton)
