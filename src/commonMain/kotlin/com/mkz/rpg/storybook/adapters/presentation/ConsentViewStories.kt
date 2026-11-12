package com.mkz.rpg.storybook.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.ConsentView
import com.mkz.rpg.storybook.domain.Storybook.Dto.ComponentDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.StoryDto
import korlibs.korge.view.View

object ConsentViewStories : StoryBinding {
    private val asked =
        StoryDto(
            id = "screen-consent--asked",
            name = "Asked",
            description = "At the first launch, or from Privacy: Decline and Accept with equal weight, and Read more opening the privacy notice.",
            args = emptyMap(),
        )

    override fun component(context: StoryContext) =
        ComponentDto(
            id = "screen-consent",
            group = "Screen",
            title = "Consent",
            description = "A panel over the dimmed main menu asking to record how the game is played, blocking the menu until answered. Nothing is recorded unless the player accepts.",
            controls = emptyList(),
            args = emptyMap(),
            stories = listOf(asked),
        )

    override fun render(
        args: Args,
        context: StoryContext,
    ): View =
        ConsentView(context.messages).apply {
            setDelegate(
                object : ConsentView.Delegate {
                    override fun consentAccepted() = context.action("onAccepted")

                    override fun consentDeclined() = context.action("onDeclined")

                    override fun consentReadMoreSelected() = context.action("onReadMore")
                },
            )
            display()
        }
}
