package com.mkz.tessavar.storybook.usecases.queries

import com.mkz.tessavar.storybook.adapters.storage.InMemoryStorybookRepository
import com.mkz.tessavar.storybook.domain.Storybook.Dto.ValueDto
import com.mkz.tessavar.storybook.domain.StorybookMother.storybook
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class SearchArgsTest {
    private val storybookRepository = InMemoryStorybookRepository()
    private val searchArgs = SearchArgs(storybookRepository)

    @Test
    fun `should return the args of the selected story when the catalogue is open`() {
        // Given
        storybookRepository.save(storybook(requestedStoryId = "screen-health-bar--full", requestedArgs = mapOf("remaining" to "10")))
        // When
        val args = searchArgs()
        // Then
        args shouldBe mapOf("remaining" to ValueDto.int(10))
    }

    @Test
    fun `should return no args when the catalogue is not open`() {
        // When
        val args = searchArgs()
        // Then
        args.shouldBeEmpty()
    }
}
