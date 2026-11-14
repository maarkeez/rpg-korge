package com.mkz.tessavar.inventory.usecases.commands

import com.mkz.tessavar.inventory.domain.InventoryError.GoldAboveMaximum
import com.mkz.tessavar.inventory.domain.InventoryError.NotAPartyMember
import com.mkz.tessavar.inventory.domain.InventoryError.NotEnoughGold
import com.mkz.tessavar.inventory.domain.InventoryError.NotExploringTheParty
import com.mkz.tessavar.inventory.domain.InventoryEvent.GoldGiven
import com.mkz.tessavar.inventory.domain.InventoryEvent.GoldReceived
import com.mkz.tessavar.inventory.domain.InventoryMother.inventory
import com.mkz.tessavar.inventory.usecases.services.GiveMother
import com.mkz.tessavar.shared.domain.FakeEventBus
import com.mkz.tessavar.shared.domain.shouldHavePublishedEvents
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlin.test.BeforeTest
import kotlin.test.Test

class GiveGoldTest {
    private val give = GiveMother()
    private val eventBus = FakeEventBus()
    private val giveGold = GiveGold(give.inventoryRepository, give.giveRule, eventBus)

    @BeforeTest
    fun setUp() {
        give.inventoryRepository.create(inventory(battleUnitId = "knight-1", gold = 120))
        give.inventoryRepository.create(inventory(battleUnitId = "viking-1", gold = 100))
        give.inventoryRepository.create(inventory(battleUnitId = "mage-1", gold = 99_990))
        give.inventoryRepository.create(inventory(battleUnitId = "rat-1", gold = 10))
    }

    private fun gold(battleUnitId: String): Int? =
        give.inventoryRepository
            .searchByBattleUnitId(battleUnitId)
            ?.toDto()
            ?.gold

    @Test
    fun `should move the gold from the giver to the receiver`() {
        // When
        giveGold(giverBattleUnitId = "knight-1", receiverBattleUnitId = "viking-1", amount = 25)
        // Then
        gold("knight-1") shouldBe 95
        gold("viking-1") shouldBe 125
        eventBus.shouldHavePublishedEvents(
            GoldGiven(battleUnitId = "knight-1", receiverBattleUnitId = "viking-1", gold = 25),
            GoldReceived(battleUnitId = "viking-1", giverBattleUnitId = "knight-1", gold = 25),
        )
    }

    @Test
    fun `should fail, keeping both inventories, when the receiver has no room`() {
        // Then
        shouldThrow<GoldAboveMaximum> { giveGold(giverBattleUnitId = "knight-1", receiverBattleUnitId = "mage-1", amount = 10) }
        gold("knight-1") shouldBe 120
        gold("mage-1") shouldBe 99_990
    }

    @Test
    fun `should fail, keeping both inventories, when the giver does not carry the amount`() {
        // Then
        shouldThrow<NotEnoughGold> { giveGold(giverBattleUnitId = "knight-1", receiverBattleUnitId = "viking-1", amount = 121) }
        gold("knight-1") shouldBe 120
        gold("viking-1") shouldBe 100
    }

    @Test
    fun `should fail when the receiver is no party member, or the giver itself`() {
        // Then
        shouldThrow<NotAPartyMember> { giveGold(giverBattleUnitId = "knight-1", receiverBattleUnitId = "rat-1", amount = 25) }
        shouldThrow<NotAPartyMember> { giveGold(giverBattleUnitId = "knight-1", receiverBattleUnitId = "knight-1", amount = 25) }
    }

    @Test
    fun `should fail when the party does not explore, in a battle, or while talking to an npc`() {
        // Given
        give.battle()
        // Then
        shouldThrow<NotExploringTheParty> { giveGold(giverBattleUnitId = "knight-1", receiverBattleUnitId = "viking-1", amount = 25) }
        // Given
        give.talkTo("merchant")
        // Then
        shouldThrow<NotExploringTheParty> { giveGold(giverBattleUnitId = "knight-1", receiverBattleUnitId = "viking-1", amount = 25) }
    }
}
