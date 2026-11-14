package com.mkz.tessavar.shared.adapters.resources

import korlibs.io.async.asyncImmediately
import korlibs.io.file.Vfs
import korlibs.io.file.VfsFile
import korlibs.io.file.std.openAsZip
import korlibs.io.file.std.resourcesVfs
import korlibs.io.stream.openAsync
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred

object ResourcesBundle {
    const val FILE_NAME = "resources.zip"

    val root: VfsFile = BundleVfs().root

    private var opening: Deferred<VfsFile>? = null
    private var opened: VfsFile? = null

    fun prefetch(scope: CoroutineScope) {
        if (opening == null && opened == null) opening = scope.asyncImmediately { open() }
    }

    private suspend fun bundle(): VfsFile = opened ?: (opening?.await() ?: open()).also { opened = it }

    private suspend fun open(): VfsFile = resourcesVfs[FILE_NAME].readAll().openAsync().openAsZip()

    private class BundleVfs : Vfs.Proxy() {
        override suspend fun access(path: String): VfsFile = bundle()[path]
    }
}
