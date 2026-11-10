package com.mkz.rpg.storybook.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.VirtualPadView
import com.mkz.rpg.storybook.domain.Storybook.Dto.ComponentDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.StoryDto
import korlibs.korge.view.View

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
                "The pad the player explores with, in the place of the HUD: the arrows walk the party, A talks to the npc next to the leader, " +
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
            var isHeldRecorded = false
            setOnDirectionPressed { direction ->
                isHeldRecorded = false
                context.action("onDirectionPressed", direction)
            }
            setOnDirectionHeld { direction ->
                if (!isHeldRecorded) context.action("onDirectionHeld", direction)
                isHeldRecorded = true
            }
            setOnAPressed { context.action("onAPressed") }
            setOnBPressed { context.action("onBPressed") }
            setOnStartPressed { context.action("onStartPressed") }
        }
}
