package com.mkz.rpg.gameSetup.adapters.presentation

import com.mkz.rpg.gameSetup.usecases.commands.SetUpGame
import com.mkz.rpg.shared.domain.EventBus

class GameSetupApi(
    eventBus: EventBus,
) {
    val setUpGame = SetUpGame(eventBus)
}
