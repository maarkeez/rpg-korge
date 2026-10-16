package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.battleUnit.usecases.queries.PreviewCastOngoingEffects
import com.mkz.rpg.effect.adapters.resources.ResourcesEffectLoader
import com.mkz.rpg.screen.adapters.presentation.BattleSceneResourcesLoaderMother.loadedResourcesLoader
import com.mkz.rpg.shared.adapters.presentation.sharedViewsTest
import io.kotest.assertions.withClue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldNotBeEmpty
import korlibs.korge.tests.ViewsForTesting
import kotlin.test.Test

// The effects staying on the battle units are displayed as icons, which fail when the effect has no image.
class EffectImagesTest : ViewsForTesting() {
    @Test
    fun `should have an image when is an effect staying on the battle units`() =
        sharedViewsTest {
            // Given
            val effectLoader = ResourcesEffectLoader().also { loader -> loader.initResources() }
            val resourcesLoader = loadedResourcesLoader()
            // When
            val ongoingEffectIds =
                effectLoader
                    .loadEffects()
                    .filter { effect -> effect.application.type in PreviewCastOngoingEffects.ONGOING_APPLICATION_TYPES }
                    .map { effect -> effect.id }
            val effectIdsWithoutImage = ongoingEffectIds.filter { effectId -> resourcesLoader.bitmapOrNull("effect/$effectId.png") == null }
            // Then
            ongoingEffectIds.shouldNotBeEmpty()
            withClue("Add an image at 'resources/effect/<effect id>.png' for the effects $effectIdsWithoutImage") {
                effectIdsWithoutImage.shouldBeEmpty()
            }
        }
}
