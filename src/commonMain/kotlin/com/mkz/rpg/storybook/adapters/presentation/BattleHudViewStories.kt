package com.mkz.rpg.storybook.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.AbilityButtonView
import com.mkz.rpg.screen.adapters.presentation.AttackPreviewView
import com.mkz.rpg.screen.adapters.presentation.BattleHudView
import com.mkz.rpg.screen.adapters.presentation.BattleUnitInfo
import com.mkz.rpg.screen.adapters.presentation.BattleUnitInfoView
import com.mkz.rpg.screen.adapters.presentation.InventoryView
import com.mkz.rpg.storybook.domain.Storybook.Dto.ArgDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ComponentDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ControlDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.StoryDto
import korlibs.korge.view.View
import korlibs.math.geom.Size

/**
 * The stories of [BattleHudView], built with its real children, which display a hero with its abilities, a cast on a
 * goat wizard, and four items. As in the game, tapping the bag opens the inventory, and closing it displays the info.
 */
object BattleHudViewStories : StoryBinding {
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

    private const val DISPLAY = "display"

    /** What the HUD displays. */
    private enum class Display { BATTLE_UNIT_INFO, ATTACK_PREVIEW, INVENTORY, HIDDEN }

    private val battleUnitInfo =
        StoryDto(id = "screen-battle-hud--battle-unit-info", name = "Battle unit info", description = "A battle unit selected: its info, with its abilities and its bag.", args = emptyMap())
    private val attackPreview =
        StoryDto(
            id = "screen-battle-hud--attack-preview",
            name = "Attack preview",
            description = "An ability cast waiting to be confirmed, in place of the info.",
            args = mapOf(DISPLAY to ArgDto.text(Display.ATTACK_PREVIEW.name)),
        )
    private val inventory =
        StoryDto(
            id = "screen-battle-hud--inventory",
            name = "Inventory",
            description = "The items of the battle unit, in place of the info, after tapping its bag.",
            args = mapOf(DISPLAY to ArgDto.text(Display.INVENTORY.name)),
        )
    private val hidden =
        StoryDto(
            id = "screen-battle-hud--hidden",
            name = "Hidden",
            description = "No battle unit selected, e.g.: on the turn of the CPU: nothing is displayed.",
            args = mapOf(DISPLAY to ArgDto.text(Display.HIDDEN.name)),
        )

    override fun component(context: StoryContext) =
        ComponentDto(
            id = "screen-battle-hud",
            group = "Screen",
            title = "Battle HUD",
            description = "Under the battlefield, in a battle: it displays one of the battle unit info, the attack preview and the inventory at a time, or none.",
            controls = listOf(ControlDto.enum(DISPLAY, Display.entries.map { display -> display.name })),
            args = mapOf(DISPLAY to ArgDto.text(Display.BATTLE_UNIT_INFO.name)),
            stories = listOf(battleUnitInfo, attackPreview, inventory, hidden),
        )

    override fun render(
        args: Args,
        context: StoryContext,
    ): View {
        val display = Display.valueOf(args.text(DISPLAY))
        val heroInfo =
            BattleUnitInfo(
                unitId = UNIT,
                remainingHealthPoints = MAXIMUM_HEALTH,
                maximumHealthPoints = MAXIMUM_HEALTH,
                remainingManaPoints = MAXIMUM_MANA,
                maximumManaPoints = MAXIMUM_MANA,
                effectIds = emptyList(),
            )
        val battleUnitInfoView =
            BattleUnitInfoView(context.resourcesLoader, context.texts).apply {
                setDelegate(
                    object : AbilityButtonView.Delegate {
                        override fun abilitySelected(abilityId: String) = context.action("abilitySelected", abilityId)
                    },
                )
                displayBag(onBagSelected = { context.action("onBagSelected", setArg = DISPLAY to ArgDto.text(Display.INVENTORY.name)) })
                display(
                    battleUnit = heroInfo,
                    turnActions = BattleUnitInfoView.TurnActions(remainingSteps = MAXIMUM_STEPS, maximumSteps = MAXIMUM_STEPS, remainingCasts = MAXIMUM_CASTS, maximumCasts = MAXIMUM_CASTS),
                    remainingTurns = null,
                    abilities = context.abilityIds().take(ABILITIES).map { abilityId -> BattleUnitInfoView.Ability(abilityId = abilityId, cooldownTurnsLeft = 0, hasEnoughMana = true) },
                )
            }
        val attackPreviewView =
            AttackPreviewView(context.resourcesLoader, context.texts).apply {
                display(
                    casterBattleUnit = heroInfo,
                    manaCost = 10,
                    receivers =
                        listOf(
                            AttackPreviewView.Receiver(
                                battleUnit = heroInfo.copy(unitId = OTHER_UNIT),
                                remainingHealthPoints = 18,
                                newEffectIds = emptyList(),
                            ),
                        ),
                )
            }
        val itemIds = context.lootableItemIds()
        val inventoryView =
            InventoryView(context.resourcesLoader, context.texts).apply {
                setDelegate(
                    object : InventoryView.Delegate {
                        override fun slotSelected(index: Int) = context.action("slotSelected", index)

                        override fun inventoryOwnerStepped(step: Int) = context.action("inventoryOwnerStepped", step)

                        override fun inventoryClosed() = context.action("inventoryClosed", setArg = DISPLAY to ArgDto.text(Display.BATTLE_UNIT_INFO.name))
                    },
                )
                // The first lootable items, in alphabetical order, as many times as needed.
                display(slots = List(ITEMS) { index -> InventoryView.Slot(itemIds[index % itemIds.size], isEquipped = false) }, gold = GOLD)
            }
        return BattleHudView(SIZE, battleUnitInfoView, attackPreviewView, inventoryView).apply {
            when (display) {
                Display.BATTLE_UNIT_INFO -> displayBattleUnitInfoView()
                Display.ATTACK_PREVIEW -> displayAttackPreviewView()
                Display.INVENTORY -> displayInventoryView()
                Display.HIDDEN -> hide()
            }
        }
    }
}
