package com.mkz.rpg.mapBuilder.usecases.queries

import com.mkz.rpg.battlefield.adapters.resources.TomlBattlefieldEncoder
import com.mkz.rpg.battlefield.usecases.queries.ExportBattlefield
import com.mkz.rpg.mapBuilder.adapters.storage.InMemoryMapBuilderRepository
import com.mkz.rpg.mapBuilder.domain.MapBuilder
import com.mkz.rpg.mapBuilder.domain.MapBuilderError.MapNotCreated
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class ExportMapTest {
    private val mapBuilderRepository = InMemoryMapBuilderRepository()
    private val exportMap = ExportMap(mapBuilderRepository, ExportBattlefield(TomlBattlefieldEncoder()))

    @Test
    fun `should write the map tiles as the battlefield resource when the map is exported`() {
        // Given
        val mapBuilder =
            MapBuilder
                .create()
                .resize(rows = 1, columns = 2)
                .selectTerrain("lava")
                .paint(row = 0, column = 1) { _, _ -> true }
        mapBuilderRepository.save(mapBuilder)
        // When
        val exported = exportMap()
        // Then
        exported shouldBe TomlBattlefieldEncoder().encode(listOf(listOf("sand", "lava")))
    }

    @Test
    fun `should fail when the map has not been created`() {
        // When
        val export = { exportMap() }
        // Then
        shouldThrow<MapNotCreated> { export() }
    }
}
