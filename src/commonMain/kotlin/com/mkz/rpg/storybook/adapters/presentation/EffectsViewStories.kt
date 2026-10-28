package com.mkz.rpg.storybook.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.EffectsView
import com.mkz.rpg.storybook.domain.Storybook.Dto.ArgDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ComponentDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ControlDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.StoryDto
import korlibs.korge.view.View

/** The stories of [EffectsView], which holds ten icons before it adds a plus sign. */
object EffectsViewStories : StoryBinding {
    private const val EFFECTS_LIMIT = 15

    private const val EFFECTS = "effects"
    private const val NEW_EFFECTS = "newEffects"

    private val some = StoryDto(id = "screen-effects--some", name = "Some", description = "A few effects of a battle unit, one icon each.", args = emptyMap())
    private val none = StoryDto(id = "screen-effects--none", name = "None", description = "A battle unit with no effect: nothing is drawn.", args = mapOf(EFFECTS to ArgDto.int(0)))
    private val withNewEffects =
        StoryDto(
            id = "screen-effects--with-new-effects",
            name = "With new effects",
            description = "The effects an ability cast would apply, blinking after the ones the battle unit has.",
            args = mapOf(NEW_EFFECTS to ArgDto.int(2)),
        )
    private val overflow =
        StoryDto(
            id = "screen-effects--overflow",
            name = "Overflow",
            description = "More effects than the ten icons: the first ten, followed by a plus sign.",
            args = mapOf(EFFECTS to ArgDto.int(12)),
        )

    override fun component(context: StoryContext) =
        ComponentDto(
            id = "screen-effects",
            group = "Screen",
            title = "Effects",
            description = "The row of effect icons of a battle unit, in its info and in the previews, followed by the effects an ability cast would apply.",
            controls = listOf(ControlDto.int(EFFECTS, minimum = 0, maximum = EFFECTS_LIMIT), ControlDto.int(NEW_EFFECTS, minimum = 0, maximum = EFFECTS_LIMIT)),
            args = mapOf(EFFECTS to ArgDto.int(3), NEW_EFFECTS to ArgDto.int(0)),
            stories = listOf(some, none, withNewEffects, overflow),
        )

    override fun render(
        args: Args,
        context: StoryContext,
    ): View {
        val effectIds = context.effectIds()
        // The effects with an icon, as many times as needed.
        val effects = List(args.int(EFFECTS)) { index -> effectIds[index % effectIds.size] }
        val newEffects = List(args.int(NEW_EFFECTS)) { index -> effectIds[index % effectIds.size] }
        return EffectsView(context.resourcesLoader).apply { display(effectIds = effects, newEffectIds = newEffects) }
    }
}
