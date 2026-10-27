package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.BattleSceneResourcesLoaderMother.loadedResourcesLoader
import com.mkz.rpg.screen.adapters.presentation.FormationView.Row
import com.mkz.rpg.shared.adapters.presentation.sharedViewsTest
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import korlibs.korge.tests.ViewsForTesting
import kotlin.test.Test

class FormationViewTest : ViewsForTesting() {
    // Records what the player does on the formation.
    private class RecordingDelegate : FormationView.Delegate {
        val selectedRows = mutableListOf<Int>()
        var isCloseSelected = false

        override fun formationRowSelected(index: Int) {
            selectedRows += index
        }

        override fun formationCloseSelected() {
            isCloseSelected = true
        }
    }

    private val knight = Row(unitId = "knight", name = "Knight", remainingHealth = 20, maximumHealth = 30, remainingMana = 5, maximumMana = 10, isLeader = true)
    private val viking = Row(unitId = "viking", name = "Viking", remainingHealth = 40, maximumHealth = 40, remainingMana = 0, maximumMana = 0, isLeader = false)

    @Test
    fun `Init should stack the rows above the hint and Close`() =
        sharedViewsTest {
            // When
            val formationView = FormationView(loadedResourcesLoader(), TextsMother.texts())
            addChild(formationView)
            // Then
            formationView.isLaidOut().shouldBeTrue()
        }

    @Test
    fun `Display should display a row per party member in order, with its portrait, name, health and mana`() =
        sharedViewsTest {
            // Given
            val resourcesLoader = loadedResourcesLoader()
            val formationView = FormationView(resourcesLoader, TextsMother.texts())
            // When
            formationView.display(listOf(knight, viking))
            // Then
            formationView.names().shouldContainExactly("Knight", "Viking")
            formationView.portraitBitmaps().shouldContainExactly(
                resourcesLoader.bitmap("unit/knight_portrait.png"),
                resourcesLoader.bitmap("unit/viking_portrait.png"),
            )
            formationView.bars().shouldContainExactly("20 / 30" to "5 / 10", "40 / 40" to "0 / 0")
        }

    @Test
    fun `Display should crown the leader only`() =
        sharedViewsTest {
            // Given
            val resourcesLoader = loadedResourcesLoader()
            val formationView = FormationView(resourcesLoader, TextsMother.texts())
            // When
            formationView.display(listOf(knight, viking))
            // Then
            formationView.crownedRowIndexes().shouldContainExactly(0)
            formationView.crownBitmap() shouldBe resourcesLoader.bitmap(FormationView.CROWN)
        }

    @Test
    fun `Display should move the crown when another party member leads`() =
        sharedViewsTest {
            // Given
            val formationView = FormationView(loadedResourcesLoader(), TextsMother.texts())
            formationView.display(listOf(knight, viking))
            // When
            formationView.display(listOf(viking.copy(isLeader = true), knight.copy(isLeader = false)))
            // Then
            formationView.names().shouldContainExactly("Viking", "Knight")
            formationView.crownedRowIndexes().shouldContainExactly(0)
        }

    @Test
    fun `Display should hide the rows of a smaller party and clear the selection`() =
        sharedViewsTest {
            // Given
            val formationView = FormationView(loadedResourcesLoader(), TextsMother.texts())
            formationView.display(listOf(knight, viking))
            formationView.selectRow(1)
            // When
            formationView.display(listOf(knight))
            // Then
            formationView.names().shouldContainExactly("Knight")
            formationView.selectedRowIndexes().shouldBeEmpty()
        }

    @Test
    fun `SelectRow should select only the given row, or none`() =
        sharedViewsTest {
            // Given
            val formationView = FormationView(loadedResourcesLoader(), TextsMother.texts())
            formationView.display(listOf(knight, viking))
            // When
            formationView.selectRow(1)
            // Then
            formationView.selectedRowIndexes().shouldContainExactly(1)
            // When
            formationView.selectRow(null)
            // Then
            formationView.selectedRowIndexes().shouldBeEmpty()
        }

    @Test
    fun `DisplayHint should display the hint next to Close`() =
        sharedViewsTest {
            // Given
            val formationView = FormationView(loadedResourcesLoader(), TextsMother.texts())
            // When
            formationView.displayHint("Tap two members to swap them")
            // Then
            formationView.hint() shouldBe "Tap two members to swap them"
        }

    @Test
    fun `ClickRow should notify the delegate of the row tapped, also on its portrait`() =
        sharedViewsTest {
            // Given
            val formationView = FormationView(loadedResourcesLoader(), TextsMother.texts())
            val delegate = RecordingDelegate()
            formationView.setDelegate(delegate)
            formationView.display(listOf(knight, viking))
            addChild(formationView)
            // When
            clickRow(formationView, 1)
            clickPortrait(formationView, 0)
            // Then
            delegate.selectedRows.shouldContainExactly(1, 0)
        }

    @Test
    fun `ClickClose should notify the delegate`() =
        sharedViewsTest {
            // Given
            val formationView = FormationView(loadedResourcesLoader(), TextsMother.texts())
            val delegate = RecordingDelegate()
            formationView.setDelegate(delegate)
            formationView.display(listOf(knight))
            addChild(formationView)
            // When
            clickClose(formationView)
            // Then
            delegate.isCloseSelected.shouldBeTrue()
            delegate.selectedRows.shouldBeEmpty()
        }

    @Test
    fun `Init should display no crown before the party is displayed`() =
        sharedViewsTest {
            // When
            val formationView = FormationView(loadedResourcesLoader(), TextsMother.texts())
            // Then
            formationView.crownBitmap().shouldBeNull()
        }
}
