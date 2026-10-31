package com.mkz.rpg.screen.adapters.presentation

import korlibs.image.paint.Paint

// Page object of [PointsView].

/** How many points remain, displayed as filled pips. */
fun PointsView.filledPoints(): Int = points.count { point -> point.name == PointsView.FILLED }

/** How many points have been spent, displayed as tinted pips with an outline. */
fun PointsView.hollowPoints(): Int = points.count { point -> point.name == PointsView.HOLLOW }

/** The colours the points are drawn with: the fill of the filled points and the outline of every point. */
fun PointsView.pointColors(): Set<Paint> = points.flatMap { point -> if (point.name == PointsView.FILLED) listOf(point.fill, point.stroke) else listOf(point.stroke) }.toSet()

/** The fills of the spent points. */
fun PointsView.spentPointFills(): Set<Paint> = points.filter { point -> point.name == PointsView.HOLLOW }.map { point -> point.fill }.toSet()
