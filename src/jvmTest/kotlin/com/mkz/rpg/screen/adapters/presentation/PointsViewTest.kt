package com.mkz.rpg.screen.adapters.presentation

import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import korlibs.image.color.Colors
import korlibs.korge.tests.ViewsForTesting
import kotlin.test.Test

class PointsViewTest : ViewsForTesting() {
    @Test
    fun `Display should display every point filled when none has been spent`() =
        viewsTest {
            // Given
            val pointsView = PointsView(Colors.WHITE)
            // When
            pointsView.display(remaining = 3, maximum = 3)
            // Then
            pointsView.filledPoints() shouldBe 3
            pointsView.hollowPoints() shouldBe 0
        }

    @Test
    fun `Display should display the spent points hollow when some have been spent`() =
        viewsTest {
            // Given
            val pointsView = PointsView(Colors.WHITE)
            // When
            pointsView.display(remaining = 1, maximum = 3)
            // Then
            pointsView.filledPoints() shouldBe 1
            pointsView.hollowPoints() shouldBe 2
        }

    @Test
    fun `Display should display every point hollow when all have been spent`() =
        viewsTest {
            // Given
            val pointsView = PointsView(Colors.WHITE)
            // When
            pointsView.display(remaining = 0, maximum = 1)
            // Then
            pointsView.filledPoints() shouldBe 0
            pointsView.hollowPoints() shouldBe 1
        }

    @Test
    fun `Display should draw every point with its colour when points are displayed`() =
        viewsTest {
            // Given
            val pointsView = PointsView(BattleUnitInfoView.CAST_POINTS_COLOR)
            // When
            pointsView.display(remaining = 1, maximum = 2)
            // Then
            pointsView.pointColors().shouldContainExactly(BattleUnitInfoView.CAST_POINTS_COLOR)
        }

    @Test
    fun `Display should display hollow points as big as filled points when some have been spent`() =
        viewsTest {
            // Given
            val pointsView = PointsView(Colors.WHITE)
            addChild(pointsView)
            // When
            pointsView.display(remaining = 1, maximum = 2)
            // Then
            pointsView.pointSizes().shouldContainExactly(PointsView.POINT_RADIUS * 2 to PointsView.POINT_RADIUS * 2)
        }

    @Test
    fun `Display should replace the previous points when points are displayed again`() =
        viewsTest {
            // Given
            val pointsView = PointsView(Colors.WHITE)
            pointsView.display(remaining = 5, maximum = 5)
            // When
            pointsView.display(remaining = 1, maximum = 2)
            // Then
            pointsView.filledPoints() shouldBe 1
            pointsView.hollowPoints() shouldBe 1
        }
}
