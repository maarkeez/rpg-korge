package com.mkz.rpg.terrain.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class TerrainTest {
    @Nested
    inner class Create {
        @Test
        fun `should create terrain when the dto is valid`() {
            // Given
            val id = "sand"
            val canBeOccupied = true
            val allowedTransitionTo = setOf("void")
            // When
            val terrain =
                Terrain.create(
                    Terrain.Dto(
                        id = id,
                        canBeOccupied = canBeOccupied,
                        allowedTransitionTo = allowedTransitionTo,
                    ),
                )
            // Then
            assertThat(terrain.toDto())
                .isEqualTo(Terrain.Dto(id = id, canBeOccupied = canBeOccupied, allowedTransitionTo = allowedTransitionTo))
        }

        @Test
        fun `should publish the terrain created event when a terrain is created`() {
            // Given
            val id = "sand"
            // When
            val terrain = Terrain.create(Terrain.Dto(id = id, canBeOccupied = true))
            val (events, _) = terrain.pullEvents()
            // Then
            assertThat(events).containsExactly(TerrainEvent.TerrainCreated(terrainId = id))
        }
    }

    @Nested
    inner class CanTransitionTo {
        @Test
        fun `should be true when the terrain allows the transition`() {
            // Given
            val terrain = TerrainMother.occupiableTerrain(id = "sand", allowedTransitionTo = setOf("void"))
            // When
            val canTransition = terrain.canTransitionTo("void")
            // Then
            assertThat(canTransition).isTrue()
        }

        @Test
        fun `should be false when the terrain does not allow the transition`() {
            // Given
            val terrain = TerrainMother.occupiableTerrain(id = "sand", allowedTransitionTo = setOf("void"))
            // When
            val canTransition = terrain.canTransitionTo("forest")
            // Then
            assertThat(canTransition).isFalse()
        }

        @Test
        fun `should be false when the terrain has no allowed transitions`() {
            // Given
            val terrain = TerrainMother.occupiableTerrain(id = "sand")
            // When
            val canTransition = terrain.canTransitionTo("void")
            // Then
            assertThat(canTransition).isFalse()
        }
    }

    @Nested
    inner class ToDto {
        @Test
        fun `should expose the terrain data when converted to dto`() {
            // Given
            val terrain = TerrainMother.occupiableTerrain(id = "sand", allowedTransitionTo = setOf("void"))
            // When
            val dto = terrain.toDto()
            // Then
            assertThat(dto).isEqualTo(Terrain.Dto(id = "sand", canBeOccupied = true, allowedTransitionTo = setOf("void")))
        }
    }

    @Nested
    inner class PullEvents {
        @Test
        fun `should pull the created event when the terrain has pending events`() {
            // Given
            val terrain = TerrainMother.occupiableTerrain(id = "sand")
            // When
            val (events, _) = terrain.pullEvents()
            // Then
            assertThat(events).isNotEmpty()
        }

        @Test
        fun `should not pull events when there are no pending events`() {
            // Given
            val (events, terrain) = TerrainMother.occupiableTerrain(id = "sand").pullEvents()
            // When
            val (pulledEvents, _) = terrain.pullEvents()
            // Then
            assertThat(events).isNotEmpty()
            assertThat(pulledEvents).isEmpty()
        }
    }
}
