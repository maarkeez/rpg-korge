package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.GameSceneResourcesLoaderMother.loadedResourcesLoader
import com.mkz.rpg.shared.adapters.presentation.GameButton
import com.mkz.rpg.shared.adapters.presentation.sharedViewsTest
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import korlibs.korge.tests.ViewsForTesting
import kotlin.test.Test

class CallToActionViewTest : ViewsForTesting() {
    @Test
    fun `DisplayFinishPlayerTurn should display finish turn when finish turn is displayed`() =
        sharedViewsTest {
            // Given
            val callToActionView = CallToActionView(MessagesMother.messages())
            // When
            callToActionView.displayFinishPlayerTurn(onPlayerTurnFinished = {})
            // Then
            callToActionView.isDisplayingFinishPlayerTurn().shouldBeTrue()
            callToActionView.isDisplayingCancelAndConfirm().shouldBeFalse()
        }

    @Test
    fun `DisplayFinishPlayerTurn should finish turn when finish turn is clicked`() =
        sharedViewsTest {
            // Given
            var turnFinished = false
            val callToActionView = CallToActionView(MessagesMother.messages())
            callToActionView.displayFinishPlayerTurn(onPlayerTurnFinished = { turnFinished = true })
            addChild(callToActionView)
            // When
            clickFinishPlayerTurn(callToActionView)
            // Then
            turnFinished.shouldBeTrue()
        }

    @Test
    fun `SetFinishPlayerTurnEnabled should display the finish turn disabled when finish turn is disabled`() =
        sharedViewsTest {
            // Given
            val callToActionView = CallToActionView(MessagesMother.messages())
            callToActionView.displayFinishPlayerTurn(onPlayerTurnFinished = {})
            // When
            callToActionView.setFinishPlayerTurnEnabled(false)
            // Then
            callToActionView.isDisplayingFinishPlayerTurn().shouldBeTrue()
            callToActionView.isFinishPlayerTurnEnabled().shouldBeFalse()
        }

    @Test
    fun `SetFinishPlayerTurnEnabled should display the finish turn disabled when it was disabled before being displayed`() =
        sharedViewsTest {
            // Given
            val callToActionView = CallToActionView(MessagesMother.messages())
            callToActionView.setFinishPlayerTurnEnabled(false)
            // When
            callToActionView.displayFinishPlayerTurn(onPlayerTurnFinished = {})
            // Then
            callToActionView.isFinishPlayerTurnEnabled().shouldBeFalse()
        }

    @Test
    fun `SetFinishPlayerTurnEnabled should display the finish turn enabled again when finish turn is enabled`() =
        sharedViewsTest {
            // Given
            val callToActionView = CallToActionView(MessagesMother.messages())
            callToActionView.displayFinishPlayerTurn(onPlayerTurnFinished = {})
            callToActionView.setFinishPlayerTurnEnabled(false)
            // When
            callToActionView.setFinishPlayerTurnEnabled(true)
            // Then
            callToActionView.isFinishPlayerTurnEnabled().shouldBeTrue()
        }

    @Test
    fun `SetFinishPlayerTurnEnabled should display the finish turn disabled again when it is disabled and cancel is clicked`() =
        sharedViewsTest {
            // Given
            val callToActionView = CallToActionView(MessagesMother.messages())
            callToActionView.displayFinishPlayerTurn(onPlayerTurnFinished = {})
            callToActionView.displayCancelAndConfirm(onCancelled = {}, onConfirmed = {})
            callToActionView.setFinishPlayerTurnEnabled(false)
            addChild(callToActionView)
            // When
            clickCancel(callToActionView)
            // Then
            callToActionView.isDisplayingFinishPlayerTurn().shouldBeTrue()
            callToActionView.isFinishPlayerTurnEnabled().shouldBeFalse()
        }

    @Test
    fun `DisplayCancelAndConfirm should replace finish turn with cancel and confirm when cancel and confirm is displayed`() =
        sharedViewsTest {
            // Given
            val callToActionView = CallToActionView(MessagesMother.messages())
            callToActionView.displayFinishPlayerTurn(onPlayerTurnFinished = {})
            // When
            callToActionView.displayCancelAndConfirm(onCancelled = {}, onConfirmed = {})
            // Then
            callToActionView.isDisplayingCancelAndConfirm().shouldBeTrue()
            callToActionView.isDisplayingFinishPlayerTurn().shouldBeFalse()
        }

    @Test
    fun `DisplayCancelAndConfirm should cancel and display finish turn again when cancel is clicked`() =
        sharedViewsTest {
            // Given
            var cancelled = false
            var confirmed = false
            val callToActionView = CallToActionView(MessagesMother.messages())
            callToActionView.displayFinishPlayerTurn(onPlayerTurnFinished = {})
            callToActionView.displayCancelAndConfirm(onCancelled = { cancelled = true }, onConfirmed = { confirmed = true })
            addChild(callToActionView)
            // When
            clickCancel(callToActionView)
            // Then
            cancelled.shouldBeTrue()
            confirmed.shouldBeFalse()
            callToActionView.isDisplayingFinishPlayerTurn().shouldBeTrue()
        }

    @Test
    fun `DisplayCancelAndConfirm should confirm and display finish turn again when confirm is clicked`() =
        sharedViewsTest {
            // Given
            var cancelled = false
            var confirmed = false
            val callToActionView = CallToActionView(MessagesMother.messages())
            callToActionView.displayFinishPlayerTurn(onPlayerTurnFinished = {})
            callToActionView.displayCancelAndConfirm(onCancelled = { cancelled = true }, onConfirmed = { confirmed = true })
            addChild(callToActionView)
            // When
            clickConfirm(callToActionView)
            // Then
            confirmed.shouldBeTrue()
            cancelled.shouldBeFalse()
            callToActionView.isDisplayingFinishPlayerTurn().shouldBeTrue()
        }

    @Test
    fun `Hide should not display any call to action when is hidden`() =
        sharedViewsTest {
            // Given
            val callToActionView = CallToActionView(MessagesMother.messages())
            callToActionView.displayFinishPlayerTurn(onPlayerTurnFinished = {})
            // When
            callToActionView.hide()
            // Then
            callToActionView.isDisplayingFinishPlayerTurn().shouldBeFalse()
            callToActionView.isDisplayingCancelAndConfirm().shouldBeFalse()
        }

    @Test
    fun `DisplayItemButtons should display the item name, its sell price and its actions when the item buttons are displayed`() =
        sharedViewsTest {
            // Given
            val callToActionView = CallToActionView(MessagesMother.messages())
            callToActionView.displayFinishPlayerTurn(onPlayerTurnFinished = {})
            val actions =
                listOf(
                    ItemButtonsView.Button(label = "Equip") {},
                    ItemButtonsView.Button(label = "Discard", isEnabled = false) {},
                    ItemButtonsView.Button(label = "Cancel") {},
                )
            // When
            callToActionView.displayItemButtons(itemName = "Hammer", itemSellPrice = 50, actions = actions)
            // Then
            callToActionView.isDisplayingItemButtons().shouldBeTrue()
            callToActionView.itemName() shouldBe "Hammer"
            callToActionView.itemSellPrice() shouldBe "50 G"
            callToActionView.itemButtonLabels().shouldContainExactly("Equip", "Discard", "Cancel")
            callToActionView.enabledItemButtonLabels().shouldContainExactly("Equip", "Cancel")
            callToActionView.fadedItemButtonLabels().shouldContainExactly("Discard")
        }

    @Test
    fun `DisplayItemButtons should run the action and display the finish turn again when an item button is clicked`() =
        sharedViewsTest {
            // Given
            var consumed = false
            val callToActionView = CallToActionView(MessagesMother.messages())
            callToActionView.displayFinishPlayerTurn(onPlayerTurnFinished = {})
            callToActionView.displayItemButtons(itemName = "Health Potion", itemSellPrice = 10, actions = listOf(ItemButtonsView.Button(label = "Consume") { consumed = true }))
            addChild(callToActionView)
            // When
            clickItemAction(callToActionView, "Consume")
            // Then
            consumed.shouldBeTrue()
            callToActionView.isDisplayingFinishPlayerTurn().shouldBeTrue()
        }

    @Test
    fun `DisplayItemButtons should keep what the action displayed instead when an item button is clicked, e g give`() =
        sharedViewsTest {
            // Given
            val callToActionView = CallToActionView(MessagesMother.messages())
            callToActionView.displayFinishPlayerTurn(onPlayerTurnFinished = {})
            val give = ItemButtonsView.Button(label = "Give") { callToActionView.displayGiveItem(itemName = "Hammer", onCancelled = {}) }
            callToActionView.displayItemButtons(itemName = "Hammer", itemSellPrice = 50, actions = listOf(give))
            addChild(callToActionView)
            // When
            clickItemAction(callToActionView, "Give")
            // Then
            callToActionView.isDisplayingGiveItem().shouldBeTrue()
        }

    @Test
    fun `DisplayGiveItem should ask to tap a party member, and cancel when cancel is clicked`() =
        sharedViewsTest {
            // Given
            var cancelled = false
            val callToActionView = CallToActionView(MessagesMother.messages())
            addChild(callToActionView)
            callToActionView.displayGiveItem(itemName = "Hammer", onCancelled = { cancelled = true })
            // When
            clickCancelGiving(callToActionView)
            // Then
            callToActionView.giveItemMessage() shouldBe "Tap a party member to give: Hammer"
            cancelled.shouldBeTrue()
        }

    @Test
    fun `DisplayItemButtons should display an action in the colour it is given, and grey by default`() =
        sharedViewsTest {
            // Given
            val callToActionView = CallToActionView(MessagesMother.messages(), loadedResourcesLoader())
            addChild(callToActionView)
            // When
            callToActionView.displayItemButtons(
                itemName = "Health Potion",
                itemSellPrice = 10,
                actions =
                    listOf(
                        ItemButtonsView.Button(label = "Cancel") {},
                        ItemButtonsView.Button(label = "Consume", color = ConfirmButton.COLOR, pressedColor = ConfirmButton.PRESSED_COLOR) {},
                    ),
            )
            // Then
            callToActionView.itemButtonColor("Cancel") shouldBe GameButton.GREY
            callToActionView.itemButtonColor("Consume") shouldBe ConfirmButton.COLOR
        }

    @Test
    fun `DisplayDiscardQuestion should display the question with Back in grey and Discard in red`() =
        sharedViewsTest {
            // Given
            val callToActionView = CallToActionView(MessagesMother.messages(), loadedResourcesLoader())
            addChild(callToActionView)
            // When
            callToActionView.displayDiscardQuestion(itemName = "Health Potion", onBack = {}, onDiscarded = {})
            // Then
            callToActionView.discardQuestion() shouldBe "Discard Health Potion? It can't be undone."
            callToActionView.discardQuestionLabels().shouldContainExactly("Back", "Discard")
            callToActionView.discardQuestionColors().shouldContainExactly(GameButton.GREY, HudStyle.DANGER)
        }

    @Test
    fun `DisplayDiscardQuestion should call the answer, staying displayed, when a button is clicked`() =
        sharedViewsTest {
            // Given
            val answers = mutableListOf<String>()
            val callToActionView = CallToActionView(MessagesMother.messages(), loadedResourcesLoader())
            addChild(callToActionView)
            callToActionView.displayDiscardQuestion(itemName = "Health Potion", onBack = { answers += "back" }, onDiscarded = { answers += "discarded" })
            // When
            clickDiscardQuestionBack(callToActionView)
            clickDiscardQuestionDiscard(callToActionView)
            // Then
            answers.shouldContainExactly("back", "discarded")
            callToActionView.discardQuestionLabels().shouldContainExactly("Back", "Discard")
        }

    @Test
    fun `DisplayInventoryFull should display the found item with its sell price and leave it when leave it is clicked`() =
        sharedViewsTest {
            // Given
            var left = false
            val callToActionView = CallToActionView(MessagesMother.messages(), loadedResourcesLoader())
            addChild(callToActionView)
            callToActionView.displayInventoryFull(foundItemName = "Health Potion", foundItemSellPrice = 10, foundItemId = "health-potion", onLeft = { left = true })
            // When
            clickLeaveIt(callToActionView)
            // Then
            callToActionView.isDisplayingInventoryFull().shouldBeTrue()
            callToActionView.inventoryFullMessage() shouldBe "Tap an item to discard for: Health Potion (10 G)"
            callToActionView.isFoundItemIconDisplayed().shouldBeTrue()
            left.shouldBeTrue()
        }

    @Test
    fun `Dismiss should display the finish turn again when the item buttons were displayed`() =
        sharedViewsTest {
            // Given
            val callToActionView = CallToActionView(MessagesMother.messages())
            callToActionView.displayFinishPlayerTurn(onPlayerTurnFinished = {})
            callToActionView.displayItemButtons(itemName = "Hammer", itemSellPrice = 50, actions = listOf(ItemButtonsView.Button(label = "Cancel") {}))
            // When
            callToActionView.dismiss()
            // Then
            callToActionView.isDisplayingFinishPlayerTurn().shouldBeTrue()
        }

    @Test
    fun `Dismiss should display nothing when the item buttons were displayed and the finish turn was never displayed`() =
        sharedViewsTest {
            // Given
            val callToActionView = CallToActionView(MessagesMother.messages())
            callToActionView.displayItemButtons(itemName = "Hammer", itemSellPrice = 50, actions = listOf(ItemButtonsView.Button(label = "Cancel") {}))
            // When
            callToActionView.dismiss()
            // Then
            callToActionView.isDisplayingNothing().shouldBeTrue()
        }

    @Test
    fun `HideFinishPlayerTurn should display nothing when the finish turn is displayed`() =
        sharedViewsTest {
            // Given
            val callToActionView = CallToActionView(MessagesMother.messages())
            callToActionView.displayFinishPlayerTurn(onPlayerTurnFinished = {})
            // When
            callToActionView.hideFinishPlayerTurn()
            // Then
            callToActionView.isDisplayingNothing().shouldBeTrue()
        }

    @Test
    fun `HideFinishPlayerTurn should keep the item buttons displayed when the item buttons are displayed`() =
        sharedViewsTest {
            // Given
            val callToActionView = CallToActionView(MessagesMother.messages())
            callToActionView.displayFinishPlayerTurn(onPlayerTurnFinished = {})
            callToActionView.displayItemButtons(itemName = "Hammer", itemSellPrice = 50, actions = listOf(ItemButtonsView.Button(label = "Cancel") {}))
            // When
            callToActionView.hideFinishPlayerTurn()
            // Then
            callToActionView.isDisplayingItemButtons().shouldBeTrue()
        }

    @Test
    fun `HideFinishPlayerTurn should display nothing once an item button is clicked when the finish turn is hidden`() =
        sharedViewsTest {
            // Given
            val callToActionView = CallToActionView(MessagesMother.messages())
            callToActionView.displayFinishPlayerTurn(onPlayerTurnFinished = {})
            callToActionView.hideFinishPlayerTurn()
            callToActionView.displayItemButtons(itemName = "Health Potion", itemSellPrice = 10, actions = listOf(ItemButtonsView.Button(label = "Consume") {}))
            addChild(callToActionView)
            // When
            clickItemAction(callToActionView, "Consume")
            // Then
            callToActionView.isDisplayingNothing().shouldBeTrue()
        }

    @Test
    fun `DisplayGiveGold should display Cancel, All and Give with the amount, and UpdateGiveGold write it again`() =
        sharedViewsTest {
            // Given
            val callToActionView = CallToActionView(MessagesMother.messages())
            // When
            callToActionView.displayGiveGold(amount = 1, maximum = 120, onCancelled = {}, onAll = {}, onGiven = {})
            // Then
            callToActionView.isDisplayingGiveGold().shouldBeTrue()
            callToActionView.giveGoldLabel() shouldBe "Give 1 G"
            // When
            callToActionView.updateGiveGold(amount = 120, maximum = 120)
            // Then
            callToActionView.giveGoldLabel() shouldBe "Give 120 G"
            callToActionView.isGiveAllEnabled().shouldBeFalse()
        }

    @Test
    fun `DisplayGiveGoldRefusal should display why, and OK should dismiss it`() =
        sharedViewsTest {
            // Given
            var isDismissed = false
            val callToActionView = CallToActionView(MessagesMother.messages())
            addChild(callToActionView)
            // When
            callToActionView.displayGiveGoldRefusal("No gold to give", onDismissed = { isDismissed = true })
            // Then
            callToActionView.isDisplayingGiveGoldRefusal().shouldBeTrue()
            callToActionView.giveGoldRefusalMessage() shouldBe "No gold to give"
            // When
            clickGiveGoldRefusalOk(callToActionView)
            // Then
            isDismissed.shouldBeTrue()
            callToActionView.isDisplayingNothing().shouldBeTrue()
        }

    private val backAction = ButtonRowView.Action("Back") {}
    private val abandonAction = ButtonRowView.Action("Abandon") {}

    @Test
    fun `DisplayActions should display every action, sharing the width of Finish turn, when they are displayed`() =
        sharedViewsTest {
            // Given
            val callToActionView = CallToActionView(MessagesMother.messages())
            addChild(callToActionView)
            // When
            callToActionView.displayButtons(listOf(backAction, abandonAction))
            // Then
            callToActionView.isDisplayingActions().shouldBeTrue()
            callToActionView.actionLabels().shouldContainExactly("Back", "Abandon")
            callToActionView.width shouldBe FinishPlayerTurnView(MessagesMother.messages()) {}.width
        }

    @Test
    fun `DisplayActions should keep the row displayed when an action is clicked`() =
        sharedViewsTest {
            // Given
            var isSelected = false
            val callToActionView = CallToActionView(MessagesMother.messages())
            addChild(callToActionView)
            callToActionView.displayButtons(listOf(ButtonRowView.Action("Back") { isSelected = true }))
            // When
            clickAction(callToActionView, "Back")
            // Then
            isSelected.shouldBeTrue()
            callToActionView.isDisplayingActions().shouldBeTrue()
        }

    @Test
    fun `DisplayActions should keep the next row tappable when an action displays it, as buying displays the row again`() =
        sharedViewsTest {
            // Given
            var buys = 0
            val callToActionView = CallToActionView(MessagesMother.messages())
            addChild(callToActionView)

            fun displayBuy() {
                callToActionView.displayButtons(
                    listOf(
                        ButtonRowView.Action("Buy") {
                            buys++
                            displayBuy()
                        },
                    ),
                )
            }
            displayBuy()
            // When
            clickAction(callToActionView, "Buy")
            clickAction(callToActionView, "Buy")
            // Then
            buys shouldBe 2
        }

    @Test
    fun `DisplayActions should make the view visible when it is hidden`() =
        sharedViewsTest {
            // Given
            val callToActionView = CallToActionView(MessagesMother.messages())
            callToActionView.visible = false
            // When
            callToActionView.displayButtons(listOf(backAction))
            // Then
            callToActionView.visible.shouldBeTrue()
        }

    @Test
    fun `DisplayConfirmation should draw the confirm action in danger`() =
        sharedViewsTest {
            // Given
            val callToActionView = CallToActionView(MessagesMother.messages())
            // When
            callToActionView.displayConfirmation(cancelLabel = "Cancel", confirmLabel = "Quit to menu", onCancelled = {}, onConfirmed = {})
            // Then
            callToActionView.actionLabels().shouldContainExactly("Cancel", "Quit to menu")
            callToActionView.actionColor("Quit to menu") shouldBe HudStyle.DANGER
        }

    @Test
    fun `DisplayConfirmation should call the action of the choice tapped`() =
        sharedViewsTest {
            // Given
            val choices = mutableListOf<String>()
            val callToActionView = CallToActionView(MessagesMother.messages())
            addChild(callToActionView)
            callToActionView.displayConfirmation(cancelLabel = "Cancel", confirmLabel = "Quit to menu", onCancelled = { choices += "cancel" }, onConfirmed = { choices += "confirm" })
            // When
            clickCancelConfirmation(callToActionView)
            clickConfirmation(callToActionView)
            // Then
            choices.shouldContainExactly("cancel", "confirm")
        }

    @Test
    fun `HideActions should display the finish turn again when the battle has one`() =
        sharedViewsTest {
            // Given
            val callToActionView = CallToActionView(MessagesMother.messages())
            callToActionView.displayFinishPlayerTurn(onPlayerTurnFinished = {})
            callToActionView.displayButtons(listOf(backAction))
            // When
            callToActionView.hideButtons()
            // Then
            callToActionView.isDisplayingFinishPlayerTurn().shouldBeTrue()
        }

    @Test
    fun `HideActions should display nothing when no battle is on`() =
        sharedViewsTest {
            // Given
            val callToActionView = CallToActionView(MessagesMother.messages())
            callToActionView.displayButtons(listOf(backAction))
            // When
            callToActionView.hideButtons()
            // Then
            callToActionView.isDisplayingNothing().shouldBeTrue()
        }

    @Test
    fun `HideActions should keep the inventory full displayed when no row is displayed`() =
        sharedViewsTest {
            // Given
            val callToActionView = CallToActionView(MessagesMother.messages(), loadedResourcesLoader())
            callToActionView.displayInventoryFull(foundItemName = "Health potion", foundItemSellPrice = 25, foundItemId = "health-potion", onLeft = {})
            // When
            callToActionView.hideButtons()
            // Then
            callToActionView.isDisplayingInventoryFull().shouldBeTrue()
        }
}
