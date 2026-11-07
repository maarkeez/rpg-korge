package com.mkz.rpg.worldBuilder.usecases.queries

import com.mkz.rpg.battlefield.adapters.resources.TomlBattlefieldEncoder
import com.mkz.rpg.battlefield.usecases.queries.ExportBattlefield
import com.mkz.rpg.worldBuilder.adapters.storage.InMemoryWorldBuilderRepository
import com.mkz.rpg.worldBuilder.domain.WorldBuilder
import com.mkz.rpg.worldBuilder.domain.WorldBuilderError.WorldNotCreated
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class ExportWorldTest {
    private val worldBuilderRepository = InMemoryWorldBuilderRepository()
    private val exportWorld = ExportWorld(worldBuilderRepository, ExportBattlefield(TomlBattlefieldEncoder()))

    @Test
    fun `should write the world tiles as the battlefield resource when the world is exported`() {
        // Given
        val worldBuilder =
            WorldBuilder
                .create()
                .resize(rows = 1, columns = 2)
                .selectTerrain("lava")
                .paint(row = 0, column = 1) { _, _ -> true }
        worldBuilderRepository.save(worldBuilder)
        // When
        val exported = exportWorld()
        // Then
        exported shouldBe TomlBattlefieldEncoder().encode(listOf(listOf("sand", "lava")))
    }

    @Test
    fun `should fail when the world has not been created`() {
        // When
        val export = { exportWorld() }
        // Then
        shouldThrow<WorldNotCreated> { export() }
    }
}
