package com.mkz.rpg.shared.adapters.toml

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import korlibs.io.file.std.MemoryVfsMix
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlin.test.Test

class TomlResourcesTest {
    @Serializable
    data class Definition(
        val name: String,
    )

    enum class Colour {
        RED,
        BLUE,
    }

    @Test
    fun `ReadTomlFiles should read every toml file keyed by id and sorted when the directory has toml files`() {
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
        definitions.keys.toList().shouldContainExactly("a", "b")
        definitions["a"] shouldBe Definition(name = "A")
    }

    @Test
    fun `ReadTomlFiles should read no definitions when the directory does not exist`() {
        // Given
        val directory = MemoryVfsMix()["things"]
        // When
        val definitions = runBlocking { directory.readTomlFiles<Definition>() }
        // Then
        definitions.shouldBeEmpty()
    }

    @Test
    fun `ReadTomlFile should fail with the file path when the toml file is invalid`() {
        // Given
        val file = MemoryVfsMix("things/a.toml" to "unknown = \"A\"")["things/a.toml"]
        // When
        val reading = { runBlocking { file.readTomlFile<Definition>() } }
        // Then
        shouldThrow<IllegalStateException> { reading() }.message.shouldContain("things/a.toml")
    }

    @Test
    fun `ReadTomlFile should fail with the file path when the toml file does not exist`() {
        // Given
        val file = MemoryVfsMix()["things/a.toml"]
        // When
        val reading = { runBlocking { file.readTomlFile<Definition>() } }
        // Then
        shouldThrow<IllegalStateException> { reading() }.message.shouldContain("things/a.toml")
    }

    @Test
    fun `FromResource should fail with the resource path when the conversion fails`() {
        // Given
        val conversion = { fromResource("things/a.toml") { error("wrong value") } }
        // When
        // Then
        shouldThrow<IllegalStateException> { conversion() }.message.shouldContain("things/a.toml").shouldContain("wrong value")
    }

    @Test
    fun `EnumOf should return the enum value when the value is one of its names`() {
        // Given
        val value = "BLUE"
        // When
        val colour = enumOf<Colour>("colour", value)
        // Then
        colour shouldBe Colour.BLUE
    }

    @Test
    fun `EnumOf should fail with the field and the allowed values when the value is not one of its names`() {
        // Given
        val value = "GREEN"
        // When
        val parsing = { enumOf<Colour>("colour", value) }
        // Then
        shouldThrow<IllegalArgumentException> { parsing() }
            .message
            .shouldContain("'colour'")
            .shouldContain("[RED, BLUE]")
            .shouldContain("GREEN")
    }

    @Test
    fun `Required should fail with the field when the value is missing`() {
        // Given
        val value: Int? = null
        // When
        val requiring = { required("damage", value) }
        // Then
        shouldThrow<IllegalArgumentException> { requiring() }.message.shouldContain("'damage' is required")
    }
}
