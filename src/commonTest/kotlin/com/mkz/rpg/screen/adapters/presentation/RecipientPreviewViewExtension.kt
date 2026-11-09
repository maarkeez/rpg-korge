package com.mkz.rpg.screen.adapters.presentation

// Page object of [RecipientPreviewView].

fun RecipientPreviewView.unitName(): String = nameText.text

/** Whether KO is displayed, as for a recipient the cast would leave with no health. */
fun RecipientPreviewView.isKnockedOut(): Boolean = koPill.visible && koPill.labelText() == "KO"

/** The change of health, e.g.: "-12" or "+10". */
fun RecipientPreviewView.healthChange(): String = changeText.text

fun RecipientPreviewView.healthChangeColor() = changeText.color

fun RecipientPreviewView.portraitRingColor() = unitPortraitView.ringColor()
