package com.mkz.rpg.screen.adapters.presentation

object GameSceneResourcesLoaderMother {
    private var resourcesLoader: GameSceneResourcesLoader? = null

    suspend fun loadedResourcesLoader(): GameSceneResourcesLoader =
        resourcesLoader ?: GameSceneResourcesLoader().also { loader ->
            loader.load()
            resourcesLoader = loader
        }
}
