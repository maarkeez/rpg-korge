package com.mkz.rpg.storybook.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.ItemActionsView
import com.mkz.rpg.screen.adapters.presentation.UiText
import com.mkz.rpg.storybook.domain.Storybook.Dto.ArgDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ComponentDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ControlDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.StoryDto
import korlibs.korge.view.View

/**
 * The stories of [ItemActionsView], at the width of the screen, with the texts of the language global. The actions are
 * the ones the inventory gives each kind of item while exploring: Cancel, Discard, Give, and Consume or Equip.
 */
object ItemActionsViewStories : StoryBinding {
    private const val PRICE_LIMIT = 9999

    private const val ITEM_NAME = "itemName"
    private const val ITEM_PRICE = "itemPrice"
    private const val ACTIONS = "actions"

    /** What the player can do with the item, as the inventory decides it. */
    private enum class Kind(
        val actions: List<Pair<UiText, Boolean>>,
    ) {
        MERCHANT_ITEM(emptyList()),
        CONSUMABLE(listOf(UiText.ITEM_ACTIONS_CANCEL to true, UiText.ITEM_ACTIONS_DISCARD to true, UiText.ITEM_ACTIONS_GIVE to true, UiText.ITEM_ACTIONS_CONSUME to true)),
        CAN_NOT_CONSUME(listOf(UiText.ITEM_ACTIONS_CANCEL to true, UiText.ITEM_ACTIONS_DISCARD to true, UiText.ITEM_ACTIONS_GIVE to true, UiText.ITEM_ACTIONS_CONSUME to false)),
        EQUIPPABLE(listOf(UiText.ITEM_ACTIONS_CANCEL to true, UiText.ITEM_ACTIONS_DISCARD to true, UiText.ITEM_ACTIONS_GIVE to true, UiText.ITEM_ACTIONS_EQUIP to true)),
        EQUIPPED(listOf(UiText.ITEM_ACTIONS_CANCEL to true, UiText.ITEM_ACTIONS_DISCARD to false, UiText.ITEM_ACTIONS_GIVE to true, UiText.ITEM_ACTIONS_UNEQUIP to true)),
        NO_ONE_TO_GIVE_TO(listOf(UiText.ITEM_ACTIONS_CANCEL to true, UiText.ITEM_ACTIONS_DISCARD to true, UiText.ITEM_ACTIONS_NO_ONE to false, UiText.ITEM_ACTIONS_EQUIP to true)),
        BAGS_FULL(listOf(UiText.ITEM_ACTIONS_CANCEL to true, UiText.ITEM_ACTIONS_DISCARD to true, UiText.ITEM_ACTIONS_BAGS_FULL to false, UiText.ITEM_ACTIONS_EQUIP to true)),
    }

    private val merchantItem =
        StoryDto(
            id = "screen-item-actions--merchant-item",
            name = "Merchant item",
            description = "An item a merchant sells: only its name and price, with no actions.",
            args = mapOf(ACTIONS to ArgDto.text(Kind.MERCHANT_ITEM.name)),
        )
    private val consumable = StoryDto(id = "screen-item-actions--consumable", name = "Consumable", description = "An item to consume, e.g.: a potion.", args = emptyMap())
    private val canNotConsume =
        StoryDto(
            id = "screen-item-actions--can-not-consume",
            name = "Can not consume",
            description = "An item teaching an ability the battle unit already knows: Consume is faded.",
            args = mapOf(ACTIONS to ArgDto.text(Kind.CAN_NOT_CONSUME.name)),
        )
    private val equippable =
        StoryDto(id = "screen-item-actions--equippable", name = "Equippable", description = "An item to wear, e.g.: a sword.", args = mapOf(ACTIONS to ArgDto.text(Kind.EQUIPPABLE.name)))
    private val equipped =
        StoryDto(
            id = "screen-item-actions--equipped",
            name = "Equipped",
            description = "An item the battle unit wears: it can be unequipped, and Discard is faded.",
            args = mapOf(ACTIONS to ArgDto.text(Kind.EQUIPPED.name)),
        )
    private val noOneToGiveTo =
        StoryDto(
            id = "screen-item-actions--no-one-to-give-to",
            name = "No one to give to",
            description = "No other party member is in reach: Give reads No one, faded.",
            args = mapOf(ACTIONS to ArgDto.text(Kind.NO_ONE_TO_GIVE_TO.name)),
        )
    private val bagsFull =
        StoryDto(
            id = "screen-item-actions--bags-full",
            name = "Bags full",
            description = "The bags of the other party members are full: Give reads Bags full, faded.",
            args = mapOf(ACTIONS to ArgDto.text(Kind.BAGS_FULL.name)),
        )

    override fun component(context: StoryContext) =
        ComponentDto(
            id = "screen-item-actions",
            group = "Screen",
            title = "Item actions",
            description = "The name and price of the item selected in the inventory, and what the player can do with it. actions: the kind of item.",
            controls =
                listOf(
                    ControlDto.text(ITEM_NAME),
                    ControlDto.int(ITEM_PRICE, minimum = 0, maximum = PRICE_LIMIT),
                    ControlDto.enum(ACTIONS, Kind.entries.map { kind -> kind.name }),
                ),
            args = mapOf(ITEM_NAME to ArgDto.text("Health potion"), ITEM_PRICE to ArgDto.int(25), ACTIONS to ArgDto.text(Kind.CONSUMABLE.name)),
            stories = listOf(merchantItem, consumable, canNotConsume, equippable, equipped, noOneToGiveTo, bagsFull),
        )

    override fun render(
        args: Args,
        context: StoryContext,
    ): View =
        ItemActionsView(
            context.texts,
            itemName = args.text(ITEM_NAME),
            itemPrice = args.int(ITEM_PRICE),
            actions =
                Kind.valueOf(args.text(ACTIONS)).actions.map { (text, isEnabled) ->
                    val label = context.texts[text]
                    ItemActionsView.Action(label = label, isEnabled = isEnabled) { context.action("onSelected", label) }
                },
        )
}
