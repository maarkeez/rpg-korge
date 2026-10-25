package com.mkz.pixelart

import java.io.ByteArrayOutputStream
import java.util.zip.Deflater

/** Assembles Aseprite files byte by byte, following the spec, for the cases the tool never writes itself. */
class AseBytes(
    private val width: Int,
    private val height: Int,
    private val depth: Int = 8,
    private val frames: Int = 1,
    private val headerFlags: Int = 1,
    private val transparentIndex: Int = 0,
) {
    private val chunks = mutableListOf<ByteArray>()

    fun palette(vararg argb: Long) =
        apply {
            val data =
                Le()
                    .dword(argb.size)
                    .dword(0)
                    .dword(argb.size - 1)
                    .zeros(8)
            argb.forEach {
                data
                    .word(0)
                    .byte((it shr 16).toInt())
                    .byte((it shr 8).toInt())
                    .byte(it.toInt())
                    .byte((it shr 24).toInt())
            }
            chunk(0x2019, data.bytes())
        }

    fun layer(
        name: String,
        flags: Int = 3,
        type: Int = 0,
        childLevel: Int = 0,
        blendMode: Int = 0,
        opacity: Int = 255,
    ) = apply {
        chunk(
            0x2004,
            Le()
                .word(flags)
                .word(type)
                .word(childLevel)
                .word(0)
                .word(0)
                .word(blendMode)
                .byte(opacity)
                .zeros(3)
                .string(name)
                .bytes(),
        )
    }

    fun cel(
        layerIndex: Int,
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        indexes: List<Int>,
        type: Int = 2,
        opacity: Int = 255,
        zIndex: Int = 0,
    ) = apply {
        val raw = ByteArray(indexes.size) { indexes[it].toByte() }
        val data = if (type == 2) deflate(raw) else raw
        chunk(
            0x2005,
            Le()
                .word(layerIndex)
                .word(x)
                .word(y)
                .byte(opacity)
                .word(type)
                .word(zIndex)
                .zeros(5)
                .word(width)
                .word(height)
                .raw(data)
                .bytes(),
        )
    }

    fun chunk(
        type: Int,
        data: ByteArray,
    ) = apply {
        chunks +=
            Le()
                .dword(6 + data.size)
                .word(type)
                .raw(data)
                .bytes()
    }

    fun bytes(): ByteArray {
        val body = chunks.fold(ByteArray(0)) { all, chunk -> all + chunk }
        val frame =
            Le()
                .dword(16 + body.size)
                .word(0xF1FA)
                .word(chunks.size)
                .word(100)
                .zeros(2)
                .dword(chunks.size)
                .raw(body)
                .bytes()
        val header =
            Le()
                .dword(128 + frame.size * frames)
                .word(0xA5E0)
                .word(frames)
                .word(width)
                .word(height)
                .word(depth)
                .dword(headerFlags)
                .word(100)
                .zeros(8)
                .byte(transparentIndex)
                .zeros(3)
                .word(0)
                .zeros(2 + 8 + 84)
                .bytes()
        return (0 until frames).fold(header) { all, _ -> all + frame }
    }

    private fun deflate(raw: ByteArray): ByteArray {
        val deflater = Deflater()
        deflater.setInput(raw)
        deflater.finish()
        val buffer = ByteArray(raw.size + 64)
        val size = deflater.deflate(buffer)
        deflater.end()
        return buffer.copyOf(size)
    }

    private class Le {
        private val output = ByteArrayOutputStream()

        fun byte(value: Int) = apply { output.write(value and 0xff) }

        fun word(value: Int) = apply { byte(value).byte(value shr 8) }

        fun dword(value: Int) = apply { word(value).word(value shr 16) }

        fun zeros(count: Int) = apply { repeat(count) { byte(0) } }

        fun raw(value: ByteArray) = apply { output.write(value) }

        fun string(value: String) = apply { word(value.length).raw(value.toByteArray()) }

        fun bytes(): ByteArray = output.toByteArray()
    }
}
