package com.mkz.rpg.terrain.domain

import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class TerrainTest {
    @Test
    fun `Create should create terrain when the dto is valid`() {
        // Given
        val id = "sand"
        val canBeOccupied = true
        val transitionsTo = setOf("void")
        // When
        val terrain =
            Terrain.create(
                Terrain.Dto(
                    id = id,
                    canBeOccupied = canBeOccupied,
                    transitionsTo = transitionsTo,
                    effectId = null,
                ),
            )
        // Then
        terrain.toDto() shouldBe Terrain.Dto(id = id, canBeOccupied = canBeOccupied, transitionsTo = transitionsTo, effectId = null)
    }

    @Test
    fun `Create should create terrain with the effect id when the dto contains an effect id`() {
        // Given
        val id = "swamp"
        val effectId = "swamp-effect"
        // When
        val terrain = Terrain.create(Terrain.Dto(id = id, canBeOccupied = true, transitionsTo = emptySet(), effectId = effectId))
        // Then
        terrain.toDto().effectId shouldBe effectId
    }

    @Test
    fun `Create should create terrain without an effect id when the dto does not contain an effect id`() {
        // Given
        val id = "sand"
        // When
        val terrain = Terrain.create(Terrain.Dto(id = id, canBeOccupied = true, transitionsTo = emptySet(), effectId = null))
        // Then
        terrain.toDto().effectId.shouldBeNull()
    }

    @Test
    fun `Create should publish the terrain created event when a terrain is created`() {
        // Given
        val id = "sand"
        // When
        val terrain = Terrain.create(Terrain.Dto(id = id, canBeOccupied = true, transitionsTo = emptySet(), effectId = null))
        val (events, _) = terrain.pullEvents()
        // Then
        events.shouldContainExactly(TerrainEvent.TerrainCreated(terrainId = id))
    }

    @Test
    fun `HasTransitionTo should be true when the terrain has the transition`() {
        // Given
        val terrain = TerrainMother.terrainThatCanBeOccupied(id = "sand", transitionsTo = setOf("void"))
        // When
        val hasTransition = terrain.hasTransitionTo("void")
        // Then
        hasTransition.shouldBeTrue()
    }

    @Test
    fun `HasTransitionTo should be false when the terrain does not have the transition`() {
        // Given
        val terrain = TerrainMother.terrainThatCanBeOccupied(id = "sand", transitionsTo = setOf("void"))
        // When
        val hasTransition = terrain.hasTransitionTo("forest")
        // Then
        hasTransition.shouldBeFalse()
    }

    @Test
    fun `HasTransitionTo should be false when the terrain has no transitions`() {
        // Given
        val terrain = TerrainMother.terrainThatCanBeOccupied(id = "sand")
        // When
        val hasTransition = terrain.hasTransitionTo("void")
        // Then
        hasTransition.shouldBeFalse()
    }

    @Test
    fun `ToDto should expose the terrain data when converted to dto`() {
        // Given
        val terrain = TerrainMother.terrainThatCanBeOccupied(id = "sand", transitionsTo = setOf("void"))
        // When
        val dto = terrain.toDto()
        // Then
        dto shouldBe Terrain.Dto(id = "sand", canBeOccupied = true, transitionsTo = setOf("void"), effectId = null)
    }

    @Test
    fun `ToDto should expose the effect id when the terrain has an effect assigned`() {
        // Given
        val terrain = Terrain.create(Terrain.Dto(id = "swamp", canBeOccupied = true, transitionsTo = emptySet(), effectId = "swamp-effect"))
        // When
        val dto = terrain.toDto()
        // Then
        dto.effectId shouldBe "swamp-effect"
    }

    @Test
    fun `PullEvents should pull the created event when the terrain has pending events`() {
        // Given
        val terrain = TerrainMother.terrainThatCanBeOccupied(id = "sand")
        // When
        val (events, _) = terrain.pullEvents()
        // Then
        events.shouldNotBeEmpty()
    }

    @Test
    fun `PullEvents should not pull events when there are no pending events`() {
        // Given
        val (events, terrain) = TerrainMother.terrainThatCanBeOccupied(id = "sand").pullEvents()
        // When
        val (pulledEvents, _) = terrain.pullEvents()
        // Then
        events.shouldNotBeEmpty()
        pulledEvents.shouldBeEmpty()
    }
}
