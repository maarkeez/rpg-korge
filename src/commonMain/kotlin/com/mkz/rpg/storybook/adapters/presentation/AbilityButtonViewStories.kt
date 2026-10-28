package com.mkz.rpg.storybook.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.AbilityButtonView
import com.mkz.rpg.storybook.domain.Storybook.Dto.ArgDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ComponentDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ControlDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.StoryDto
import korlibs.korge.view.View
import korlibs.math.geom.Size

/** The stories of [AbilityButtonView], at the size the battle unit info displays it. Tapping it selects the ability, as the battle does. */
object AbilityButtonViewStories : StoryBinding {
    private val SIZE = Size(48.75, 48.75)
    private const val COOLDOWN_LIMIT = 9
    private const val DEFAULT_ABILITY = "sword"

    private const val ABILITY = "ability"
    private const val COOLDOWN_TURNS_LEFT = "cooldownTurnsLeft"
    private const val HAS_ENOUGH_MANA = "hasEnoughMana"
    private const val SELECTED = "selected"

    private val ready = StoryDto(id = "screen-ability-button--ready", name = "Ready", description = "An ability the battle unit can cast.", args = emptyMap())
    private val selected = StoryDto(id = "screen-ability-button--selected", name = "Selected", description = "The ability tapped last, framed.", args = mapOf(SELECTED to ArgDto.boolean(true)))
    private val inCooldown =
        StoryDto(
            id = "screen-ability-button--in-cooldown",
            name = "In cooldown",
            description = "An ability that can not be cast again yet: darkened, with the turns left in a badge.",
            args = mapOf(COOLDOWN_TURNS_LEFT to ArgDto.int(2)),
        )
    private val notEnoughMana =
        StoryDto(
            id = "screen-ability-button--not-enough-mana",
            name = "Not enough mana",
            description = "An ability that costs more mana than the battle unit has: its icon in shades of blue, as the mana bar.",
            args = mapOf(HAS_ENOUGH_MANA to ArgDto.boolean(false)),
        )

    override fun component(context: StoryContext) =
        ComponentDto(
            id = "screen-ability-button",
            group = "Screen",
            title = "Ability button",
            description = "An ability of the selected battle unit, to tap to select it. A cooldown wins over a lack of mana, as it is the longer wait.",
            controls =
                listOf(
                    ControlDto.pick(ABILITY, context.abilityIds()),
                    ControlDto.int(COOLDOWN_TURNS_LEFT, minimum = 0, maximum = COOLDOWN_LIMIT),
                    ControlDto.boolean(HAS_ENOUGH_MANA),
                    ControlDto.boolean(SELECTED),
                ),
            args = mapOf(ABILITY to ArgDto.text(DEFAULT_ABILITY), COOLDOWN_TURNS_LEFT to ArgDto.int(0), HAS_ENOUGH_MANA to ArgDto.boolean(true), SELECTED to ArgDto.boolean(false)),
            stories = listOf(ready, selected, inCooldown, notEnoughMana),
        )

    override fun render(
        args: Args,
        context: StoryContext,
    ): View =
        AbilityButtonView(SIZE, context.resourcesLoader).apply {
            setDelegate(
                object : AbilityButtonView.Delegate {
                    override fun abilitySelected(abilityId: String) = context.action("abilitySelected", abilityId, setArg = SELECTED to ArgDto.boolean(true))
                },
            )
            display(abilityId = args.text(ABILITY), cooldownTurnsLeft = args.int(COOLDOWN_TURNS_LEFT), hasEnoughMana = args.boolean(HAS_ENOUGH_MANA))
            if (args.boolean(SELECTED)) select()
        }
}
