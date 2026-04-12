# Track 041: External BLE Sensors

## Objective
Provide support for connecting external medical-grade Bluetooth Low Energy (BLE) sensors (like Polar H10 chest straps or Empatica wristbands) directly to the mobile app for high-fidelity clinical research data collection.

## Key Files & Context
- `mobile/src/main/java/com/heart/sense/service/BleSensorService.kt` (New: BLE scanner and parser)
- `mobile/src/main/java/com/heart/sense/data/db/BleSensorData.kt` (New Entity)
- `mobile/src/main/java/com/heart/sense/ui/SensorSetupScreen.kt` (New UI)

## Implementation Steps
1. **BLE Scanner**: Implement a BLE scanner to discover medical devices (GATT HR service).
2. **Data Parsing**: Implement parsers for standard Heart Rate and HRV GATT characteristics.
3. **Multi-Source Fusion**: Allow the system to use external sensor data as "ground truth" when it is available, overriding the watch's PPG sensor for clinical research.
4. **Data Logging**: Log external data into the local Room database alongside the watch telemetry.
