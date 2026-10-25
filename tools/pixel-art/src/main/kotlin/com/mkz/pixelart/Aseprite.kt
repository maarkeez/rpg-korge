package com.mkz.pixelart

import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.zip.DataFormatException
import java.util.zip.Deflater
import java.util.zip.Inflater

/**
 * Reads and writes the subset of the Aseprite format the tool supports, following
 * https://github.com/aseprite/aseprite/blob/main/docs/ase-file-specs.md: one frame of image layers, in indexed colour
 * mode. Anything else is rejected, naming it, so saving a file never loses what the tool does not understand.
 */
object Aseprite {
    private const val HEADER_SIZE = 128
    private const val FILE_MAGIC = 0xA5E0
    private const val FRAME_MAGIC = 0xF1FA
    private const val INDEXED_DEPTH = 8
    private const val LAYER_OPACITY_IS_VALID = 1
    private const val LAYERS_HAVE_UUID = 4
    private const val FRAME_DURATION = 100

    private const val OLD_PALETTE_CHUNK = 0x0004
    private const val OLD_PALETTE_63_CHUNK = 0x0011
    private const val LAYER_CHUNK = 0x2004
    private const val CEL_CHUNK = 0x2005
    private const val CEL_EXTRA_CHUNK = 0x2006
    private const val COLOR_PROFILE_CHUNK = 0x2007
    private const val EXTERNAL_FILES_CHUNK = 0x2008
    private const val MASK_CHUNK = 0x2016
    private const val PATH_CHUNK = 0x2017
    private const val TAGS_CHUNK = 0x2018
    private const val PALETTE_CHUNK = 0x2019
    private const val USER_DATA_CHUNK = 0x2020
    private const val SLICE_CHUNK = 0x2022
    private const val TILESET_CHUNK = 0x2023

    private const val VISIBLE = 1
    private const val EDITABLE = 2
    private const val BACKGROUND = 8
    private const val REFERENCE = 64

    private const val RAW_CEL = 0
    private const val LINKED_CEL = 1
    private const val COMPRESSED_CEL = 2

    fun read(
        bytes: ByteArray,
        source: String,
    ): Sprite = Reader(bytes, source).read()

    fun write(sprite: Sprite): ByteArray {
        val chunks = mutableListOf(colorProfileChunk(), paletteChunk())
        sprite.layers.forEach { chunks += layerChunk(it) }
        sprite.layers.forEachIndexed { index, layer -> celChunk(index, layer.pixels)?.let { chunks += it } }

        val frameBody = chunks.fold(ByteArray(0)) { all, chunk -> all + chunk }
        val frame =
            Bytes()
                .dword(16 + frameBody.size)
                .word(FRAME_MAGIC)
                .word(minOf(chunks.size, 0xFFFF))
                .word(FRAME_DURATION)
                .zeros(2)
                .dword(chunks.size)
                .bytes(frameBody)
                .toByteArray()
        val header =
            Bytes()
                .dword(HEADER_SIZE + frame.size)
                .word(FILE_MAGIC)
                .word(1)
                .word(sprite.width)
                .word(sprite.height)
                .word(INDEXED_DEPTH)
                .dword(LAYER_OPACITY_IS_VALID)
                .word(FRAME_DURATION)
                .zeros(8)
                .byte(Palette.TRANSPARENT)
                .zeros(3)
                .word(Palette.size)
                .byte(1)
                .byte(1)
                .word(0)
                .word(0)
                .word(16)
                .word(16)
                .zeros(84)
                .toByteArray()
        return header + frame
    }

    private fun chunk(
        type: Int,
        data: ByteArray,
    ): ByteArray =
        Bytes()
            .dword(6 + data.size)
            .word(type)
            .bytes(data)
            .toByteArray()

    // sRGB, as Aseprite writes it.
    private fun colorProfileChunk(): ByteArray =
        chunk(
            COLOR_PROFILE_CHUNK,
            Bytes()
                .word(1)
                .word(0)
                .dword(0)
                .zeros(8)
                .toByteArray(),
        )

    private fun paletteChunk(): ByteArray {
        val data =
            Bytes()
                .dword(Palette.size)
                .dword(0)
                .dword(Palette.size - 1)
                .zeros(8)
        for (index in 0 until Palette.size) {
            val rgb = Palette.rgb(index)
            data.word(0)
            if (rgb == null) {
                data.zeros(4)
            } else {
                data
                    .byte(rgb shr 16 and 0xff)
                    .byte(rgb shr 8 and 0xff)
                    .byte(rgb and 0xff)
                    .byte(0xff)
            }
        }
        return chunk(PALETTE_CHUNK, data.toByteArray())
    }

    private fun layerChunk(layer: Layer): ByteArray =
        chunk(
            LAYER_CHUNK,
            Bytes()
                .word((if (layer.visible) VISIBLE else 0) or EDITABLE)
                .word(0)
                .word(0)
                .word(0)
                .word(0)
                .word(0)
                .byte(layer.opacity)
                .zeros(3)
                .string(layer.name)
                .toByteArray(),
        )

    // As Aseprite does, a cel only spans the opaque pixels of its layer, and an empty layer has no cel.
    private fun celChunk(
        layerIndex: Int,
        pixels: Pixels,
    ): ByteArray? {
        val opaque = (0 until pixels.height).flatMap { y -> (0 until pixels.width).map { x -> x to y } }.filter { (x, y) -> pixels[x, y] != Palette.TRANSPARENT }
        if (opaque.isEmpty()) return null
        val left = opaque.minOf { it.first }
        val top = opaque.minOf { it.second }
        val width = opaque.maxOf { it.first } - left + 1
        val height = opaque.maxOf { it.second } - top + 1
        val raw = ByteArray(width * height) { pixels[left + it % width, top + it / width].toByte() }
        return chunk(
            CEL_CHUNK,
            Bytes()
                .word(layerIndex)
                .word(left)
                .word(top)
                .byte(255)
                .word(COMPRESSED_CEL)
                .word(0)
                .zeros(5)
                .word(width)
                .word(height)
                .bytes(deflate(raw))
                .toByteArray(),
        )
    }

    private fun deflate(raw: ByteArray): ByteArray {
        val deflater = Deflater(Deflater.BEST_COMPRESSION)
        deflater.setInput(raw)
        deflater.finish()
        val output = ByteArrayOutputStream()
        val buffer = ByteArray(4096)
        while (!deflater.finished()) output.write(buffer, 0, deflater.deflate(buffer))
        deflater.end()
        return output.toByteArray()
    }

    private class Bytes {
        private val output = ByteArrayOutputStream()

        fun byte(value: Int) = apply { output.write(value and 0xff) }

        fun word(value: Int) = apply { byte(value).byte(value shr 8) }

        fun dword(value: Int) = apply { word(value).word(value shr 16) }

        fun zeros(count: Int) = apply { repeat(count) { byte(0) } }

        fun bytes(value: ByteArray) = apply { output.write(value) }

        fun string(value: String) =
            apply {
                val utf8 = value.toByteArray(Charsets.UTF_8)
                word(utf8.size).bytes(utf8)
            }

        fun toByteArray(): ByteArray = output.toByteArray()
    }

    private class RawLayer(
        val name: String,
        val flags: Int,
        val opacity: Int,
    )

    private class RawCel(
        val layerIndex: Int,
        val x: Int,
        val y: Int,
        val width: Int,
        val height: Int,
        val indexes: ByteArray,
    )

    private class Reader(
        bytes: ByteArray,
        private val source: String,
    ) {
        private val buffer: ByteBuffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
        private val layers = mutableListOf<RawLayer>()
        private val cels = mutableListOf<RawCel>()
        private var palette: Array<Int?>? = null
        private var oldPalette: Array<Int?>? = null

        fun read(): Sprite =
            try {
                readSprite()
            } catch (exception: java.nio.BufferUnderflowException) {
                unreadable("it ends before its last chunk")
            } catch (exception: IllegalArgumentException) {
                unreadable("a chunk points outside of the file")
            }

        private fun readSprite(): Sprite {
            if (buffer.remaining() < HEADER_SIZE) unreadable("it is shorter than an Aseprite header")
            buffer.int
            if (word() != FILE_MAGIC) unreadable("it is not an Aseprite file")
            val frames = word()
            val width = word()
            val height = word()
            val depth = word()
            val flags = buffer.int
            buffer.position(28)
            val transparentIndex = byte()
            buffer.position(HEADER_SIZE)

            if (frames != 1) unsupported("it has $frames frames, and only single frame sprites are supported")
            if (depth != INDEXED_DEPTH) unsupported("its colour mode is ${if (depth == 32) "RGBA" else "grayscale"}, and only indexed sprites are supported. Import a PNG instead")
            if (width == 0 || height == 0) unreadable("its size is ${width}x$height")

            readFrame(flags)
            val colours = palette ?: oldPalette ?: unreadable("it has no palette")
            return sprite(width, height, flags, transparentIndex, colours)
        }

        private fun readFrame(headerFlags: Int) {
            val frameStart = buffer.position()
            val frameSize = buffer.int
            if (word() != FRAME_MAGIC) unreadable("its frame header is invalid")
            val oldChunks = word()
            word()
            buffer.position(buffer.position() + 2)
            val newChunks = buffer.int
            val chunks = if (newChunks == 0) oldChunks else newChunks
            repeat(chunks) { readChunk(headerFlags) }
            buffer.position(frameStart + frameSize)
        }

        private fun readChunk(headerFlags: Int) {
            val start = buffer.position()
            val size = buffer.int
            val type = word()
            if (size < 6 || start + size > buffer.limit()) unreadable("a chunk at byte $start overflows the file")
            val end = start + size
            when (type) {
                OLD_PALETTE_CHUNK, OLD_PALETTE_63_CHUNK -> oldPalette = readOldPalette(scale = if (type == OLD_PALETTE_63_CHUNK) 4 else 1)
                LAYER_CHUNK -> readLayer(headerFlags)
                CEL_CHUNK -> readCel(end)
                CEL_EXTRA_CHUNK -> if (buffer.int != 0) unsupported("a cel has precise bounds")
                COLOR_PROFILE_CHUNK, MASK_CHUNK, PATH_CHUNK -> Unit
                PALETTE_CHUNK -> readPalette()
                TAGS_CHUNK -> if (word() > 0) unsupported("it has animation tags")
                USER_DATA_CHUNK -> if (buffer.int != 0) unsupported("it has user data (a text, a colour or properties)")
                EXTERNAL_FILES_CHUNK -> unsupported("it links external files")
                SLICE_CHUNK -> unsupported("it has slices")
                TILESET_CHUNK -> unsupported("it has tilesets")
                else -> unsupported("it has a chunk of unknown type 0x%04x".format(type))
            }
            buffer.position(end)
        }

        private fun readLayer(headerFlags: Int) {
            val flags = word()
            val type = word()
            val childLevel = word()
            word()
            word()
            val blendMode = word()
            val opacity = byte()
            buffer.position(buffer.position() + 3)
            val name = string()
            if (headerFlags and LAYERS_HAVE_UUID != 0) buffer.position(buffer.position() + 16)
            when (type) {
                0 -> Unit
                1 -> unsupported("layer '$name' is a group")
                2 -> unsupported("layer '$name' is a tilemap")
                else -> unsupported("layer '$name' has an unknown type $type")
            }
            if (childLevel != 0) unsupported("layer '$name' is inside a group")
            if (flags and REFERENCE != 0) unsupported("layer '$name' is a reference layer")
            if (blendMode != 0) unsupported("layer '$name' has a blend mode other than Normal")
            if (layers.any { it.name == name }) unsupported("there are two layers named '$name'")
            layers += RawLayer(name, flags, if (headerFlags and LAYER_OPACITY_IS_VALID != 0) opacity else 255)
        }

        private fun readCel(end: Int) {
            val layerIndex = word()
            val x = buffer.short.toInt()
            val y = buffer.short.toInt()
            val opacity = byte()
            val type = word()
            val zIndex = buffer.short.toInt()
            buffer.position(buffer.position() + 5)
            val layerName = layers.getOrNull(layerIndex)?.name ?: unreadable("a cel refers to layer $layerIndex, which does not exist")
            if (opacity != 255) unsupported("the cel of layer '$layerName' is not fully opaque")
            if (zIndex != 0) unsupported("the cel of layer '$layerName' has a z-index")
            if (cels.any { it.layerIndex == layerIndex }) unsupported("layer '$layerName' has two cels")
            val width = word()
            val height = word()
            val data = ByteArray(end - buffer.position()).also { buffer.get(it) }
            val indexes =
                when (type) {
                    RAW_CEL -> data
                    COMPRESSED_CEL -> inflate(data, width * height, layerName)
                    LINKED_CEL -> unsupported("the cel of layer '$layerName' is linked")
                    else -> unsupported("the cel of layer '$layerName' is a tilemap")
                }
            if (indexes.size < width * height) unreadable("the cel of layer '$layerName' has fewer pixels than its size")
            cels += RawCel(layerIndex, x, y, width, height, indexes)
        }

        private fun inflate(
            data: ByteArray,
            size: Int,
            layerName: String,
        ): ByteArray {
            val inflater = Inflater()
            inflater.setInput(data)
            val output = ByteArray(size)
            try {
                var read = 0
                while (read < size && !inflater.finished()) {
                    val inflated = inflater.inflate(output, read, size - read)
                    if (inflated == 0 && (inflater.needsInput() || inflater.needsDictionary())) break
                    read += inflated
                }
                if (read < size) unreadable("the cel of layer '$layerName' has fewer pixels than its size")
            } catch (exception: DataFormatException) {
                unreadable("the cel of layer '$layerName' is corrupt (${exception.message})")
            } finally {
                inflater.end()
            }
            return output
        }

        private fun readPalette() {
            val size = buffer.int
            val first = buffer.int
            val last = buffer.int
            buffer.position(buffer.position() + 8)
            val colours = palette?.copyOf(maxOf(size, palette!!.size)) ?: arrayOfNulls(size)
            for (index in first..last) {
                val flags = word()
                val red = byte()
                val green = byte()
                val blue = byte()
                val alpha = byte()
                if (flags and 1 != 0) string()
                if (index < colours.size) colours[index] = if (alpha == 255) (red shl 16) or (green shl 8) or blue else null
            }
            palette = colours
        }

        private fun readOldPalette(scale: Int): Array<Int?> {
            val colours = arrayOfNulls<Int>(256)
            var index = 0
            repeat(word()) {
                index += byte()
                val count = byte().let { if (it == 0) 256 else it }
                repeat(count) {
                    val red = byte() * scale
                    val green = byte() * scale
                    val blue = byte() * scale
                    if (index < colours.size) colours[index] = (red shl 16) or (green shl 8) or blue
                    index++
                }
            }
            return colours
        }

        private fun sprite(
            width: Int,
            height: Int,
            headerFlags: Int,
            transparentIndex: Int,
            colours: Array<Int?>,
        ): Sprite {
            val sprite = Sprite(width, height)
            layers.forEachIndexed { layerIndex, rawLayer ->
                val background = rawLayer.flags and BACKGROUND != 0
                val pixels = Pixels(width, height)
                cels.find { it.layerIndex == layerIndex }?.let { cel ->
                    for (celY in 0 until cel.height) {
                        for (celX in 0 until cel.width) {
                            val x = cel.x + celX
                            val y = cel.y + celY
                            if (!pixels.contains(x, y)) continue
                            val fileIndex = cel.indexes[celY * cel.width + celX].toInt() and 0xff
                            pixels[x, y] = paletteIndex(fileIndex, transparentIndex, background, colours, rawLayer.name)
                        }
                    }
                }
                // A background layer with no cel displays the transparent index as a colour too.
                if (background && cels.none { it.layerIndex == layerIndex }) {
                    val index = paletteIndex(transparentIndex, transparentIndex, true, colours, rawLayer.name)
                    for (y in 0 until height) for (x in 0 until width) pixels[x, y] = index
                }
                sprite.layers += Layer(rawLayer.name, pixels, visible = rawLayer.flags and VISIBLE != 0, opacity = rawLayer.opacity)
            }
            if (headerFlags and LAYER_OPACITY_IS_VALID == 0) sprite.layers.forEach { it.opacity = 255 }
            return sprite
        }

        // A background layer is opaque: Aseprite displays its pixels of the transparent index with that entry's colour.
        private fun paletteIndex(
            fileIndex: Int,
            transparentIndex: Int,
            background: Boolean,
            colours: Array<Int?>,
            layerName: String,
        ): Int {
            if (fileIndex == transparentIndex && !background) return Palette.TRANSPARENT
            val rgb = colours.getOrNull(fileIndex) ?: unsupported("layer '$layerName' uses palette entry $fileIndex, which is missing or not fully opaque")
            return Palette.indexOfRgb(rgb) ?: unsupported("layer '$layerName' uses ${Palette.hex(rgb)}, which is not a FamiCube colour (its closest one is ${Palette.describe(Palette.nearest(rgb))})")
        }

        private fun byte(): Int = buffer.get().toInt() and 0xff

        private fun word(): Int = buffer.short.toInt() and 0xffff

        private fun string(): String {
            val bytes = ByteArray(word()).also { buffer.get(it) }
            return String(bytes, Charsets.UTF_8)
        }

        private fun unsupported(reason: String): Nothing = throw PixelArtException("$source is not supported: $reason.")

        private fun unreadable(reason: String): Nothing = throw PixelArtException("$source can not be read: $reason.")
    }
}
