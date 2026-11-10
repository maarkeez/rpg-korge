package com.mkz.rpg.localization.usecases.services

import com.mkz.rpg.localization.domain.Localization

class FakeLocalizationLoader(
    private val localization: Localization.Dto,
) : LocalizationLoader {
    override fun loadLocalization(): Localization.Dto = localization
}
