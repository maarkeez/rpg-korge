package com.mkz.rpg.storybook.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.HealthBarView
import com.mkz.rpg.shared.adapters.presentation.sharedViewsTest
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import korlibs.korge.tests.ViewsForTesting
import korlibs.math.geom.Size
import kotlin.test.Test

class PhoneFrameViewTest : ViewsForTesting() {
    @Test
    fun `PhoneFrameView should display an empty screen as large as the game window when it is displayed`() =
        sharedViewsTest {
            // When
            val phoneFrameView = PhoneFrameView()
            // Then
            phoneFrameView.displayedView().shouldBeNull()
            phoneFrameView.screenSize() shouldBe (390.0 to 844.0)
        }

    @Test
    fun `Display should show the view in the middle of the screen`() =
        sharedViewsTest {
            // Given
            val phoneFrameView = PhoneFrameView()
            addChild(phoneFrameView)
            val healthBarView = HealthBarView(Size(281.5, 16)).apply { display(remaining = 30, maximum = 30) }
            // When
            phoneFrameView.display(healthBarView)
            // Then
            phoneFrameView.displayedView() shouldBeSameInstanceAs healthBarView
            phoneFrameView.isViewCenteredOnScreen().shouldBeTrue()
        }

    @Test
    fun `Display should show only the last view when another view is displayed`() =
        sharedViewsTest {
            // Given
            val phoneFrameView = PhoneFrameView()
            addChild(phoneFrameView)
            val fullHealthBarView = HealthBarView(Size(281.5, 16)).apply { display(remaining = 30, maximum = 30) }
            val emptyHealthBarView = HealthBarView(Size(281.5, 16)).apply { display(remaining = 0, maximum = 30) }
            phoneFrameView.display(fullHealthBarView)
            // When
            phoneFrameView.display(emptyHealthBarView)
            // Then
            phoneFrameView.displayedView() shouldBeSameInstanceAs emptyHealthBarView
            phoneFrameView.displayedViewCount() shouldBe 1
        }
}
