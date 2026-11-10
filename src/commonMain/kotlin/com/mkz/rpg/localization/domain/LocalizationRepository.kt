package com.mkz.rpg.localization.domain

interface LocalizationRepository {
    fun save(localization: Localization)

    fun search(): Localization?
}
