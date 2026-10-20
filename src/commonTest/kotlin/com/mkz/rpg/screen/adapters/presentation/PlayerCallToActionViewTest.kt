package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.BattleSceneResourcesLoaderMother.loadedResourcesLoader
import com.mkz.rpg.shared.adapters.presentation.sharedViewsTest
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import korlibs.korge.tests.ViewsForTesting
import kotlin.test.Test

class PlayerCallToActionViewTest : ViewsForTesting() {
    @Test
    fun `DisplayFinishTurn should display finish turn when finish turn is displayed`() =
        sharedViewsTest {
            // Given
            val playerCallToActionView = PlayerCallToActionView()
            // When
            playerCallToActionView.displayFinishTurn(onTurnFinished = {})
            // Then
            playerCallToActionView.isDisplayingFinishTurn().shouldBeTrue()
            playerCallToActionView.isDisplayingCancelAndConfirm().shouldBeFalse()
        }

    @Test
    fun `DisplayFinishTurn should finish turn when finish turn is clicked`() =
        sharedViewsTest {
            // Given
            var turnFinished = false
            val playerCallToActionView = PlayerCallToActionView()
            playerCallToActionView.displayFinishTurn(onTurnFinished = { turnFinished = true })
            addChild(playerCallToActionView)
            // When
            clickFinishTurn(playerCallToActionView)
            // Then
            turnFinished.shouldBeTrue()
        }

    @Test
    fun `SetFinishTurnEnabled should display the finish turn disabled when finish turn is disabled`() =
        sharedViewsTest {
            // Given
            val playerCallToActionView = PlayerCallToActionView()
            playerCallToActionView.displayFinishTurn(onTurnFinished = {})
            // When
            playerCallToActionView.setFinishTurnEnabled(false)
            // Then
            playerCallToActionView.isDisplayingFinishTurn().shouldBeTrue()
            playerCallToActionView.isFinishTurnEnabled().shouldBeFalse()
        }

    @Test
    fun `SetFinishTurnEnabled should display the finish turn disabled when it was disabled before being displayed`() =
        sharedViewsTest {
            // Given
            val playerCallToActionView = PlayerCallToActionView()
            playerCallToActionView.setFinishTurnEnabled(false)
            // When
            playerCallToActionView.displayFinishTurn(onTurnFinished = {})
            // Then
            playerCallToActionView.isFinishTurnEnabled().shouldBeFalse()
        }

    @Test
    fun `SetFinishTurnEnabled should display the finish turn enabled again when finish turn is enabled`() =
        sharedViewsTest {
            // Given
            val playerCallToActionView = PlayerCallToActionView()
            playerCallToActionView.displayFinishTurn(onTurnFinished = {})
            playerCallToActionView.setFinishTurnEnabled(false)
            // When
            playerCallToActionView.setFinishTurnEnabled(true)
            // Then
            playerCallToActionView.isFinishTurnEnabled().shouldBeTrue()
        }

    @Test
    fun `SetFinishTurnEnabled should display the finish turn disabled again when it is disabled and cancel is clicked`() =
        sharedViewsTest {
            // Given
            val playerCallToActionView = PlayerCallToActionView()
            playerCallToActionView.displayFinishTurn(onTurnFinished = {})
            playerCallToActionView.displayCancelAndConfirm(onCancelled = {}, onConfirmed = {})
            playerCallToActionView.setFinishTurnEnabled(false)
            addChild(playerCallToActionView)
            // When
            clickCancel(playerCallToActionView)
            // Then
            playerCallToActionView.isDisplayingFinishTurn().shouldBeTrue()
            playerCallToActionView.isFinishTurnEnabled().shouldBeFalse()
        }

    @Test
    fun `DisplayCancelAndConfirm should replace finish turn with cancel and confirm when cancel and confirm is displayed`() =
        sharedViewsTest {
            // Given
            val playerCallToActionView = PlayerCallToActionView()
            playerCallToActionView.displayFinishTurn(onTurnFinished = {})
            // When
            playerCallToActionView.displayCancelAndConfirm(onCancelled = {}, onConfirmed = {})
            // Then
            playerCallToActionView.isDisplayingCancelAndConfirm().shouldBeTrue()
            playerCallToActionView.isDisplayingFinishTurn().shouldBeFalse()
        }

    @Test
    fun `DisplayCancelAndConfirm should cancel and display finish turn again when cancel is clicked`() =
        sharedViewsTest {
            // Given
            var cancelled = false
            var confirmed = false
            val playerCallToActionView = PlayerCallToActionView()
            playerCallToActionView.displayFinishTurn(onTurnFinished = {})
            playerCallToActionView.displayCancelAndConfirm(onCancelled = { cancelled = true }, onConfirmed = { confirmed = true })
            addChild(playerCallToActionView)
            // When
            clickCancel(playerCallToActionView)
            // Then
            cancelled.shouldBeTrue()
            confirmed.shouldBeFalse()
            playerCallToActionView.isDisplayingFinishTurn().shouldBeTrue()
        }

    @Test
    fun `DisplayCancelAndConfirm should confirm and display finish turn again when confirm is clicked`() =
        sharedViewsTest {
            // Given
            var cancelled = false
            var confirmed = false
            val playerCallToActionView = PlayerCallToActionView()
            playerCallToActionView.displayFinishTurn(onTurnFinished = {})
            playerCallToActionView.displayCancelAndConfirm(onCancelled = { cancelled = true }, onConfirmed = { confirmed = true })
            addChild(playerCallToActionView)
            // When
            clickConfirm(playerCallToActionView)
            // Then
            confirmed.shouldBeTrue()
            cancelled.shouldBeFalse()
            playerCallToActionView.isDisplayingFinishTurn().shouldBeTrue()
        }

    @Test
    fun `Hide should not display any call to action when is hidden`() =
        sharedViewsTest {
            // Given
            val playerCallToActionView = PlayerCallToActionView()
            playerCallToActionView.displayFinishTurn(onTurnFinished = {})
            // When
            playerCallToActionView.hide()
            // Then
            playerCallToActionView.isDisplayingFinishTurn().shouldBeFalse()
            playerCallToActionView.isDisplayingCancelAndConfirm().shouldBeFalse()
        }

    @Test
    fun `DisplayItemActions should display the item name, its price and its actions when the item actions are displayed`() =
        sharedViewsTest {
            // Given
            val playerCallToActionView = PlayerCallToActionView()
            playerCallToActionView.displayFinishTurn(onTurnFinished = {})
            val actions =
                listOf(
                    ItemActionsView.Action(label = "Equip") {},
                    ItemActionsView.Action(label = "Discard", isEnabled = false) {},
                    ItemActionsView.Action(label = "Cancel") {},
                )
            // When
            playerCallToActionView.displayItemActions(itemName = "Hammer", itemPrice = 50, actions = actions)
            // Then
            playerCallToActionView.isDisplayingItemActions().shouldBeTrue()
            playerCallToActionView.itemName() shouldBe "Hammer"
            playerCallToActionView.itemPrice() shouldBe "50 G"
            playerCallToActionView.itemActionLabels().shouldContainExactly("Equip", "Discard", "Cancel")
            playerCallToActionView.enabledItemActionLabels().shouldContainExactly("Equip", "Cancel")
        }

    @Test
    fun `DisplayItemActions should display only the item name and its price when the item has no actions`() =
        sharedViewsTest {
            // Given
            val playerCallToActionView = PlayerCallToActionView()
            playerCallToActionView.displayFinishTurn(onTurnFinished = {})
            // When
            playerCallToActionView.displayItemActions(itemName = "Hammer", itemPrice = 50, actions = emptyList())
            // Then
            playerCallToActionView.isDisplayingItemActions().shouldBeTrue()
            playerCallToActionView.itemName() shouldBe "Hammer"
            playerCallToActionView.itemPrice() shouldBe "50 G"
            playerCallToActionView.itemActionLabels().shouldBeEmpty()
        }

    @Test
    fun `DisplayItemActions should run the action and keep the item actions when they do not redisplay the finish turn`() =
        sharedViewsTest {
            // Given
            var bought = false
            val playerCallToActionView = PlayerCallToActionView()
            playerCallToActionView.displayFinishTurn(onTurnFinished = {})
            playerCallToActionView.displayItemActions(
                itemName = "Hammer",
                itemPrice = 50,
                actions = listOf(ItemActionsView.Action(label = "Buy") { bought = true }),
                redisplaysFinishTurn = false,
            )
            addChild(playerCallToActionView)
            // When
            clickItemAction(playerCallToActionView, "Buy")
            // Then
            bought.shouldBeTrue()
            playerCallToActionView.isDisplayingItemActions().shouldBeTrue()
        }

    @Test
    fun `DisplayItemActions should run the action and display the finish turn again when an item action is clicked`() =
        sharedViewsTest {
            // Given
            var consumed = false
            val playerCallToActionView = PlayerCallToActionView()
            playerCallToActionView.displayFinishTurn(onTurnFinished = {})
            playerCallToActionView.displayItemActions(itemName = "Health Potion", itemPrice = 10, actions = listOf(ItemActionsView.Action(label = "Consume") { consumed = true }))
            addChild(playerCallToActionView)
            // When
            clickItemAction(playerCallToActionView, "Consume")
            // Then
            consumed.shouldBeTrue()
            playerCallToActionView.isDisplayingFinishTurn().shouldBeTrue()
        }

    @Test
    fun `DisplayInventoryFull should display the found item with its price and leave it when leave it is clicked`() =
        sharedViewsTest {
            // Given
            var left = false
            val playerCallToActionView = PlayerCallToActionView(loadedResourcesLoader())
            addChild(playerCallToActionView)
            playerCallToActionView.displayInventoryFull(foundItemName = "Health Potion", foundItemPrice = 10, foundItemId = "health-potion", onLeft = { left = true })
            // When
            clickLeaveIt(playerCallToActionView)
            // Then
            playerCallToActionView.isDisplayingInventoryFull().shouldBeTrue()
            playerCallToActionView.inventoryFullMessage() shouldBe "Tap an item to swap it for the Health Potion (10 G)"
            playerCallToActionView.isFoundItemIconDisplayed().shouldBeTrue()
            left.shouldBeTrue()
        }

    @Test
    fun `RedisplayFinishTurn should display the finish turn again when the item actions were displayed`() =
        sharedViewsTest {
            // Given
            val playerCallToActionView = PlayerCallToActionView()
            playerCallToActionView.displayFinishTurn(onTurnFinished = {})
            playerCallToActionView.displayItemActions(itemName = "Hammer", itemPrice = 50, actions = listOf(ItemActionsView.Action(label = "Cancel") {}))
            // When
            playerCallToActionView.redisplayFinishTurn()
            // Then
            playerCallToActionView.isDisplayingFinishTurn().shouldBeTrue()
        }
}
