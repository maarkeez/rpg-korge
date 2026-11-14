package com.mkz.tessavar.storybook.domain

import com.mkz.tessavar.shared.domain.DomainEvent

sealed interface StorybookEvent : DomainEvent {
    object CatalogueOpened : StorybookEvent

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
