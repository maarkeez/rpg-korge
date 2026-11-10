package com.mkz.rpg.localization.usecases.services

import com.mkz.rpg.localization.domain.Localization

interface LocalizationLoader {
    suspend fun initResources() {}

    fun loadLocalization(): Localization.Dto
}
