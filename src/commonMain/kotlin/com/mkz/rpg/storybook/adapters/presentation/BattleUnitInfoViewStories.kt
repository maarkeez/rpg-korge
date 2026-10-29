package com.mkz.rpg.storybook.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.AbilityButtonView
import com.mkz.rpg.screen.adapters.presentation.BattleUnitInfo
import com.mkz.rpg.screen.adapters.presentation.BattleUnitInfoView
import com.mkz.rpg.storybook.domain.Storybook.Dto.ArgDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ComponentDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ControlDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.StoryDto
import korlibs.korge.view.View

/**
 * The stories of [BattleUnitInfoView], built with its real children. Its battle unit has 30 health points and 20 mana
 * points at most, 3 steps and 1 cast each turn. Its abilities go round the ones of the game: the second is in cooldown,
 * the third costs more mana than the battle unit has, the fourth is ready again, and so on.
 */
object BattleUnitInfoViewStories : StoryBinding {
    private const val DEFAULT_UNIT = "hero"
    private const val MAXIMUM_HEALTH = 30
    private const val MAXIMUM_MANA = 20
    private const val MAXIMUM_STEPS = 3
    private const val MAXIMUM_CASTS = 1
    private const val REMAINING_TURNS_LIMIT = 9
    private const val EFFECTS_LIMIT = 12
    private const val ABILITIES_LIMIT = 6
    private const val COOLDOWN_TURNS_LEFT = 2

    private const val UNIT = "unit"
    private const val HEALTH = "health"
    private const val MANA = "mana"
    private const val STEPS = "steps"
    private const val CASTS = "casts"
    private const val REMAINING_TURNS = "remainingTurns"
    private const val EFFECTS = "effects"
    private const val ABILITIES = "abilities"

    private val ready =
        StoryDto(id = "screen-battle-unit-info--ready", name = "Ready", description = "A battle unit at the start of its turn: healthy, with all its steps and its cast.", args = emptyMap())
    private val hurt =
        StoryDto(
            id = "screen-battle-unit-info--hurt",
            name = "Hurt",
            description = "A battle unit with little health and mana left.",
            args = mapOf(HEALTH to ArgDto.int(8), MANA to ArgDto.int(5)),
        )
    private val turnSpent =
        StoryDto(
            id = "screen-battle-unit-info--turn-spent",
            name = "Turn spent",
            description = "A battle unit that has walked and cast: its points are hollow.",
            args = mapOf(STEPS to ArgDto.int(0), CASTS to ArgDto.int(0)),
        )
    private val withEffects =
        StoryDto(
            id = "screen-battle-unit-info--with-effects",
            name = "With effects",
            description = "A battle unit under a few effects, one icon each.",
            args = mapOf(EFFECTS to ArgDto.int(4)),
        )
    private val summon =
        StoryDto(
            id = "screen-battle-unit-info--summon",
            name = "Summon",
            description = "A summoned battle unit tells the turns it has left before it vanishes, after its casts.",
            args = mapOf(REMAINING_TURNS to ArgDto.int(3)),
        )
    private val sixAbilities =
        StoryDto(
            id = "screen-battle-unit-info--six-abilities",
            name = "Six abilities",
            description = "All the ability buttons: ready, in cooldown and without the mana to be cast.",
            args = mapOf(ABILITIES to ArgDto.int(ABILITIES_LIMIT)),
        )

    override fun component(context: StoryContext) =
        ComponentDto(
            id = "screen-battle-unit-info",
            group = "Screen",
            title = "Battle unit info",
            description =
                "The selected battle unit in the HUD: its portrait and name, the steps and casts it has left, its health, mana and effects, " +
                    "its abilities and its bag. remainingTurns: the turns a summon has left, none (0) for the others. abilities: how many it has.",
            controls =
                listOf(
                    ControlDto.pick(UNIT, context.unitIds()),
                    ControlDto.int(HEALTH, minimum = 0, maximum = MAXIMUM_HEALTH),
                    ControlDto.int(MANA, minimum = 0, maximum = MAXIMUM_MANA),
                    ControlDto.int(STEPS, minimum = 0, maximum = MAXIMUM_STEPS),
                    ControlDto.int(CASTS, minimum = 0, maximum = MAXIMUM_CASTS),
                    ControlDto.int(REMAINING_TURNS, minimum = 0, maximum = REMAINING_TURNS_LIMIT),
                    ControlDto.int(EFFECTS, minimum = 0, maximum = EFFECTS_LIMIT),
                    ControlDto.int(ABILITIES, minimum = 0, maximum = ABILITIES_LIMIT),
                ),
            args =
                mapOf(
                    UNIT to ArgDto.text(DEFAULT_UNIT),
                    HEALTH to ArgDto.int(MAXIMUM_HEALTH),
                    MANA to ArgDto.int(MAXIMUM_MANA),
                    STEPS to ArgDto.int(MAXIMUM_STEPS),
                    CASTS to ArgDto.int(MAXIMUM_CASTS),
                    REMAINING_TURNS to ArgDto.int(0),
                    EFFECTS to ArgDto.int(0),
                    ABILITIES to ArgDto.int(4),
                ),
            stories = listOf(ready, hurt, turnSpent, withEffects, summon, sixAbilities),
        )

    override fun render(
        args: Args,
        context: StoryContext,
    ): View {
        val effectIds = context.effectIds()
        val abilityIds = context.abilityIds()
        val battleUnit =
            BattleUnitInfo(
                unitId = args.text(UNIT),
                remainingHealthPoints = args.int(HEALTH),
                maximumHealthPoints = MAXIMUM_HEALTH,
                remainingManaPoints = args.int(MANA),
                maximumManaPoints = MAXIMUM_MANA,
                // The effects with an icon, as many times as needed.
                effectIds = List(args.int(EFFECTS)) { index -> effectIds[index % effectIds.size] },
            )
        val abilities =
            List(args.int(ABILITIES)) { index ->
                BattleUnitInfoView.Ability(
                    abilityId = abilityIds[index % abilityIds.size],
                    cooldownTurnsLeft = if (index % 3 == 1) COOLDOWN_TURNS_LEFT else 0,
                    hasEnoughMana = index % 3 != 2,
                )
            }
        return BattleUnitInfoView(context.resourcesLoader, context.texts).apply {
            setDelegate(
                object : AbilityButtonView.Delegate {
                    override fun abilitySelected(abilityId: String) = context.action("abilitySelected", abilityId)
                },
            )
            displayBag(onBagSelected = { context.action("onBagSelected") })
            display(
                battleUnit = battleUnit,
                turnActions =
                    BattleUnitInfoView.TurnActions(
                        remainingSteps = args.int(STEPS),
                        maximumSteps = MAXIMUM_STEPS,
                        remainingCasts = args.int(CASTS),
                        maximumCasts = MAXIMUM_CASTS,
                    ),
                remainingTurns = args.int(REMAINING_TURNS).takeIf { remainingTurns -> remainingTurns > 0 },
                abilities = abilities,
            )
        }
    }
}
