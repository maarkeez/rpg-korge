package com.mkz.rpg.shared.adapters.toml

import korlibs.io.file.std.MemoryVfsMix
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class TomlResourcesTest {
    @Serializable
    data class Definition(
        val name: String,
    )

    enum class Colour {
        RED,
        BLUE,
    }

    @Nested
    inner class ReadTomlFiles {
        @Test
        fun `should read every toml file keyed by id and sorted when the directory has toml files`() {
            // Given
            val directory =
                MemoryVfsMix(
                    "things/b.toml" to "name = \"B\"",
                    "things/a.toml" to "name = \"A\"",
                    "things/a.png" to "not a toml file",
                )["things"]
            // When
            val definitions = runBlocking { directory.readTomlFiles<Definition>() }
            // Then
            assertThat(definitions.keys).containsExactly("a", "b")
            assertThat(definitions["a"]).isEqualTo(Definition(name = "A"))
        }

        @Test
        fun `should read no definitions when the directory does not exist`() {
            // Given
            val directory = MemoryVfsMix()["things"]
            // When
            val definitions = runBlocking { directory.readTomlFiles<Definition>() }
            // Then
            assertThat(definitions).isEmpty()
        }
    }

    @Nested
    inner class ReadTomlFile {
        @Test
        fun `should fail with the file path when the toml file is invalid`() {
            // Given
            val file = MemoryVfsMix("things/a.toml" to "unknown = \"A\"")["things/a.toml"]
            // When
            val reading = { runBlocking { file.readTomlFile<Definition>() } }
            // Then
            assertThatThrownBy { reading() }
                .isInstanceOf(IllegalStateException::class.java)
                .hasMessageContaining("things/a.toml")
        }

        @Test
        fun `should fail with the file path when the toml file does not exist`() {
            // Given
            val file = MemoryVfsMix()["things/a.toml"]
            // When
            val reading = { runBlocking { file.readTomlFile<Definition>() } }
            // Then
            assertThatThrownBy { reading() }
                .isInstanceOf(IllegalStateException::class.java)
                .hasMessageContaining("things/a.toml")
        }
    }

    @Nested
    inner class FromResource {
        @Test
        fun `should fail with the resource path when the conversion fails`() {
            // Given
            val conversion = { fromResource("things/a.toml") { error("wrong value") } }
            // When
            // Then
            assertThatThrownBy { conversion() }
                .isInstanceOf(IllegalStateException::class.java)
                .hasMessageContaining("things/a.toml")
                .hasMessageContaining("wrong value")
        }
    }

    @Nested
    inner class EnumOf {
        @Test
        fun `should return the enum value when the value is one of its names`() {
            // Given
            val value = "BLUE"
            // When
            val colour = enumOf<Colour>("colour", value)
            // Then
            assertThat(colour).isEqualTo(Colour.BLUE)
        }

        @Test
        fun `should fail with the field and the allowed values when the value is not one of its names`() {
            // Given
            val value = "GREEN"
            // When
            val parsing = { enumOf<Colour>("colour", value) }
            // Then
            assertThatThrownBy { parsing() }
                .isInstanceOf(IllegalArgumentException::class.java)
                .hasMessageContaining("'colour'")
                .hasMessageContaining("[RED, BLUE]")
                .hasMessageContaining("GREEN")
        }
    }

    @Nested
    inner class Required {
        @Test
        fun `should fail with the field when the value is missing`() {
            // Given
            val value: Int? = null
            // When
            val requiring = { required("damage", value) }
            // Then
            assertThatThrownBy { requiring() }
                .isInstanceOf(IllegalArgumentException::class.java)
                .hasMessageContaining("'damage' is required")
        }
    }
}
