package com.mkz.rpg.storybook.adapters.presentation

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
    private const val PRICE_LIMIT = 9999
    private const val DEFAULT_ITEM = "health-potion"

    private const val CALL_TO_ACTION = "callToAction"
    private const val FINISH_TURN_ENABLED = "finishTurnEnabled"
    private const val ITEM_NAME = "itemName"
    private const val ITEM_PRICE = "itemPrice"
    private const val ITEM = "item"

    /** What the player is called to do. */
    private enum class CallToAction { FINISH_TURN, CANCEL_AND_CONFIRM, ITEM_ACTIONS, GIVE_ITEM, INVENTORY_FULL }

    private val finishTurn =
        StoryDto(id = "screen-player-call-to-action--finish-turn", name = "Finish turn", description = "The player has nothing else to choose: the button ends the turn.", args = emptyMap())
    private val finishTurnDisabled =
        StoryDto(
            id = "screen-player-call-to-action--finish-turn-disabled",
            name = "Finish turn, disabled",
            description = "The turn can not be ended for now: the button is greyed out.",
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
            description = "An item selected in the inventory: its name and price, and what to do with it.",
            args = mapOf(CALL_TO_ACTION to ArgDto.text(CallToAction.ITEM_ACTIONS.name)),
        )
    private val giveItem =
        StoryDto(
            id = "screen-player-call-to-action--give-item",
            name = "Give item",
            description = "Asks to tap the party member who receives the item, with Cancel.",
            args = mapOf(CALL_TO_ACTION to ArgDto.text(CallToAction.GIVE_ITEM.name)),
        )
    private val inventoryFull =
        StoryDto(
            id = "screen-player-call-to-action--inventory-full",
            name = "Inventory full",
            description = "An item found with a full inventory: swap it for a stored one, or leave it.",
            args = mapOf(CALL_TO_ACTION to ArgDto.text(CallToAction.INVENTORY_FULL.name)),
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
                    ControlDto.int(ITEM_PRICE, minimum = 0, maximum = PRICE_LIMIT),
                    ControlDto.pick(ITEM, context.lootableItemIds()),
                ),
            args =
                mapOf(
                    CALL_TO_ACTION to ArgDto.text(CallToAction.FINISH_TURN.name),
                    FINISH_TURN_ENABLED to ArgDto.boolean(true),
                    ITEM_NAME to ArgDto.text("Health potion"),
                    ITEM_PRICE to ArgDto.int(25),
                    ITEM to ArgDto.text(DEFAULT_ITEM),
                ),
            stories = listOf(finishTurn, finishTurnDisabled, cancelAndConfirm, itemActions, giveItem, inventoryFull),
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
                        itemPrice = args.int(ITEM_PRICE),
                        actions =
                            listOf(UiText.ITEM_ACTIONS_CANCEL, UiText.ITEM_ACTIONS_DISCARD, UiText.ITEM_ACTIONS_CONSUME).map { text ->
                                val label = texts[text]
                                ItemActionsView.Action(label = label) { context.action("onSelected", label, setArg = dismissed) }
                            },
                    )
                CallToAction.GIVE_ITEM -> displayGiveItem(itemName = args.text(ITEM_NAME), onCancelled = { context.action("onCancelled", setArg = dismissed) })
                CallToAction.INVENTORY_FULL ->
                    displayInventoryFull(
                        foundItemName = args.text(ITEM_NAME),
                        foundItemPrice = args.int(ITEM_PRICE),
                        foundItemId = args.text(ITEM),
                        onLeft = { context.action("onLeft", setArg = dismissed) },
                    )
            }
        }
    }
}
