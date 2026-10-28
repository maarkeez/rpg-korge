package com.mkz.rpg.storybook.adapters.location

import com.mkz.rpg.storybook.domain.StorybookMother.location
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class LocationQueryTest {
    @Test
    fun `Decode should read the story, the args and the globals when the query has them`() {
        // When
        val location = LocationQuery.decode("?story=screen-shop--sell&args=mode:SELL;buyerGold:0&globals=language:es")
        // Then
        location shouldBe location(storyId = "screen-shop--sell", args = mapOf("mode" to "SELL", "buyerGold" to "0"), globals = mapOf("language" to "es"))
    }

    @Test
    fun `Decode should read nothing when the query is empty`() {
        // When
        val location = LocationQuery.decode("")
        // Then
        location shouldBe location()
    }

    @Test
    fun `Decode should decode the values when they are URL-encoded`() {
        // When
        val location = LocationQuery.decode("?args=label:Iron%20hammer%3B%3A;title:a+b")
        // Then
        location.args shouldBe mapOf("label" to "Iron hammer;:", "title" to "a b")
    }

    @Test
    fun `Decode should leave out the values it can not read when they are malformed`() {
        // When
        val location = LocationQuery.decode("?args=remaining:%G1;maximum;size:3")
        // Then
        location.args shouldBe mapOf("size" to "3")
    }

    @Test
    fun `Encode should write the story, the args and the globals, keeping the other parameters, when the query has them`() {
        // Given
        val location = location(storyId = "screen-health-bar--full", args = mapOf("label" to "Iron hammer;:", "remaining" to "3"), globals = mapOf("language" to "es"))
        // When
        val query = LocationQuery.encode(location, query = "?story=screen-health-bar--empty&args=remaining:1&debug=true")
        // Then
        query shouldBe "story=screen-health-bar--full&args=label:Iron%20hammer%3B%3A;remaining:3&globals=language:es&debug=true"
    }

    @Test
    fun `Encode should write only the story when no arg nor global was changed`() {
        // When
        val query = LocationQuery.encode(location(storyId = "screen-health-bar--full"), query = "?story=screen-health-bar--empty&args=remaining:1&globals=language:es")
        // Then
        query shouldBe "story=screen-health-bar--full"
    }

    @Test
    fun `Decode should read what Encode writes when the values have separators`() {
        // Given
        val location = location(storyId = "screen-health-bar--full", args = mapOf("label" to "a:b;c&d=e f+g%"), globals = mapOf("language" to "es"))
        // When
        val decoded = LocationQuery.decode("?" + LocationQuery.encode(location, query = ""))
        // Then
        decoded shouldBe location
    }
}
