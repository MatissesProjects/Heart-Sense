# Track 033: CGM Integration

## Objective
Sync Heart-Sense with Continuous Glucose Monitors (CGM) via Health Connect to analyze the correlation between blood sugar volatility and physiological stress responses (meltdowns, anxiety, or fatigue).

## Key Files & Context
- `mobile/src/main/java/com/heart/sense/data/HealthConnectRepository.kt`: Expand to request permissions for and read `BloodGlucoseRecord`.
- `mobile/src/main/java/com/heart/sense/data/AlertHandler.kt`: Update logic to check for recent glucose drops when evaluating a high-stress event.
- `mobile/src/main/java/com/heart/sense/ui/DashboardScreen.kt`: Overlay glucose levels onto the HR/HRV charts.

## Implementation Steps
1. **Health Connect Expansion**: Update manifest and `HealthConnectRepository` to request `BloodGlucoseRecord` permissions.
2. **Data Sync**: Implement background workers to periodically pull CGM data and store an aggregated version locally.
3. **Correlation Engine**: When a stress event triggers, analyze blood sugar trends in the preceding 60 minutes. Flag events that correlate with "crashing" sugar levels.
4. **UI Updates**: Visualize glucose trends on the main historical dashboard.
