package com.mkz.rpg.localization.domain

import com.mkz.rpg.shared.domain.DomainEvent

sealed interface LocalizationEvent : DomainEvent {
    data class LocalizationInitialised(
        val languageCodes: List<String>,
        val selectedLanguageCode: String,
    ) : LocalizationEvent

    data class LanguageSelected(
        val languageCode: String,
    ) : LocalizationEvent
}
