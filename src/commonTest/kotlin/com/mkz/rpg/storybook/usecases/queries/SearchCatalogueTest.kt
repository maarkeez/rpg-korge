package com.mkz.rpg.storybook.usecases.queries

import com.mkz.rpg.storybook.adapters.storage.InMemoryStorybookRepository
import com.mkz.rpg.storybook.domain.StorybookMother.component
import com.mkz.rpg.storybook.domain.StorybookMother.storybook
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import kotlin.test.Test

class SearchCatalogueTest {
    private val storybookRepository = InMemoryStorybookRepository()
    private val searchCatalogue = SearchCatalogue(storybookRepository)

    @Test
    fun `should return the components in their order when the catalogue is initialised`() {
        // Given
        val healthBar = component(id = "screen-health-bar")
        val manaBar = component(id = "screen-mana-bar")
        storybookRepository.save(storybook(components = listOf(healthBar, manaBar)))
        // When
        val components = searchCatalogue()
        // Then
        components.shouldContainExactly(healthBar, manaBar)
    }

    @Test
    fun `should return no component when the catalogue is not initialised`() {
        // When
        val components = searchCatalogue()
        // Then
        components.shouldBeEmpty()
    }
}
