package com.mkz.tessavar.localization.usecases.services

import com.mkz.tessavar.localization.domain.Localization

class FakeLocalizationLoader(
    private val localization: Localization.Dto,
) : LocalizationLoader {
    override fun loadLocalization(): Localization.Dto = localization
}
