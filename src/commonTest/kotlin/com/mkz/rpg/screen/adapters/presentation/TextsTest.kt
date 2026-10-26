package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.localization.domain.Localization.Dto.LanguageDto
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class TextsTest {
    private val texts = TextsMother.texts()

    @Test
    fun `get should return the English text when no language is selected`() {
        // When
        val text = texts[UiText.SHOP_NOT_ENOUGH_GOLD]
        // Then
        text shouldBe "Not enough gold"
    }

    @Test
    fun `get should replace the placeholders by the arguments of the same name`() {
        // When
        val text = texts[UiText.BATTLE_INFO_PLAYER_TURN, "player" to "Player 1"]
        // Then
        text shouldBe "Player 1 turn"
    }

    @Test
    fun `plural should return the singular when the count is one`() {
        // When
        val text = texts.plural(UiText.REMAINING_TURNS, 1, "turns" to 1)
        // Then
        text shouldBe "Vanishes in 1 turn"
    }

    @Test
    fun `plural should return the plural when the count is not one`() {
        // When
        val text = texts.plural(UiText.REMAINING_TURNS, 3, "turns" to 3)
        // Then
        text shouldBe "Vanishes in 3 turns"
    }

    @Test
    fun `gold should write the amount in gold`() {
        // When
        val text = texts.gold(50)
        // Then
        text shouldBe "50 G"
    }

    @Test
    fun `gold should write the amount in gold when it is zero`() {
        // When
        val text = texts.gold(0)
        // Then
        text shouldBe "0 G"
    }

    @Test
    fun `languages should return English first, then the translations, named in their language`() {
        // When
        val languages = texts.languages()
        // Then
        languages.shouldContainExactly(LanguageDto(code = "en", name = "English"), LanguageDto(code = "es", name = "Español"))
    }

    @Test
    fun `selectLanguage should translate the texts and the game data into the language selected`() {
        // When
        texts.selectLanguage("es")
        // Then
        texts[UiText.SHOP_NOT_ENOUGH_GOLD] shouldBe "No hay suficiente oro"
        texts.plural(UiText.REMAINING_TURNS, 3, "turns" to 3) shouldBe "Desaparece en 3 turnos"
        texts.unitName("bat") shouldBe "Murciélago"
        texts.npcLine("viking") shouldBe "¡Esas bestias me tuvieron aquí atrapado durante días! Por el Valhalla, lucharé a vuestro lado."
    }

    @Test
    fun `unitName should return the English name of the unit when no language is selected`() {
        texts.unitName("hero") shouldBe "Mage"
    }
}
