package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.shared.adapters.presentation.sharedViewsTest
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import korlibs.korge.tests.ViewsForTesting
import kotlin.test.Test

class PrivacyNoticeViewTest : ViewsForTesting() {
    private var backs = 0

    private fun newPrivacyNoticeView() =
        PrivacyNoticeView(MessagesMother.messages()).apply {
            setDelegate(
                object : PrivacyNoticeView.Delegate {
                    override fun privacyNoticeBackSelected() {
                        backs++
                    }
                },
            )
            display()
        }

    @Test
    fun `PrivacyNoticeView should say what is recorded and who receives it when it is displayed`() =
        sharedViewsTest {
            // When
            val privacyNoticeView = newPrivacyNoticeView()
            // Then
            privacyNoticeView.privacyNoticeTitle() shouldBe "Privacy notice"
            privacyNoticeView.privacyNoticeBody() shouldContain "What is recorded"
            privacyNoticeView.privacyNoticeBody() shouldContain "Who receives it"
        }

    @Test
    fun `PrivacyNoticeView should run privacyNoticeBackSelected when Back is tapped`() =
        sharedViewsTest {
            // Given
            val privacyNoticeView = newPrivacyNoticeView()
            addChild(privacyNoticeView)
            // When
            clickPrivacyNoticeBack(privacyNoticeView)
            // Then
            backs shouldBe 1
        }
}
