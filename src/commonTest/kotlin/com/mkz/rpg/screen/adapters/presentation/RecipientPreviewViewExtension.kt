package com.mkz.rpg.screen.adapters.presentation

fun RecipientPreviewView.unitName(): String = nameText.text

fun RecipientPreviewView.isKnockedOut(): Boolean = koPill.visible && koPill.labelText() == "KO"

fun RecipientPreviewView.healthChange(): String = changeText.text

fun RecipientPreviewView.healthChangeColor() = changeText.color

fun RecipientPreviewView.portraitRingColor() = unitPortraitView.ringColor()
