# Track 024: Active Biofeedback

## Overview
Use watch haptics and UI to guide users through calming exercises during stress events.

## Objectives
- Implement "Box Breathing" and "4-7-8" breathing patterns.
- Trigger haptic pulses to guide inhalation/exhalation.
- Provide real-time visual feedback of HR during the exercise.
- Automatically suggest exercises when `StressRisk.HIGH` is detected.

## Key Files
- `wear/src/main/java/com/heart/sense/wear/ui/biofeedback/BiofeedbackActivity.kt` (New)
- `wear/src/main/java/com/heart/sense/wear/util/HapticFeedbackHelper.kt` (New)
