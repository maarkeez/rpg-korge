package com.mkz.tessavar.screen.adapters.presentation

import com.mkz.tessavar.battlefield.domain.Battlefield

fun Battlefield.toTerrain(): BattlefieldTerrain = BattlefieldMapper.terrain(toDto())
