package com.mkz.rpg.storybook.usecases.queries

import com.mkz.rpg.storybook.adapters.storage.InMemoryStorybookRepository
import com.mkz.rpg.storybook.domain.Storybook.Dto.ArgDto
import com.mkz.rpg.storybook.domain.StorybookMother.storybook
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class SearchGlobalsTest {
    private val storybookRepository = InMemoryStorybookRepository()
    private val searchGlobals = SearchGlobals(storybookRepository)

    @Test
    fun `should return the globals when the catalogue is initialised`() {
        // Given
        storybookRepository.save(storybook(requestedGlobals = mapOf("language" to "es")))
        // When
        val globals = searchGlobals()
        // Then
        globals shouldBe mapOf("language" to ArgDto.text("es"))
    }

    @Test
    fun `should return no globals when the catalogue is not initialised`() {
        // When
        val globals = searchGlobals()
        // Then
        globals.shouldBeEmpty()
    }
}
