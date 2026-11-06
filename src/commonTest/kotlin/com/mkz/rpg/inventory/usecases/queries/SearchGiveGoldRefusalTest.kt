package com.mkz.rpg.inventory.usecases.queries

import com.mkz.rpg.inventory.domain.Inventory.Dto.GiveGoldRefusalDto
import com.mkz.rpg.inventory.domain.InventoryMother.inventory
import com.mkz.rpg.inventory.usecases.services.GiveMother
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlin.test.BeforeTest
import kotlin.test.Test

class SearchGiveGoldRefusalTest {
    private val give = GiveMother()
    private val searchGoldReceivers = SearchGoldReceivers(give.inventoryRepository, give.giveRule)
    private val searchGiveGoldRefusal = SearchGiveGoldRefusal(give.inventoryRepository, give.giveRule, searchGoldReceivers)

    // The knight carries 120 G. The viking and the mage have room for more.
    @BeforeTest
    fun setUp() {
        give.inventoryRepository.create(inventory(battleUnitId = "knight-1", gold = 120))
        give.inventoryRepository.create(inventory(battleUnitId = "viking-1", gold = 100))
        give.inventoryRepository.create(inventory(battleUnitId = "mage-1", gold = 0))
    }

    @Test
    fun `should answer not exploring when in a battle, or while talking to an npc`() {
        // Given
        give.battle()
        // Then
        searchGiveGoldRefusal("knight-1") shouldBe GiveGoldRefusalDto.NOT_EXPLORING
        // Given
        give.talkTo("merchant")
        // Then
        searchGiveGoldRefusal("knight-1") shouldBe GiveGoldRefusalDto.NOT_EXPLORING
    }

    @Test
    fun `should answer no gold when the giver carries none`() {
        // Then
        searchGiveGoldRefusal("mage-1") shouldBe GiveGoldRefusalDto.NO_GOLD
    }

    @Test
    fun `should answer no one when the giver is alone in the party`() {
        // Given
        give.explore(listOf("knight-1"))
        // Then
        searchGiveGoldRefusal("knight-1") shouldBe GiveGoldRefusalDto.NO_ONE
    }

    @Test
    fun `should answer gold full when every other member is at its maximum gold`() {
        // Given
        give.inventoryRepository.create(inventory(battleUnitId = "viking-1", gold = 99_999))
        give.inventoryRepository.create(inventory(battleUnitId = "mage-1", gold = 99_999))
        // Then
        searchGiveGoldRefusal("knight-1") shouldBe GiveGoldRefusalDto.GOLD_FULL
    }

    @Test
    fun `should answer nothing when another member has room`() {
        // Given
        give.inventoryRepository.create(inventory(battleUnitId = "viking-1", gold = 99_999))
        // Then
        searchGiveGoldRefusal("knight-1").shouldBeNull()
    }
}
