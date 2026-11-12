package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.shared.adapters.presentation.sharedViewsTest
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldStartWith
import korlibs.korge.tests.ViewsForTesting
import kotlin.test.Test

class ConsentViewTest : ViewsForTesting() {
    private val choices = mutableListOf<String>()

    private fun newConsentView() =
        ConsentView(MessagesMother.messages()).apply {
            setDelegate(
                object : ConsentView.Delegate {
                    override fun consentAccepted() {
                        choices += "accepted"
                    }

                    override fun consentDeclined() {
                        choices += "declined"
                    }

                    override fun consentReadMoreSelected() {
                        choices += "read more"
                    }
                },
            )
            display()
        }

    @Test
    fun `ConsentView should ask to record how the game is played with Read more, Decline and Accept when it is displayed`() =
        sharedViewsTest {
            // When
            val consentView = newConsentView()
            // Then
            consentView.consentTitle() shouldBe "Help improve the game"
            consentView.consentBody() shouldStartWith "May we record how you play"
            consentView.consentLabels().shouldContainExactly("Read more", "Decline", "Accept")
        }

    @Test
    fun `ConsentView should run consentAccepted when Accept is tapped`() =
        sharedViewsTest {
            // Given
            val consentView = newConsentView()
            addChild(consentView)
            // When
            clickAccept(consentView)
            // Then
            choices.shouldContainExactly("accepted")
        }

    @Test
    fun `ConsentView should run consentDeclined when Decline is tapped`() =
        sharedViewsTest {
            // Given
            val consentView = newConsentView()
            addChild(consentView)
            // When
            clickDecline(consentView)
            // Then
            choices.shouldContainExactly("declined")
        }

    @Test
    fun `ConsentView should run consentReadMoreSelected when Read more is tapped`() =
        sharedViewsTest {
            // Given
            val consentView = newConsentView()
            addChild(consentView)
            // When
            clickReadMore(consentView)
            // Then
            choices.shouldContainExactly("read more")
        }
}
