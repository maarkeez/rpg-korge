package com.mkz.pixelart

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import korlibs.image.bitmap.extract
import korlibs.image.format.ASE
import korlibs.image.format.ImageDecodingProps
import korlibs.image.format.onlyReadVisibleLayers
import korlibs.io.stream.openSync
import kotlin.test.Test

class AsepriteTest {
    private val sprite =
        spriteOf(
            layerOf("base", "....", ".==.", ".==.", "...."),
            layerOf("hidden", "0...", "....", "....", "...0", visible = false),
            layerOf("outline", "WWWW", "W..W", "W..W", "WWWW", opacity = 128),
            layerOf("empty", "....", "....", "....", "...."),
        )

    @Test
    fun `should read the layers it wrote when is written`() {
        // When
        val read = Aseprite.read(Aseprite.write(sprite), "sprite.aseprite")
        // Then
        read.width shouldBe 4
        read.height shouldBe 4
        read.layers.map { it.name } shouldContainExactly listOf("base", "hidden", "outline", "empty")
        read.layers.map { it.visible } shouldContainExactly listOf(true, false, true, true)
        read.layers.map { it.opacity } shouldContainExactly listOf(255, 255, 128, 255)
        read.layers.map { it.pixels } shouldContainExactly sprite.layers.map { it.pixels }
    }

    @Test
    fun `should be read by KorGE's Aseprite reader when is written`() {
        // Given
        val props = ImageDecodingProps(premultiplied = false).also { it.onlyReadVisibleLayers = false }
        // When
        val read = ASE.readImage(Aseprite.write(sprite).openSync(), props)
        // Then
        read.width shouldBe 4
        read.height shouldBe 4
        read.layers.map { it.name } shouldContainExactly listOf("base", "hidden", "outline", "empty")
        val frame = read.frames.single()
        for (layer in sprite.layers) {
            val korgeLayer = frame.layerData.find { it.layer.name == layer.name }
            for (y in 0 until 4) {
                for (x in 0 until 4) {
                    val rgba =
                        korgeLayer?.let {
                            val celX = x - it.targetX
                            val celY = y - it.targetY
                            val bitmap = it.slice.extract().toBMP32()
                            if (celX in 0 until bitmap.width && celY in 0 until bitmap.height) bitmap.getRgbaRaw(celX, celY) else null
                        }
                    val expected = Palette.rgb(layer.pixels[x, y])
                    val actual = rgba?.takeIf { it.a > 0 }?.let { (it.r shl 16) or (it.g shl 8) or it.b }
                    if (actual != null) rgba.a shouldBe 255
                    actual shouldBe expected
                }
            }
        }
    }

    @Test
    fun `should read the pixels of a cel away from the top-left pixel when its palette has another order`() {
        // Given
        val bytes =
            AseBytes(4, 3)
                .palette(0x00000000, 0xffffffff, 0xff151515)
                .layer("base")
                .cel(0, 1, 1, 2, 2, listOf(1, 2, 0, 1))
                .bytes()
        // When
        val read = Aseprite.read(bytes, "sprite.aseprite")
        // Then
        read.layer("base").pixels shouldBe pixelsOf("....", ".=W.", "..=.")
    }

    @Test
    fun `should read a raw cel when is not compressed`() {
        // Given
        val bytes =
            AseBytes(2, 1)
                .palette(0x00000000, 0xff151515)
                .layer("base")
                .cel(0, 0, 0, 2, 1, listOf(1, 0), type = 0)
                .bytes()
        // When
        val read = Aseprite.read(bytes, "sprite.aseprite")
        // Then
        read.layer("base").pixels shouldBe pixelsOf("W.")
    }

    @Test
    fun `should display the colour of the transparent index when is a background layer`() {
        // Given
        val bytes =
            AseBytes(2, 1)
                .palette(0xff000000, 0xff151515)
                .layer("Background", flags = 3 or 8)
                .cel(0, 0, 0, 2, 1, listOf(1, 0))
                .bytes()
        // When
        val read = Aseprite.read(bytes, "sprite.aseprite")
        // Then
        read.layer("Background").pixels shouldBe pixelsOf("${OUTLINE}$BLACK")
    }

    @Test
    fun `should fail naming what is not supported when the file has it`() {
        val transparent = 0x00000000L
        val outline = 0xff151515L
        val cases =
            mapOf(
                "it has 2 frames" to AseBytes(1, 1, frames = 2).palette(transparent).bytes(),
                "its colour mode is RGBA" to AseBytes(1, 1, depth = 32).bytes(),
                "layer 'group' is a group" to AseBytes(1, 1).palette(transparent).layer("group", type = 1).bytes(),
                "layer 'tiles' is a tilemap" to AseBytes(1, 1).palette(transparent).layer("tiles", type = 2).bytes(),
                "layer 'child' is inside a group" to AseBytes(1, 1).palette(transparent).layer("child", childLevel = 1).bytes(),
                "layer 'shade' has a blend mode other than Normal" to AseBytes(1, 1).palette(transparent).layer("shade", blendMode = 1).bytes(),
                "the cel of layer 'base' is not fully opaque" to
                    AseBytes(1, 1)
                        .palette(transparent, outline)
                        .layer("base")
                        .cel(0, 0, 0, 1, 1, listOf(1), opacity = 10)
                        .bytes(),
                "the cel of layer 'base' has a z-index" to
                    AseBytes(1, 1)
                        .palette(transparent, outline)
                        .layer("base")
                        .cel(0, 0, 0, 1, 1, listOf(1), zIndex = 1)
                        .bytes(),
                "the cel of layer 'base' is linked" to
                    AseBytes(1, 1)
                        .palette(transparent)
                        .layer("base")
                        .cel(0, 0, 0, 0, 0, emptyList(), type = 1)
                        .bytes(),
                "it has animation tags" to AseBytes(1, 1).palette(transparent).chunk(0x2018, byteArrayOf(1, 0) + ByteArray(8)).bytes(),
                "it has user data" to AseBytes(1, 1).palette(transparent).chunk(0x2020, byteArrayOf(1, 0, 0, 0, 0, 0)).bytes(),
                "it has slices" to AseBytes(1, 1).palette(transparent).chunk(0x2022, ByteArray(12)).bytes(),
                "it has a chunk of unknown type 0x7777" to AseBytes(1, 1).palette(transparent).chunk(0x7777, ByteArray(0)).bytes(),
                "layer 'base' uses #123456, which is not a FamiCube colour" to
                    AseBytes(1, 1)
                        .palette(transparent, 0xff123456)
                        .layer("base")
                        .cel(0, 0, 0, 1, 1, listOf(1))
                        .bytes(),
                "there are two layers named 'base'" to
                    AseBytes(1, 1)
                        .palette(transparent)
                        .layer("base")
                        .layer("base")
                        .bytes(),
            )
        for ((reason, bytes) in cases) {
            // When
            val exception = shouldThrow<PixelArtException> { Aseprite.read(bytes, "sprite.aseprite") }
            // Then
            exception.message shouldContain "sprite.aseprite is not supported: $reason"
        }
    }

    @Test
    fun `should fail when the file is not an Aseprite file or is cut short`() {
        // Given
        val written = Aseprite.write(sprite)
        // When
        val notAseprite = shouldThrow<PixelArtException> { Aseprite.read(ByteArray(200), "sprite.aseprite") }
        val cutShort = shouldThrow<PixelArtException> { Aseprite.read(written.copyOf(written.size - 10), "sprite.aseprite") }
        // Then
        notAseprite.message shouldBe "sprite.aseprite can not be read: it is not an Aseprite file."
        cutShort.message shouldContain "sprite.aseprite can not be read"
    }

    @Test
    fun `should ignore unused palette entries when they are not FamiCube colours`() {
        // Given
        val bytes =
            AseBytes(1, 1)
                .palette(0x00000000, 0xff123456, 0xff151515)
                .layer("base")
                .cel(0, 0, 0, 1, 1, listOf(2))
                .bytes()
        // When
        val read = Aseprite.read(bytes, "sprite.aseprite")
        // Then
        read.layer("base").pixels shouldBe pixelsOf("$OUTLINE")
    }
}
