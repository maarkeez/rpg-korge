package com.mkz.rpg.localization.usecases.services

import com.mkz.rpg.localization.domain.Localization

interface LocalizationLoader {
    suspend fun initResources() {}

    /** The source catalog and its translations, with the source language selected. */
    fun loadLocalization(): Localization.Dto
}
