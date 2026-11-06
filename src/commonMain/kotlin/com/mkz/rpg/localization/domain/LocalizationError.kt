package com.mkz.rpg.localization.domain

sealed class LocalizationError(
    message: String,
) : Throwable(message = message) {
    class EmptyLanguageCode : LocalizationError(message = "Language code can not be empty")

    class InvalidPluralRule(
        languageCode: String,
        pluralRule: String,
    ) : LocalizationError(message = "Language '$languageCode' has an invalid plural rule '$pluralRule'")

    class TooManyPluralForms(
        languageCode: String,
        key: String,
        formCount: Int,
    ) : LocalizationError(message = "Message '$key' of language '$languageCode' has more than $formCount plural forms")

    class LanguageNotFound(
        languageCode: String,
    ) : LocalizationError(message = "Language '$languageCode' not found")

    class LocalizationNotFound : LocalizationError(message = "Localization not found. Was it initialised?")
}
