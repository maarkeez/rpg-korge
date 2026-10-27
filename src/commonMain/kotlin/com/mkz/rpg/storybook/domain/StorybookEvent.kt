package com.mkz.rpg.storybook.domain

import com.mkz.rpg.shared.domain.DomainEvent

sealed interface StorybookEvent : DomainEvent {
    object CatalogueInitialised : StorybookEvent

    data class StorySelected(
        val storyId: String,
    ) : StorybookEvent
}
