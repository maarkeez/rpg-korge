package com.mkz.rpg.inventory.usecases.queries

import com.mkz.rpg.inventory.domain.Inventory.Dto.GoldReceiverDto
import com.mkz.rpg.inventory.domain.InventoryMother.inventory
import com.mkz.rpg.inventory.usecases.services.GiveMother
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import kotlin.test.BeforeTest
import kotlin.test.Test

class SearchGoldReceiversTest {
    private val give = GiveMother()
    private val searchGoldReceivers = SearchGoldReceivers(give.inventoryRepository, give.giveRule)

    @BeforeTest
    fun setUp() {
        give.inventoryRepository.create(inventory(battleUnitId = "knight-1", gold = 120))
        give.inventoryRepository.create(inventory(battleUnitId = "viking-1", gold = 100))
        give.inventoryRepository.create(inventory(battleUnitId = "mage-1", gold = 99_990))
    }

    @Test
    fun `should answer the other members with room, in party order, with their gold and room`() {
        // Then
        searchGoldReceivers("viking-1").shouldContainExactly(
            GoldReceiverDto(battleUnitId = "knight-1", gold = 120, room = 99_879),
            GoldReceiverDto(battleUnitId = "mage-1", gold = 99_990, room = 9),
        )
    }

    @Test
    fun `should leave out the members at the limit`() {
        // Given
        give.inventoryRepository.create(inventory(battleUnitId = "mage-1", gold = 99_999))
        // Then
        searchGoldReceivers("knight-1").shouldContainExactly(GoldReceiverDto(battleUnitId = "viking-1", gold = 100, room = 99_899))
    }

    @Test
    fun `should answer nothing when the party does not explore`() {
        // Given
        give.battle()
        // Then
        searchGoldReceivers("knight-1").shouldBeEmpty()
    }
}
