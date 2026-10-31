package com.mkz.rpg.storybook.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.VirtualPadView
import com.mkz.rpg.storybook.domain.Storybook.Dto.ComponentDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.StoryDto
import korlibs.korge.view.View

/**
 * The stories of [VirtualPadView], at the size the game displays it. Pressing an arrow logs `onDirectionPressed`, and the
 * first frame it is held `onDirectionHeld`, which the game calls on every frame the arrow is held and would fill the
 * Actions.
 */
object VirtualPadViewStories : StoryBinding {
    private val pad =
        StoryDto(
            id = "screen-virtual-pad--pad",
            name = "Pad",
            description = "The four arrows on the left, the round B and A buttons on the right, and Start at the bottom. Tap them to see what they call.",
            args = emptyMap(),
        )

    override fun component(context: StoryContext) =
        ComponentDto(
            id = "screen-virtual-pad",
            group = "Screen",
            title = "Virtual pad",
            description =
                "The pad the player explores with, in the place of the battle HUD: the arrows walk the party, A talks to the npc next to the leader, " +
                    "B opens the formation and Start pauses the game.",
            controls = emptyList(),
            args = emptyMap(),
            stories = listOf(pad),
        )

    override fun render(
        args: Args,
        context: StoryContext,
    ): View =
        VirtualPadView(VirtualPadView.SIZE).apply {
            var isHeldLogged = false
            setOnDirectionPressed { direction ->
                isHeldLogged = false
                context.action("onDirectionPressed", direction)
            }
            setOnDirectionHeld { direction ->
                if (!isHeldLogged) context.action("onDirectionHeld", direction)
                isHeldLogged = true
            }
            setOnAPressed { context.action("onAPressed") }
            setOnBPressed { context.action("onBPressed") }
            setOnStartPressed { context.action("onStartPressed") }
        }
}
