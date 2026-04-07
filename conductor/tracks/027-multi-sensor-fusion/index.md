# Track 027: Advanced Sensors

## Overview
Incorporate skin temperature and electrodermal activity (GSR) into stress detection.

## Objectives
- Research Pixel Watch 3 specific APIs for skin temperature and GSR.
- Integrate these metrics into the `EnvironmentalSensorRepository` or a new `AdvancedSensorRepository`.
- Refine `StressEvaluator` to use a multi-sensor fusion model.
- Monitor for "Cold Sweats" or "Temperature Spikes" as early stress indicators.

## Key Files
- `wear/src/main/java/com/heart/sense/wear/data/AdvancedSensorRepository.kt` (New)
- `wear/src/main/java/com/heart/sense/wear/util/StressEvaluator.kt` (Refactor)
