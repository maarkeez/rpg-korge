package com.mkz.rpg.storybook.usecases.queries

import com.mkz.rpg.storybook.adapters.storage.InMemoryStorybookRepository
import com.mkz.rpg.storybook.domain.Storybook.Dto.ValueDto
import com.mkz.rpg.storybook.domain.StorybookMother.storybook
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class SearchGlobalsTest {
    private val storybookRepository = InMemoryStorybookRepository()
    private val searchGlobals = SearchGlobals(storybookRepository)

    @Test
    fun `should return the globals when the catalogue is open`() {
        // Given
        storybookRepository.save(storybook(requestedGlobals = mapOf("language" to "es")))
        // When
        val globals = searchGlobals()
        // Then
        globals shouldBe mapOf("language" to ValueDto.text("es"))
    }

    @Test
    fun `should return no globals when the catalogue is not open`() {
        // When
        val globals = searchGlobals()
        // Then
        globals.shouldBeEmpty()
    }
}
