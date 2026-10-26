package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.localization.adapters.presentation.LocalizationApi

/**
 * The texts the screen displays, translated into the selected language, e.g.: `texts[UiText.SHOP_NOT_ENOUGH_GOLD]`.
 * The placeholders of a text are replaced by the arguments of the same name, e.g.: `texts[UiText.BATTLE_INFO_ROUND, "round" to 3]`.
 */
class Texts(
    private val localizationApi: LocalizationApi,
) {
    operator fun get(
        text: UiText,
        vararg arguments: Pair<String, Any>,
    ): String = localizationApi.translate(text.key, arguments.toArguments())

    /** The plural form of the [text] for [count], e.g.: `Vanishes in 3 turns`. */
    fun plural(
        text: UiText,
        count: Int,
        vararg arguments: Pair<String, Any>,
    ): String = localizationApi.translatePlural(text.key, count, arguments.toArguments())

    /** An amount of gold, e.g.: `50 G`. */
    fun gold(amount: Int): String = get(UiText.GOLD, "gold" to amount)

    private fun Array<out Pair<String, Any>>.toArguments(): Map<String, String> = associate { (name, value) -> name to value.toString() }
}
