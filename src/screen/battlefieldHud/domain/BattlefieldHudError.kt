package screen.battlefieldHud.domain

sealed class BattlefieldHudError(message: String) : Throwable(message=message) {
    class BattlefieldHudNotFound : BattlefieldHudError("Battlefield hud not found")
}
