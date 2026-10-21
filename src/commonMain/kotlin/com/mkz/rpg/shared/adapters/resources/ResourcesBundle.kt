package com.mkz.rpg.shared.adapters.resources

import korlibs.io.async.asyncImmediately
import korlibs.io.file.Vfs
import korlibs.io.file.VfsFile
import korlibs.io.file.std.openAsZip
import korlibs.io.file.std.resourcesVfs
import korlibs.io.stream.openAsync
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred

/**
 * The game resources, read from the single bundle the build makes of the `resources` folder, so they are loaded with
 * one request instead of one per file. See docs/architecture/assets.md.
 *
 * [root] reads the files as `resourcesVfs` would, e.g.: `root["unit/knight.toml"]`. The bundle is downloaded once,
 * when a file is first read or when [prefetch] starts it earlier.
 */
object ResourcesBundle {
    const val FILE_NAME = "resources.zip"

    val root: VfsFile = BundleVfs().root

    private var opening: Deferred<VfsFile>? = null
    private var opened: VfsFile? = null

    /** Starts downloading the bundle in the background, e.g.: while the menu is displayed. */
    fun prefetch(scope: CoroutineScope) {
        if (opening == null && opened == null) opening = scope.asyncImmediately { open() }
    }

    private suspend fun bundle(): VfsFile = opened ?: (opening?.await() ?: open()).also { opened = it }

    private suspend fun open(): VfsFile = resourcesVfs[FILE_NAME].readAll().openAsync().openAsZip()

    private class BundleVfs : Vfs.Proxy() {
        override suspend fun access(path: String): VfsFile = bundle()[path]
    }
}
