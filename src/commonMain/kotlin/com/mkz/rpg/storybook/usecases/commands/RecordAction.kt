package com.mkz.rpg.storybook.usecases.commands

import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.storybook.domain.StorybookRepository

/**
 * Logs a callback the view on show made, e.g.: `shopSlotSelected` with `3`, as the newest action. A link does not keep
 * the actions.
 */
class RecordAction(
    private val storybookRepository: StorybookRepository,
    private val eventBus: EventBus,
) {
    operator fun invoke(
        name: String,
        arguments: List<String>,
    ) {
        val storybook = storybookRepository.search() ?: return
        val (events, recordedStorybook) = storybook.recordAction(name, arguments).pullEvents()
        storybookRepository.save(recordedStorybook)
        eventBus.publish(events)
    }
}
