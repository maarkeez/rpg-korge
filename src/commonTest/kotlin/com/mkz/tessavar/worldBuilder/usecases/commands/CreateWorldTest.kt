package com.mkz.tessavar.worldBuilder.usecases.commands

import com.mkz.tessavar.shared.domain.FakeEventBus
import com.mkz.tessavar.shared.domain.shouldHavePublishedEvents
import com.mkz.tessavar.worldBuilder.adapters.storage.InMemoryWorldBuilderRepository
import com.mkz.tessavar.worldBuilder.domain.WorldBuilder
import com.mkz.tessavar.worldBuilder.domain.WorldBuilderEvent.WorldCreated
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class CreateWorldTest {
    private val worldBuilderRepository = InMemoryWorldBuilderRepository()
    private val eventBus = FakeEventBus()
    private val createWorld = CreateWorld(worldBuilderRepository, eventBus)

    @Test
    fun `should store a new world and announce it when a world is created`() {
        // When
        createWorld()
        // Then
        worldBuilderRepository.search()?.toDto() shouldBe WorldBuilder.create().toDto()
        eventBus.shouldHavePublishedEvents(WorldCreated(rows = 16, columns = 16))
    }

    @Test
    fun `should replace the previous world when a world is created again`() {
        // Given
        createWorld()
        ResizeWorld(worldBuilderRepository, FakeEventBus())(rows = 4, columns = 4)
        // When
        createWorld()
        // Then
        worldBuilderRepository.search()?.toDto()?.rows shouldBe 16
    }
}
