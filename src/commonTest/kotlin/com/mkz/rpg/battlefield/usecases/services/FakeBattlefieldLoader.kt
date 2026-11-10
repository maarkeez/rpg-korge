package com.mkz.rpg.battlefield.usecases.services

class FakeBattlefieldLoader(
    private val tiles: List<List<String>> = emptyList(),
) : BattlefieldLoader {
    override fun loadTiles(): List<List<String>> = tiles
}
