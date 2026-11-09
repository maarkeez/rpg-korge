package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.localization.domain.Localization.Dto.LanguageDto
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class MessagesTest {
    private val messages = MessagesMother.messages()

    @Test
    fun `get should return the English text when no language is selected`() {
        // When
        val text = messages[UiMessage.SHOP_NOT_ENOUGH_GOLD]
        // Then
        text shouldBe "Not enough gold"
    }

    @Test
    fun `get should replace the placeholders by the arguments of the same name`() {
        // When
        val text = messages[UiMessage.BANNER_PLAYER_TURN, "player" to "Player 1"]
        // Then
        text shouldBe "Player 1 turn"
    }

    @Test
    fun `plural should return the singular when the count is one`() {
        // When
        val text = messages.plural(UiMessage.REMAINING_SUMMON_DURATION, 1, "turns" to 1)
        // Then
        text shouldBe "Vanishes in 1 turn"
    }

    @Test
    fun `plural should return the plural when the count is not one`() {
        // When
        val text = messages.plural(UiMessage.REMAINING_SUMMON_DURATION, 3, "turns" to 3)
        // Then
        text shouldBe "Vanishes in 3 turns"
    }

    @Test
    fun `gold should write the amount in gold`() {
        // When
        val text = messages.gold(50)
        // Then
        text shouldBe "50 G"
    }

    @Test
    fun `gold should write the amount in gold when it is zero`() {
        // When
        val text = messages.gold(0)
        // Then
        text shouldBe "0 G"
    }

    @Test
    fun `languages should return English first, then the translations, named in their language`() {
        // When
        val languages = messages.languages()
        // Then
        languages.shouldContainExactly(LanguageDto(code = "en", name = "English"), LanguageDto(code = "es", name = "Español"))
    }

    @Test
    fun `selectLanguage should translate the messages and the game data into the language selected`() {
        // When
        messages.selectLanguage("es")
        // Then
        messages[UiMessage.SHOP_NOT_ENOUGH_GOLD] shouldBe "No hay suficiente oro"
        messages.plural(UiMessage.REMAINING_SUMMON_DURATION, 3, "turns" to 3) shouldBe "Desaparece en 3 turnos"
        messages.unitName("bat") shouldBe "Murciélago"
        messages.npcLine("viking") shouldBe "¡Esas bestias me tuvieron aquí atrapado durante días! Por el Valhalla, lucharé a vuestro lado."
    }

    @Test
    fun `unitName should return the English name of the unit when no language is selected`() {
        messages.unitName("hero") shouldBe "Mage"
    }
}
