package com.mkz.rpg.storybook.adapters.presentation

import com.mkz.rpg.shared.adapters.presentation.addChildScaled
import com.mkz.rpg.shared.adapters.presentation.sharedViewsTest
import com.mkz.rpg.storybook.domain.StorybookMother.component
import com.mkz.rpg.storybook.domain.StorybookMother.story
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.doubles.shouldBeLessThan
import io.kotest.matchers.nulls.shouldBeNull
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
            sidebarView.searchText() shouldBe ""
            sidebarView.searchPlaceholderText() shouldBe "Search stories"
            sidebarView.isClearSearchShown().shouldBeFalse()
            sidebarView.noStoriesMessage().shouldBeNull()
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

    // Taller than the sidebar: 12 components of 3 stories each, `Story 1.1` to `Story 12.3`.
    private val manyComponents =
        (1..12).map { number ->
            component(
                id = "screen-component-$number",
                group = "Screen",
                title = "Component $number",
                stories = (1..3).map { storyNumber -> story(id = "screen-component-$number--story-$storyNumber", name = "Story $number.$storyNumber") },
            )
        }

    @Test
    fun `Display should not scroll the list when it fits the sidebar`() =
        sharedViewsTest {
            // Given
            val sidebarView = newSidebarView()
            addChild(sidebarView)
            sidebarView.display(listOf(healthBar, manaBar, barView))
            // When
            scrollSidebar(sidebarView, 200.0)
            dragSidebar(sidebarView, -200.0)
            // Then
            sidebarView.scrollOffset() shouldBe 0.0
            sidebarView.visibleStoryNames().shouldContainExactly("Full", "Empty", "Full mana", "Half")
        }

    @Test
    fun `Display should show the first stories only when the list is taller than the sidebar`() =
        sharedViewsTest {
            // Given
            val sidebarView = newSidebarView()
            addChild(sidebarView)
            // When
            sidebarView.display(manyComponents)
            // Then
            sidebarView.scrollOffset() shouldBe 0.0
            sidebarView.visibleStoryNames().first() shouldBe "Story 1.1"
            sidebarView.visibleStoryNames() shouldNotContain "Story 12.3"
        }

    @Test
    fun `Scroll should move the list with the mouse wheel, within the list`() =
        sharedViewsTest {
            // Given
            val sidebarView = newSidebarView()
            addChild(sidebarView)
            sidebarView.display(manyComponents)
            // When
            scrollSidebar(sidebarView, 100.0)
            // Then
            sidebarView.scrollOffset() shouldBe -100.0
            // When
            scrollSidebar(sidebarView, -300.0)
            // Then
            sidebarView.scrollOffset() shouldBe 0.0
            // When
            scrollSidebar(sidebarView, 10_000.0)
            // Then
            sidebarView.scrollOffset() shouldBeLessThan -100.0
            sidebarView.visibleStoryNames().last() shouldBe "Story 12.3"
            sidebarView.visibleStoryNames() shouldNotContain "Story 1.1"
        }

    @Test
    fun `Drag should move the list with the finger, within the list`() =
        sharedViewsTest {
            // Given
            val sidebarView = newSidebarView()
            addChild(sidebarView)
            sidebarView.display(manyComponents)
            // When
            dragSidebar(sidebarView, -150.0)
            // Then
            sidebarView.scrollOffset() shouldBe -150.0
            selectedStoryIds.shouldBeEmpty()
            // When
            dragSidebar(sidebarView, 400.0)
            // Then
            sidebarView.scrollOffset() shouldBe 0.0
            selectedStoryIds.shouldBeEmpty()
        }

    @Test
    fun `Scroll should not move the list when Ctrl is held, which zooms the Storybook instead`() =
        sharedViewsTest {
            // Given
            val sidebarView = newSidebarView()
            addChild(sidebarView)
            sidebarView.display(manyComponents)
            // When
            scrollSidebar(sidebarView, 300.0, ctrl = true)
            // Then
            sidebarView.scrollOffset() shouldBe 0.0
        }

    @Test
    fun `Drag should move the list with the finger when the Storybook is zoomed`() =
        sharedViewsTest {
            // Given
            val sidebarView = newSidebarView()
            addChildScaled(sidebarView, scale = 2.0)
            sidebarView.display(manyComponents)
            // When
            dragSidebar(sidebarView, -150.0)
            // Then the list, drawn twice its size, moves half as many of its units as the finger.
            sidebarView.scrollOffset() shouldBe -75.0
        }

    @Test
    fun `ClickStory should select the story under the finger when the list is scrolled`() =
        sharedViewsTest {
            // Given
            val sidebarView = newSidebarView()
            addChild(sidebarView)
            sidebarView.display(manyComponents)
            scrollSidebar(sidebarView, 10_000.0)
            // When
            clickStory(sidebarView, "Story 12.2")
            // Then
            selectedStoryIds.shouldContainExactly("screen-component-12--story-2")
        }

    @Test
    fun `SelectStory should scroll to the story when it is not in sight`() =
        sharedViewsTest {
            // Given
            val sidebarView = newSidebarView()
            addChild(sidebarView)
            sidebarView.display(manyComponents)
            // When
            sidebarView.selectStory("screen-component-9--story-2")
            // Then
            sidebarView.visibleStoryNames() shouldContain "Story 9.2"
            sidebarView.selectedStoryNames().shouldContainExactly("Story 9.2")
            // When
            sidebarView.selectStory("screen-component-1--story-1")
            // Then
            sidebarView.scrollOffset() shouldBe 0.0
            sidebarView.visibleStoryNames() shouldContain "Story 1.1"
        }

    @Test
    fun `SelectStory should not scroll the list when the story is in sight`() =
        sharedViewsTest {
            // Given
            val sidebarView = newSidebarView()
            addChild(sidebarView)
            sidebarView.display(manyComponents)
            // When
            sidebarView.selectStory("screen-component-2--story-1")
            // Then
            sidebarView.scrollOffset() shouldBe 0.0
        }

    @Test
    fun `SelectStory should keep the scroll when the story on show is selected again`() =
        sharedViewsTest {
            // Given
            val sidebarView = newSidebarView()
            addChild(sidebarView)
            sidebarView.display(manyComponents)
            sidebarView.selectStory("screen-component-1--story-1")
            scrollSidebar(sidebarView, 400.0)
            // When
            sidebarView.selectStory("screen-component-1--story-1")
            // Then
            sidebarView.scrollOffset() shouldBe -400.0
        }

    @Test
    fun `Display should keep the list inside its bounds when fewer components are displayed again`() =
        sharedViewsTest {
            // Given
            val sidebarView = newSidebarView()
            addChild(sidebarView)
            sidebarView.display(manyComponents)
            scrollSidebar(sidebarView, 10_000.0)
            // When
            sidebarView.display(listOf(healthBar, manaBar))
            // Then
            sidebarView.scrollOffset() shouldBe 0.0
            sidebarView.visibleStoryNames().shouldContainExactly("Full", "Empty", "Full mana")
        }

    @Test
    fun `TypeSearch should list only the stories that match, under their group and component`() =
        sharedViewsTest {
            // Given
            val sidebarView = newSidebarView()
            addChild(sidebarView)
            sidebarView.display(listOf(healthBar, manaBar, barView))
            // When
            sidebarView.typeSearch("full")
            // Then
            sidebarView.groupNames().shouldContainExactly("SCREEN")
            sidebarView.componentTitles().shouldContainExactly("Health bar", "Mana bar")
            sidebarView.storyNames().shouldContainExactly("Full", "Full mana")
            sidebarView.isListedTopDown().shouldBeTrue()
            sidebarView.searchPlaceholderText().shouldBeNull()
            sidebarView.isClearSearchShown().shouldBeTrue()
            sidebarView.noStoriesMessage().shouldBeNull()
        }

    @Test
    fun `TypeSearch should list the stories that hold every word, in any order and ignoring case`() =
        sharedViewsTest {
            // Given
            val sidebarView = newSidebarView()
            sidebarView.display(listOf(healthBar, manaBar, barView))
            // When
            sidebarView.typeSearch("MANA screen")
            // Then
            sidebarView.storyNames().shouldContainExactly("Full mana")
        }

    @Test
    fun `TypeSearch should say no stories match when none does`() =
        sharedViewsTest {
            // Given
            val sidebarView = newSidebarView()
            addChild(sidebarView)
            sidebarView.display(listOf(healthBar, manaBar, barView))
            // When
            sidebarView.typeSearch("dragon")
            // Then
            sidebarView.groupNames().shouldBeEmpty()
            sidebarView.componentTitles().shouldBeEmpty()
            sidebarView.storyNames().shouldBeEmpty()
            sidebarView.noStoriesMessage() shouldBe "No stories match"
        }

    @Test
    fun `ClickClearSearch should empty the search and list every story again`() =
        sharedViewsTest {
            // Given
            val sidebarView = newSidebarView()
            addChild(sidebarView)
            sidebarView.display(listOf(healthBar, manaBar, barView))
            sidebarView.typeSearch("dragon")
            // When
            clickClearSearch(sidebarView)
            // Then
            sidebarView.searchText() shouldBe ""
            sidebarView.storyNames().shouldContainExactly("Full", "Empty", "Full mana", "Half")
            sidebarView.searchPlaceholderText() shouldBe "Search stories"
            sidebarView.isClearSearchShown().shouldBeFalse()
            sidebarView.noStoriesMessage().shouldBeNull()
            selectedStoryIds.shouldBeEmpty()
        }

    @Test
    fun `ClickStory should select a story listed by the search, and keep the search`() =
        sharedViewsTest {
            // Given
            val sidebarView = newSidebarView()
            addChild(sidebarView)
            sidebarView.display(listOf(healthBar, manaBar, barView))
            sidebarView.typeSearch("full")
            // When
            clickStory(sidebarView, "Full mana")
            sidebarView.selectStory("screen-mana-bar--full")
            // Then
            selectedStoryIds.shouldContainExactly("screen-mana-bar--full")
            sidebarView.selectedStoryNames().shouldContainExactly("Full mana")
            sidebarView.searchText() shouldBe "full"
            sidebarView.storyNames().shouldContainExactly("Full", "Full mana")
        }

    @Test
    fun `TypeSearch should keep the story on show, highlighted again once it is listed`() =
        sharedViewsTest {
            // Given
            val sidebarView = newSidebarView()
            addChild(sidebarView)
            sidebarView.display(listOf(healthBar, manaBar, barView))
            sidebarView.selectStory("screen-health-bar--empty")
            // When
            sidebarView.typeSearch("half")
            // Then
            sidebarView.selectedStoryNames().shouldBeEmpty()
            // When
            clickClearSearch(sidebarView)
            // Then
            sidebarView.selectedStoryNames().shouldContainExactly("Empty")
            selectedStoryIds.shouldBeEmpty()
        }

    @Test
    fun `Display should list only the stories that match the search when it is displayed again`() =
        sharedViewsTest {
            // Given
            val sidebarView = newSidebarView()
            sidebarView.display(listOf(healthBar, manaBar))
            sidebarView.typeSearch("half")
            // When
            sidebarView.display(listOf(healthBar, manaBar, barView))
            // Then
            sidebarView.storyNames().shouldContainExactly("Half")
        }

    @Test
    fun `TypeSearch should scroll the list back to the top`() =
        sharedViewsTest {
            // Given
            val sidebarView = newSidebarView()
            addChild(sidebarView)
            sidebarView.display(manyComponents)
            scrollSidebar(sidebarView, 10_000.0)
            // When
            sidebarView.typeSearch("story")
            // Then
            sidebarView.scrollOffset() shouldBe 0.0
            sidebarView.visibleStoryNames().first() shouldBe "Story 1.1"
        }

    @Test
    fun `ClickClearSearch should scroll the list to the story on show when it is not in sight`() =
        sharedViewsTest {
            // Given
            val sidebarView = newSidebarView()
            addChild(sidebarView)
            sidebarView.display(manyComponents)
            sidebarView.selectStory("screen-component-9--story-2")
            sidebarView.typeSearch("component 12")
            // When
            clickClearSearch(sidebarView)
            // Then
            sidebarView.visibleStoryNames() shouldContain "Story 9.2"
            sidebarView.selectedStoryNames().shouldContainExactly("Story 9.2")
        }

    @Test
    fun `ClickClearSearch should keep the list at the top when the story on show is in sight`() =
        sharedViewsTest {
            // Given
            val sidebarView = newSidebarView()
            addChild(sidebarView)
            sidebarView.display(manyComponents)
            sidebarView.selectStory("screen-component-1--story-1")
            sidebarView.typeSearch("component 12")
            // When
            clickClearSearch(sidebarView)
            // Then
            sidebarView.scrollOffset() shouldBe 0.0
        }
}
