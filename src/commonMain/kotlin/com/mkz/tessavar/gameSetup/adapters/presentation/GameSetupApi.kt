package com.mkz.tessavar.gameSetup.adapters.presentation

import com.mkz.tessavar.gameSetup.usecases.commands.SetUpGame
import com.mkz.tessavar.shared.domain.EventBus

class GameSetupApi(
    eventBus: EventBus,
) {
    val setUpGame = SetUpGame(eventBus)
}
