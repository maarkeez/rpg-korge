package com.mkz.rpg.storybook.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.AbilityButtonView
import com.mkz.rpg.screen.adapters.presentation.BattleUnitInfo
import com.mkz.rpg.screen.adapters.presentation.BattleUnitInfoView
import com.mkz.rpg.screen.adapters.presentation.CastPreviewView
import com.mkz.rpg.screen.adapters.presentation.GiveGoldView
import com.mkz.rpg.screen.adapters.presentation.HudView
import com.mkz.rpg.screen.adapters.presentation.InventoryView
import com.mkz.rpg.storybook.domain.Storybook.Dto.ComponentDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ControlDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.StoryDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ValueDto
import korlibs.korge.view.View
import korlibs.math.geom.Size

object HudViewStories : StoryBinding {
    private val SIZE = Size(390, 300)
    private const val UNIT = "hero"
    private const val OTHER_UNIT = "goat-wizard"
    private const val MAXIMUM_HEALTH = 30
    private const val MAXIMUM_MANA = 20
    private const val MAXIMUM_STEPS = 3
    private const val MAXIMUM_CASTS = 1
    private const val ABILITIES = 4
    private const val ITEMS = 4
    private const val GOLD = 120
    private const val OTHER_GOLD = 100
    private const val GIVEN_GOLD = 25

    private const val DISPLAY = "display"

    private enum class Display { BATTLE_UNIT_INFO, ATTACK_PREVIEW, INVENTORY, GIVE_GOLD, HIDDEN }

    private val battleUnitInfo =
        StoryDto(id = "screen-hud--battle-unit-info", name = "Battle unit info", description = "A battle unit selected: its info, with its abilities and its inventory button.", args = emptyMap())
    private val attackPreview =
        StoryDto(
            id = "screen-hud--cast-preview",
            name = "Cast preview",
            description = "An ability cast waiting to be confirmed, in place of the info.",
            args = mapOf(DISPLAY to ValueDto.text(Display.ATTACK_PREVIEW.name)),
        )
    private val inventory =
        StoryDto(
            id = "screen-hud--inventory",
            name = "Inventory",
            description = "The items of the battle unit, in place of the info, after tapping its inventory button.",
            args = mapOf(DISPLAY to ValueDto.text(Display.INVENTORY.name)),
        )
    private val giveGold =
        StoryDto(
            id = "screen-hud--give-gold",
            name = "Give gold",
            description = "The panel to give gold to another party member, in place of the inventory, after tapping the gold button while exploring.",
            args = mapOf(DISPLAY to ValueDto.text(Display.GIVE_GOLD.name)),
        )
    private val hidden =
        StoryDto(
            id = "screen-hud--hidden",
            name = "Hidden",
            description = "No battle unit selected, e.g.: on the turn of the CPU: nothing is displayed.",
            args = mapOf(DISPLAY to ValueDto.text(Display.HIDDEN.name)),
        )

    override fun component(context: StoryContext) =
        ComponentDto(
            id = "screen-hud",
            group = "Screen",
            title = "HUD",
            description = "Under the battlefield, in a battle: it displays one of the battle unit info, the cast preview, the inventory and the panel to give gold at a time, or none.",
            controls = listOf(ControlDto.enum(DISPLAY, Display.entries.map { display -> display.name })),
            args = mapOf(DISPLAY to ValueDto.text(Display.BATTLE_UNIT_INFO.name)),
            stories = listOf(battleUnitInfo, attackPreview, inventory, giveGold, hidden),
        )

    override fun render(
        args: Args,
        context: StoryContext,
    ): View {
        val display = Display.valueOf(args.text(DISPLAY))
        val heroInfo =
            BattleUnitInfo(
                unitId = UNIT,
                remainingHealth = MAXIMUM_HEALTH,
                maximumHealth = MAXIMUM_HEALTH,
                remainingMana = MAXIMUM_MANA,
                maximumMana = MAXIMUM_MANA,
                effectIds = emptyList(),
                isHumanPlayers = true,
            )
        val battleUnitInfoView =
            BattleUnitInfoView(context.resourcesLoader, context.messages).apply {
                setDelegate(
                    object : AbilityButtonView.Delegate {
                        override fun abilitySelected(abilityId: String) = context.action("abilitySelected", abilityId)
                    },
                )
                displayInventoryButton(onInventoryButtonSelected = { context.action("onInventoryButtonSelected", setArg = DISPLAY to ValueDto.text(Display.INVENTORY.name)) })
                display(
                    battleUnit = heroInfo,
                    turnActions = BattleUnitInfoView.TurnActions(remainingSteps = MAXIMUM_STEPS, maximumSteps = MAXIMUM_STEPS, remainingCasts = MAXIMUM_CASTS, maximumCasts = MAXIMUM_CASTS),
                    remainingTurns = null,
                    abilities = context.abilityIds().take(ABILITIES).map { abilityId -> BattleUnitInfoView.Ability(abilityId = abilityId, remainingCooldown = 0, hasEnoughMana = true, manaCost = 0) },
                )
            }
        val castPreviewView =
            CastPreviewView(context.resourcesLoader, context.messages).apply {
                display(
                    abilityId = "mushroom",
                    casterBattleUnit = heroInfo,
                    manaCost = 10,
                    recipients =
                        listOf(
                            CastPreviewView.Recipient(
                                battleUnit = heroInfo.copy(unitId = OTHER_UNIT),
                                remainingHealth = 18,
                                newEffectIds = emptyList(),
                            ),
                        ),
                )
            }
        val itemIds = context.lootableItemIds()
        val inventoryView =
            InventoryView(context.resourcesLoader, context.messages).apply {
                setDelegate(
                    object : InventoryView.Delegate {
                        override fun slotSelected(index: Int) = context.action("slotSelected", index)

                        override fun inventoryOwnerStepped(step: Int) = context.action("inventoryOwnerStepped", step)

                        override fun inventoryClosed() = context.action("inventoryClosed", setArg = DISPLAY to ValueDto.text(Display.BATTLE_UNIT_INFO.name))

                        override fun goldSelected() = context.action("goldSelected")
                    },
                )
                display(slots = List(ITEMS) { index -> InventoryView.Slot(itemIds[index % itemIds.size], isEquipped = false) }, gold = GOLD)
            }
        val giveGoldView =
            GiveGoldView(context.resourcesLoader, context.messages).apply {
                setDelegate(
                    object : GiveGoldView.Delegate {
                        override fun amountChanged(amount: Int) = context.action("amountChanged", amount)

                        override fun receiverStepped(step: Int) = context.action("receiverStepped", step)
                    },
                )
                display(
                    giver = GiveGoldView.Member("Knight", "knight", GOLD),
                    receiver = GiveGoldView.Member("Viking", "viking", OTHER_GOLD),
                    amount = GIVEN_GOLD,
                    maximum = GOLD,
                    canStepToPrevious = false,
                    canStepToNext = false,
                )
            }
        return HudView(SIZE, battleUnitInfoView, castPreviewView, inventoryView, giveGoldView).apply {
            when (display) {
                Display.BATTLE_UNIT_INFO -> displayBattleUnitInfoView()
                Display.ATTACK_PREVIEW -> displayCastPreviewView()
                Display.INVENTORY -> displayInventoryView()
                Display.GIVE_GOLD -> displayGiveGoldView()
                Display.HIDDEN -> hide()
            }
        }
    }
}
