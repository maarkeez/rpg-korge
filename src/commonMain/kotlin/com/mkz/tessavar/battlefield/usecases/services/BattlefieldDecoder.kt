package com.mkz.tessavar.battlefield.usecases.services

interface BattlefieldDecoder {
    fun decode(text: String): List<List<String>>
}
