package com.mkz.rpg.unit.domain

import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

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
                name = "Knight",
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
                name = "Knight",
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
