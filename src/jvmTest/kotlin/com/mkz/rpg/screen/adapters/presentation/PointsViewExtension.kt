package com.mkz.rpg.screen.adapters.presentation

import korlibs.image.paint.Paint

// Page object of [PointsView].

/** How many points remain, displayed as filled circles. */
fun PointsView.filledPoints(): Int = points.count { point -> point.name == PointsView.FILLED }

/** How many points have been spent, displayed as hollow circles. */
fun PointsView.hollowPoints(): Int = points.count { point -> point.name == PointsView.HOLLOW }

/** The colours the points are drawn with: the fill of the filled points and the outline of every point. */
fun PointsView.pointColors(): Set<Paint> = points.flatMap { point -> if (point.name == PointsView.FILLED) listOf(point.fill, point.stroke) else listOf(point.stroke) }.toSet()

/** The (width, height) of every point as displayed, outline included. */
fun PointsView.pointSizes(): Set<Pair<Double, Double>> = points.map { point -> point.getGlobalBounds().let { bounds -> bounds.width to bounds.height } }.toSet()
