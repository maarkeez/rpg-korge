package com.mkz.pixelart

import com.github.ajalt.clikt.testing.test
import io.kotest.assertions.withClue
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import java.io.File
import javax.imageio.ImageIO
import kotlin.test.Test

class FeedbackCommandsTest {
    private fun run(vararg argv: String): String {
        val result = pixel().test(argv.toList())
        result.statusCode shouldBe 0
        return result.stdout
    }

    private fun sprite(root: File): File {
        val file = File(root, "art/bat.aseprite")
        SpriteFiles.writeSprite(spriteOf(layerOf("base", "00..", "00.."), layerOf("outline", "....", "....")), file)
        return file
    }

    private fun git(
        root: File,
        vararg args: String,
    ) {
        val process = ProcessBuilder(listOf("git", "-C", root.path, "-c", "user.name=test", "-c", "user.email=test@example.com") + args).redirectErrorStream(true).start()
        val output = process.inputStream.bufferedReader().readText()
        val exitCode = process.waitFor()
        withClue(output) { exitCode shouldBe 0 }
    }

    @Test
    fun `should print the layer drawn on unless quiet when a pixel is set`() =
        withProject { root ->
            // Given
            val file = sprite(root)
            // When
            val printed = run("set", file.path, "--layer", "outline", "3,1=W")
            val quiet = run("set", file.path, "--layer", "outline", "3,0=W", "--quiet")
            // Then
            printed shouldContain
                """
                layer 'outline':
                  0123
                0 ....
                1 ...W
                """.trimIndent()
            quiet shouldNotContain "layer 'outline':"
        }

    @Test
    fun `should print every layer drawn on once when a script is applied`() =
        withProject { root ->
            // Given
            val file = sprite(root)
            val script = File(root, "script.txt").apply { writeText("set --layer outline 0,0=W\nset --layer outline 1,0=W\nset --layer base 3,1==\n") }
            // When
            val output = run("apply", file.path, script.path)
            // Then
            output.split("layer 'outline':").size shouldBe 2
            output.split("layer 'base':").size shouldBe 2
        }

    @Test
    fun `should upscale every pixel with the coordinates around when is previewed`() =
        withProject { root ->
            // Given
            val file = sprite(root)
            val out = File(root, "preview.png")
            // When
            val output = run("preview", file.path, "--out", out.path, "--scale", "20", "--no-grid")
            // Then
            output.trim() shouldBe out.absolutePath
            val image = ImageIO.read(out)
            val offset = image.width - 4 * 20
            image.height - 2 * 20 shouldBe offset
            image.getRGB(offset + 10, offset + 10) shouldBe (0xff000000.toInt())
            (image.getRGB(offset + 2 * 20 + 2, offset + 2) and 0xffffff) shouldBe 0xe6e6e6
            (image.getRGB(offset + 2 * 20 + 12, offset + 2) and 0xffffff) shouldBe 0xbdbdbd
        }

    @Test
    fun `should show only the changed pixels before and after when is compared with another sprite`() =
        withProject { root ->
            // Given
            val file = sprite(root)
            val before = File(root, "before.aseprite").also { file.copyTo(it) }
            run("set", file.path, "--layer", "outline", "2,1=W", "3,1=W", "--quiet")
            run("layer", "add", file.path, "eyes")
            // When
            val output = run("diff", file.path, "--with", before.path)
            // Then
            output shouldContain "layer 'eyes': added"
            output shouldContain "layer 'outline': 2 px changed"
            output shouldContain "2 px of the composite changed, in region 2,1,2x1."
            output shouldContain
                """
                before:
                  23
                1 ..

                legend:
                  . transparent (2 px)

                after:
                  23
                1 WW
                """.trimIndent()
        }

    @Test
    fun `should compare with the committed sprite when is compared with a git revision`() =
        withProject { root ->
            // Given
            val file = sprite(root)
            git(root, "init", "-q")
            git(root, "add", ".")
            git(root, "commit", "-q", "-m", "bat")
            run("set", file.path, "--layer", "base", "0,0=.", "--quiet")
            // When
            val changed = run("diff", file.path)
            val layer = run("diff", file.path, "--layer", "outline")
            // Then
            changed shouldContain "compared with HEAD:"
            changed shouldContain "1 px of the composite changed, in region 0,0,1x1."
            layer shouldContain "No pixel of the layer 'outline' changed."
        }
}
