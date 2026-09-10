package com.mkz.rpg.screen.battlefieldHud.domain

sealed class BattlefieldHudError(
    message: String,
) : Throwable(message = message) {
    class BattlefieldHudNotFound : BattlefieldHudError("Battlefield hud not found")

    class InvalidBattlefieldHudState : BattlefieldHudError("Invalid battlefield hud state")
}
