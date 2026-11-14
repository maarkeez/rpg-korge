package com.mkz.tessavar.screen.adapters.presentation

fun CastPreviewView.abilityName(): String = abilityNameText.text

fun CastPreviewView.caster(): String = casterText.text

fun CastPreviewView.manaSpent(): String? = manaPill.takeIf { pill -> pill.visible }?.labelText()

fun CastPreviewView.isAbilityIconDisplayed(): Boolean = abilityIcon != null

fun CastPreviewView.rows(): List<RecipientPreviewView> = rowViews.filter { rowView -> rowView.visible }

fun CastPreviewView.isCasterRowDisplayed(): Boolean = casterRowView?.visible == true

fun CastPreviewView.recipientsCount(): Int = rows().size - if (isCasterRowDisplayed()) 1 else 0

fun CastPreviewView.isRecipientPortraitVisible(index: Int): Boolean = rowViews.getOrNull(index)?.let { it.visible && it.unitPortraitView.visible } ?: false

fun CastPreviewView.areRowsInSight(height: Double): Boolean = rows().all { rowView -> rowView.y + RecipientPreviewView.HEIGHT <= height }
