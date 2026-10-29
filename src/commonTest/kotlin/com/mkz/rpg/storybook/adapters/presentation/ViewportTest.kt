package com.mkz.rpg.storybook.adapters.presentation

import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe
import korlibs.math.geom.Point
import korlibs.math.geom.Rectangle
import korlibs.math.geom.Size
import kotlin.test.Test

class ViewportTest {
    private val storybook = Size(1280, 900)

    // A 1280x900 window, as on a desktop: the Storybook fills it.
    private val window = Rectangle(0, 0, 1280, 900)

    // A 390x844 portrait phone: KorGE fits the 1280 of the Storybook in its width, and the screen goes far above and below it.
    private val phoneScreen = Rectangle(0.0, -935.0, 1280.0, 2770.0)

    // The phone frame of the Storybook, 414x868, as laid out by the scene.
    private val phoneFrame = Rectangle(332.0, 16.0, 414.0, 868.0)

    private fun Viewport.shouldBeAt(
        x: Double,
        y: Double,
    ) {
        position.x shouldBe (x plusOrMinus 0.001)
        position.y shouldBe (y plusOrMinus 0.001)
    }

    @Test
    fun `Viewport should show the whole Storybook in the middle of the screen when it is created`() {
        // When
        val viewport = Viewport(storybook, phoneScreen)
        // Then
        viewport.zoom shouldBe 1.0
        viewport.isFit.shouldBeTrue()
        viewport.shouldBeAt(0.0, 0.0)
    }

    @Test
    fun `ZoomIn should zoom 1,25 times around the middle of the screen`() {
        // Given
        val viewport = Viewport(storybook, window)
        // When
        viewport.zoomIn()
        // Then
        viewport.zoom shouldBe 1.25
        viewport.isFit.shouldBeFalse()
        viewport.shouldBeAt(-160.0, -112.5)
    }

    @Test
    fun `ZoomIn should keep the point under it where it is on the screen when zoomed around a point`() {
        // Given
        val viewport = Viewport(storybook, window)
        // When
        viewport.zoomIn(around = Point(100, 100))
        // Then
        viewport.shouldBeAt(-25.0, -25.0)
    }

    @Test
    fun `ZoomIn should stop at the maximum zoom when zoomed in many times`() {
        // Given
        val viewport = Viewport(storybook, window)
        // When
        repeat(10) { viewport.zoomIn() }
        // Then
        viewport.zoom shouldBe Viewport.MAXIMUM_ZOOM
    }

    @Test
    fun `ZoomOut should come back to Fit when zoomed out as many times as in`() {
        // Given
        val viewport = Viewport(storybook, window)
        repeat(3) { viewport.zoomIn(around = Point(200, 700)) }
        // When
        repeat(3) { viewport.zoomOut() }
        // Then
        viewport.zoom shouldBe (1.0 plusOrMinus 0.000001)
        viewport.isFit.shouldBeTrue()
        viewport.shouldBeAt(0.0, 0.0)
    }

    @Test
    fun `ZoomOut should not make the Storybook smaller than at Fit`() {
        // Given
        val viewport = Viewport(storybook, window)
        // When
        viewport.zoomOut()
        // Then
        viewport.zoom shouldBe 1.0
        viewport.shouldBeAt(0.0, 0.0)
    }

    @Test
    fun `ZoomTo should keep the Storybook on the screen when zoomed around a point near its edge`() {
        // Given
        val viewport = Viewport(storybook, window)
        // When
        viewport.zoomTo(2.0, around = Point(1280, 900))
        // Then
        viewport.shouldBeAt(-1280.0, -900.0)
    }

    @Test
    fun `ZoomTo should centre the Storybook on a side where it is smaller than the screen`() {
        // Given
        val viewport = Viewport(storybook, phoneScreen)
        // When
        viewport.zoomTo(2.0, around = Point(0, 0))
        // Then
        viewport.shouldBeAt(0.0, -450.0)
    }

    @Test
    fun `ZoomOnto should fill the screen with the phone in its middle when zoomed onto it on a portrait phone`() {
        // Given
        val viewport = Viewport(storybook, phoneScreen)
        // When
        viewport.zoomOnto(phoneFrame)
        // Then
        viewport.zoom shouldBe (1280.0 / 414.0 plusOrMinus 0.000001)
        viewport.shouldBeAt(-332.0 * viewport.zoom, 450.0 - 450.0 * viewport.zoom)
    }

    @Test
    fun `ZoomOnto should keep the Storybook on the screen when zoomed onto the phone on a desktop window`() {
        // Given
        val viewport = Viewport(storybook, window)
        // When
        viewport.zoomOnto(phoneFrame)
        // Then
        viewport.zoom shouldBe (900.0 / 868.0 plusOrMinus 0.000001)
        viewport.position.y shouldBe (450.0 - 450.0 * viewport.zoom plusOrMinus 0.001)
    }

    @Test
    fun `PanBy should move the Storybook when it is larger than the screen`() {
        // Given
        val viewport = Viewport(storybook, window)
        viewport.zoomTo(2.0)
        // When
        viewport.panBy(Point(100, -50))
        // Then
        viewport.shouldBeAt(-540.0, -500.0)
    }

    @Test
    fun `PanBy should stop at the edges of the Storybook`() {
        // Given
        val viewport = Viewport(storybook, window)
        viewport.zoomTo(2.0)
        // When
        viewport.panBy(Point(5000, -5000))
        // Then
        viewport.shouldBeAt(0.0, -900.0)
    }

    @Test
    fun `PanBy should not move the Storybook at Fit`() {
        // Given
        val viewport = Viewport(storybook, window)
        // When
        viewport.panBy(Point(100, 100))
        // Then
        viewport.shouldBeAt(0.0, 0.0)
    }

    @Test
    fun `Resize should stay at Fit when the screen changes at Fit`() {
        // Given
        val viewport = Viewport(storybook, window)
        // When
        viewport.resize(phoneScreen)
        // Then
        viewport.isFit.shouldBeTrue()
        viewport.screen shouldBe phoneScreen
        viewport.shouldBeAt(0.0, 0.0)
    }

    @Test
    fun `Resize should keep the zoom and the point in the middle of the screen when zoomed in`() {
        // Given
        val viewport = Viewport(storybook, phoneScreen)
        viewport.zoomOnto(phoneFrame)
        val zoom = viewport.zoom
        // When the phone is turned to landscape, 844x390: KorGE fits the 900 of the Storybook in its height.
        viewport.resize(Rectangle(-333.5, 0.0, 1947.0, 900.0))
        // Then
        viewport.zoom shouldBe zoom
        // The middle of the phone is still in the middle of the screen.
        (viewport.position.x + 539.0 * zoom) shouldBe (640.0 plusOrMinus 0.001)
        (viewport.position.y + 450.0 * zoom) shouldBe (450.0 plusOrMinus 0.001)
    }
}
