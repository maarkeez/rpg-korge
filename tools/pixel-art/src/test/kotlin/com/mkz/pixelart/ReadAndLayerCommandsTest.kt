package com.mkz.pixelart

import com.github.ajalt.clikt.testing.test
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.test.Test

class ReadAndLayerCommandsTest {
    private fun run(vararg argv: String): String {
        val result = pixel().test(argv.toList())
        result.statusCode shouldBe 0
        return result.stdout
    }

    private fun png(
        file: File,
        vararg argb: Int,
    ): File {
        val image = BufferedImage(argb.size, 1, BufferedImage.TYPE_INT_ARGB)
        argb.forEachIndexed { x, colour -> image.setRGB(x, 0, colour) }
        ImageIO.write(image, "png", file)
        return file
    }

    @Test
    fun `should create the layers from the top one and export an empty PNG when is new under art`() =
        withProject { root ->
            // Given
            val file = File(root, "art/unit/goblin.aseprite")
            // When
            run("new", file.path, "--size", "16x32", "--layer", "outline", "--layer", "base")
            // Then
            val sprite = SpriteFiles.readSprite(file)
            sprite.width shouldBe 16
            sprite.height shouldBe 32
            sprite.layerNamesFromTop() shouldContainExactly listOf("outline", "base")
            ImageIO.read(File(root, "resources/unit/goblin.png")).width shouldBe 16
        }

    @Test
    fun `should fail when is new and the file exists`() =
        withProject { root ->
            // Given
            val file = File(root, "art/goblin.aseprite")
            run("new", file.path, "--size", "4x4")
            // When
            val exception = shouldThrow<PixelArtException> { run("new", file.path, "--size", "4x4") }
            // Then
            exception.message shouldBe "$file already exists. Use --force to overwrite it."
        }

    @Test
    fun `should import a PNG into a layer and export the composite when is imported`() =
        withProject { root ->
            // Given
            val source = png(File(root, "source.png"), 0xff151515.toInt(), 0, 0xffffffff.toInt())
            val file = File(root, "art/bat.aseprite")
            // When
            run("import", source.path, file.path)
            run("import", source.path, file.path, "--layer", "copy")
            // Then
            val sprite = SpriteFiles.readSprite(file)
            sprite.layerNamesFromTop() shouldContainExactly listOf("copy", "base")
            sprite.layer("base").pixels shouldBe pixelsOf("W.=")
            val exported = ImageIO.read(File(root, "resources/bat.png"))
            (0 until 3).map { exported.getRGB(it, 0) } shouldContainExactly listOf(0xff151515.toInt(), 0, 0xffffffff.toInt())
        }

    @Test
    fun `should list the off-palette colours with their closest one when a PNG is imported without snapping`() =
        withProject { root ->
            // Given
            val source = png(File(root, "source.png"), 0xff161616.toInt(), 0x80151515.toInt())
            // When
            val exception = shouldThrow<PixelArtException> { run("import", source.path, File(root, "art/bat.aseprite").path) }
            // Then
            exception.message shouldContain "#161616 (1 px), closest is #151515 'W'"
            exception.message shouldContain "1 semi-transparent px"
        }

    @Test
    fun `should replace the off-palette colours and alphas when a PNG is imported snapping`() =
        withProject { root ->
            // Given
            val source = png(File(root, "source.png"), 0xff161616.toInt(), 0x80151515.toInt(), 0x7f151515)
            val file = File(root, "art/bat.aseprite")
            // When
            val output = run("import", source.path, file.path, "--snap")
            // Then
            output shouldContain "Snapped #161616 (1 px) to #151515 'W'"
            SpriteFiles.readSprite(file).layer("base").pixels shouldBe pixelsOf("WW.")
        }

    @Test
    fun `should add, move, rename, hide and remove layers when are managed`() =
        withProject { root ->
            // Given
            val file = File(root, "art/bat.aseprite")
            run("new", file.path, "--size", "2x2", "--layer", "outline", "--layer", "base")
            // When
            run("layer", "add", file.path, "shade", "--above", "base")
            run("layer", "add", file.path, "sketch", "--bottom")
            run("layer", "move", file.path, "outline", "--below", "shade")
            run("layer", "rename", file.path, "shade", "shadow")
            run("layer", "hide", file.path, "sketch")
            run("layer", "remove", file.path, "base")
            // Then
            val sprite = SpriteFiles.readSprite(file)
            sprite.layerNamesFromTop() shouldContainExactly listOf("shadow", "outline", "sketch")
            sprite.layer("sketch").visible shouldBe false
        }

    @Test
    fun `should print the composite, a layer or a pixel when is read`() =
        withProject { root ->
            // Given
            val file = File(root, "art/bat.aseprite")
            SpriteFiles.writeSprite(spriteOf(layerOf("base", "00", "00"), layerOf("outline", "W.", "..")), file)
            // When
            val composite = run("grid", file.path, "--plain")
            val layer = run("grid", file.path, "--layer", "outline", "--plain")
            val pixel = run("get", file.path, "1,0")
            val info = run("info", file.path)
            // Then
            composite shouldBe "W0\n00\n"
            layer shouldBe "W.\n..\n"
            pixel shouldBe "1,0 0 #000000 (layer 'base')\n"
            info shouldContain "outline  visible, 1 opaque px"
            info shouldContain "0 #000000 (3 px)"
        }

    @Test
    fun `should export a layer to the given PNG when is exported with out`() =
        withProject { root ->
            // Given
            val file = File(root, "sketch.aseprite")
            SpriteFiles.writeSprite(spriteOf(layerOf("base", "00"), layerOf("outline", "W.", visible = false)), file)
            val out = File(root, "outline.png")
            // When
            run("export", file.path, "--layer", "outline", "--out", out.path)
            // Then
            val exported = ImageIO.read(out)
            listOf(exported.getRGB(0, 0), exported.getRGB(1, 0)) shouldContainExactly listOf(0xff151515.toInt(), 0)
        }

    @Test
    fun `should leave the PNG untouched when its pixels did not change`() =
        withProject { root ->
            // Given
            val file = File(root, "art/bat.aseprite")
            run("new", file.path, "--size", "2x2")
            val png = File(root, "resources/bat.png")
            png.setLastModified(0)
            // When
            run("layer", "add", file.path, "sketch")
            // Then
            png.lastModified() shouldBe 0
        }
}
