package com.heart.sense.wear.util

import kotlin.math.sqrt

enum class RhythmState {
    NORMAL,
    IRREGULAR,
    INSUFFICIENT_DATA
}

object RhythmEvaluator {
    private const val CV_THRESHOLD = 0.15 // 15% variability is high for resting
    private const val MIN_INTERVALS = 10

    /**
     * Evaluates the rhythm based on RR intervals.
     * Intervals are in milliseconds.
     */
    fun evaluate(intervals: List<Long>?): RhythmState {
        if (intervals == null || intervals.size < MIN_INTERVALS) {
            return RhythmState.INSUFFICIENT_DATA
        }

        val doubleIntervals = intervals.map { it.toDouble() }
        val mean = doubleIntervals.average()
        if (mean == 0.0) return RhythmState.INSUFFICIENT_DATA

        val variance = doubleIntervals.fold(0.0) { acc, d -> acc + (d - mean) * (d - mean) } / doubleIntervals.size
        val stdDev = sqrt(variance)
        val cv = stdDev / mean

        return if (cv > CV_THRESHOLD) {
            RhythmState.IRREGULAR
        } else {
            RhythmState.NORMAL
        }
    }
}
