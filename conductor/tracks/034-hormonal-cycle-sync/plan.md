# Track 034: Hormonal Cycle Sync

## Objective
Adjust the Heart-Sense AI's "Calm Baseline" dynamically based on the user's hormonal/menstrual cycle phases via Health Connect, as cycle phases drastically alter natural resting Heart Rate and Heart Rate Variability.

## Key Files & Context
- `mobile/src/main/java/com/heart/sense/data/HealthConnectRepository.kt`: Add permissions and sync logic for `MenstruationPeriodRecord`.
- `mobile/src/main/java/com/heart/sense/data/DailyAverageRepository.kt`: Modify `calculateAdaptiveBaseline` to account for cycle phase offsets.
- `wear/src/main/java/com/heart/sense/wear/ai/StressPredictor.kt`: Provide cycle context to the AI model.

## Implementation Steps
1. **Health Connect Access**: Sync menstrual cycle phase data from Google Health Connect.
2. **Baseline Offsets**: Implement an algorithm to shift the user's expected baseline HR up or down depending on their current cycle phase (e.g., elevated RHR during luteal phase).
3. **AI Integration**: Feed the current phase as a categorical feature into the TFLite `StressPredictor` model to prevent false positive stress alerts during natural physiological shifts.
