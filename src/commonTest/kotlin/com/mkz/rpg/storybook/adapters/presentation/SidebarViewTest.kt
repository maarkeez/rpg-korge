package com.mkz.rpg.storybook.adapters.presentation

import com.mkz.rpg.shared.adapters.presentation.sharedViewsTest
import com.mkz.rpg.storybook.domain.StorybookMother.component
import com.mkz.rpg.storybook.domain.StorybookMother.story
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import korlibs.korge.tests.ViewsForTesting
import kotlin.test.Test

class SidebarViewTest : ViewsForTesting() {
    private val selectedStoryIds = mutableListOf<String>()
    private val healthBar =
        component(
            id = "screen-health-bar",
            group = "Screen",
            title = "Health bar",
            stories = listOf(story(id = "screen-health-bar--full", name = "Full"), story(id = "screen-health-bar--empty", name = "Empty")),
        )
    private val manaBar = component(id = "screen-mana-bar", group = "Screen", title = "Mana bar", stories = listOf(story(id = "screen-mana-bar--full", name = "Full mana")))
    private val barView = component(id = "shared-bar", group = "Shared", title = "Bar", stories = listOf(story(id = "shared-bar--half", name = "Half")))

    private fun newSidebarView() =
        SidebarView().apply {
            setDelegate(
                object : SidebarView.Delegate {
                    override fun storySelected(storyId: String) {
                        selectedStoryIds += storyId
                    }
                },
            )
        }

    @Test
    fun `SidebarView should display its title when it is displayed`() =
        sharedViewsTest {
            // When
            val sidebarView = newSidebarView()
            // Then
            sidebarView.titleText() shouldBe "Storybook"
            sidebarView.storyNames().shouldBeEmpty()
        }

    @Test
    fun `Display should list the components under their group, each with its stories, in their order`() =
        sharedViewsTest {
            // Given
            val sidebarView = newSidebarView()
            addChild(sidebarView)
            // When
            sidebarView.display(listOf(healthBar, manaBar, barView))
            // Then
            sidebarView.groupNames().shouldContainExactly("SCREEN", "SHARED")
            sidebarView.componentTitles().shouldContainExactly("Health bar", "Mana bar", "Bar")
            sidebarView.storyNames().shouldContainExactly("Full", "Empty", "Full mana", "Half")
            sidebarView.isListedTopDown().shouldBeTrue()
        }

    @Test
    fun `Display should list only the given components when they are displayed again`() =
        sharedViewsTest {
            // Given
            val sidebarView = newSidebarView()
            sidebarView.display(listOf(healthBar, manaBar))
            // When
            sidebarView.display(listOf(barView))
            // Then
            sidebarView.groupNames().shouldContainExactly("SHARED")
            sidebarView.storyNames().shouldContainExactly("Half")
        }

    @Test
    fun `SelectStory should highlight only the story on show`() =
        sharedViewsTest {
            // Given
            val sidebarView = newSidebarView()
            sidebarView.display(listOf(healthBar, manaBar))
            sidebarView.selectStory("screen-health-bar--full")
            // When
            sidebarView.selectStory("screen-health-bar--empty")
            // Then
            sidebarView.selectedStoryNames().shouldContainExactly("Empty")
        }

    @Test
    fun `ClickStory should select the story when it is clicked`() =
        sharedViewsTest {
            // Given
            val sidebarView = newSidebarView()
            addChild(sidebarView)
            sidebarView.display(listOf(healthBar, manaBar))
            // When
            clickStory(sidebarView, "Full mana")
            // Then
            selectedStoryIds.shouldContainExactly("screen-mana-bar--full")
        }
}
