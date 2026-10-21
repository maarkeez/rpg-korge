package com.mkz.rpg.battlefield.adapters.events

import com.mkz.rpg.battlefield.adapters.storage.InMemoryBattlefieldRepository
import com.mkz.rpg.battlefield.domain.Battlefield.Dto.PositionDto
import com.mkz.rpg.battlefield.domain.BattlefieldEvent
import com.mkz.rpg.battlefield.domain.BattlefieldEvent.LootFound
import com.mkz.rpg.battlefield.domain.BattlefieldMother.battlefield
import com.mkz.rpg.battlefield.usecases.commands.FindLoot
import com.mkz.rpg.battlefield.usecases.commands.UpdateBattlefieldOccupancy
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import com.mkz.rpg.shared.domain.subscribe
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class OnRequestMoveOccupantTest {
    private val eventBus = InMemoryEventBus()
    private val battlefieldRepository = InMemoryBattlefieldRepository()
    private val lootFoundEvents = mutableListOf<LootFound>().also { found -> eventBus.subscribe<LootFound> { found += it } }
    private val onRequestMoveOccupant =
        OnRequestMoveOccupant(
            updateBattlefieldOccupancy = UpdateBattlefieldOccupancy(battlefieldRepository, eventBus),
            findLoot = FindLoot(battlefieldRepository, eventBus),
            eventBus = eventBus,
        )

    @Test
    fun `should update battlefield occupancy and announce the loot found on its path when the move of an occupant is requested`() {
        // Given
        battlefieldRepository.create(battlefield(". . .", droppedItems = mapOf(PositionDto(row = 0, column = 1) to listOf("health-potion"))))
        eventBus.publish(
            BattlefieldEvent.RequestMoveOccupant(
                occupantId = "battle-unit-1",
                toRow = 0,
                toColumn = 2,
                path = listOf(PositionDto(row = 0, column = 1), PositionDto(row = 0, column = 2)),
            ),
        )
        // When
        eventBus.dispatch()
        // Then
        battlefieldRepository.search()?.occupant(row = 0, column = 2) shouldBe "battle-unit-1"
        lootFoundEvents.shouldContainExactly(LootFound(occupantId = "battle-unit-1", lootableItemIds = listOf("health-potion"), row = 0, column = 1))
    }
}
