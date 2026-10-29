package com.mkz.rpg.storybook.adapters.presentation

import com.mkz.rpg.STORYBOOK_WINDOW_SIZE
import com.mkz.rpg.shared.adapters.presentation.nextFrame
import com.mkz.rpg.shared.adapters.presentation.sharedViewsTest
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe
import korlibs.korge.tests.ViewsForTesting
import korlibs.math.geom.Rectangle
import kotlin.test.Test

class ZoomViewTest : ViewsForTesting(windowSize = STORYBOOK_WINDOW_SIZE) {
    private val window = Rectangle(0, 0, 1280, 900)

    // The phone frame of the Storybook, 414x868, as laid out by the scene.
    private val phoneArea = Rectangle(332.0, 16.0, 414.0, 868.0)

    private fun newZoomView() = ZoomView(STORYBOOK_WINDOW_SIZE).apply { phoneArea = this@ZoomViewTest.phoneArea }

    // KorGE rounds the screen to whole units, so the Storybook in its middle may be a fraction of a unit off.
    private fun Rectangle.shouldBeAbout(expected: Rectangle) {
        left shouldBe (expected.left plusOrMinus 0.5)
        top shouldBe (expected.top plusOrMinus 0.5)
        right shouldBe (expected.right plusOrMinus 0.5)
        bottom shouldBe (expected.bottom plusOrMinus 0.5)
    }

    @Test
    fun `ZoomView should show the whole Storybook, as without a zoom, and its bar at the bottom right when it is displayed`() =
        sharedViewsTest {
            // When
            val zoomView = newZoomView()
            addChild(zoomView)
            nextFrame()
            // Then
            zoomView.zoom() shouldBe 1.0
            zoomView.storybookArea().shouldBeAbout(window)
            zoomView.zoomBar().zoomText() shouldBe "100%"
            zoomView.isBarAtBottomRightOf(window).shouldBeTrue()
        }

    @Test
    fun `ZoomIn should zoom the Storybook 1,25 times around the middle of the screen when + is tapped`() =
        sharedViewsTest {
            // Given
            val zoomView = newZoomView()
            addChild(zoomView)
            // When
            clickZoomIn(zoomView.zoomBar())
            // Then
            zoomView.zoom() shouldBe 1.25
            zoomView.storybookArea().shouldBeAbout(Rectangle(-160.0, -112.5, 1600.0, 1125.0))
            zoomView.zoomBar().zoomText() shouldBe "125%"
            zoomView.isBarAtBottomRightOf(window).shouldBeTrue()
        }

    @Test
    fun `ZoomOut should show the whole Storybook again when - is tapped as many times as +`() =
        sharedViewsTest {
            // Given
            val zoomView = newZoomView()
            addChild(zoomView)
            clickZoomIn(zoomView.zoomBar())
            clickZoomIn(zoomView.zoomBar())
            // When
            clickZoomOut(zoomView.zoomBar())
            clickZoomOut(zoomView.zoomBar())
            // Then
            zoomView.zoom() shouldBe (1.0 plusOrMinus 0.000001)
            zoomView.storybookArea().shouldBeAbout(window)
            zoomView.zoomBar().zoomText() shouldBe "100%"
        }

    @Test
    fun `Fit should show the whole Storybook when Fit is tapped`() =
        sharedViewsTest {
            // Given
            val zoomView = newZoomView()
            addChild(zoomView)
            clickZoomIn(zoomView.zoomBar())
            // When
            clickFit(zoomView.zoomBar())
            // Then
            zoomView.zoom() shouldBe 1.0
            zoomView.storybookArea().shouldBeAbout(window)
            zoomView.zoomBar().zoomText() shouldBe "100%"
        }

    @Test
    fun `Phone should fill the height of the window with the phone, keeping the Storybook on the screen, when Phone is tapped`() =
        sharedViewsTest {
            // Given
            val zoomView = newZoomView()
            addChild(zoomView)
            // When
            clickPhone(zoomView.zoomBar())
            // Then
            val phone = zoomView.areaOnStage(phoneArea)
            phone.height shouldBe (900.0 plusOrMinus 0.01)
            phone.center.y shouldBe (450.0 plusOrMinus 0.01)
            // The phone is left of the middle of the Storybook, which can not be moved further right than the left of the window.
            zoomView.storybookArea().left shouldBe (0.0 plusOrMinus 0.01)
            zoomView.zoomBar().zoomText() shouldBe "104%"
        }

    @Test
    fun `ZoomView should stay at Fit, with its bar on the screen at the same size, when the screen turns into a portrait phone`() =
        sharedViewsTest {
            // Given
            val zoomView = newZoomView()
            addChild(zoomView)
            nextFrame()
            val barWidthOnWindow = zoomView.zoomBar().getGlobalBounds().width
            // When
            views.resized(390, 844)
            nextFrame()
            // Then
            val screen = views.actualVirtualBounds
            zoomView.zoom() shouldBe 1.0
            zoomView.storybookArea().shouldBeAbout(window)
            zoomView.isBarAtBottomRightOf(screen).shouldBeTrue()
            // Drawn 390 / 1280 times smaller on the phone than on the window, it is as wide on both.
            (zoomView.zoomBar().getGlobalBounds().width * 390 / 1280) shouldBe (barWidthOnWindow plusOrMinus 0.5)
        }

    @Test
    fun `Phone should fill the width of a portrait phone with the phone when Phone is tapped`() =
        sharedViewsTest {
            // Given
            val zoomView = newZoomView()
            addChild(zoomView)
            views.resized(390, 844)
            nextFrame()
            // When
            clickPhone(zoomView.zoomBar())
            // Then
            val phone = zoomView.areaOnStage(phoneArea)
            phone.width shouldBe (views.actualVirtualBounds.width plusOrMinus 0.01)
            phone.center.x shouldBe (views.actualVirtualBounds.center.x plusOrMinus 0.01)
            zoomView.zoomBar().zoomText() shouldBe "309%"
        }
}
