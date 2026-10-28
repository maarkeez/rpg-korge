package com.mkz.rpg.storybook.domain

import com.mkz.rpg.shared.domain.DomainEvent

sealed interface StorybookEvent : DomainEvent {
    object CatalogueInitialised : StorybookEvent

    data class StorySelected(
        val storyId: String,
    ) : StorybookEvent

    data class ArgChanged(
        val storyId: String,
        val argName: String,
    ) : StorybookEvent

    data class ArgsReset(
        val storyId: String,
    ) : StorybookEvent

    data class GlobalChanged(
        val globalName: String,
    ) : StorybookEvent

    data class ActionRecorded(
        val storyId: String,
        val actionName: String,
    ) : StorybookEvent

    data class ActionsCleared(
        val storyId: String,
    ) : StorybookEvent
}
