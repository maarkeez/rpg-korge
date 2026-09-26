package com.mkz.rpg.terrain.adapters.resources

import korlibs.io.file.std.MemoryVfsMix
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ResourceTerrainTransitionsServiceTest {
    @Test
    fun `should return the transition when a transition asset exists`() {
        // Given
        val transitionsRoot = MemoryVfsMix("terrain/transitions/sand_to_void.png" to "png")["terrain/transitions"]
        val service = ResourceTerrainTransitionsService(transitionsRoot = transitionsRoot)
        // When
        val transitions = service.searchTransitions()
        // Then
        assertThat(transitions).isEqualTo(mapOf("sand" to setOf("void")))
    }

    @Test
    fun `should return all the transitions when there are multiple transition assets`() {
        // Given
        val transitionsRoot =
            MemoryVfsMix(
                "terrain/transitions/sand_to_void.png" to "png",
                "terrain/transitions/void_to_forest.png" to "png",
            )["terrain/transitions"]
        val service = ResourceTerrainTransitionsService(transitionsRoot = transitionsRoot)
        // When
        val transitions = service.searchTransitions()
        // Then
        assertThat(transitions).isEqualTo(mapOf("sand" to setOf("void"), "void" to setOf("forest")))
    }

    @Test
    fun `should accumulate the transitions of a terrain when it appears in multiple assets`() {
        // Given
        val transitionsRoot =
            MemoryVfsMix(
                "terrain/transitions/sand_to_void.png" to "png",
                "terrain/transitions/sand_to_forest.png" to "png",
            )["terrain/transitions"]
        val service = ResourceTerrainTransitionsService(transitionsRoot = transitionsRoot)
        // When
        val transitions = service.searchTransitions()
        // Then
        assertThat(transitions).isEqualTo(mapOf("sand" to setOf("void", "forest")))
    }

    @Test
    fun `should not return a transition when the asset does not follow the naming convention`() {
        // Given
        val transitionsRoot =
            MemoryVfsMix(
                "terrain/transitions/sand.png" to "png",
                "terrain/transitions/sand_to.png" to "png",
                "terrain/transitions/_to_void.png" to "png",
                "terrain/transitions/sand_to_void_to_forest.png" to "png",
            )["terrain/transitions"]
        val service = ResourceTerrainTransitionsService(transitionsRoot = transitionsRoot)
        // When
        val transitions = service.searchTransitions()
        // Then
        assertThat(transitions).isEmpty()
    }

    @Test
    fun `should not return a transition when the asset is not a png file`() {
        // Given
        val transitionsRoot = MemoryVfsMix("terrain/transitions/sand_to_void.txt" to "text")["terrain/transitions"]
        val service = ResourceTerrainTransitionsService(transitionsRoot = transitionsRoot)
        // When
        val transitions = service.searchTransitions()
        // Then
        assertThat(transitions).isEmpty()
    }

    @Test
    fun `should return no transitions when the transitions folder does not exist`() {
        // Given
        val transitionsRoot = MemoryVfsMix()
        val service = ResourceTerrainTransitionsService(transitionsRoot = transitionsRoot["terrain/transitions"])
        // When
        val transitions = service.searchTransitions()
        // Then
        assertThat(transitions).isEmpty()
    }

    @Test
    fun `should return the committed transitions when the default resources are used`() {
        // When
        val transitions = ResourceTerrainTransitionsService().searchTransitions()
        // Then
        assertThat(transitions.keys).contains("sand")
        assertThat(transitions.getValue("sand")).contains("void")
    }
}
