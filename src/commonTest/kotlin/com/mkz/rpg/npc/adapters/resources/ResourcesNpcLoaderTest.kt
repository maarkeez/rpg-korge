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
                    "npc/the-guy.toml" to "name = \"The Guy\"\nline = \"G'day bloke!\"\nrow = 10\ncolumn = 2\n",
                )["npc"]
            val loader = ResourcesNpcLoader(npcRoot = npcRoot)
            // When
            loader.initResources()
            // Then
            loader.loadNpcs().shouldContainExactly(Npc.Dto(id = "the-guy", name = "The Guy", line = "G'day bloke!", row = 10, column = 2))
        }

    @Test
    fun `should fail with the file when the npc has no line`() =
        runTest {
            // Given
            val npcRoot = MemoryVfsMix("npc/the-guy.toml" to "name = \"The Guy\"\nrow = 10\ncolumn = 2\n")["npc"]
            val loader = ResourcesNpcLoader(npcRoot = npcRoot)
            // When
            val initialisation = suspend { loader.initResources() }
            // Then
            shouldThrow<IllegalStateException> { initialisation() }.message.shouldContain("npc/the-guy.toml")
        }
}
