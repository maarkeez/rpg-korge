package com.mkz.rpg.lootDrop.adapters.events

import com.mkz.rpg.lootDrop.adapters.storage.InMemoryLootDropRepository
import com.mkz.rpg.lootDrop.domain.LootDrop.Dto.PositionDto
import com.mkz.rpg.lootDrop.domain.LootDropEvent.LootFound
import com.mkz.rpg.lootDrop.domain.LootDropEvent.RequestFindLoot
import com.mkz.rpg.lootDrop.domain.LootDropMother.lootDrop
import com.mkz.rpg.lootDrop.usecases.commands.FindLoot
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import com.mkz.rpg.shared.domain.subscribe
import io.kotest.matchers.collections.shouldContainExactly
import kotlin.test.Test

class OnRequestFindLootTest {
    private val eventBus = InMemoryEventBus()
    private val lootDropRepository = InMemoryLootDropRepository()
    private val onRequestFindLoot = OnRequestFindLoot(FindLoot(lootDropRepository, eventBus), eventBus)
    private val lootFoundEvents = mutableListOf<LootFound>().also { found -> eventBus.subscribe<LootFound> { found += it } }

    @Test
    fun `should announce the loot found on its path when finding loot is requested for a battle unit`() {
        // Given
        lootDropRepository.create(lootDrop(unitId = "bat", droppedItems = mapOf(PositionDto(row = 0, column = 1) to listOf("health-potion"))))
        eventBus.publish(RequestFindLoot(battleUnitId = "battle-unit-1", path = listOf(PositionDto(row = 0, column = 1), PositionDto(row = 0, column = 2))))
        // When
        eventBus.dispatch()
        // Then
        lootFoundEvents.shouldContainExactly(LootFound(battleUnitId = "battle-unit-1", lootableItemIds = listOf("health-potion"), row = 0, column = 1))
    }
}
