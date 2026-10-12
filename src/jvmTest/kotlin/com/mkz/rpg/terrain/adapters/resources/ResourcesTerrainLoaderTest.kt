package com.mkz.rpg.terrain.adapters.resources

import com.mkz.rpg.terrain.domain.Terrain
import com.mkz.rpg.terrain.domain.TerrainError.TerrainNotFound
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import korlibs.io.file.std.MemoryVfsMix
import kotlinx.coroutines.runBlocking
import kotlin.test.Test

class ResourcesTerrainLoaderTest {
    @Test
    fun `InitResources should load the terrain definitions when resources are initialized`() {
        // Given
        val terrainRoot =
            MemoryVfsMix(
                "terrain/sand.toml" to "canBeOccupied = true",
                "terrain/void.toml" to "canBeOccupied = false",
            )["terrain"]
        val loader = ResourcesTerrainLoader(terrainRoot = terrainRoot)
        // When
        runBlocking { loader.initResources() }
        // Then
        val terrains = loader.loadTerrains()
        terrains.map { it.id }.shouldContainExactlyInAnyOrder("sand", "void")
    }

    @Test
    fun `InitResources should load no terrain definitions when the terrain folder does not exist`() {
        // Given
        val terrainRoot = MemoryVfsMix()
        val loader = ResourcesTerrainLoader(terrainRoot = terrainRoot["terrain"])
        // When
        runBlocking { loader.initResources() }
        // Then
        val terrains = loader.loadTerrains()
        terrains.shouldBeEmpty()
    }

    @Test
    fun `LoadTerrains should return the terrain when a definition exists`() {
        // Given
        val terrainRoot = MemoryVfsMix("terrain/sand.toml" to "canBeOccupied = true")["terrain"]
        val loader = ResourcesTerrainLoader(terrainRoot = terrainRoot)
        runBlocking { loader.initResources() }
        // When
        val terrains = loader.loadTerrains()
        // Then
        terrains.shouldContainExactly(Terrain.Dto(id = "sand", canBeOccupied = true, allowedTransitionTo = setOf("sand")))
    }

    @Test
    fun `LoadTerrains should return all the terrains when there are multiple definitions`() {
        // Given
        val terrainRoot =
            MemoryVfsMix(
                "terrain/sand.toml" to "canBeOccupied = true",
                "terrain/void.toml" to "canBeOccupied = false",
                "terrain/forest.toml" to "canBeOccupied = true",
            )["terrain"]
        val loader = ResourcesTerrainLoader(terrainRoot = terrainRoot)
        runBlocking { loader.initResources() }
        // When
        val terrains = loader.loadTerrains()
        // Then
        terrains.map { it.id }.shouldContainExactly("forest", "sand", "void")
        terrains.first { it.id == "void" }.canBeOccupied.shouldBeFalse()
        terrains.first { it.id == "forest" }.canBeOccupied.shouldBeTrue()
    }

    @Test
    fun `LoadTerrains should allow the transition only to the target terrain when a transition asset exists`() {
        // Given
        val terrainRoot =
            MemoryVfsMix(
                "terrain/sand.toml" to "canBeOccupied = true",
                "terrain/void.toml" to "canBeOccupied = false",
                "terrain/transitions/sand_to_void.png" to "png",
            )["terrain"]
        val loader = ResourcesTerrainLoader(terrainRoot = terrainRoot)
        runBlocking { loader.initResources() }
        // When
        val terrains = loader.loadTerrains()
        // Then
        terrains.first { it.id == "sand" }.allowedTransitionTo.shouldContainExactlyInAnyOrder("sand", "void")
        terrains.first { it.id == "void" }.allowedTransitionTo.shouldContainExactly("void")
    }

    @Test
    fun `LoadTerrains should allow the transition to itself when a terrain is loaded`() {
        // Given
        val terrainRoot = MemoryVfsMix("terrain/sand.toml" to "canBeOccupied = true")["terrain"]
        val loader = ResourcesTerrainLoader(terrainRoot = terrainRoot)
        runBlocking { loader.initResources() }
        // When
        val terrains = loader.loadTerrains()
        // Then
        terrains.first { it.id == "sand" }.allowedTransitionTo.shouldContain("sand")
    }

    @Test
    fun `LoadTerrains should accumulate the transitions of a terrain when it appears in multiple assets`() {
        // Given
        val terrainRoot =
            MemoryVfsMix(
                "terrain/sand.toml" to "canBeOccupied = true",
                "terrain/void.toml" to "canBeOccupied = false",
                "terrain/forest.toml" to "canBeOccupied = true",
                "terrain/transitions/sand_to_void.png" to "png",
                "terrain/transitions/sand_to_forest.png" to "png",
            )["terrain"]
        val loader = ResourcesTerrainLoader(terrainRoot = terrainRoot)
        runBlocking { loader.initResources() }
        // When
        val terrains = loader.loadTerrains()
        // Then
        terrains.first { it.id == "sand" }.allowedTransitionTo.shouldContainExactlyInAnyOrder("sand", "void", "forest")
        terrains.first { it.id == "void" }.allowedTransitionTo.shouldContainExactly("void")
    }

    @Test
    fun `LoadTerrains should fail when a transition references a terrain without a definition`() {
        // Given
        val terrainRoot =
            MemoryVfsMix(
                "terrain/sand.toml" to "canBeOccupied = true",
                "terrain/transitions/sand_to_void.png" to "png",
            )["terrain"]
        val loader = ResourcesTerrainLoader(terrainRoot = terrainRoot)
        runBlocking { loader.initResources() }
        // When
        val result = runCatching { loader.loadTerrains() }
        // Then
        result.exceptionOrNull().shouldBeInstanceOf<TerrainNotFound>()
        result.exceptionOrNull().shouldBeInstanceOf<TerrainNotFound>().terrainId shouldBe "void"
    }

    @Test
    fun `LoadTerrains should not return a transition when the asset does not follow the naming convention`() {
        // Given
        val terrainRoot =
            MemoryVfsMix(
                "terrain/sand.toml" to "canBeOccupied = true",
                "terrain/transitions/sand.png" to "png",
                "terrain/transitions/sand_to.png" to "png",
                "terrain/transitions/_to_void.png" to "png",
                "terrain/transitions/sand_to_void_to_forest.png" to "png",
                "terrain/transitions/sand_to_void.txt" to "text",
            )["terrain"]
        val loader = ResourcesTerrainLoader(terrainRoot = terrainRoot)
        runBlocking { loader.initResources() }
        // When
        val terrains = loader.loadTerrains()
        // Then
        terrains.first { it.id == "sand" }.allowedTransitionTo.shouldContainExactly("sand")
    }

    @Test
    fun `LoadTerrains should not load a terrain definition when the file is not a toml file`() {
        // Given
        val terrainRoot =
            MemoryVfsMix(
                "terrain/sand.toml" to "canBeOccupied = true",
                "terrain/void.png" to "png",
            )["terrain"]
        val loader = ResourcesTerrainLoader(terrainRoot = terrainRoot)
        runBlocking { loader.initResources() }
        // When
        val terrains = loader.loadTerrains()
        // Then
        terrains.map { it.id }.shouldContainExactly("sand")
    }

    @Test
    fun `LoadTerrains should load the effect id when the terrain definition contains an effect id`() {
        // Given
        val terrainRoot = MemoryVfsMix("terrain/swamp.toml" to "canBeOccupied = true\neffectId = \"swamp-effect\"")["terrain"]
        val loader = ResourcesTerrainLoader(terrainRoot = terrainRoot)
        runBlocking { loader.initResources() }
        // When
        val terrains = loader.loadTerrains()
        // Then
        terrains.first { it.id == "swamp" }.effectId shouldBe "swamp-effect"
    }

    @Test
    fun `LoadTerrains should load no effect id when the terrain definition does not contain an effect id`() {
        // Given
        val terrainRoot = MemoryVfsMix("terrain/sand.toml" to "canBeOccupied = true")["terrain"]
        val loader = ResourcesTerrainLoader(terrainRoot = terrainRoot)
        runBlocking { loader.initResources() }
        // When
        val terrains = loader.loadTerrains()
        // Then
        terrains.first { it.id == "sand" }.effectId.shouldBeNull()
    }

    @Test
    fun `LoadTerrains should only declare valid terrain resources in the default resources`() {
        // Given
        val loader = ResourcesTerrainLoader()
        runBlocking { loader.initResources() }
        // When
        val terrains = loader.loadTerrains()
        // Then
        val terrainIds = terrains.map { it.id }.toSet()
        terrainIds.shouldNotBeEmpty()
        terrains.forEach { terrain ->
            terrainIds.shouldContainAll(terrain.allowedTransitionTo)
            terrain.allowedTransitionTo.shouldContain(terrain.id)
        }
    }
}
