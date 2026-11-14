package com.mkz.tessavar.localization.adapters.resources

import com.mkz.tessavar.localization.domain.Localization
import com.mkz.tessavar.localization.usecases.services.LocalizationLoader
import com.mkz.tessavar.shared.adapters.resources.ResourcesBundle
import com.mkz.tessavar.shared.adapters.toml.fromResource
import korlibs.io.file.VfsFile

class ResourcesLocalizationLoader(
    private val localizationRoot: VfsFile = ResourcesBundle.root[FOLDER],
) : LocalizationLoader {
    companion object {
        const val FOLDER = "localization"
        const val SOURCE_FILE_NAME = "messages.pot"
        const val SOURCE_LANGUAGE_CODE = "en"
        const val SOURCE_PLURAL_RULE = "nplurals=2; plural=(n != 1);"
        const val TRANSLATION_CATALOG_EXTENSION = ".po"
        private const val PLURAL_FORMS_HEADER = "Plural-Forms"

        fun localizationOf(files: Map<String, String>): Localization.Dto {
            val sourceContent = files[SOURCE_FILE_NAME] ?: throw IllegalStateException("Resource '$FOLDER/$SOURCE_FILE_NAME' not found")
            val sourceCatalog = fromResource("$FOLDER/$SOURCE_FILE_NAME") { sourceCatalog(PoFile.parse(sourceContent)) }
            val translationCatalogs =
                files
                    .filterKeys { fileName -> fileName.endsWith(TRANSLATION_CATALOG_EXTENSION) }
                    .entries
                    .sortedBy { (fileName, _) -> fileName }
                    .map { (fileName, content) -> fromResource("$FOLDER/$fileName") { translationCatalog(fileName.removeSuffix(TRANSLATION_CATALOG_EXTENSION), PoFile.parse(content)) } }
            return Localization.Dto(sourceCatalog = sourceCatalog, translationCatalogs = translationCatalogs, selectedLanguageCode = SOURCE_LANGUAGE_CODE)
        }

        private fun sourceCatalog(file: PoFile): Localization.Dto.CatalogDto =
            Localization.Dto.CatalogDto(
                languageCode = SOURCE_LANGUAGE_CODE,
                pluralRule = SOURCE_PLURAL_RULE,
                messages = file.entries.mapNotNull { entry -> entry.context?.let { key -> Localization.Dto.MessageDto(key = key, forms = listOfNotNull(entry.id, entry.idPlural)) } },
            )

        private fun translationCatalog(
            languageCode: String,
            file: PoFile,
        ): Localization.Dto.CatalogDto =
            Localization.Dto.CatalogDto(
                languageCode = languageCode,
                pluralRule = file.header[PLURAL_FORMS_HEADER] ?: throw IllegalArgumentException("'$PLURAL_FORMS_HEADER' is required"),
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
                .filter { fileName -> fileName == SOURCE_FILE_NAME || fileName.endsWith(TRANSLATION_CATALOG_EXTENSION) }
                .associateWith { fileName -> localizationRoot[fileName].readString() }
        localization = localizationOf(files)
    }

    override fun loadLocalization(): Localization.Dto = localization ?: throw IllegalStateException("The localization resources were not read. Was initResources called?")
}
