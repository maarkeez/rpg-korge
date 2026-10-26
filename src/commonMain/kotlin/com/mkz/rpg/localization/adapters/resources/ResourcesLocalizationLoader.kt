package com.mkz.rpg.localization.adapters.resources

import com.mkz.rpg.localization.domain.Localization
import com.mkz.rpg.localization.usecases.services.LocalizationLoader
import com.mkz.rpg.shared.adapters.resources.ResourcesBundle
import com.mkz.rpg.shared.adapters.toml.fromResource
import korlibs.io.file.VfsFile

/**
 * Loads the source catalog from `localization/messages.pot`, in English, and one translation per
 * `localization/<language code>.po`, e.g.: `es.po`. Only the translated entries are kept: the others fall back to English.
 */
class ResourcesLocalizationLoader(
    private val localizationRoot: VfsFile = ResourcesBundle.root[FOLDER],
) : LocalizationLoader {
    companion object {
        const val FOLDER = "localization"
        const val SOURCE_FILE_NAME = "messages.pot"
        const val SOURCE_LANGUAGE_CODE = "en"
        const val SOURCE_PLURAL_FORMS = "nplurals=2; plural=(n != 1);"
        const val TRANSLATION_EXTENSION = ".po"
        private const val PLURAL_FORMS_HEADER = "Plural-Forms"

        /** The localization of the catalog files, keyed by file name (e.g.: `messages.pot`, `es.po`), with English selected. */
        fun localizationOf(files: Map<String, String>): Localization.Dto {
            val sourceContent = files[SOURCE_FILE_NAME] ?: throw IllegalStateException("Resource '$FOLDER/$SOURCE_FILE_NAME' not found")
            val source = fromResource("$FOLDER/$SOURCE_FILE_NAME") { sourceCatalog(PoFile.parse(sourceContent)) }
            val translations =
                files
                    .filterKeys { fileName -> fileName.endsWith(TRANSLATION_EXTENSION) }
                    .entries
                    .sortedBy { (fileName, _) -> fileName }
                    .map { (fileName, content) -> fromResource("$FOLDER/$fileName") { translationCatalog(fileName.removeSuffix(TRANSLATION_EXTENSION), PoFile.parse(content)) } }
            return Localization.Dto(source = source, translations = translations, selectedLanguageCode = SOURCE_LANGUAGE_CODE)
        }

        private fun sourceCatalog(file: PoFile): Localization.Dto.CatalogDto =
            Localization.Dto.CatalogDto(
                languageCode = SOURCE_LANGUAGE_CODE,
                pluralForms = SOURCE_PLURAL_FORMS,
                messages = file.entries.mapNotNull { entry -> entry.context?.let { key -> Localization.Dto.MessageDto(key = key, forms = listOfNotNull(entry.id, entry.idPlural)) } },
            )

        private fun translationCatalog(
            languageCode: String,
            file: PoFile,
        ): Localization.Dto.CatalogDto =
            Localization.Dto.CatalogDto(
                languageCode = languageCode,
                pluralForms = file.header[PLURAL_FORMS_HEADER] ?: throw IllegalArgumentException("'$PLURAL_FORMS_HEADER' is required"),
                messages =
                    file.entries
                        .filter(PoFile.Entry::isTranslated)
                        .mapNotNull { entry -> entry.context?.let { key -> Localization.Dto.MessageDto(key = key, forms = entry.translations) } },
            )
    }

    private var localization: Localization.Dto? = null

    override suspend fun initResources() {
        val fileNames = if (localizationRoot.exists()) localizationRoot.listNames() else emptyList()
        val files =
            fileNames
                .filter { fileName -> fileName == SOURCE_FILE_NAME || fileName.endsWith(TRANSLATION_EXTENSION) }
                .associateWith { fileName -> localizationRoot[fileName].readString() }
        localization = localizationOf(files)
    }

    override fun loadLocalization(): Localization.Dto = localization ?: throw IllegalStateException("The localization resources were not read. Was initResources called?")
}
