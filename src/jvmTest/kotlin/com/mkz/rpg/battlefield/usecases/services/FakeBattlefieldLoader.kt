package com.mkz.rpg.battlefield.usecases.services

// Loads the given tiles, instead of reading them from the resources.
class FakeBattlefieldLoader(
    private val tiles: List<List<String>> = emptyList(),
) : BattlefieldLoader {
    override fun loadTiles(): List<List<String>> = tiles
}
