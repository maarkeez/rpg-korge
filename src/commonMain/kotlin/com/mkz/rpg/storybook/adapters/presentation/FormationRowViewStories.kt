package com.mkz.rpg.storybook.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.FormationRowView
import com.mkz.rpg.screen.adapters.presentation.FormationView
import com.mkz.rpg.storybook.domain.Storybook.Dto.ArgDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ComponentDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ControlDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.StoryDto
import korlibs.korge.view.View
import korlibs.math.geom.Size

/** The stories of [FormationRowView], at the size the formation displays it. Tapping it selects it, as the formation does. */
object FormationRowViewStories : StoryBinding {
    private val SIZE = Size(370, 44)
    private const val DEFAULT_UNIT = "hero"
    private const val STAT_LIMIT = 999

    private const val UNIT = "unit"
    private const val NAME = "name"
    private const val REMAINING_HEALTH = "remainingHealth"
    private const val MAXIMUM_HEALTH = "maximumHealth"
    private const val REMAINING_MANA = "remainingMana"
    private const val MAXIMUM_MANA = "maximumMana"
    private const val LEADER = "leader"
    private const val SELECTED = "selected"

    private val member = StoryDto(id = "screen-formation-row--member", name = "Member", description = "A party member who is not the leader.", args = emptyMap())
    private val leader =
        StoryDto(id = "screen-formation-row--leader", name = "Leader", description = "The party member who leads: the crown on its left.", args = mapOf(LEADER to ArgDto.boolean(true)))
    private val selected =
        StoryDto(
            id = "screen-formation-row--selected",
            name = "Selected",
            description = "The row tapped last, in blue, to be swapped with the next one tapped.",
            args =
                mapOf(
                    SELECTED to ArgDto.boolean(true),
                ),
        )
    private val hurt =
        StoryDto(
            id = "screen-formation-row--hurt",
            name = "Hurt",
            description = "A party member who lost health and mana.",
            args = mapOf(REMAINING_HEALTH to ArgDto.int(8), REMAINING_MANA to ArgDto.int(3)),
        )
    private val longName =
        StoryDto(
            id = "screen-formation-row--long-name",
            name = "Long name",
            description = "A name longer than the room it has.",
            args = mapOf(NAME to ArgDto.text("Sir Reginald the Unbreakable")),
        )

    override fun component(context: StoryContext) =
        ComponentDto(
            id = "screen-formation-row",
            group = "Screen",
            title = "Formation row",
            description = "A party member in the formation: the crown when it leads, its portrait, its name, and its health and mana bars. The whole row can be tapped.",
            controls =
                listOf(
                    ControlDto.pick(UNIT, context.unitIds()),
                    ControlDto.text(NAME),
                    ControlDto.int(REMAINING_HEALTH, minimum = 0, maximum = STAT_LIMIT),
                    ControlDto.int(MAXIMUM_HEALTH, minimum = 1, maximum = STAT_LIMIT),
                    ControlDto.int(REMAINING_MANA, minimum = 0, maximum = STAT_LIMIT),
                    ControlDto.int(MAXIMUM_MANA, minimum = 1, maximum = STAT_LIMIT),
                    ControlDto.boolean(LEADER),
                    ControlDto.boolean(SELECTED),
                ),
            args =
                mapOf(
                    UNIT to ArgDto.text(DEFAULT_UNIT),
                    NAME to ArgDto.text("Knight"),
                    REMAINING_HEALTH to ArgDto.int(20),
                    MAXIMUM_HEALTH to ArgDto.int(20),
                    REMAINING_MANA to ArgDto.int(10),
                    MAXIMUM_MANA to ArgDto.int(10),
                    LEADER to ArgDto.boolean(false),
                    SELECTED to ArgDto.boolean(false),
                ),
            stories = listOf(member, leader, selected, hurt, longName),
        )

    override fun render(
        args: Args,
        context: StoryContext,
    ): View =
        FormationRowView(SIZE, context.resourcesLoader).apply {
            // Tapping the selected row selects none.
            setOnSelected { context.action("onSelected", setArg = SELECTED to ArgDto.boolean(!isSelected)) }
            display(
                FormationView.Row(
                    unitId = args.text(UNIT),
                    name = args.text(NAME),
                    remainingHealth = args.int(REMAINING_HEALTH),
                    maximumHealth = args.int(MAXIMUM_HEALTH),
                    remainingMana = args.int(REMAINING_MANA),
                    maximumMana = args.int(MAXIMUM_MANA),
                    isLeader = args.boolean(LEADER),
                ),
            )
            isSelected = args.boolean(SELECTED)
        }
}
