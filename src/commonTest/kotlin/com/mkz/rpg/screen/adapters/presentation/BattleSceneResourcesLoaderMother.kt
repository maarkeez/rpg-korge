package com.mkz.rpg.screen.adapters.presentation

object BattleSceneResourcesLoaderMother {
    // Loaded once and shared by all the tests, as the game does with its views.
    private var resourcesLoader: BattleSceneResourcesLoader? = null

    suspend fun loadedResourcesLoader(): BattleSceneResourcesLoader =
        resourcesLoader ?: BattleSceneResourcesLoader().also { loader ->
            loader.load()
            resourcesLoader = loader
        }
}
