package com.mkz.rpg.localization.domain

import com.mkz.rpg.localization.domain.Localization.Dto.CatalogDto
import com.mkz.rpg.localization.domain.Localization.Dto.MessageDto

object LocalizationMother {
    const val ENGLISH_PLURAL_FORMS = "nplurals=2; plural=(n != 1);"

    // Polish has three plural forms: one, a few (2-4, 22-24, ...) and many.
    const val POLISH_PLURAL_FORMS = "nplurals=3; plural=(n==1 ? 0 : n%10>=2 && n%10<=4 && (n%100<10 || n%100>=20) ? 1 : 2);"

    fun english() =
        CatalogDto(
            languageCode = "en",
            pluralForms = ENGLISH_PLURAL_FORMS,
            messages =
                listOf(
                    MessageDto(key = Localization.LANGUAGE_NAME_KEY, forms = listOf("English")),
                    MessageDto(key = "ui.shop.buy", forms = listOf("Buy")),
                    MessageDto(key = "ui.shop.sell", forms = listOf("Sell")),
                    MessageDto(key = "ui.battleInfo.round", forms = listOf("Round {round}")),
                    MessageDto(key = "ui.remainingTurns.vanishes", forms = listOf("Vanishes in {turns} turn", "Vanishes in {turns} turns")),
                ),
        )

    fun spanish() =
        CatalogDto(
            languageCode = "es",
            pluralForms = ENGLISH_PLURAL_FORMS,
            messages =
                listOf(
                    MessageDto(key = Localization.LANGUAGE_NAME_KEY, forms = listOf("Español")),
                    MessageDto(key = "ui.shop.buy", forms = listOf("Comprar")),
                    MessageDto(key = "ui.battleInfo.round", forms = listOf("Ronda {round}")),
                    MessageDto(key = "ui.remainingTurns.vanishes", forms = listOf("Desaparece en {turns} turno", "Desaparece en {turns} turnos")),
                ),
        )

    fun polish() =
        CatalogDto(
            languageCode = "pl",
            pluralForms = POLISH_PLURAL_FORMS,
            messages =
                listOf(
                    MessageDto(key = "ui.remainingTurns.vanishes", forms = listOf("Znika za {turns} turę", "Znika za {turns} tury", "Znika za {turns} tur")),
                ),
        )

    fun localization(
        source: CatalogDto = english(),
        translations: List<CatalogDto> = listOf(spanish()),
        selectedLanguageCode: String = "en",
    ) = Localization.create(Localization.Dto(source = source, translations = translations, selectedLanguageCode = selectedLanguageCode))
}
