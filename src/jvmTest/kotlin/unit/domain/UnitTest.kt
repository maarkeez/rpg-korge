package unit.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class UnitTest {
    @Nested
    inner class Create {
        @Test
        fun `should create unit when the dto is valid`() {
            // Given
            val unitDto =
                UnitMother
                    .unit()
                    .toDto()
            // When
            val createdUnit = Unit.create(unitDto)
            // Then
            assertThat(createdUnit.toDto()).isEqualTo(unitDto)
        }

        @Test
        fun `should publish the unit created event when the unit is created`() {
            // Given
            val unitDto =
                UnitMother
                    .unit()
                    .toDto()
            // When
            val createdUnit = Unit.create(unitDto)
            // Then
            val (events, _) = createdUnit.pullEvents()
            assertThat(events).containsExactly(UnitEvent.UnitCreated(unitId = unitDto.id))
        }
    }

    @Nested
    inner class ToDto {
        @Test
        fun `should expose the unit data when converted to dto`() {
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
            assertThat(result)
                .isEqualTo(
                    Unit.Dto(
                        id = "unit-1",
                        name = "Knight",
                        healthPoints = 10,
                        manaPoints = 5,
                        abilities = listOf("ability-1"),
                        movementRange = 3,
                    ),
                )
        }
    }

    @Nested
    inner class PullEvents {
        @Test
        fun `should pull the created event when the unit has pending events`() {
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
            assertThat(events).containsExactly(UnitEvent.UnitCreated(unitId = createdUnit.toDto().id))
        }

        @Test
        fun `should not pull events when there are no pending events`() {
            // Given
            val unit = UnitMother.unit()
            // When
            val (events, _) = unit.pullEvents()
            // Then
            assertThat(events).isEmpty()
        }
    }
}
