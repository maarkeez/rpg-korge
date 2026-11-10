package com.mkz.rpg.storybook.adapters.presentation

import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.storybook.adapters.storage.InMemoryStorybookRepository
import com.mkz.rpg.storybook.domain.Storybook.Dto.ControlDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ValueDto
import com.mkz.rpg.storybook.domain.StorybookRepository
import com.mkz.rpg.storybook.usecases.commands.ChangeArg
import com.mkz.rpg.storybook.usecases.commands.ChangeGlobal
import com.mkz.rpg.storybook.usecases.commands.ClearActions
import com.mkz.rpg.storybook.usecases.commands.OpenCatalogue
import com.mkz.rpg.storybook.usecases.commands.RecordAction
import com.mkz.rpg.storybook.usecases.commands.ResetArgs
import com.mkz.rpg.storybook.usecases.commands.SelectStory
import com.mkz.rpg.storybook.usecases.queries.SearchActions
import com.mkz.rpg.storybook.usecases.queries.SearchArgs
import com.mkz.rpg.storybook.usecases.queries.SearchCatalogue
import com.mkz.rpg.storybook.usecases.queries.SearchGlobals
import com.mkz.rpg.storybook.usecases.queries.SearchSelectedStory
import com.mkz.rpg.storybook.usecases.services.StoryLocation

class StorybookApi(
    eventBus: EventBus,
    storyLocation: StoryLocation,
    private val storyBindings: List<StoryBinding>,
) {
    companion object {
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
                RemainingSummonDurationViewStories,
                PointsViewStories,
                PillViewStories,
                EffectViewStories,
                EffectsViewStories,
                AbilityButtonViewStories,
                AbilityInfoViewStories,
                InventoryButtonViewStories,
                FinishPlayerTurnViewStories,
                CancelButtonStories,
                ConfirmButtonStories,
                CancelAndConfirmViewStories,
                InventorySlotViewStories,
                InventoryViewStories,
                GoldButtonViewStories,
                ItemButtonsViewStories,
                DiscardQuestionViewStories,
                GiveItemViewStories,
                GiveGoldViewStories,
                GiveGoldActionsViewStories,
                GiveGoldRefusalViewStories,
                InventoryFullViewStories,
                ButtonRowViewStories,
                CallToActionViewStories,
                BattlefieldViewStories,
                TerrainTileViewStories,
                TileHighlightViewStories,
                TileDimViewStories,
                TileBlinkViewStories,
                UnitSpriteViewStories,
                BattlefieldHealthBarPreviewViewStories,
                BattleUnitInfoViewStories,
                CastPreviewViewStories,
                RecipientPreviewViewStories,
                HudViewStories,
                TalkViewStories,
                VirtualPadViewStories,
                FormationRowViewStories,
                FormationViewStories,
                PauseViewStories,
                SaveSlotsViewStories,
                ConfirmDialogViewStories,
                BannerViewStories,
                QuestTrackerViewStories,
                QuestDetailViewStories,
                QuestJournalViewStories,
                QuestAnnouncementViewStories,
                QuestMarkerViewStories,
                MenuViewStories,
                WorldBuilderInfoViewStories,
                WorldBuilderHudViewStories,
                WorldBuilderCallToActionViewStories,
                WorldBuilderMenuViewStories,
                ShopViewStories,
            )

        const val LANGUAGE_GLOBAL = "language"
    }

    private val storybookRepository: StorybookRepository = InMemoryStorybookRepository()

    val openCatalogue = OpenCatalogue(storybookRepository, storyLocation, eventBus)
    val selectStory = SelectStory(storybookRepository, storyLocation, eventBus)
    val changeArg = ChangeArg(storybookRepository, storyLocation, eventBus)
    val resetArgs = ResetArgs(storybookRepository, storyLocation, eventBus)
    val changeGlobal = ChangeGlobal(storybookRepository, storyLocation, eventBus)
    val recordAction = RecordAction(storybookRepository, eventBus)
    val clearActions = ClearActions(storybookRepository, eventBus)

    val searchCatalogue = SearchCatalogue(storybookRepository)
    val searchSelectedStory = SearchSelectedStory(storybookRepository)
    val searchArgs = SearchArgs(storybookRepository)
    val searchGlobals = SearchGlobals(storybookRepository)
    val searchActions = SearchActions(storybookRepository)

    private var bindingsByComponentId: Map<String, StoryBinding> = emptyMap()

    fun init(context: StoryContext) {
        val components = storyBindings.map { binding -> binding.component(context) }
        bindingsByComponentId = components.map { component -> component.id }.zip(storyBindings).toMap()
        val languageCodes = context.messages.languages().map { language -> language.code }
        openCatalogue(
            components = components,
            globalControls = listOf(ControlDto.pick(LANGUAGE_GLOBAL, languageCodes)),
            defaultGlobals = mapOf(LANGUAGE_GLOBAL to ValueDto.text(languageCodes.first())),
        )
    }

    fun bindingOf(componentId: String): StoryBinding = bindingsByComponentId.getValue(componentId)
}
