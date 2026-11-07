package com.mkz.rpg.worldBuilder.usecases.commands

import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import com.mkz.rpg.worldBuilder.adapters.storage.InMemoryWorldBuilderRepository
import com.mkz.rpg.worldBuilder.domain.WorldBuilder
import com.mkz.rpg.worldBuilder.domain.WorldBuilderError.WorldNotCreated
import com.mkz.rpg.worldBuilder.domain.WorldBuilderEvent.WorldResized
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class ResizeWorldTest {
    private val worldBuilderRepository = InMemoryWorldBuilderRepository()
    private val eventBus = FakeEventBus()
    private val resizeWorld = ResizeWorld(worldBuilderRepository, eventBus)

    @Test
    fun `should store the resized world and announce it when the world is resized`() {
        // Given
        worldBuilderRepository.save(WorldBuilder.create().pullEvents().second)
        // When
        resizeWorld(rows = 3, columns = 20)
        // Then
        worldBuilderRepository.search()?.toDto()?.let { world -> world.rows to world.columns } shouldBe (3 to 20)
        eventBus.shouldHavePublishedEvents(WorldResized(rows = 3, columns = 20))
    }

    @Test
    fun `should fail when the world has not been created`() {
        // When
        val resize = { resizeWorld(rows = 3, columns = 3) }
        // Then
        shouldThrow<WorldNotCreated> { resize() }
    }
}
