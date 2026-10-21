package resourcesbundle

import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.file.Files
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import kotlin.test.Test

class ResourcesBundleTest {
    private val sourceRoot: File = Files.createTempDirectory("resources").toFile()

    @Test
    fun `should bundle every file and folder sorted by path when writes the resources`() {
        // Given
        sourceRoot.file("unit/knight.toml", "name = \"Knight\"")
        sourceRoot.file("unit/knight.png", "png")
        sourceRoot.file("palette.png", "palette")
        // When
        val entries = bundleEntries()
        // Then
        entries.map { it.first.name }.shouldContainExactly("palette.png", "unit/", "unit/knight.png", "unit/knight.toml")
        entries.first { it.first.name == "unit/knight.toml" }.second.decodeToString() shouldBe "name = \"Knight\""
    }

    @Test
    fun `should store the files uncompressed when writes the resources`() {
        // Given
        sourceRoot.file("unit/knight.toml", "name = \"Knight\"")
        // When
        val entries = bundleEntries()
        // Then
        entries.map { it.first.method }.distinct().shouldContainExactly(ZipEntry.STORED)
    }

    @Test
    fun `should leave out the hidden files and folders when writes the resources`() {
        // Given
        sourceRoot.file("unit/.DS_Store", "hidden")
        sourceRoot.file(".hidden/knight.toml", "hidden")
        sourceRoot.file("unit/knight.toml", "name = \"Knight\"")
        // When
        val names = bundleEntries().map { it.first.name }
        // Then
        names.shouldContainExactly("unit/", "unit/knight.toml")
        names.shouldNotContain(".hidden/")
    }

    @Test
    fun `should write the same bundle when writes the same resources twice`() {
        // Given
        sourceRoot.file("unit/knight.toml", "name = \"Knight\"")
        // When
        val first = bundle()
        sourceRoot.resolve("unit/knight.toml").setLastModified(0)
        val second = bundle()
        // Then
        second.contentEquals(first) shouldBe true
    }

    private fun bundle(): ByteArray = ByteArrayOutputStream().also { ResourcesBundle.write(sourceRoot, it) }.toByteArray()

    private fun bundleEntries(): List<Pair<ZipEntry, ByteArray>> =
        ZipInputStream(ByteArrayInputStream(bundle())).use { zip ->
            generateSequence { zip.nextEntry }.map { entry -> entry to zip.readBytes() }.toList()
        }

    private fun File.file(
        path: String,
        content: String,
    ) = resolve(path).apply { parentFile.mkdirs() }.writeText(content)
}
