package com.mkz.rpg.storybook.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.BattleSceneResourcesLoaderMother
import com.mkz.rpg.screen.adapters.presentation.TextsMother
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import com.mkz.rpg.shared.adapters.presentation.sharedViewsTest
import com.mkz.rpg.storybook.usecases.services.FakeStoryLocation
import io.kotest.assertions.withClue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import korlibs.korge.tests.ViewsForTesting
import kotlin.test.Test

/** Renders every story of the Storybook, as it is shown, so a story that fails is found before it is opened. */
class StorySmokeTest : ViewsForTesting() {
    @Test
    fun `Render should render every story of every story binding when they are shown with their default args, the game resources and texts`() =
        sharedViewsTest {
            // Given
            val storyContext = StoryContext(BattleSceneResourcesLoaderMother.loadedResourcesLoader(), TextsMother.texts())
            // When
            val failures =
                StorybookApi.STORY_BINDINGS.flatMap { binding ->
                    val component = binding.component(storyContext)
                    component.stories.mapNotNull { story ->
                        runCatching { addChild(binding.render(Args(component.args + story.args), storyContext)) }.exceptionOrNull()?.let { failure -> "${story.id}: $failure" }
                    }
                }
            // Then
            withClue("Stories that fail to render") { failures.shouldBeEmpty() }
        }

    @Test
    fun `StoryBindings should keep the ids of every component and story, as the links shared hold them`() =
        sharedViewsTest {
            // Given
            val storyContext = StoryContext(BattleSceneResourcesLoaderMother.loadedResourcesLoader(), TextsMother.texts())
            // When
            val components = StorybookApi.STORY_BINDINGS.map { binding -> binding.component(storyContext) }
            val ids = components.flatMap { component -> listOf(component.id) + component.stories.map { story -> story.id } }
            // Then
            withClue("Renaming an id breaks the links shared: keep it, even when its component or story is renamed") {
                ids.shouldContainExactly(
                    "shared-bar",
                    "shared-bar--full",
                    "shared-bar--half",
                    "shared-bar--empty",
                    "shared-bar--other-color",
                    "shared-preview-bar",
                    "shared-preview-bar--unchanged",
                    "shared-preview-bar--decreased",
                    "shared-preview-bar--emptied",
                    "shared-preview-bar--other-colors",
                    "screen-health-bar",
                    "screen-health-bar--full",
                    "screen-health-bar--half",
                    "screen-health-bar--empty",
                    "screen-mana-bar",
                    "screen-mana-bar--full",
                    "screen-mana-bar--half",
                    "screen-mana-bar--empty",
                    "screen-health-bar-preview",
                    "screen-health-bar-preview--unchanged",
                    "screen-health-bar-preview--hurt",
                    "screen-health-bar-preview--defeated",
                    "screen-health-bar-preview--healed",
                    "screen-mana-bar-preview",
                    "screen-mana-bar-preview--unchanged",
                    "screen-mana-bar-preview--spent",
                    "screen-mana-bar-preview--drained",
                    "screen-mana-bar-preview--restored",
                    "screen-battlefield-health-bar-preview",
                    "screen-battlefield-health-bar-preview--full",
                    "screen-battlefield-health-bar-preview--three-out-of-three",
                    "screen-battlefield-health-bar-preview--two-out-of-three",
                    "screen-battlefield-health-bar-preview--one-out-of-three",
                    "screen-battlefield-health-bar-preview--empty",
                    "screen-unit-portrait",
                    "screen-unit-portrait--hero",
                    "screen-unit-portrait--goat-wizard",
                    "screen-unit-name",
                    "screen-unit-name--short",
                    "screen-unit-name--aligned-to-right",
                    "screen-unit-name--long",
                    "screen-remaining-turns",
                    "screen-remaining-turns--several-turns",
                    "screen-remaining-turns--last-turn",
                    "screen-points",
                    "screen-points--full",
                    "screen-points--partly-spent",
                    "screen-points--spent",
                    "screen-points--casts",
                    "screen-effect",
                    "screen-effect--applied",
                    "screen-effect--new",
                    "screen-effects",
                    "screen-effects--some",
                    "screen-effects--none",
                    "screen-effects--with-new-effects",
                    "screen-effects--overflow",
                    "screen-ability-button",
                    "screen-ability-button--ready",
                    "screen-ability-button--selected",
                    "screen-ability-button--in-cooldown",
                    "screen-ability-button--not-enough-mana",
                    "screen-bag-button",
                    "screen-bag-button--chest",
                    "screen-finish-turn",
                    "screen-finish-turn--enabled",
                    "screen-finish-turn--disabled",
                    "screen-cancel-button",
                    "screen-cancel-button--default",
                    "screen-cancel-button--wide",
                    "screen-cancel-button--long-label",
                    "screen-confirm-button",
                    "screen-confirm-button--default",
                    "screen-confirm-button--wide",
                    "screen-confirm-button--long-label",
                    "screen-cancel-and-confirm",
                    "screen-cancel-and-confirm--default",
                    "screen-cancel-and-confirm--export-and-back",
                    "screen-cancel-and-confirm--leave-confirmation",
                    "screen-cancel-and-confirm--long-labels",
                    "screen-inventory-slot",
                    "screen-inventory-slot--empty",
                    "screen-inventory-slot--stored",
                    "screen-inventory-slot--equipped",
                    "screen-inventory-slot--selected",
                    "screen-inventory-slot--dimmed",
                    "screen-inventory",
                    "screen-inventory--empty",
                    "screen-inventory--stored",
                    "screen-inventory--full",
                    "screen-inventory--equipped",
                    "screen-inventory--selected",
                    "screen-inventory--exploring",
                    "screen-inventory--exploring-last-member",
                    "screen-item-actions",
                    "screen-item-actions--merchant-item",
                    "screen-item-actions--consumable",
                    "screen-item-actions--can-not-consume",
                    "screen-item-actions--equippable",
                    "screen-item-actions--equipped",
                    "screen-item-actions--no-one-to-give-to",
                    "screen-item-actions--bags-full",
                    "screen-give-item",
                    "screen-give-item--default",
                    "screen-give-item--long-name",
                    "screen-inventory-full",
                    "screen-inventory-full--default",
                    "screen-inventory-full--long-name",
                    "screen-inventory-full--expensive",
                    "screen-player-call-to-action",
                    "screen-player-call-to-action--finish-turn",
                    "screen-player-call-to-action--finish-turn-disabled",
                    "screen-player-call-to-action--cancel-and-confirm",
                    "screen-player-call-to-action--item-actions",
                    "screen-player-call-to-action--give-item",
                    "screen-player-call-to-action--inventory-full",
                    "screen-formation-row",
                    "screen-formation-row--member",
                    "screen-formation-row--leader",
                    "screen-formation-row--selected",
                    "screen-formation-row--hurt",
                    "screen-formation-row--long-name",
                    "screen-formation",
                    "screen-formation--party",
                    "screen-formation--row-selected",
                    "screen-formation--full-party",
                    "screen-formation--hurt-party",
                    "screen-formation--alone",
                    "screen-battle-info",
                    "screen-battle-info--exploring",
                    "screen-battle-info--human-turn",
                    "screen-battle-info--cpu-turn",
                    "screen-battle-info--winner",
                    "screen-menu",
                    "screen-menu--options",
                    "screen-map-builder-info",
                    "screen-map-builder-info--size",
                    "screen-map-builder-info--error",
                    "screen-map-builder-info--info",
                    "screen-map-builder-info--large-map",
                    "screen-map-builder-info--long-message",
                    "screen-map-builder-hud",
                    "screen-map-builder-hud--default",
                    "screen-map-builder-hud--smallest-map",
                    "screen-map-builder-hud--largest-map",
                    "screen-map-builder-hud--other-terrain",
                    "screen-map-builder-call-to-action",
                    "screen-map-builder-call-to-action--export-and-back",
                    "screen-map-builder-call-to-action--leave-confirmation",
                    "screen-map-builder-menu",
                    "screen-map-builder-menu--closed",
                    "screen-map-builder-menu--open",
                    "screen-map-builder-menu--several-options",
                    "screen-shop",
                    "screen-shop--buy",
                    "screen-shop--buy-item-selected",
                    "screen-shop--buy-not-enough-gold",
                    "screen-shop--buy-bag-full",
                    "screen-shop--sell",
                    "screen-shop--sell-equipped",
                    "screen-shop--sell-bag-full",
                    "screen-shop--sell-nothing",
                )
            }
        }

    @Test
    fun `ControlsView should fit the controls of every story binding above the actions when they are displayed`() =
        sharedViewsTest {
            // Given
            val storyContext = StoryContext(BattleSceneResourcesLoaderMother.loadedResourcesLoader(), TextsMother.texts())
            val controlsView = ControlsView()
            addChild(controlsView)
            // When
            val overflowing =
                StorybookApi.STORY_BINDINGS.map { binding -> binding.component(storyContext) }.filterNot { component ->
                    controlsView.display(component.controls, component.args)
                    controlsView.fitsItsMaximumHeight()
                }
            // Then
            withClue("Components whose controls cover the actions: give them fewer controls") { overflowing.map { component -> component.id }.shouldBeEmpty() }
        }

    @Test
    fun `Init should open the catalogue of every story binding when their controls, args and stories are valid`() =
        sharedViewsTest {
            // Given
            val storyContext = StoryContext(BattleSceneResourcesLoaderMother.loadedResourcesLoader(), TextsMother.texts())
            val storybookApi = StorybookApi(InMemoryEventBus(), FakeStoryLocation())
            // When
            storybookApi.init(storyContext)
            // Then
            storybookApi.searchCatalogue().size shouldBe StorybookApi.STORY_BINDINGS.size
        }
}
