package com.mkz.pixelart

import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

object SpriteFiles {
    fun readSprite(file: File): Sprite {
        if (!file.isFile) throw PixelArtException("$file does not exist. Create it with 'pixel new' or 'pixel import'.")
        return Aseprite.read(file.readBytes(), file.path)
    }

    /** Writes through a temporary file, so a failure never leaves a half written sprite. */
    fun writeSprite(
        sprite: Sprite,
        file: File,
    ) {
        file.absoluteFile.parentFile.mkdirs()
        val temporary = File(file.absoluteFile.parentFile, ".${file.name}.tmp")
        temporary.writeBytes(Aseprite.write(sprite))
        replace(temporary, file)
    }

    /** Writes the sprite and, when it is under art/ and [export] is set, its PNG under resources/, which is returned. */
    fun save(
        sprite: Sprite,
        file: File,
        export: Boolean,
    ): File? {
        writeSprite(sprite, file)
        val png = if (export) exportedPng(file) else null
        png?.let { Png.write(sprite.composite(), it) }
        return png
    }

    fun replace(
        temporary: File,
        file: File,
    ) {
        Files.move(temporary.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE)
    }

    /**
     * The PNG a source under an `art` folder is exported to: the same path under the `resources` folder next to it,
     * e.g.: `art/unit/knight.aseprite` to `resources/unit/knight.png`. `null` when the source is not under `art`.
     */
    fun exportedPng(source: File): File? {
        val absolute = source.absoluteFile.normalize()
        var folder: File? = absolute.parentFile
        val relative = mutableListOf(absolute.nameWithoutExtension + ".png")
        while (folder != null && folder.name != "art") {
            relative.add(0, folder.name)
            folder = folder.parentFile
        }
        val root = folder?.parentFile ?: return null
        return relative.fold(File(root, "resources")) { path, part -> File(path, part) }
    }
}
