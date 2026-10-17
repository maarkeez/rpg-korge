package com.mkz.rpg.cpuBrain.usecases.services

/** Minimum utility the best option must gain over doing nothing (e.g.: staying still, not casting) for the CPU to act. */
const val MINIMUM_UTILITY_GAIN = 0.05

/** How many tiles, at most, a CPU battle unit walks to an enemy battle unit to engage it: approach it and cast at it. */
const val ENGAGEMENT_DISTANCE = 3
