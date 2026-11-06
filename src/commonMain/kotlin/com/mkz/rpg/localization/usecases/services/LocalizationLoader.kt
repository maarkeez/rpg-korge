package com.mkz.rpg.localization.usecases.services

import com.mkz.rpg.localization.domain.Localization

interface LocalizationLoader {
    suspend fun initResources() {}

    /** The source catalog and the catalog of every other language, with the source language selected. */
    fun loadLocalization(): Localization.Dto
}
