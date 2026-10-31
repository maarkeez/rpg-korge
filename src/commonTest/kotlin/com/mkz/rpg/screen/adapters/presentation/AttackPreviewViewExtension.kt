package com.mkz.rpg.screen.adapters.presentation

// Page object of [AttackPreviewView]. Use `ReceiverPreviewViewExtension` to inspect each row.

/** The name of the ability previewed, on the header. */
fun AttackPreviewView.abilityName(): String = abilityNameText.text

/** Who casts the ability, e.g.: "by Mage". */
fun AttackPreviewView.caster(): String = casterText.text

/** The mana the cast spends, e.g.: "-10 mana", or null when the ability is free. */
fun AttackPreviewView.manaSpent(): String? = manaPill.takeIf { pill -> pill.visible }?.labelText()

fun AttackPreviewView.isAbilityIconDisplayed(): Boolean = abilityIcon != null

/** The rows displayed: the caster's, when the cast changes it, then a row per receiver. */
fun AttackPreviewView.rows(): List<ReceiverPreviewView> = rowViews.filter { rowView -> rowView.visible }

fun AttackPreviewView.isCasterRowDisplayed(): Boolean = casterRowView?.visible == true

/** The receivers displayed, the caster's row left out. */
fun AttackPreviewView.receiversCount(): Int = rows().size - if (isCasterRowDisplayed()) 1 else 0

fun AttackPreviewView.isReceiverPortraitVisible(index: Int): Boolean = rowViews.getOrNull(index)?.let { it.visible && it.unitPortraitView.visible } ?: false

/** Whether every row displayed ends within the [height] of the battle HUD, so none has to be scrolled into view. */
fun AttackPreviewView.areRowsInSight(height: Double): Boolean = rows().all { rowView -> rowView.y + ReceiverPreviewView.HEIGHT <= height }
