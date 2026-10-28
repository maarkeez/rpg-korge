package com.mkz.rpg.storybook.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.HealthBarPreviewView
import com.mkz.rpg.storybook.domain.Storybook.Dto.ArgDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ComponentDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ControlDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.StoryDto
import korlibs.korge.view.View
import korlibs.math.geom.Size

/** The stories of [HealthBarPreviewView], at the size the attack and receiver previews display it. */
object HealthBarPreviewViewStories : StoryBinding {
    private val SIZE = Size(281.5, 16)
    private const val MAXIMUM_HEALTH = 30
    private const val HEALTH_LIMIT = 999

    private const val BEFORE = "remainingBefore"
    private const val AFTER = "remainingAfter"
    private const val MAXIMUM = "maximum"

    private val unchanged = StoryDto(id = "screen-health-bar-preview--unchanged", name = "Unchanged", description = "A cast that leaves the health of the battle unit as it is.", args = emptyMap())
    private val hurt =
        StoryDto(
            id = "screen-health-bar-preview--hurt",
            name = "Hurt",
            description = "The health a battle unit would have left after a hit, over the health it has now.",
            args = mapOf(BEFORE to ArgDto.int(MAXIMUM_HEALTH), AFTER to ArgDto.int(MAXIMUM_HEALTH / 3)),
        )
    private val defeated =
        StoryDto(
            id = "screen-health-bar-preview--defeated",
            name = "Defeated",
            description = "A hit that takes all the health the battle unit has left.",
            args = mapOf(BEFORE to ArgDto.int(MAXIMUM_HEALTH / 2), AFTER to ArgDto.int(0)),
        )
    private val healed =
        StoryDto(
            id = "screen-health-bar-preview--healed",
            name = "Healed",
            description = "A heal on a hurt battle unit: the health it would have after it, over the health it has now.",
            args = mapOf(BEFORE to ArgDto.int(MAXIMUM_HEALTH / 3), AFTER to ArgDto.int(MAXIMUM_HEALTH)),
        )

    override fun component(context: StoryContext) =
        ComponentDto(
            id = "screen-health-bar-preview",
            group = "Screen",
            title = "Health bar preview",
            description = "The health a battle unit would have left after a cast, over the health it has now, in the attack and receiver previews.",
            controls =
                listOf(
                    ControlDto.int(BEFORE, minimum = 0, maximum = HEALTH_LIMIT),
                    ControlDto.int(AFTER, minimum = 0, maximum = HEALTH_LIMIT),
                    ControlDto.int(MAXIMUM, minimum = 1, maximum = HEALTH_LIMIT),
                ),
            args = mapOf(BEFORE to ArgDto.int(MAXIMUM_HEALTH), AFTER to ArgDto.int(MAXIMUM_HEALTH), MAXIMUM to ArgDto.int(MAXIMUM_HEALTH)),
            stories = listOf(unchanged, hurt, defeated, healed),
        )

    override fun render(
        args: Args,
        context: StoryContext,
    ): View = HealthBarPreviewView(SIZE).apply { display(remainingBefore = args.int(BEFORE), remainingAfter = args.int(AFTER), maximum = args.int(MAXIMUM)) }
}
