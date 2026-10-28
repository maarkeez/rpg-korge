package com.mkz.rpg.storybook.usecases.queries

import com.mkz.rpg.storybook.adapters.storage.InMemoryStorybookRepository
import com.mkz.rpg.storybook.domain.Storybook.Dto.ArgDto
import com.mkz.rpg.storybook.domain.StorybookMother.storybook
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class SearchArgsTest {
    private val storybookRepository = InMemoryStorybookRepository()
    private val searchArgs = SearchArgs(storybookRepository)

    @Test
    fun `should return the args of the story on show when the catalogue is initialised`() {
        // Given
        storybookRepository.save(storybook(requestedStoryId = "screen-health-bar--full", requestedArgs = mapOf("remaining" to "10")))
        // When
        val args = searchArgs()
        // Then
        args shouldBe mapOf("remaining" to ArgDto.int(10))
    }

    @Test
    fun `should return no args when the catalogue is not initialised`() {
        // When
        val args = searchArgs()
        // Then
        args.shouldBeEmpty()
    }
}
