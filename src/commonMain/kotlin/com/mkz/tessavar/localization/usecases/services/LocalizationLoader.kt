package com.mkz.tessavar.localization.usecases.services

import com.mkz.tessavar.localization.domain.Localization

interface LocalizationLoader {
    suspend fun initResources() {}

    fun loadLocalization(): Localization.Dto
}
