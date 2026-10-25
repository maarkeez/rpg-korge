package com.mkz.pixelart

import com.github.ajalt.clikt.testing.test
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import java.io.File
import kotlin.test.Test

class DrawCommandsTest {
    private fun run(vararg argv: String): String {
        val result = pixel().test(argv.toList())
        result.statusCode shouldBe 0
        return result.stdout
    }

    private fun sprite(root: File): File {
        val file = File(root, "art/bat.aseprite")
        SpriteFiles.writeSprite(spriteOf(layerOf("base", "....", "...."), layerOf("outline", "....", "....")), file)
        return file
    }

    @Test
    fun `should set pixels given by character, hex and transparent when is set`() =
        withProject { root ->
            // Given
            val file = sprite(root)
            run("set", file.path, "--layer", "base", "0,0==", "1,0=#151515", "2,0=0")
            // When
            run("set", file.path, "--layer", "base", "2,0=.")
            // Then
            SpriteFiles.readSprite(file).layer("base").pixels shouldBe pixelsOf("=W..", "....")
        }

    @Test
    fun `should paint a grid read from a file when is painted`() =
        withProject { root ->
            // Given
            val file = sprite(root)
            val grid = File(root, "grid.txt").apply { writeText("W-W\n.W.\n") }
            // When
            val output = run("paint", file.path, "--layer", "outline", "--at", "1,0", grid.path)
            // Then
            output shouldContain "Painted a 3x2 grid at 1,0, 3 px changed on layer 'outline'"
            SpriteFiles.readSprite(file).layer("outline").pixels shouldBe pixelsOf(".W.W", "..W.")
        }

    @Test
    fun `should shift by negative offsets when are given as options`() =
        withProject { root ->
            // Given
            val file = sprite(root)
            run("set", file.path, "--layer", "base", "3,1=W")
            // When
            run("shift", file.path, "--layer", "base", "--dx", "-3", "--dy", "-1")
            // Then
            SpriteFiles.readSprite(file).layer("base").pixels shouldBe pixelsOf("W...", "....")
        }

    @Test
    fun `should copy a layer of another file when is copied from it`() =
        withProject { root ->
            // Given
            val file = sprite(root)
            val other = File(root, "art/other.aseprite")
            SpriteFiles.writeSprite(spriteOf(layerOf("eyes", "=0")), other)
            // When
            run("copy", file.path, "--layer", "base", "--from", other.path, "--from-layer", "eyes", "--region", "0,0,2x1", "--at", "2,1")
            // Then
            SpriteFiles.readSprite(file).layer("base").pixels shouldBe pixelsOf("....", "..=0")
        }

    @Test
    fun `should run every line and save once when a script is applied`() =
        withProject { root ->
            // Given
            val file = sprite(root)
            val script =
                File(root, "script.txt").apply {
                    writeText(
                        """
                        # The body
                        rect --layer base 0,0,2x2 0 --filled
                        layer add 'eye shine' --top
                        paint --layer outline --at 0,0
                        W-
                        end

                        mirror --layer base --from left
                        set --layer "eye shine" 3,0==
                        """.trimIndent(),
                    )
                }
            // When
            val output = run("apply", file.path, script.path)
            // Then
            val sprite = SpriteFiles.readSprite(file)
            sprite.layerNamesFromTop() shouldContainExactly listOf("eye shine", "outline", "base")
            sprite.layer("base").pixels shouldBe pixelsOf("0000", "0000")
            sprite.layer("outline").pixels shouldBe pixelsOf("W...", "....")
            sprite.composite() shouldBe pixelsOf("W00=", "0000")
            output.lines().count { it.startsWith("Saved") } shouldBe 1
        }

    @Test
    fun `should save nothing naming the line when a line of a script fails`() =
        withProject { root ->
            // Given
            val file = sprite(root)
            val before = file.readBytes()
            val script = File(root, "script.txt").apply { writeText("set --layer base 0,0=W\nfill --layer base 9,9 W\n") }
            // When
            val exception = shouldThrow<PixelArtException> { run("apply", file.path, script.path) }
            // Then
            exception.message shouldBe "Line 2 'fill --layer base 9,9 W': 9,9 is outside of the 4x2 sprite. Nothing was saved."
            file.readBytes() shouldBe before
        }

    @Test
    fun `should explain the usage naming the line when a line of a script is malformed`() =
        withProject { root ->
            // Given
            val file = sprite(root)
            val script = File(root, "script.txt").apply { writeText("rect 0,0,1x1 W\n") }
            // When
            val exception = shouldThrow<PixelArtException> { run("apply", file.path, script.path) }
            // Then
            exception.message shouldContain "Line 1 'rect 0,0,1x1 W'"
            exception.message shouldContain "--layer"
        }

    @Test
    fun `should fail when the grid of a paint line does not end`() =
        withProject { root ->
            // Given
            val file = sprite(root)
            val script = File(root, "script.txt").apply { writeText("paint --layer base --at 0,0\nWW\n") }
            // When
            val exception = shouldThrow<PixelArtException> { run("apply", file.path, script.path) }
            // Then
            exception.message shouldBe "Line 1: the grid of 'paint' does not end with a line 'end'. Nothing was saved."
        }
}
