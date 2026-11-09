package com.mkz.rpg.screen.adapters.presentation

// Page object of [CastPreviewView]. Use `RecipientPreviewViewExtension` to inspect each row.

/** The name of the ability previewed, on the header. */
fun CastPreviewView.abilityName(): String = abilityNameText.text

/** Who casts the ability, e.g.: "by Mage". */
fun CastPreviewView.caster(): String = casterText.text

/** The mana the cast spends, e.g.: "-10 mana", or null when the ability is free. */
fun CastPreviewView.manaSpent(): String? = manaPill.takeIf { pill -> pill.visible }?.labelText()

fun CastPreviewView.isAbilityIconDisplayed(): Boolean = abilityIcon != null

/** The rows displayed: the caster's, when the cast changes it, then a row per recipient. */
fun CastPreviewView.rows(): List<RecipientPreviewView> = rowViews.filter { rowView -> rowView.visible }

fun CastPreviewView.isCasterRowDisplayed(): Boolean = casterRowView?.visible == true

/** The recipients displayed, the caster's row left out. */
fun CastPreviewView.recipientsCount(): Int = rows().size - if (isCasterRowDisplayed()) 1 else 0

fun CastPreviewView.isRecipientPortraitVisible(index: Int): Boolean = rowViews.getOrNull(index)?.let { it.visible && it.unitPortraitView.visible } ?: false

/** Whether every row displayed ends within the [height] of the HUD, so none has to be scrolled into view. */
fun CastPreviewView.areRowsInSight(height: Double): Boolean = rows().all { rowView -> rowView.y + RecipientPreviewView.HEIGHT <= height }
