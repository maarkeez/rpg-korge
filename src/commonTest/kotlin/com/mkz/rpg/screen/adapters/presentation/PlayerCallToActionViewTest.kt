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
            val playerCallToActionView = PlayerCallToActionView(TextsMother.texts())
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
            val playerCallToActionView = PlayerCallToActionView(TextsMother.texts())
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
            val playerCallToActionView = PlayerCallToActionView(TextsMother.texts())
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
            val playerCallToActionView = PlayerCallToActionView(TextsMother.texts())
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
            val playerCallToActionView = PlayerCallToActionView(TextsMother.texts())
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
            val playerCallToActionView = PlayerCallToActionView(TextsMother.texts())
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
            val playerCallToActionView = PlayerCallToActionView(TextsMother.texts())
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
            val playerCallToActionView = PlayerCallToActionView(TextsMother.texts())
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
            val playerCallToActionView = PlayerCallToActionView(TextsMother.texts())
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
            val playerCallToActionView = PlayerCallToActionView(TextsMother.texts())
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
            val playerCallToActionView = PlayerCallToActionView(TextsMother.texts())
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
            playerCallToActionView.fadedItemActionLabels().shouldContainExactly("Discard")
        }

    @Test
    fun `DisplayItemActions should display only the item name and its price when the item has no actions`() =
        sharedViewsTest {
            // Given
            val playerCallToActionView = PlayerCallToActionView(TextsMother.texts())
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
    fun `DisplayItemActions should run the action and display the finish turn again when an item action is clicked`() =
        sharedViewsTest {
            // Given
            var consumed = false
            val playerCallToActionView = PlayerCallToActionView(TextsMother.texts())
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
    fun `DisplayItemActions should keep what the action displayed instead when an item action is clicked, e g give`() =
        sharedViewsTest {
            // Given
            val playerCallToActionView = PlayerCallToActionView(TextsMother.texts())
            playerCallToActionView.displayFinishTurn(onTurnFinished = {})
            val give = ItemActionsView.Action(label = "Give") { playerCallToActionView.displayGiveItem(itemName = "Hammer", onCancelled = {}) }
            playerCallToActionView.displayItemActions(itemName = "Hammer", itemPrice = 50, actions = listOf(give))
            addChild(playerCallToActionView)
            // When
            clickItemAction(playerCallToActionView, "Give")
            // Then
            playerCallToActionView.isDisplayingGiveItem().shouldBeTrue()
        }

    @Test
    fun `DisplayGiveItem should ask to tap a party member, and cancel when cancel is clicked`() =
        sharedViewsTest {
            // Given
            var cancelled = false
            val playerCallToActionView = PlayerCallToActionView(TextsMother.texts())
            addChild(playerCallToActionView)
            playerCallToActionView.displayGiveItem(itemName = "Hammer", onCancelled = { cancelled = true })
            // When
            clickCancelGiving(playerCallToActionView)
            // Then
            playerCallToActionView.giveItemMessage() shouldBe "Tap a party member to give: Hammer"
            cancelled.shouldBeTrue()
        }

    @Test
    fun `DisplayInventoryFull should display the found item with its price and leave it when leave it is clicked`() =
        sharedViewsTest {
            // Given
            var left = false
            val playerCallToActionView = PlayerCallToActionView(TextsMother.texts(), loadedResourcesLoader())
            addChild(playerCallToActionView)
            playerCallToActionView.displayInventoryFull(foundItemName = "Health Potion", foundItemPrice = 10, foundItemId = "health-potion", onLeft = { left = true })
            // When
            clickLeaveIt(playerCallToActionView)
            // Then
            playerCallToActionView.isDisplayingInventoryFull().shouldBeTrue()
            playerCallToActionView.inventoryFullMessage() shouldBe "Tap an item to swap it for: Health Potion (10 G)"
            playerCallToActionView.isFoundItemIconDisplayed().shouldBeTrue()
            left.shouldBeTrue()
        }

    @Test
    fun `Dismiss should display the finish turn again when the item actions were displayed`() =
        sharedViewsTest {
            // Given
            val playerCallToActionView = PlayerCallToActionView(TextsMother.texts())
            playerCallToActionView.displayFinishTurn(onTurnFinished = {})
            playerCallToActionView.displayItemActions(itemName = "Hammer", itemPrice = 50, actions = listOf(ItemActionsView.Action(label = "Cancel") {}))
            // When
            playerCallToActionView.dismiss()
            // Then
            playerCallToActionView.isDisplayingFinishTurn().shouldBeTrue()
        }

    @Test
    fun `Dismiss should display nothing when the item actions were displayed and the finish turn was never displayed`() =
        sharedViewsTest {
            // Given
            val playerCallToActionView = PlayerCallToActionView(TextsMother.texts())
            playerCallToActionView.displayItemActions(itemName = "Hammer", itemPrice = 50, actions = listOf(ItemActionsView.Action(label = "Cancel") {}))
            // When
            playerCallToActionView.dismiss()
            // Then
            playerCallToActionView.isDisplayingNothing().shouldBeTrue()
        }

    @Test
    fun `HideFinishTurn should display nothing when the finish turn is displayed`() =
        sharedViewsTest {
            // Given
            val playerCallToActionView = PlayerCallToActionView(TextsMother.texts())
            playerCallToActionView.displayFinishTurn(onTurnFinished = {})
            // When
            playerCallToActionView.hideFinishTurn()
            // Then
            playerCallToActionView.isDisplayingNothing().shouldBeTrue()
        }

    @Test
    fun `HideFinishTurn should keep the item actions displayed when the item actions are displayed`() =
        sharedViewsTest {
            // Given
            val playerCallToActionView = PlayerCallToActionView(TextsMother.texts())
            playerCallToActionView.displayFinishTurn(onTurnFinished = {})
            playerCallToActionView.displayItemActions(itemName = "Hammer", itemPrice = 50, actions = listOf(ItemActionsView.Action(label = "Cancel") {}))
            // When
            playerCallToActionView.hideFinishTurn()
            // Then
            playerCallToActionView.isDisplayingItemActions().shouldBeTrue()
        }

    @Test
    fun `HideFinishTurn should display nothing once an item action is clicked when the finish turn is hidden`() =
        sharedViewsTest {
            // Given
            val playerCallToActionView = PlayerCallToActionView(TextsMother.texts())
            playerCallToActionView.displayFinishTurn(onTurnFinished = {})
            playerCallToActionView.hideFinishTurn()
            playerCallToActionView.displayItemActions(itemName = "Health Potion", itemPrice = 10, actions = listOf(ItemActionsView.Action(label = "Consume") {}))
            addChild(playerCallToActionView)
            // When
            clickItemAction(playerCallToActionView, "Consume")
            // Then
            playerCallToActionView.isDisplayingNothing().shouldBeTrue()
        }

    @Test
    fun `DisplayGiveGold should display Cancel, All and Give with the amount, and UpdateGiveGold write it again`() =
        sharedViewsTest {
            // Given
            val playerCallToActionView = PlayerCallToActionView(TextsMother.texts())
            // When
            playerCallToActionView.displayGiveGold(amount = 1, maximum = 120, onCancelled = {}, onAll = {}, onGiven = {})
            // Then
            playerCallToActionView.isDisplayingGiveGold().shouldBeTrue()
            playerCallToActionView.giveGoldLabel() shouldBe "Give 1 G"
            // When
            playerCallToActionView.updateGiveGold(amount = 120, maximum = 120)
            // Then
            playerCallToActionView.giveGoldLabel() shouldBe "Give 120 G"
            playerCallToActionView.isGiveAllEnabled().shouldBeFalse()
        }

    @Test
    fun `DisplayGiveGoldRefusal should display why, and OK should dismiss it`() =
        sharedViewsTest {
            // Given
            var isDismissed = false
            val playerCallToActionView = PlayerCallToActionView(TextsMother.texts())
            addChild(playerCallToActionView)
            // When
            playerCallToActionView.displayGiveGoldRefusal("No gold to give", onDismissed = { isDismissed = true })
            // Then
            playerCallToActionView.isDisplayingGiveGoldRefusal().shouldBeTrue()
            playerCallToActionView.giveGoldRefusalMessage() shouldBe "No gold to give"
            // When
            clickGiveGoldRefusalOk(playerCallToActionView)
            // Then
            isDismissed.shouldBeTrue()
            playerCallToActionView.isDisplayingNothing().shouldBeTrue()
        }

    private val backAction = ActionRowView.Action("Back") {}
    private val abandonAction = ActionRowView.Action("Abandon") {}

    @Test
    fun `DisplayActions should display every action, sharing the width of Finish turn, when they are displayed`() =
        sharedViewsTest {
            // Given
            val playerCallToActionView = PlayerCallToActionView(TextsMother.texts())
            addChild(playerCallToActionView)
            // When
            playerCallToActionView.displayActions(listOf(backAction, abandonAction))
            // Then
            playerCallToActionView.isDisplayingActions().shouldBeTrue()
            playerCallToActionView.actionLabels().shouldContainExactly("Back", "Abandon")
            playerCallToActionView.width shouldBe FinishTurnView(TextsMother.texts()) {}.width
        }

    @Test
    fun `DisplayActions should keep the row displayed when an action is clicked`() =
        sharedViewsTest {
            // Given
            var isSelected = false
            val playerCallToActionView = PlayerCallToActionView(TextsMother.texts())
            addChild(playerCallToActionView)
            playerCallToActionView.displayActions(listOf(ActionRowView.Action("Back") { isSelected = true }))
            // When
            clickAction(playerCallToActionView, "Back")
            // Then
            isSelected.shouldBeTrue()
            playerCallToActionView.isDisplayingActions().shouldBeTrue()
        }

    @Test
    fun `DisplayActions should keep the next row tappable when an action displays it, as buying displays the row again`() =
        sharedViewsTest {
            // Given
            var buys = 0
            val playerCallToActionView = PlayerCallToActionView(TextsMother.texts())
            addChild(playerCallToActionView)

            fun displayBuy() {
                playerCallToActionView.displayActions(
                    listOf(
                        ActionRowView.Action("Buy") {
                            buys++
                            displayBuy()
                        },
                    ),
                )
            }
            displayBuy()
            // When
            clickAction(playerCallToActionView, "Buy")
            clickAction(playerCallToActionView, "Buy")
            // Then
            buys shouldBe 2
        }

    @Test
    fun `DisplayActions should make the view visible when it is hidden`() =
        sharedViewsTest {
            // Given
            val playerCallToActionView = PlayerCallToActionView(TextsMother.texts())
            playerCallToActionView.visible = false
            // When
            playerCallToActionView.displayActions(listOf(backAction))
            // Then
            playerCallToActionView.visible.shouldBeTrue()
        }

    @Test
    fun `DisplayConfirmation should draw the confirm action in danger`() =
        sharedViewsTest {
            // Given
            val playerCallToActionView = PlayerCallToActionView(TextsMother.texts())
            // When
            playerCallToActionView.displayConfirmation(cancelLabel = "Cancel", confirmLabel = "Quit to menu", onCancelled = {}, onConfirmed = {})
            // Then
            playerCallToActionView.actionLabels().shouldContainExactly("Cancel", "Quit to menu")
            playerCallToActionView.actionColor("Quit to menu") shouldBe HudStyle.DANGER
        }

    @Test
    fun `DisplayConfirmation should call the action of the choice tapped`() =
        sharedViewsTest {
            // Given
            val choices = mutableListOf<String>()
            val playerCallToActionView = PlayerCallToActionView(TextsMother.texts())
            addChild(playerCallToActionView)
            playerCallToActionView.displayConfirmation(cancelLabel = "Cancel", confirmLabel = "Quit to menu", onCancelled = { choices += "cancel" }, onConfirmed = { choices += "confirm" })
            // When
            clickCancelConfirmation(playerCallToActionView)
            clickConfirmation(playerCallToActionView)
            // Then
            choices.shouldContainExactly("cancel", "confirm")
        }

    @Test
    fun `HideActions should display the finish turn again when the battle has one`() =
        sharedViewsTest {
            // Given
            val playerCallToActionView = PlayerCallToActionView(TextsMother.texts())
            playerCallToActionView.displayFinishTurn(onTurnFinished = {})
            playerCallToActionView.displayActions(listOf(backAction))
            // When
            playerCallToActionView.hideActions()
            // Then
            playerCallToActionView.isDisplayingFinishTurn().shouldBeTrue()
        }

    @Test
    fun `HideActions should display nothing when no battle is on`() =
        sharedViewsTest {
            // Given
            val playerCallToActionView = PlayerCallToActionView(TextsMother.texts())
            playerCallToActionView.displayActions(listOf(backAction))
            // When
            playerCallToActionView.hideActions()
            // Then
            playerCallToActionView.isDisplayingNothing().shouldBeTrue()
        }

    @Test
    fun `HideActions should keep the inventory full displayed when no row is displayed`() =
        sharedViewsTest {
            // Given
            val playerCallToActionView = PlayerCallToActionView(TextsMother.texts(), loadedResourcesLoader())
            playerCallToActionView.displayInventoryFull(foundItemName = "Health potion", foundItemPrice = 25, foundItemId = "health-potion", onLeft = {})
            // When
            playerCallToActionView.hideActions()
            // Then
            playerCallToActionView.isDisplayingInventoryFull().shouldBeTrue()
        }
}
