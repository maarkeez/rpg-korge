package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.battleUnit.domain.BattleUnitMother.battleUnit
import com.mkz.rpg.screen.adapters.presentation.BattleSceneResourcesLoaderMother.loadedResourcesLoader
import com.mkz.rpg.shared.adapters.presentation.sharedViewsTest
import com.mkz.rpg.unit.domain.UnitMother.unit
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import korlibs.korge.tests.ViewsForTesting
import kotlin.test.Test

class AttackPreviewViewTest : ViewsForTesting() {
    private val casterBattleUnit = battleUnit(unit = unit(id = "knight").toDto()).toDto()
    private val casterUnit = unit(id = "knight").toDto()

    private fun receivers(count: Int): List<AttackPreviewView.Receiver> =
        List(count) {
            AttackPreviewView.Receiver(
                battleUnit = battleUnit(unit = unit(id = "rat").toDto()).toDto(),
                unit = unit(id = "rat").toDto(),
                damage = 10,
            )
        }

    private fun AttackPreviewView.display(receivers: List<AttackPreviewView.Receiver>) = display(casterBattleUnit = casterBattleUnit, casterUnit = casterUnit, manaCost = 10, receivers = receivers)

    @Test
    fun `Display should be visible when is displayed`() =
        sharedViewsTest {
            // Given
            val attackPreviewView = AttackPreviewView(loadedResourcesLoader())
            // When
            attackPreviewView.display(receivers = emptyList())
            // Then
            attackPreviewView.isVisibleToUser().shouldBeTrue()
        }

    @Test
    fun `Display should not display receivers when no receiver is provided`() =
        sharedViewsTest {
            // Given
            val attackPreviewView = AttackPreviewView(loadedResourcesLoader())
            // When
            attackPreviewView.display(receivers = emptyList())
            // Then
            attackPreviewView.isCasterPortraitVisible().shouldBeTrue()
            attackPreviewView.receiversCount() shouldBe 0
        }

    @Test
    fun `Display should display the receiver without counter when one receiver is provided`() =
        sharedViewsTest {
            // Given
            val attackPreviewView = AttackPreviewView(loadedResourcesLoader())
            // When
            attackPreviewView.display(receivers = receivers(1))
            // Then
            attackPreviewView.isCasterPortraitVisible().shouldBeTrue()
            attackPreviewView.receiversCount() shouldBe 1
            attackPreviewView.isReceiverPortraitVisible(0).shouldBeTrue()
            attackPreviewView.receiverCounter(0).shouldBeNull()
        }

    @Test
    fun `Display should display every receiver with its counter when several receivers are provided`() =
        sharedViewsTest {
            // Given
            val attackPreviewView = AttackPreviewView(loadedResourcesLoader())
            // When
            attackPreviewView.display(receivers = receivers(3))
            // Then
            attackPreviewView.receiversCount() shouldBe 3
            attackPreviewView.receiverCounter(0) shouldBe "1/3"
            attackPreviewView.receiverCounter(1) shouldBe "2/3"
            attackPreviewView.receiverCounter(2) shouldBe "3/3"
        }

    @Test
    fun `Display should hide the previous receivers when fewer receivers are provided`() =
        sharedViewsTest {
            // Given
            val attackPreviewView = AttackPreviewView(loadedResourcesLoader())
            attackPreviewView.display(receivers = receivers(3))
            // When
            attackPreviewView.display(receivers = receivers(1))
            // Then
            attackPreviewView.receiversCount() shouldBe 1
            attackPreviewView.isReceiverPortraitVisible(1).shouldBeFalse()
        }

    @Test
    fun `Drag should scroll to the next receiver when several receivers are displayed`() =
        sharedViewsTest {
            // Given
            val attackPreviewView = AttackPreviewView(loadedResourcesLoader())
            addChild(attackPreviewView)
            attackPreviewView.display(receivers = receivers(3))
            // When
            dragVertically(attackPreviewView, dy = -100.0)
            // Then
            attackPreviewView.firstVisibleRow() shouldBe 1.0
        }

    @Test
    fun `Drag should stop on the last receiver when dragged beyond it`() =
        sharedViewsTest {
            // Given
            val attackPreviewView = AttackPreviewView(loadedResourcesLoader())
            addChild(attackPreviewView)
            attackPreviewView.display(receivers = receivers(3))
            // When
            dragVertically(attackPreviewView, dy = -1000.0)
            // Then
            attackPreviewView.firstVisibleRow() shouldBe 2.0
        }

    @Test
    fun `Drag should stop on the closest row when dragged less than half a row`() =
        sharedViewsTest {
            // Given
            val attackPreviewView = AttackPreviewView(loadedResourcesLoader())
            addChild(attackPreviewView)
            attackPreviewView.display(receivers = receivers(3))
            // When
            dragVertically(attackPreviewView, dy = -40.0)
            // Then
            attackPreviewView.firstVisibleRow() shouldBe 0.0
        }

    @Test
    fun `Drag should not scroll when one receiver is displayed`() =
        sharedViewsTest {
            // Given
            val attackPreviewView = AttackPreviewView(loadedResourcesLoader())
            addChild(attackPreviewView)
            attackPreviewView.display(receivers = receivers(1))
            // When
            dragVertically(attackPreviewView, dy = -100.0)
            // Then
            attackPreviewView.firstVisibleRow() shouldBe 0.0
        }

    @Test
    fun `Display should show the caster again when it was scrolled out of view`() =
        sharedViewsTest {
            // Given
            val attackPreviewView = AttackPreviewView(loadedResourcesLoader())
            addChild(attackPreviewView)
            attackPreviewView.display(receivers = receivers(3))
            dragVertically(attackPreviewView, dy = -100.0)
            // When
            attackPreviewView.display(receivers = receivers(3))
            // Then
            attackPreviewView.firstVisibleRow() shouldBe 0.0
        }

    @Test
    fun `Hide should not be visible when is hidden`() =
        sharedViewsTest {
            // Given
            val attackPreviewView = AttackPreviewView(loadedResourcesLoader())
            attackPreviewView.display(receivers = emptyList())
            // When
            attackPreviewView.hide()
            // Then
            attackPreviewView.isVisibleToUser().shouldBeFalse()
        }
}
