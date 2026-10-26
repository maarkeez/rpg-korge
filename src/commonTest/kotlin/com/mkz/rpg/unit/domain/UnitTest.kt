package com.mkz.rpg.unit.domain

import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import kotlin.test.Test

class UnitTest {
    @Test
    fun `Create should create unit when the dto is valid`() {
        // Given
        val unitDto =
            UnitMother
                .unit()
                .toDto()
        // When
        val createdUnit = Unit.create(unitDto)
        // Then
        createdUnit.toDto() shouldBe unitDto
    }

    @Test
    fun `Create should create a unit with its starting gold, from 0 to 99,999`() {
        // Then
        UnitMother.unit(startingGold = 0).toDto().startingGold shouldBe 0
        UnitMother.unit(startingGold = 99_999).toDto().startingGold shouldBe 99_999
    }

    @Test
    fun `Create should fail when the starting gold is negative or above 99,999`() {
        // Then
        runCatching { UnitMother.unit(startingGold = -1) }.exceptionOrNull().shouldBeTypeOf<UnitError.NegativeStartingGold>()
        runCatching { UnitMother.unit(startingGold = 100_000) }.exceptionOrNull().shouldBeTypeOf<UnitError.StartingGoldAboveLimit>()
    }

    @Test
    fun `Create should create a unit with up to 6 abilities, one per button of the ability bar`() {
        // Given
        val abilities = List(6) { index -> "ability-$index" }
        // When
        val unit = UnitMother.unit(abilities = abilities)
        // Then
        unit.toDto().abilities shouldBe abilities
    }

    @Test
    fun `Create should fail when the unit has more than 6 abilities`() {
        // Given
        val abilities = List(7) { index -> "ability-$index" }
        // When
        val result = runCatching { UnitMother.unit(abilities = abilities) }
        // Then
        result.exceptionOrNull().shouldBeTypeOf<UnitError.AbilitiesAboveLimit>()
    }

    @Test
    fun `Create should publish the unit created event when the unit is created`() {
        // Given
        val unitDto =
            UnitMother
                .unit()
                .toDto()
        // When
        val createdUnit = Unit.create(unitDto)
        // Then
        val (events, _) = createdUnit.pullEvents()
        events.shouldContainExactly(UnitEvent.UnitCreated(unitId = unitDto.id))
    }

    @Test
    fun `ToDto should expose the unit data when converted to dto`() {
        // Given
        val unit =
            UnitMother.unit(
                id = "unit-1",
                healthPoints = 10,
                manaPoints = 5,
                abilities = listOf("ability-1"),
                movementRange = 3,
            )
        // When
        val result = unit.toDto()
        // Then
        result shouldBe
            Unit.Dto(
                id = "unit-1",
                healthPoints = 10,
                manaPoints = 5,
                abilities = listOf("ability-1"),
                movementRange = 3,
            )
    }

    @Test
    fun `PullEvents should pull the created event when the unit has pending events`() {
        // Given
        val createdUnit =
            Unit.create(
                UnitMother
                    .unit()
                    .toDto(),
            )
        // When
        val (events, _) = createdUnit.pullEvents()
        // Then
        events.shouldContainExactly(UnitEvent.UnitCreated(unitId = createdUnit.toDto().id))
    }

    @Test
    fun `PullEvents should not pull events when there are no pending events`() {
        // Given
        val unit = UnitMother.unit()
        // When
        val (events, _) = unit.pullEvents()
        // Then
        events.shouldBeEmpty()
    }
}
