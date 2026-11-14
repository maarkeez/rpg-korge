package com.mkz.tessavar.storybook.adapters.presentation

import com.mkz.tessavar.storybook.domain.Storybook.Dto.ComponentDto

object CatalogueSearch {
    private val SPACES = Regex("\\s+")

    fun search(
        components: List<ComponentDto>,
        text: String,
    ): List<ComponentDto> {
        val words =
            text
                .trim()
                .lowercase()
                .split(SPACES)
                .filter(String::isNotEmpty)
        if (words.isEmpty()) return components
        return components.mapNotNull { component ->
            val stories =
                component.stories.filter { story ->
                    val searched = "${component.group} ${component.title} ${story.name}".lowercase()
                    words.all { word -> word in searched }
                }
            if (stories.isEmpty()) null else component.copy(stories = stories)
        }
    }
}
