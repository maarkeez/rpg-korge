package com.mkz.rpg.screen.adapters.presentation

object GameSceneResourcesLoaderMother {
    // Loaded once and shared by all the tests, as the game does with its views.
    private var resourcesLoader: GameSceneResourcesLoader? = null

    suspend fun loadedResourcesLoader(): GameSceneResourcesLoader =
        resourcesLoader ?: GameSceneResourcesLoader().also { loader ->
            loader.load()
            resourcesLoader = loader
        }
}
