package com.mkz.tessavar.localization.domain

interface LocalizationRepository {
    fun save(localization: Localization)

    fun search(): Localization?
}
