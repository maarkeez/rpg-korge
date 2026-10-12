package com.mkz.rpg.shared.adapters.toml

import io.kotest.matchers.shouldBe
import kotlinx.serialization.Serializable
import kotlin.test.Test

class TomlServiceTest {
    @Serializable
    data class GameConfig(
        val title: String,
        val version: Int,
    )

    private val tomlService = TomlService()

    @Test
    fun `should deserialize a toml string into the expected model`() {
        // Given
        val toml =
            """
            title = "Hello World"
            version = 1
            """.trimIndent()
        // When
        val config = tomlService.deserialize<GameConfig>(toml)
        // Then
        config shouldBe GameConfig(title = "Hello World", version = 1)
    }
}
