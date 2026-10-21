package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.shared.adapters.resources.ResourcesBundle
import korlibs.image.bitmap.Bitmap
import korlibs.image.format.readBitmap
import korlibs.io.file.VfsFile

/**
 * Loads every image under [root] just once, so views can share them.
 * Images are identified by their path relative to [root], e.g.: `unit/knight.png`.
 * New images are discovered automatically: there is no need to change this loader when adding them.
 */
class BattleSceneResourcesLoader(
    private val root: VfsFile = ResourcesBundle.root,
) {
    companion object {
        private val IMAGE_EXTENSIONS = setOf("png", "jpg", "jpeg")
        private const val PATH_SEPARATOR = "/"
    }

    private var bitmaps: Map<String, Bitmap> = emptyMap()

    suspend fun load() {
        bitmaps = buildMap { loadBitmaps(directory = root, directoryPath = "") }
    }

    fun bitmap(path: String): Bitmap = bitmapOrNull(path) ?: throw IllegalStateException("Resource '$path' not found. Was it loaded?")

    fun bitmapOrNull(path: String): Bitmap? = bitmaps[path]

    fun bitmaps(directory: String): Map<String, Bitmap> {
        val directoryPrefix = directory.removeSuffix(PATH_SEPARATOR) + PATH_SEPARATOR
        return bitmaps
            .filterKeys { path -> path.startsWith(directoryPrefix) && !path.removePrefix(directoryPrefix).contains(PATH_SEPARATOR) }
            .mapKeys { (path, _) -> path.removePrefix(directoryPrefix) }
    }

    private suspend fun MutableMap<String, Bitmap>.loadBitmaps(
        directory: VfsFile,
        directoryPath: String,
    ) {
        directory
            .listNames()
            .map { name -> name.removeSuffix(PATH_SEPARATOR) }
            .filterNot { name -> name.startsWith(".") }
            .forEach { name ->
                val file = directory[name]
                val path = directoryPath + name
                if (file.isDirectory()) {
                    loadBitmaps(directory = file, directoryPath = path + PATH_SEPARATOR)
                } else if (name.substringAfterLast(".").lowercase() in IMAGE_EXTENSIONS) {
                    put(path, file.readBitmap())
                }
            }
    }
}
