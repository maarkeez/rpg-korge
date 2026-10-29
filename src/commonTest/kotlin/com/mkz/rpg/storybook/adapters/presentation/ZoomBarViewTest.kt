package com.mkz.rpg.storybook.adapters.presentation

import com.mkz.rpg.shared.adapters.presentation.sharedViewsTest
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import korlibs.korge.tests.ViewsForTesting
import kotlin.test.Test

class ZoomBarViewTest : ViewsForTesting() {
    private val tapped = mutableListOf<String>()

    private fun newZoomBarView() =
        ZoomBarView().apply {
            setDelegate(
                object : ZoomBarView.Delegate {
                    override fun zoomOutTapped() {
                        tapped += "zoomOut"
                    }

                    override fun zoomInTapped() {
                        tapped += "zoomIn"
                    }

                    override fun fitTapped() {
                        tapped += "fit"
                    }

                    override fun phoneTapped() {
                        tapped += "phone"
                    }
                },
            )
        }

    @Test
    fun `ZoomBarView should show the zoom at Fit when it is displayed`() =
        sharedViewsTest {
            // When
            val zoomBarView = newZoomBarView()
            // Then
            zoomBarView.zoomText() shouldBe "100%"
        }

    @Test
    fun `Display should show the zoom as a percentage, rounded`() =
        sharedViewsTest {
            // Given
            val zoomBarView = newZoomBarView()
            // When
            zoomBarView.display(zoom = 1.0369)
            // Then
            zoomBarView.zoomText() shouldBe "104%"
        }

    @Test
    fun `ZoomBarView should tell what was tapped when its buttons are tapped`() =
        sharedViewsTest {
            // Given
            val zoomBarView = newZoomBarView()
            addChild(zoomBarView)
            // When
            clickZoomIn(zoomBarView)
            clickZoomOut(zoomBarView)
            clickFit(zoomBarView)
            clickPhone(zoomBarView)
            // Then
            tapped.shouldContainExactly("zoomIn", "zoomOut", "fit", "phone")
        }
}
