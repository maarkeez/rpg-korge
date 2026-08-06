package battlefield.domain

sealed class BattlefieldError(message: String) : Throwable(message=message) {
    class TileIsNotVacant: BattlefieldError("Tile is not vacant")
    class TileNotFound: BattlefieldError("Tile not found")
}
