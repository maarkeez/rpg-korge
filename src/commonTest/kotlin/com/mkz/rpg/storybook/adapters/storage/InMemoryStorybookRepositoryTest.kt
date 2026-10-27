package com.mkz.rpg.storybook.adapters.storage

import com.mkz.rpg.storybook.domain.StorybookMother.storybook
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class InMemoryStorybookRepositoryTest {
    private val storybookRepository = InMemoryStorybookRepository()

    @Test
    fun `Save should store the storybook`() {
        // Given
        val storybook = storybook()
        // When
        storybookRepository.save(storybook)
        // Then
        storybookRepository.search() shouldBe storybook
    }

    @Test
    fun `Search should return null when no storybook is stored`() {
        // When
        val storybook = storybookRepository.search()
        // Then
        storybook.shouldBeNull()
    }
}
