package com.mkz.rpg.screen.adapters.presentation

import korlibs.image.paint.Paint

fun PointsView.filledPoints(): Int = points.count { point -> point.name == PointsView.FILLED }

fun PointsView.hollowPoints(): Int = points.count { point -> point.name == PointsView.HOLLOW }

fun PointsView.pointColors(): Set<Paint> = points.flatMap { point -> if (point.name == PointsView.FILLED) listOf(point.fill, point.stroke) else listOf(point.stroke) }.toSet()

fun PointsView.spentPointFills(): Set<Paint> = points.filter { point -> point.name == PointsView.HOLLOW }.map { point -> point.fill }.toSet()
