package com.mkz.rpg.storybook.adapters.presentation

import com.mkz.rpg.shared.adapters.presentation.sharedViewsTest
import com.mkz.rpg.storybook.domain.StorybookMother.component
import com.mkz.rpg.storybook.domain.StorybookMother.story
import io.kotest.assertions.assertSoftly
import io.kotest.matchers.shouldBe
import korlibs.korge.tests.ViewsForTesting
import kotlin.test.Test

class StoryInfoViewTest : ViewsForTesting() {
    @Test
    fun `Display should display the component, by its group and title, and the story, each with its description`() =
        sharedViewsTest {
            // Given
            val storyInfoView = StoryInfoView()
            val full = story(name = "Full", description = "A battle unit that has not been hurt.")
            val healthBar = component(group = "Screen", title = "Health bar", description = "The health a battle unit has left.", stories = listOf(full))
            // When
            storyInfoView.display(healthBar, full)
            // Then
            assertSoftly {
                storyInfoView.componentLabel() shouldBe "Screen / Health bar"
                storyInfoView.componentDescription() shouldBe "The health a battle unit has left."
                storyInfoView.storyName() shouldBe "Full"
                storyInfoView.storyDescription() shouldBe "A battle unit that has not been hurt."
            }
        }
}
