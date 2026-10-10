package com.mkz.rpg.assets

import com.mkz.rpg.terrain.adapters.resources.ResourcesTerrainLoader
import korlibs.io.file.std.resourcesVfs
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

private const val TRANSITIONS_PATH: String = "terrain/transitions"
private val TRANSITION_NAME_PATTERN = Regex("""([a-z]+)_to_([a-z]+)\.png""")
private const val SAND_TERRAIN_ID: String = "sand"

class TransitionStripTest {
    @Test
    fun `should name every transition file as from_to_to when the transitions folder is listed`() {
        // Given
        val transitionFiles = listTransitionFiles()
        // When
        val badNames = transitionFiles.filter { TRANSITION_NAME_PATTERN.matchEntire(it) == null }
        // Then
        assertThat(badNames).describedAs("Transition files not named <from>_to_<to>.png:\n%s", badNames.joinToString("\n")).isEmpty()
    }

    @Test
    fun `should have a terrain definition for both sides when a transition file is listed`() {
        // Given
        val transitionFiles = listTransitionFiles()
        // When
        val missingTerrains =
            runBlocking {
                transitionFiles
                    .mapNotNull { TRANSITION_NAME_PATTERN.matchEntire(it) }
                    .flatMap { match -> match.groupValues.drop(1) }
                    .distinct()
                    .filterNot { terrainId -> resourcesVfs["terrain/$terrainId.toml"].exists() }
            }
        // Then
        assertThat(missingTerrains).describedAs("Transition terrains without a terrain/<id>.toml:\n%s", missingTerrains.joinToString("\n")).isEmpty()
    }

    @Test
    fun `should be 256 by 16 pixels when every transition strip is listed`() {
        // Given
        val transitionFiles = listTransitionPngs()
        // When
        val wrongSizes = mutableListOf<String>()
        runBlocking {
            for (fileName in transitionFiles) {
                val bitmap = loadBitmap32("$TRANSITIONS_PATH/$fileName")
                if (bitmap.width != 256 || bitmap.height != 16) {
                    wrongSizes += "$fileName: ${bitmap.width}x${bitmap.height}"
                }
            }
        }
        // Then
        assertThat(wrongSizes).describedAs("Transition strips that are not 256x16:\n%s", wrongSizes.joinToString("\n")).isEmpty()
    }

    @Test
    fun `should be fully opaque at tile 0 and tile 15 when every transition strip is checked`() {
        // Given
        val transitionFiles = listTransitionPngs()
        // When
        val transparentEdges = mutableListOf<String>()
        runBlocking {
            for (fileName in transitionFiles) {
                val strip = loadBitmap32("$TRANSITIONS_PATH/$fileName")
                if (!strip.tile(0).isFullyOpaque() || !strip.tile(15).isFullyOpaque()) {
                    transparentEdges += fileName
                }
            }
        }
        // Then
        assertThat(transparentEdges).describedAs("Transition strips with transparent tile 0 or tile 15:\n%s", transparentEdges.joinToString("\n")).isEmpty()
    }

    @Test
    fun `should have an identical tile 0 in every sand strip when the sand base tile is compared`() {
        // Given
        val sandFiles = listTransitionFiles().filter { it.startsWith("${SAND_TERRAIN_ID}_to_") }
        // When
        val sandBaseTiles =
            runBlocking {
                sandFiles.associateWith { fileName -> loadBitmap32("$TRANSITIONS_PATH/$fileName").tile(0) }
            }
        val referenceEntry = sandBaseTiles.entries.firstOrNull()
        val differentFiles =
            referenceEntry
                ?.let { reference -> sandBaseTiles.filterValues { !it.isPixelIdentical(reference.value) }.keys }
                .orEmpty()
        // Then
        assertThat(referenceEntry).describedAs("At least one sand_to_* strip must exist").isNotNull
        assertThat(differentFiles)
            .describedAs("Sand strips whose tile 0 differs from %s; update all sand strips together", referenceEntry?.key)
            .isEmpty()
    }

    // Every entry is returned, not only PNGs, so that stray files are reported by the naming test.
    private fun listTransitionFiles(): List<String> = runBlocking { resourcesVfs[TRANSITIONS_PATH].listNames() }

    private fun listTransitionPngs(): List<String> = listTransitionFiles().filter { it.endsWith(".png") }

    @Test
    fun `should have a transition strip for exactly every allowed terrain pair when the terrain rules are read`() {
        // Given
        val loader = ResourcesTerrainLoader()
        runBlocking { loader.initResources() }
        val allowedPairs =
            loader
                .loadTerrains()
                .flatMap { terrain -> (terrain.allowedTransitionTo - terrain.id).map { "${terrain.id}_to_$it.png" } }
                .toSet()
        // When
        val strips = listTransitionPngs().toSet()
        // Then
        assertThat(strips).describedAs("Transition strips must match the transitionsTo rules in terrain/<id>.toml").isEqualTo(allowedPairs)
    }
}
