package com.mkz.rpg.screen.adapters.presentation

// Page object of [ReceiverPreviewView].

fun ReceiverPreviewView.unitName(): String = nameText.text

/** Whether KO is displayed, as for a receiver the cast would leave with no health. */
fun ReceiverPreviewView.isKnockedOut(): Boolean = koPill.visible && koPill.labelText() == "KO"

/** The change of health, e.g.: "-12" or "+10". */
fun ReceiverPreviewView.healthChange(): String = changeText.text

fun ReceiverPreviewView.healthChangeColor() = changeText.color

fun ReceiverPreviewView.portraitRingColor() = unitPortraitView.ringColor()
