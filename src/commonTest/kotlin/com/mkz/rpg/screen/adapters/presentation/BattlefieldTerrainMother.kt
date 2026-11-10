package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.battlefield.domain.Battlefield

fun Battlefield.toTerrain(): BattlefieldTerrain = BattlefieldMapper.terrain(toDto())
