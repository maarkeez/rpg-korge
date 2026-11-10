package com.mkz.rpg.battlefield.usecases.services

interface BattlefieldDecoder {
    fun decode(text: String): List<List<String>>
}
