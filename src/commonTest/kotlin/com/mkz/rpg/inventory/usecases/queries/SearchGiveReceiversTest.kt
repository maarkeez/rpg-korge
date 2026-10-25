package com.mkz.rpg.inventory.usecases.queries

import com.mkz.rpg.inventory.domain.InventoryMother.fullInventory
import com.mkz.rpg.inventory.domain.InventoryMother.inventory
import com.mkz.rpg.inventory.usecases.services.GiveMother
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import kotlin.test.BeforeTest
import kotlin.test.Test

class SearchGiveReceiversTest {
    private val give = GiveMother()
    private val searchGiveReceivers = SearchGiveReceivers(give.inventoryRepository, give.giveRule)

    @BeforeTest
    fun setUp() {
        give.inventoryRepository.create(inventory(battleUnitId = "knight-1"))
        give.inventoryRepository.create(inventory(battleUnitId = "viking-1"))
        give.inventoryRepository.create(inventory(battleUnitId = "mage-1"))
    }

    @Test
    fun `should find the other party members with space left, in party order`() {
        // Then
        searchGiveReceivers("viking-1").shouldContainExactly("knight-1", "mage-1")
    }

    @Test
    fun `should not find the members with no space left`() {
        // Given
        give.inventoryRepository.create(fullInventory(battleUnitId = "knight-1"))
        // Then
        searchGiveReceivers("viking-1").shouldContainExactly("mage-1")
    }

    @Test
    fun `should find no one when the party does not explore, or the giver is no party member`() {
        // Then
        searchGiveReceivers("rat-1").shouldBeEmpty()
        // Given
        give.talkTo("merchant")
        // Then
        searchGiveReceivers("viking-1").shouldBeEmpty()
    }
}
