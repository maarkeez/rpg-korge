package com.mkz.rpg.storybook.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.ConfirmButton
import com.mkz.rpg.screen.adapters.presentation.ItemButtonsView
import com.mkz.rpg.screen.adapters.presentation.UiMessage
import com.mkz.rpg.storybook.domain.Storybook.Dto.ComponentDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ControlDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.StoryDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ValueDto
import korlibs.korge.view.View

/**
 * The stories of [ItemButtonsView], at the width of the screen, with the messages of the language global. The actions are
 * the ones the inventory gives each kind of item while exploring: Cancel, Discard, Give, and Consume or Equip.
 */
object ItemButtonsViewStories : StoryBinding {
    private const val SELL_PRICE_LIMIT = 9999

    private const val ITEM_NAME = "itemName"
    private const val ITEM_SELL_PRICE = "itemSellPrice"
    private const val KIND = "kind"

    // In blue, as in the inventory.
    private val PRIMARY_BUTTONS = setOf(UiMessage.ITEM_BUTTONS_CONSUME, UiMessage.ITEM_BUTTONS_EQUIP, UiMessage.ITEM_BUTTONS_UNEQUIP)

    /** What the player can do with the item, as the inventory decides it. */
    private enum class Kind(
        val buttons: List<Pair<UiMessage, Boolean>>,
    ) {
        CONSUMABLE(listOf(UiMessage.ITEM_BUTTONS_CANCEL to true, UiMessage.ITEM_BUTTONS_DISCARD to true, UiMessage.ITEM_BUTTONS_GIVE to true, UiMessage.ITEM_BUTTONS_CONSUME to true)),
        CAN_NOT_CONSUME(listOf(UiMessage.ITEM_BUTTONS_CANCEL to true, UiMessage.ITEM_BUTTONS_DISCARD to true, UiMessage.ITEM_BUTTONS_GIVE to true, UiMessage.ITEM_BUTTONS_CONSUME to false)),
        EQUIPPABLE(listOf(UiMessage.ITEM_BUTTONS_CANCEL to true, UiMessage.ITEM_BUTTONS_DISCARD to true, UiMessage.ITEM_BUTTONS_GIVE to true, UiMessage.ITEM_BUTTONS_EQUIP to true)),
        EQUIPPED(listOf(UiMessage.ITEM_BUTTONS_CANCEL to true, UiMessage.ITEM_BUTTONS_DISCARD to false, UiMessage.ITEM_BUTTONS_GIVE to true, UiMessage.ITEM_BUTTONS_UNEQUIP to true)),
        NO_ONE_TO_GIVE_TO(listOf(UiMessage.ITEM_BUTTONS_CANCEL to true, UiMessage.ITEM_BUTTONS_DISCARD to true, UiMessage.ITEM_BUTTONS_NO_ONE to false, UiMessage.ITEM_BUTTONS_EQUIP to true)),
        NO_SPACE(listOf(UiMessage.ITEM_BUTTONS_CANCEL to true, UiMessage.ITEM_BUTTONS_DISCARD to true, UiMessage.ITEM_BUTTONS_NO_SPACE to false, UiMessage.ITEM_BUTTONS_EQUIP to true)),
    }

    private val consumable = StoryDto(id = "screen-item-buttons--consumable", name = "Consumable", description = "An item to consume, e.g.: a potion.", args = emptyMap())
    private val canNotConsume =
        StoryDto(
            id = "screen-item-buttons--can-not-consume",
            name = "Can not consume",
            description = "An item teaching an ability the battle unit already knows: Consume is faded.",
            args = mapOf(KIND to ValueDto.text(Kind.CAN_NOT_CONSUME.name)),
        )
    private val equippable =
        StoryDto(id = "screen-item-buttons--equippable", name = "Equippable", description = "An item to wear, e.g.: a hammer.", args = mapOf(KIND to ValueDto.text(Kind.EQUIPPABLE.name)))
    private val equipped =
        StoryDto(
            id = "screen-item-buttons--equipped",
            name = "Equipped",
            description = "An item the battle unit wears: it can be unequipped, and Discard is faded.",
            args = mapOf(KIND to ValueDto.text(Kind.EQUIPPED.name)),
        )
    private val noOneToGiveTo =
        StoryDto(
            id = "screen-item-buttons--no-one-to-give-to",
            name = "No one to give to",
            description = "No other party member is in reach: Give reads No one, faded.",
            args = mapOf(KIND to ValueDto.text(Kind.NO_ONE_TO_GIVE_TO.name)),
        )
    private val noSpace =
        StoryDto(
            id = "screen-item-buttons--no-space",
            name = "No space",
            description = "The inventories of the other party members are full: Give reads No space, faded.",
            args = mapOf(KIND to ValueDto.text(Kind.NO_SPACE.name)),
        )

    override fun component(context: StoryContext) =
        ComponentDto(
            id = "screen-item-buttons",
            group = "Screen",
            title = "Item buttons",
            description = "The name and sell price of the item selected in the inventory, and what the player can do with it. kind: the kind of item.",
            controls =
                listOf(
                    ControlDto.text(ITEM_NAME),
                    ControlDto.int(ITEM_SELL_PRICE, minimum = 0, maximum = SELL_PRICE_LIMIT),
                    ControlDto.enum(KIND, Kind.entries.map { kind -> kind.name }),
                ),
            args = mapOf(ITEM_NAME to ValueDto.text("Health potion"), ITEM_SELL_PRICE to ValueDto.int(25), KIND to ValueDto.text(Kind.CONSUMABLE.name)),
            stories = listOf(consumable, canNotConsume, equippable, equipped, noOneToGiveTo, noSpace),
        )

    override fun render(
        args: Args,
        context: StoryContext,
    ): View =
        ItemButtonsView(
            context.messages,
            itemName = args.text(ITEM_NAME),
            itemSellPrice = args.int(ITEM_SELL_PRICE),
            actions =
                Kind.valueOf(args.text(KIND)).buttons.map { (text, isEnabled) ->
                    val label = context.messages[text]
                    val onSelected = { context.action("onSelected", label) }
                    if (text in PRIMARY_BUTTONS) {
                        ItemButtonsView.Button(label, isEnabled, ConfirmButton.COLOR, ConfirmButton.PRESSED_COLOR, onSelected)
                    } else {
                        ItemButtonsView.Button(label = label, isEnabled = isEnabled, onSelected = onSelected)
                    }
                },
        )
}
