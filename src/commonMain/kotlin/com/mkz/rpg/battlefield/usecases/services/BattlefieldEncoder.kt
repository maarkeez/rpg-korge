package com.mkz.rpg.battlefield.usecases.services

interface BattlefieldEncoder {
    fun encode(tiles: List<List<String>>): String
}
