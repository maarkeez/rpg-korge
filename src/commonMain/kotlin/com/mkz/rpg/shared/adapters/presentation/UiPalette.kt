package com.mkz.rpg.shared.adapters.presentation

import korlibs.image.color.RGBA

/**
 * Semantic UI colors. Every token is a Famicube palette color (`resources/famicube-palette.png`).
 */
object UiPalette {
    val background = RGBA(0x34, 0x34, 0x34)
    val panel = RGBA(0x15, 0x15, 0x15)
    val panelBorder = RGBA(0xD7, 0xD7, 0xD7)
    val textPrimary = RGBA(0xFF, 0xFF, 0xFF)
    val textMuted = RGBA(0xA8, 0xA8, 0xA8)
    val barBackground = RGBA(0x7B, 0x7B, 0x7B)
    val hp = RGBA(0xCF, 0x3C, 0x71)
    val hpLoss = RGBA(0xE0, 0x3C, 0x28)
    val hpGain = RGBA(0x58, 0xD3, 0x32)
    val mana = RGBA(0x00, 0x84, 0xFF)
    val manaCost = RGBA(0x5B, 0xA8, 0xFF)
    val ally = RGBA(0x00, 0x84, 0xFF)
    val enemy = RGBA(0xE0, 0x3C, 0x28)
    val selection = RGBA(0xFF, 0xE7, 0x37)
    val move = RGBA(0x98, 0xDC, 0xFF)
    val castValid = RGBA(0xFF, 0xBB, 0x31)
    val castPreview = RGBA(0xF6, 0x8F, 0x37)
    val conditional = RGBA(0xD5, 0x9C, 0xFC)
    val danger = RGBA(0xE0, 0x3C, 0x28)
    val primaryButton = RGBA(0x00, 0x84, 0xFF)
    val primaryButtonOver = RGBA(0x02, 0x4A, 0xCA)
    val secondaryButton = RGBA(0x7B, 0x7B, 0x7B)
    val secondaryButtonOver = RGBA(0x34, 0x34, 0x34)
    val portraitBackground = RGBA(0xFF, 0xFF, 0xFF)
    val portraitBackgroundOver = RGBA(0x98, 0xDC, 0xFF)
    val placeholder = RGBA(0xA3, 0x28, 0xB3)

    val all: List<RGBA> =
        listOf(
            background,
            panel,
            panelBorder,
            textPrimary,
            textMuted,
            barBackground,
            hp,
            hpLoss,
            hpGain,
            mana,
            manaCost,
            ally,
            enemy,
            selection,
            move,
            castValid,
            castPreview,
            conditional,
            danger,
            primaryButton,
            primaryButtonOver,
            secondaryButton,
            secondaryButtonOver,
            portraitBackground,
            portraitBackgroundOver,
            placeholder,
        )
}
