package com.mkz.rpg.npc.adapters.resources

import com.mkz.rpg.npc.domain.Npc
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.string.shouldContain
import korlibs.io.file.std.MemoryVfsMix
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class ResourcesNpcLoaderTest {
    @Test
    fun `should load one npc per toml file when the npc resources are initialised`() =
        runTest {
            // Given
            val npcRoot =
                MemoryVfsMix(
                    "npc/the-guy.toml" to "row = 10\ncolumn = 2\n",
                )["npc"]
            val loader = ResourcesNpcLoader(npcRoot = npcRoot)
            // When
            loader.initResources()
            // Then
            loader.loadNpcs().shouldContainExactly(Npc.Dto(id = "the-guy", row = 10, column = 2, unitId = null, stock = emptyList()))
        }

    @Test
    fun `should load the unit of a recruitable npc when it has one`() =
        runTest {
            // Given
            val npcRoot =
                MemoryVfsMix(
                    "npc/viking.toml" to "row = 12\ncolumn = 2\nunitId = \"viking\"\n",
                )["npc"]
            val loader = ResourcesNpcLoader(npcRoot = npcRoot)
            // When
            loader.initResources()
            // Then
            loader.loadNpcs().shouldContainExactly(Npc.Dto(id = "viking", row = 12, column = 2, unitId = "viking", stock = emptyList()))
        }

    @Test
    fun `should load the stock of a merchant, in its order, when it has one`() =
        runTest {
            // Given
            val npcRoot =
                MemoryVfsMix(
                    "npc/merchant.toml" to "row = 14\ncolumn = 2\nstock = [\"mana-potion\", \"hammer\"]\n",
                )["npc"]
            val loader = ResourcesNpcLoader(npcRoot = npcRoot)
            // When
            loader.initResources()
            // Then
            loader.loadNpcs().shouldContainExactly(
                Npc.Dto(id = "merchant", row = 14, column = 2, unitId = null, stock = listOf("mana-potion", "hammer")),
            )
        }

    @Test
    fun `should fail with the file when the npc has no row`() =
        runTest {
            // Given
            val npcRoot = MemoryVfsMix("npc/the-guy.toml" to "column = 2\n")["npc"]
            val loader = ResourcesNpcLoader(npcRoot = npcRoot)
            // When
            val initialisation = suspend { loader.initResources() }
            // Then
            shouldThrow<IllegalStateException> { initialisation() }.message.shouldContain("npc/the-guy.toml")
        }
}
