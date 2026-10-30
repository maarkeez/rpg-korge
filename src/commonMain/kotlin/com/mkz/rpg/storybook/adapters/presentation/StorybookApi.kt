package com.mkz.rpg.storybook.adapters.presentation

import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.storybook.adapters.location.platformStoryLocation
import com.mkz.rpg.storybook.adapters.storage.InMemoryStorybookRepository
import com.mkz.rpg.storybook.domain.Storybook.Dto.ArgDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ControlDto
import com.mkz.rpg.storybook.domain.StorybookRepository
import com.mkz.rpg.storybook.usecases.commands.ChangeArg
import com.mkz.rpg.storybook.usecases.commands.ChangeGlobal
import com.mkz.rpg.storybook.usecases.commands.ClearActions
import com.mkz.rpg.storybook.usecases.commands.InitialiseCatalogue
import com.mkz.rpg.storybook.usecases.commands.RecordAction
import com.mkz.rpg.storybook.usecases.commands.ResetArgs
import com.mkz.rpg.storybook.usecases.commands.SelectStory
import com.mkz.rpg.storybook.usecases.queries.SearchActions
import com.mkz.rpg.storybook.usecases.queries.SearchArgs
import com.mkz.rpg.storybook.usecases.queries.SearchCatalogue
import com.mkz.rpg.storybook.usecases.queries.SearchGlobals
import com.mkz.rpg.storybook.usecases.queries.SearchSelectedStory
import com.mkz.rpg.storybook.usecases.services.StoryLocation

/**
 * The Storybook: the views of the game shown in isolation, in each of their states. Location: where what is on show is
 * kept. Story bindings: the components of the catalogue, every story binding unless a test needs fewer.
 */
class StorybookApi(
    eventBus: EventBus,
    storyLocation: StoryLocation = platformStoryLocation(),
    private val storyBindings: List<StoryBinding> = STORY_BINDINGS,
) {
    companion object {
        /** Every story binding, in the order the sidebar lists them. They are listed by hand, as JS has no reflection. */
        val STORY_BINDINGS: List<StoryBinding> =
            listOf(
                BarViewStories,
                PreviewBarViewStories,
                GameButtonStories,
                HealthBarViewStories,
                ManaBarViewStories,
                HealthBarPreviewViewStories,
                ManaBarPreviewViewStories,
                UnitPortraitViewStories,
                UnitNameViewStories,
                RemainingTurnsViewStories,
                PointsViewStories,
                EffectViewStories,
                EffectsViewStories,
                AbilityButtonViewStories,
                BagButtonViewStories,
                FinishTurnViewStories,
                CancelButtonStories,
                ConfirmButtonStories,
                CancelAndConfirmViewStories,
                InventorySlotViewStories,
                InventoryViewStories,
                ItemActionsViewStories,
                GiveItemViewStories,
                InventoryFullViewStories,
                PlayerCallToActionViewStories,
                BattlefieldViewStories,
                TerrainTileViewStories,
                TileHighlightViewStories,
                TileDimViewStories,
                TileFlashViewStories,
                UnitSpriteViewStories,
                BattlefieldHealthBarPreviewViewStories,
                BattleUnitInfoViewStories,
                AttackPreviewViewStories,
                ReceiverPreviewViewStories,
                BattleHudViewStories,
                NpcDialogViewStories,
                VirtualPadViewStories,
                FormationRowViewStories,
                FormationViewStories,
                PauseViewStories,
                ConfirmDialogViewStories,
                BattleInfoViewStories,
                MenuViewStories,
                MapBuilderInfoViewStories,
                MapBuilderHudViewStories,
                MapBuilderCallToActionViewStories,
                MapBuilderMenuViewStories,
                ShopViewStories,
            )

        /** The global choosing the language of the views on show, by its code, e.g.: `es`. */
        const val LANGUAGE_GLOBAL = "language"
    }

    // Storage
    private val storybookRepository: StorybookRepository = InMemoryStorybookRepository()

    // Commands
    val initialiseCatalogue = InitialiseCatalogue(storybookRepository, storyLocation, eventBus)
    val selectStory = SelectStory(storybookRepository, storyLocation, eventBus)
    val changeArg = ChangeArg(storybookRepository, storyLocation, eventBus)
    val resetArgs = ResetArgs(storybookRepository, storyLocation, eventBus)
    val changeGlobal = ChangeGlobal(storybookRepository, storyLocation, eventBus)
    val recordAction = RecordAction(storybookRepository, eventBus)
    val clearActions = ClearActions(storybookRepository, eventBus)

    // Queries
    val searchCatalogue = SearchCatalogue(storybookRepository)
    val searchSelectedStory = SearchSelectedStory(storybookRepository)
    val searchArgs = SearchArgs(storybookRepository)
    val searchGlobals = SearchGlobals(storybookRepository)
    val searchActions = SearchActions(storybookRepository)

    // The binding of each component, by its id, once the catalogue is open.
    private var bindingsByComponentId: Map<String, StoryBinding> = emptyMap()

    /**
     * Opens the catalogue of the story bindings, on what the location asks for. The language global lists the languages
     * of the game, English first, which is its default.
     */
    fun init(context: StoryContext) {
        val components = storyBindings.map { binding -> binding.component(context) }
        bindingsByComponentId = components.map { component -> component.id }.zip(storyBindings).toMap()
        val languageCodes = context.texts.languages().map { language -> language.code }
        initialiseCatalogue(
            components = components,
            globalControls = listOf(ControlDto.pick(LANGUAGE_GLOBAL, languageCodes)),
            defaultGlobals = mapOf(LANGUAGE_GLOBAL to ArgDto.text(languageCodes.first())),
        )
    }

    /** The binding of the component, once the catalogue is open. */
    fun bindingOf(componentId: String): StoryBinding = bindingsByComponentId.getValue(componentId)
}
