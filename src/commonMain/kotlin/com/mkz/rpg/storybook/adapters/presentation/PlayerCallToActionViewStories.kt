package com.mkz.rpg.storybook.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.ActionRowView
import com.mkz.rpg.screen.adapters.presentation.ConfirmButton
import com.mkz.rpg.screen.adapters.presentation.ItemActionsView
import com.mkz.rpg.screen.adapters.presentation.PlayerCallToActionView
import com.mkz.rpg.screen.adapters.presentation.UiText
import com.mkz.rpg.storybook.domain.Storybook.Dto.ArgDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ComponentDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ControlDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.StoryDto
import korlibs.korge.view.View

/**
 * The stories of [PlayerCallToActionView], built with its real children. As in the game, choosing something dismisses
 * the call to action and displays the finish turn again, through its args.
 */
object PlayerCallToActionViewStories : StoryBinding {
    private const val SELL_PRICE_LIMIT = 9999
    private const val DEFAULT_ITEM = "health-potion"
    private const val GIVEN_GOLD = 25
    private const val MAXIMUM_GIVEN_GOLD = 120

    private const val CALL_TO_ACTION = "callToAction"
    private const val FINISH_TURN_ENABLED = "finishTurnEnabled"
    private const val ITEM_NAME = "itemName"
    private const val ITEM_SELL_PRICE = "itemSellPrice"
    private const val ITEM = "item"

    /** What the player is called to do. */
    private enum class CallToAction { FINISH_TURN, CANCEL_AND_CONFIRM, ITEM_ACTIONS, GIVE_ITEM, GIVE_GOLD, GIVE_GOLD_REFUSAL, INVENTORY_FULL, ACTIONS, CONFIRMATION }

    private val finishTurn =
        StoryDto(id = "screen-player-call-to-action--finish-turn", name = "Finish turn", description = "The player has nothing else to choose: the button ends the turn.", args = emptyMap())
    private val finishTurnDisabled =
        StoryDto(
            id = "screen-player-call-to-action--finish-turn-disabled",
            name = "Finish turn, disabled",
            description = "The turn can not be ended for now: the button is faded.",
            args = mapOf(FINISH_TURN_ENABLED to ArgDto.boolean(false)),
        )
    private val cancelAndConfirm =
        StoryDto(
            id = "screen-player-call-to-action--cancel-and-confirm",
            name = "Cancel and confirm",
            description = "A cast waiting for the player to confirm it, or to cancel it.",
            args = mapOf(CALL_TO_ACTION to ArgDto.text(CallToAction.CANCEL_AND_CONFIRM.name)),
        )
    private val itemActions =
        StoryDto(
            id = "screen-player-call-to-action--item-actions",
            name = "Item actions",
            description = "An item selected in the inventory: its name and sell price, and what to do with it.",
            args = mapOf(CALL_TO_ACTION to ArgDto.text(CallToAction.ITEM_ACTIONS.name)),
        )
    private val giveItem =
        StoryDto(
            id = "screen-player-call-to-action--give-item",
            name = "Give item",
            description = "Asks to tap the party member who receives the item, with Cancel.",
            args = mapOf(CALL_TO_ACTION to ArgDto.text(CallToAction.GIVE_ITEM.name)),
        )
    private val giveGold =
        StoryDto(
            id = "screen-player-call-to-action--give-gold",
            name = "Give gold",
            description = "The panel to give gold on show: Cancel, All and Give, which reads the amount.",
            args = mapOf(CALL_TO_ACTION to ArgDto.text(CallToAction.GIVE_GOLD.name)),
        )
    private val giveGoldRefusal =
        StoryDto(
            id = "screen-player-call-to-action--give-gold-refusal",
            name = "Give gold refusal",
            description = "The faded gold button tapped: why no gold can be given, with OK.",
            args = mapOf(CALL_TO_ACTION to ArgDto.text(CallToAction.GIVE_GOLD_REFUSAL.name)),
        )
    private val inventoryFull =
        StoryDto(
            id = "screen-player-call-to-action--inventory-full",
            name = "Inventory full",
            description = "An item found with a full inventory: swap it for a stored one, or leave it.",
            args = mapOf(CALL_TO_ACTION to ArgDto.text(CallToAction.INVENTORY_FULL.name)),
        )

    private val actions =
        StoryDto(
            id = "screen-player-call-to-action--actions",
            name = "Actions",
            description = "A panel open in place of the pad: its buttons, which stay until the presenter displays what follows.",
            args = mapOf(CALL_TO_ACTION to ArgDto.text(CallToAction.ACTIONS.name)),
        )
    private val confirmation =
        StoryDto(
            id = "screen-player-call-to-action--confirmation",
            name = "Confirmation",
            description = "A question asked before an action that can not be undone: Cancel, and the action in red.",
            args = mapOf(CALL_TO_ACTION to ArgDto.text(CallToAction.CONFIRMATION.name)),
        )

    override fun component(context: StoryContext) =
        ComponentDto(
            id = "screen-player-call-to-action",
            group = "Screen",
            title = "Player call to action",
            description =
                "Under the battlefield: what the player is called to do, in place of the pad's buttons. " +
                    "callToAction: what is displayed. item: the icon of the item found, with the inventory full.",
            controls =
                listOf(
                    ControlDto.enum(CALL_TO_ACTION, CallToAction.entries.map { callToAction -> callToAction.name }),
                    ControlDto.boolean(FINISH_TURN_ENABLED),
                    ControlDto.text(ITEM_NAME),
                    ControlDto.int(ITEM_SELL_PRICE, minimum = 0, maximum = SELL_PRICE_LIMIT),
                    ControlDto.pick(ITEM, context.lootableItemIds()),
                ),
            args =
                mapOf(
                    CALL_TO_ACTION to ArgDto.text(CallToAction.FINISH_TURN.name),
                    FINISH_TURN_ENABLED to ArgDto.boolean(true),
                    ITEM_NAME to ArgDto.text("Health potion"),
                    ITEM_SELL_PRICE to ArgDto.int(25),
                    ITEM to ArgDto.text(DEFAULT_ITEM),
                ),
            stories = listOf(finishTurn, finishTurnDisabled, cancelAndConfirm, itemActions, giveItem, giveGold, giveGoldRefusal, inventoryFull, actions, confirmation),
        )

    override fun render(
        args: Args,
        context: StoryContext,
    ): View {
        // Choosing dismisses the call to action, and the finish turn is displayed again.
        val dismissed = CALL_TO_ACTION to ArgDto.text(CallToAction.FINISH_TURN.name)
        val texts = context.texts
        return PlayerCallToActionView(texts, context.resourcesLoader).apply {
            when (CallToAction.valueOf(args.text(CALL_TO_ACTION))) {
                CallToAction.FINISH_TURN -> {
                    displayFinishTurn(onTurnFinished = { context.action("onTurnFinished") })
                    setFinishTurnEnabled(args.boolean(FINISH_TURN_ENABLED))
                }
                CallToAction.CANCEL_AND_CONFIRM ->
                    displayCancelAndConfirm(
                        onCancelled = { context.action("onCancelled", setArg = dismissed) },
                        onConfirmed = { context.action("onConfirmed", setArg = dismissed) },
                    )
                CallToAction.ITEM_ACTIONS ->
                    displayItemActions(
                        itemName = args.text(ITEM_NAME),
                        itemSellPrice = args.int(ITEM_SELL_PRICE),
                        actions =
                            listOf(UiText.ITEM_ACTIONS_CANCEL, UiText.ITEM_ACTIONS_DISCARD, UiText.ITEM_ACTIONS_CONSUME).map { text ->
                                val label = texts[text]
                                val onSelected = { context.action("onSelected", label, setArg = dismissed) }
                                if (text == UiText.ITEM_ACTIONS_CONSUME) {
                                    ItemActionsView.Action(label, color = ConfirmButton.COLOR, pressedColor = ConfirmButton.PRESSED_COLOR, onSelected = onSelected)
                                } else {
                                    ItemActionsView.Action(label = label, onSelected = onSelected)
                                }
                            },
                    )
                CallToAction.GIVE_ITEM -> displayGiveItem(itemName = args.text(ITEM_NAME), onCancelled = { context.action("onCancelled", setArg = dismissed) })
                CallToAction.GIVE_GOLD ->
                    displayGiveGold(
                        amount = GIVEN_GOLD,
                        maximum = MAXIMUM_GIVEN_GOLD,
                        onCancelled = { context.action("onCancelled", setArg = dismissed) },
                        onAll = { context.action("onAll") },
                        onGiven = { context.action("onGiven", setArg = dismissed) },
                    )
                CallToAction.GIVE_GOLD_REFUSAL -> displayGiveGoldRefusal(texts[UiText.GIVE_GOLD_NO_GOLD], onDismissed = { context.action("onDismissed", setArg = dismissed) })
                CallToAction.INVENTORY_FULL ->
                    displayInventoryFull(
                        foundItemName = args.text(ITEM_NAME),
                        foundItemSellPrice = args.int(ITEM_SELL_PRICE),
                        foundItemId = args.text(ITEM),
                        onLeft = { context.action("onLeft", setArg = dismissed) },
                    )
                CallToAction.ACTIONS ->
                    displayActions(
                        listOf(UiText.QUEST_DETAIL_BACK, UiText.QUEST_ABANDON).map { text ->
                            val label = texts[text]
                            ActionRowView.Action(label = label) { context.action("onSelected", label) }
                        },
                    )
                CallToAction.CONFIRMATION ->
                    displayConfirmation(
                        cancelLabel = texts[UiText.CALL_TO_ACTION_CANCEL],
                        confirmLabel = texts[UiText.PAUSE_QUIT],
                        onCancelled = { context.action("onCancelled") },
                        onConfirmed = { context.action("onConfirmed") },
                    )
            }
        }
    }
}
