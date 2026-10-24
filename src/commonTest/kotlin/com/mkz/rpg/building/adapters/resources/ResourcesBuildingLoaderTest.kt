package com.mkz.rpg.building.adapters.resources

import com.mkz.rpg.building.domain.Building
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.string.shouldContain
import korlibs.io.file.std.MemoryVfsMix
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class ResourcesBuildingLoaderTest {
    @Test
    fun `should load one building per toml file when the building resources are initialised`() =
        runTest {
            // Given
            val buildingRoot =
                MemoryVfsMix(
                    "building/fortress.toml" to "row = 17\ncolumn = 13\nrows = 4\ncolumns = 4\n",
                )["building"]
            val loader = ResourcesBuildingLoader(buildingRoot = buildingRoot)
            // When
            loader.initResources()
            // Then
            loader.loadBuildings().shouldContainExactly(Building.Dto(id = "fortress", row = 17, column = 13, rows = 4, columns = 4))
        }

    @Test
    fun `should fail with the file when the building has no size`() =
        runTest {
            // Given
            val buildingRoot = MemoryVfsMix("building/fortress.toml" to "row = 17\ncolumn = 13\n")["building"]
            val loader = ResourcesBuildingLoader(buildingRoot = buildingRoot)
            // When
            val initialisation = suspend { loader.initResources() }
            // Then
            shouldThrow<IllegalStateException> { initialisation() }.message.shouldContain("building/fortress.toml")
        }
}
