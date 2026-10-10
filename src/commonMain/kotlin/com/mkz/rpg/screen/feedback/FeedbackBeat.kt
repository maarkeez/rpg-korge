package com.mkz.rpg.screen.feedback

/**
 * One visible step of playback. Beats carry everything they show (positions, amounts), because by the time a beat plays
 * the repositories may already reflect later events and must not be diffed to find out what happened.
 */
sealed interface FeedbackBeat {
    /** A unit hops one tile. [hops] is the length of the whole walk this hop belongs to. */
    data class Move(
        val battleUnitId: String,
        val fromRow: Int,
        val fromColumn: Int,
        val toRow: Int,
        val toColumn: Int,
        val hops: Int = 1,
        val hopIndex: Int = 0,
    ) : FeedbackBeat {
        val isFirstHop: Boolean get() = hopIndex == 0
        val isLastHop: Boolean get() = hopIndex >= hops - 1
    }

    data class Teleport(
        val battleUnitId: String,
        val fromRow: Int,
        val fromColumn: Int,
        val toRow: Int,
        val toColumn: Int,
    ) : FeedbackBeat

    data class Hit(
        val battleUnitId: String,
        val amount: Int,
        val remainingHealthPoints: Int,
    ) : FeedbackBeat

    data class Heal(
        val battleUnitId: String,
        val amount: Int,
        val remainingHealthPoints: Int,
    ) : FeedbackBeat

    data class StatusApplied(
        val battleUnitId: String,
        val effectId: String,
    ) : FeedbackBeat

    data class Defeated(
        val battleUnitId: String,
        val row: Int,
        val column: Int,
    ) : FeedbackBeat

    /** A spark travels from a defeated unit to a neighbour that receives its death spread. */
    data class Spread(
        val fromRow: Int,
        val fromColumn: Int,
        val toRow: Int,
        val toColumn: Int,
    ) : FeedbackBeat

    data class Deployed(
        val battleUnitId: String,
        val row: Int,
        val column: Int,
    ) : FeedbackBeat

    data class CameraFocus(
        val row: Int,
        val column: Int,
    ) : FeedbackBeat

    /** The owner of the turn changes. Plays after everything the previous turn did. */
    data class TurnStarted(
        val playerId: String,
    ) : FeedbackBeat

    /** The overlay of a unit is redrawn once the beats before it have played. */
    data class OverlayRefresh(
        val battleUnitId: String,
    ) : FeedbackBeat

    /** The battlefield view stops showing a unit that left the battlefield without a defeat. */
    data class OccupantRemoved(
        val row: Int,
        val column: Int,
    ) : FeedbackBeat
}
