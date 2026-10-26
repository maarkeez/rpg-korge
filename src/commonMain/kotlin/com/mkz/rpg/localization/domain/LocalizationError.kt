package com.mkz.rpg.localization.domain

sealed class LocalizationError(
    message: String,
) : Throwable(message = message) {
    class EmptyLanguageCode : LocalizationError(message = "Language code can not be empty")

    class InvalidPluralForms(
        languageCode: String,
        pluralForms: String,
    ) : LocalizationError(message = "Language '$languageCode' has invalid plural forms '$pluralForms'")

    class TooManyPluralForms(
        languageCode: String,
        key: String,
        count: Int,
    ) : LocalizationError(message = "Message '$key' of language '$languageCode' has more than $count plural forms")

    class LanguageNotFound(
        languageCode: String,
    ) : LocalizationError(message = "Language '$languageCode' not found")

    class LocalizationNotFound : LocalizationError(message = "Localization not found. Was it initialised?")
}
