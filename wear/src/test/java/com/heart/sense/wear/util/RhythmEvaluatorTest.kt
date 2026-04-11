package com.heart.sense.wear.util

import org.junit.Assert.assertEquals
import org.junit.Test

class RhythmEvaluatorTest {

    @Test
    fun `evaluate returns INSUFFICIENT_DATA for short list`() {
        val intervals = listOf(1000L, 1000L, 1000L)
        val result = RhythmEvaluator.evaluate(intervals)
        assertEquals(RhythmState.INSUFFICIENT_DATA, result)
    }

    @Test
    fun `evaluate returns INSUFFICIENT_DATA for null list`() {
        val result = RhythmEvaluator.evaluate(null)
        assertEquals(RhythmState.INSUFFICIENT_DATA, result)
    }

    @Test
    fun `evaluate returns NORMAL for stable intervals`() {
        val intervals = List(10) { 1000L }
        val result = RhythmEvaluator.evaluate(intervals)
        assertEquals(RhythmState.NORMAL, result)
    }

    @Test
    fun `evaluate returns IRREGULAR for high variability`() {
        // Mean approx 1000
        // CV = stdDev / mean. Threshold 0.15
        // stdDev needs to be > 150
        val intervals = listOf(
            1000L, 700L, 1300L, 1000L, 800L, 1200L, 1000L, 700L, 1300L, 1000L
        )
        val result = RhythmEvaluator.evaluate(intervals)
        assertEquals(RhythmState.IRREGULAR, result)
    }

    @Test
    fun `evaluate returns NORMAL for slight variability`() {
        val intervals = listOf(
            1000L, 1050L, 950L, 1000L, 1020L, 980L, 1000L, 1050L, 950L, 1000L
        )
        val result = RhythmEvaluator.evaluate(intervals)
        assertEquals(RhythmState.NORMAL, result)
    }
}
