package com.mkz.rpg.storybook.adapters.presentation

import com.mkz.rpg.storybook.domain.Storybook.Dto.ComponentDto
import com.mkz.rpg.storybook.domain.StorybookMother.component
import com.mkz.rpg.storybook.domain.StorybookMother.story
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class CatalogueSearchTest {
    private val shop =
        component(
            id = "screen-shop",
            group = "Screen",
            title = "Shop",
            stories = listOf(story(id = "screen-shop--buy", name = "Buy"), story(id = "screen-shop--sell", name = "Sell"), story(id = "screen-shop--sell-bag-full", name = "Sell, bag full")),
        )
    private val healthBar =
        component(
            id = "screen-health-bar",
            group = "Screen",
            title = "Health bar",
            stories = listOf(story(id = "screen-health-bar--full", name = "Full"), story(id = "screen-health-bar--empty", name = "Empty")),
        )
    private val bar = component(id = "shared-bar", group = "Shared", title = "Bar", stories = listOf(story(id = "shared-bar--half", name = "Half")))
    private val catalogue = listOf(shop, healthBar, bar)

    private fun List<ComponentDto>.storyIds(): List<String> = flatMap { component -> component.stories.map { story -> story.id } }

    @Test
    fun `Search should find every component and story when the text is empty`() {
        // When
        val found = CatalogueSearch.search(catalogue, "")
        // Then
        found shouldBe catalogue
    }

    @Test
    fun `Search should find every component and story when the text is only spaces`() {
        // When
        val found = CatalogueSearch.search(catalogue, "   ")
        // Then
        found shouldBe catalogue
    }

    @Test
    fun `Search should find every story of a component when its title matches`() {
        // When
        val found = CatalogueSearch.search(catalogue, "shop")
        // Then
        found shouldBe listOf(shop)
    }

    @Test
    fun `Search should find the stories whose name matches, under their component only`() {
        // When
        val found = CatalogueSearch.search(catalogue, "full")
        // Then
        found.map(ComponentDto::id).shouldContainExactly("screen-shop", "screen-health-bar")
        found.storyIds().shouldContainExactly("screen-shop--sell-bag-full", "screen-health-bar--full")
    }

    @Test
    fun `Search should find every story of a group when the group matches`() {
        // When
        val found = CatalogueSearch.search(catalogue, "shared")
        // Then
        found shouldBe listOf(bar)
    }

    @Test
    fun `Search should find only the stories that hold every word, in any order`() {
        // When
        val found = CatalogueSearch.search(catalogue, "sell shop")
        // Then
        found.storyIds().shouldContainExactly("screen-shop--sell", "screen-shop--sell-bag-full")
        CatalogueSearch.search(catalogue, "shop sell bag").storyIds().shouldContainExactly("screen-shop--sell-bag-full")
    }

    @Test
    fun `Search should ignore the case and the spaces around the words`() {
        // When
        val found = CatalogueSearch.search(catalogue, "  HEALTH   Empty ")
        // Then
        found.storyIds().shouldContainExactly("screen-health-bar--empty")
    }

    @Test
    fun `Search should find part of a word`() {
        // When
        val found = CatalogueSearch.search(catalogue, "heal emp")
        // Then
        found.storyIds().shouldContainExactly("screen-health-bar--empty")
    }

    @Test
    fun `Search should keep the order of the catalogue`() {
        // When
        val found = CatalogueSearch.search(catalogue, "bar")
        // Then
        found.map(ComponentDto::id).shouldContainExactly("screen-health-bar", "shared-bar")
        found.storyIds().shouldContainExactly("screen-health-bar--full", "screen-health-bar--empty", "shared-bar--half")
    }

    @Test
    fun `Search should find nothing when a word is in no story`() {
        // When
        val found = CatalogueSearch.search(catalogue, "shop dragon")
        // Then
        found.shouldBeEmpty()
    }

    @Test
    fun `Search should not match letters that are not together`() {
        // When
        val found = CatalogueSearch.search(catalogue, "shp")
        // Then
        found.shouldBeEmpty()
    }
}
