# Track 015: AI Health Baseline

## Overview
Move beyond static thresholds by implementing an adaptive "Health Baseline" using historical data and local processing.

## Objectives
- Implement a moving average or ML model to define "Normal" for each user.
- Automatically adjust `highHrThreshold` based on historical trends (e.g., if RHR increases, adjust alert sensitivity).
- Implement trend deviation alerts (e.g., "Your resting HR is 15% higher than usual").

## Key Files
- `mobile/src/main/java/com/heart/sense/data/DailyAverageRepository.kt`
- `mobile/src/main/java/com/heart/sense/data/SettingsRepository.kt`
- `wear/src/main/java/com/heart/sense/wear/util/HeartRateEvaluator.kt`
