package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.battlefield.domain.Battlefield

/** The battlefield as the battlefield view draws it, mapped as the presenters do. */
fun Battlefield.toTerrain(): BattlefieldTerrain = BattlefieldMapper.terrain(toDto())
