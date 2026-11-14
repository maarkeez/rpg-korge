package com.mkz.tessavar.battlefield.usecases.services

interface BattlefieldEncoder {
    fun encode(tiles: List<List<String>>): String
}
