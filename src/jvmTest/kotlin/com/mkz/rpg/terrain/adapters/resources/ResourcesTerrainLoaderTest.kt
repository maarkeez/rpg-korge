package com.mkz.rpg.terrain.adapters.resources

import com.mkz.rpg.terrain.domain.Terrain
import com.mkz.rpg.terrain.domain.TerrainError.TerrainNotFound
import korlibs.io.file.std.MemoryVfsMix
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ResourcesTerrainLoaderTest {
    @Nested
    inner class InitResources {
        @Test
        fun `should load the terrain definitions when resources are initialized`() {
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
            assertThat(terrains.map { it.id }).containsExactlyInAnyOrder("sand", "void")
        }

        @Test
        fun `should load no terrain definitions when the terrain folder does not exist`() {
            // Given
            val terrainRoot = MemoryVfsMix()
            val loader = ResourcesTerrainLoader(terrainRoot = terrainRoot["terrain"])
            // When
            runBlocking { loader.initResources() }
            // Then
            val terrains = loader.loadTerrains()
            assertThat(terrains).isEmpty()
        }
    }

    @Nested
    inner class LoadTerrains {
        @Test
        fun `should return the terrain when a definition exists`() {
            // Given
            val terrainRoot = MemoryVfsMix("terrain/sand.toml" to "canBeOccupied = true")["terrain"]
            val loader = ResourcesTerrainLoader(terrainRoot = terrainRoot)
            runBlocking { loader.initResources() }
            // When
            val terrains = loader.loadTerrains()
            // Then
            assertThat(terrains).containsExactly(Terrain.Dto(id = "sand", canBeOccupied = true, allowedTransitionTo = setOf("sand")))
        }

        @Test
        fun `should return all the terrains when there are multiple definitions`() {
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
            assertThat(terrains.map { it.id }).containsExactly("forest", "sand", "void")
            assertThat(terrains.first { it.id == "void" }.canBeOccupied).isFalse()
            assertThat(terrains.first { it.id == "forest" }.canBeOccupied).isTrue()
        }

        @Test
        fun `should allow the transition only to the target terrain when a transition asset exists`() {
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
            assertThat(terrains.first { it.id == "sand" }.allowedTransitionTo).containsExactlyInAnyOrder("sand", "void")
            assertThat(terrains.first { it.id == "void" }.allowedTransitionTo).containsExactly("void")
        }

        @Test
        fun `should allow the transition to itself when a terrain is loaded`() {
            // Given
            val terrainRoot = MemoryVfsMix("terrain/sand.toml" to "canBeOccupied = true")["terrain"]
            val loader = ResourcesTerrainLoader(terrainRoot = terrainRoot)
            runBlocking { loader.initResources() }
            // When
            val terrains = loader.loadTerrains()
            // Then
            assertThat(terrains.first { it.id == "sand" }.allowedTransitionTo).contains("sand")
        }

        @Test
        fun `should accumulate the transitions of a terrain when it appears in multiple assets`() {
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
            assertThat(terrains.first { it.id == "sand" }.allowedTransitionTo).containsExactlyInAnyOrder("sand", "void", "forest")
            assertThat(terrains.first { it.id == "void" }.allowedTransitionTo).containsExactly("void")
        }

        @Test
        fun `should fail when a transition references a terrain without a definition`() {
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
            assertThat(result.exceptionOrNull()).isInstanceOf(TerrainNotFound::class.java)
            assertThat(result.exceptionOrNull() as TerrainNotFound).extracting("terrainId").isEqualTo("void")
        }

        @Test
        fun `should not return a transition when the asset does not follow the naming convention`() {
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
            assertThat(terrains.first { it.id == "sand" }.allowedTransitionTo).containsExactly("sand")
        }

        @Test
        fun `should not load a terrain definition when the file is not a toml file`() {
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
            assertThat(terrains.map { it.id }).containsExactly("sand")
        }

        @Test
        fun `should load the effect id when the terrain definition contains an effect id`() {
            // Given
            val terrainRoot = MemoryVfsMix("terrain/swamp.toml" to "canBeOccupied = true\neffectId = \"swamp-effect\"")["terrain"]
            val loader = ResourcesTerrainLoader(terrainRoot = terrainRoot)
            runBlocking { loader.initResources() }
            // When
            val terrains = loader.loadTerrains()
            // Then
            assertThat(terrains.first { it.id == "swamp" }.effectId).isEqualTo("swamp-effect")
        }

        @Test
        fun `should load no effect id when the terrain definition does not contain an effect id`() {
            // Given
            val terrainRoot = MemoryVfsMix("terrain/sand.toml" to "canBeOccupied = true")["terrain"]
            val loader = ResourcesTerrainLoader(terrainRoot = terrainRoot)
            runBlocking { loader.initResources() }
            // When
            val terrains = loader.loadTerrains()
            // Then
            assertThat(terrains.first { it.id == "sand" }.effectId).isNull()
        }

        @Test
        fun `should return the committed terrains when the default resources are used`() {
            // Given
            val loader = ResourcesTerrainLoader()
            runBlocking { loader.initResources() }
            // When
            val terrains = loader.loadTerrains()
            // Then
            assertThat(terrains).containsExactlyInAnyOrder(
                Terrain.Dto(id = "sand", canBeOccupied = true, allowedTransitionTo = setOf("sand", "void")),
                Terrain.Dto(id = "void", canBeOccupied = false, allowedTransitionTo = setOf("void")),
            )
        }
    }
}
