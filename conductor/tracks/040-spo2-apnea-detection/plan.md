# Track 040: SpO2 & Apnea Detection

## Objective
Utilize the watch's Blood Oxygen (SpO2) sensor in conjunction with Respiratory Rate (RR) during sleep to identify periods of poor oxygenation or potential sleep apnea events.

## Key Files & Context
- `wear/src/main/java/com/heart/sense/wear/service/PassiveMonitoringService.kt`: Add `SpO2` to data collection.
- `mobile/src/main/java/com/heart/sense/data/DailyAverageRepository.kt`: Include SpO2 in recovery analysis.
- `mobile/src/main/java/com/heart/sense/ui/DashboardScreen.kt`: Add SpO2 trends.

## Implementation Steps
1. **Sensor Integration**: Update the `PassiveMonitoringClient` on the watch to request SpO2 records.
2. **Sleep Window Analysis**: Detect low SpO2 "dips" during night hours.
3. **Desaturation Alerting**: Provide a "Morning Recovery Summary" if blood oxygen levels dropped below safe thresholds (e.g., <90%) for prolonged periods.
4. **Respiratory Correlation**: Correlate SpO2 drops with spikes in Respiratory Rate.
