# Track 035: Geospatial Stress Mapping

## Objective
Map physiological spikes (High HR, low HRV, stimming) to specific GPS locations to identify environmental "stress hotspots" (e.g., specific clinic rooms, grocery stores, classrooms).

## Key Files & Context
- `mobile/src/main/java/com/heart/sense/service/LocationTrackingService.kt` (New)
- `mobile/src/main/java/com/heart/sense/data/db/LocationTag.kt` (New Entity)
- `mobile/src/main/java/com/heart/sense/ui/HeatmapScreen.kt` (New UI)

## Implementation Steps
1. **Location Permissions**: Request `ACCESS_FINE_LOCATION` and `ACCESS_BACKGROUND_LOCATION` on the mobile device.
2. **Triggered GPS Logging**: When the Wear OS device transmits a "Watching Closer" or "Stress" event to the phone, the phone briefly wakes up the GPS to log the current coordinates.
3. **Data Clustering**: Cluster coordinates over time to identify frequent hotspots of high arousal.
4. **Heatmap UI**: Implement a map view (using Google Maps SDK) in the mobile app, visualizing red/hot zones where stress events frequently occur.
