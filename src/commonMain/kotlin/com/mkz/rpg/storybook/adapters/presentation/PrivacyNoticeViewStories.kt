package com.mkz.rpg.storybook.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.PrivacyNoticeView
import com.mkz.rpg.storybook.domain.Storybook.Dto.ComponentDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.StoryDto
import korlibs.korge.view.View

object PrivacyNoticeViewStories : StoryBinding {
    private val notice =
        StoryDto(
            id = "screen-privacy-notice--notice",
            name = "Notice",
            description = "What is recorded, with what, why, who receives it and how to change one's mind. Back returns to the consent panel.",
            args = emptyMap(),
        )

    override fun component(context: StoryContext) =
        ComponentDto(
            id = "screen-privacy-notice",
            group = "Screen",
            title = "Privacy notice",
            description = "Opened by Read more on the consent panel, in its place over the dimmed main menu.",
            controls = emptyList(),
            args = emptyMap(),
            stories = listOf(notice),
        )

    override fun render(
        args: Args,
        context: StoryContext,
    ): View =
        PrivacyNoticeView(context.messages).apply {
            setDelegate(
                object : PrivacyNoticeView.Delegate {
                    override fun privacyNoticeBackSelected() = context.action("onBack")
                },
            )
            display()
        }
}
