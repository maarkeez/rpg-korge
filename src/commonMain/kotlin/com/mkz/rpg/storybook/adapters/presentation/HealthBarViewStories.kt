package com.mkz.rpg.storybook.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.HealthBarView
import com.mkz.rpg.storybook.domain.Storybook.Dto.ComponentDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.StoryDto
import korlibs.korge.view.View
import korlibs.math.geom.Size

/** The stories of [HealthBarView], at the size the battle unit info displays it. */
object HealthBarViewStories : StoryBinding {
    private val SIZE = Size(281.5, 16)
    private const val MAXIMUM_HEALTH = 30

    private val full = StoryDto(id = "screen-health-bar--full", name = "Full", description = "A battle unit that has not been hurt.")
    private val half = StoryDto(id = "screen-health-bar--half", name = "Half", description = "A battle unit that has lost half of its health.")
    private val empty = StoryDto(id = "screen-health-bar--empty", name = "Empty", description = "A battle unit that has been defeated.")

    private val remainingHealth = mapOf(full.id to MAXIMUM_HEALTH, half.id to MAXIMUM_HEALTH / 2, empty.id to 0)

    override val component =
        ComponentDto(
            id = "screen-health-bar",
            group = "Screen",
            title = "Health bar",
            description = "The health a battle unit has left, out of its maximum, in the info of the selected battle unit.",
            stories = listOf(full, half, empty),
        )

    override fun render(
        storyId: String,
        context: StoryContext,
    ): View = HealthBarView(SIZE).apply { display(remaining = remainingHealth.getValue(storyId), maximum = MAXIMUM_HEALTH) }
}
