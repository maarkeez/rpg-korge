package resourcesbundle

import java.io.File
import java.io.OutputStream
import java.util.zip.CRC32
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

// Writes every file of a resources folder into a single zip, so the game reads them all with a single request.
// The entries are stored uncompressed (the resources are small and the images already compressed), sorted and with a
// fixed time, so the same resources always make the same bundle. Hidden files are left out.
object ResourcesBundle {
    private const val FIXED_TIME = 315532800000L // 1980-01-01, the earliest time a zip entry can hold

    fun write(
        sourceRoot: File,
        output: OutputStream,
    ) {
        ZipOutputStream(output).use { zip ->
            zip.setMethod(ZipOutputStream.STORED)
            sourceRoot
                .walkTopDown()
                .onEnter { it == sourceRoot || !it.isHidden() }
                .filter { it != sourceRoot && !it.isHidden() }
                .sortedBy { it.relativeTo(sourceRoot).invariantSeparatorsPath }
                .forEach { file ->
                    val path = file.relativeTo(sourceRoot).invariantSeparatorsPath
                    if (file.isDirectory) zip.putStored("$path/", ByteArray(0)) else zip.putStored(path, file.readBytes())
                }
        }
    }

    private fun File.isHidden(): Boolean = name.startsWith(".")

    private fun ZipOutputStream.putStored(
        path: String,
        bytes: ByteArray,
    ) {
        val entry = ZipEntry(path)
        entry.time = FIXED_TIME
        entry.size = bytes.size.toLong()
        entry.compressedSize = bytes.size.toLong()
        entry.crc = CRC32().apply { update(bytes) }.value
        putNextEntry(entry)
        write(bytes)
        closeEntry()
    }
}
