package com.mkz.tessavar.localization.domain

import com.mkz.tessavar.shared.domain.DomainEvent

sealed interface LocalizationEvent : DomainEvent {
    data class LocalizationInitialised(
        val languageCodes: List<String>,
        val selectedLanguageCode: String,
    ) : LocalizationEvent

    data class LanguageSelected(
        val languageCode: String,
    ) : LocalizationEvent
}
