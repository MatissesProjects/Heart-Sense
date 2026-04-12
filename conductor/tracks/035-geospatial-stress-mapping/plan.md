# Track 035: Geospatial Stress Mapping

## Objective
Map physiological spikes (High HR, low HRV, stimming) to specific GPS locations to identify environmental "stress hotspots" (e.g., specific clinic rooms, grocery stores, classrooms).

## Key Files & Context
- `mobile/src/main/java/com/heart/sense/service/LocationTrackingService.kt` (New)
- `mobile/src/main/java/com/heart/sense/data/db/LocationTag.kt` (New Entity)
- `mobile/src/main/java/com/heart/sense/ui/HeatmapScreen.kt` (New UI)

## Implementation Steps
1. **Location Permissions**: [COMPLETED] Requested `ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION`, and `ACCESS_BACKGROUND_LOCATION` in `MainActivity.kt`.
2. **Triggered GPS Logging**: [COMPLETED] Implemented `LocationRepository` and updated `AlertHandler` to log coordinates during stress/HR events.
3. **Data Clustering**: [COMPLETED] Utilized `HeatmapTileProvider` from Google Maps Utils for visual clustering.
4. **Heatmap UI**: [COMPLETED] Implemented `HeatmapScreen.kt` using Google Maps Compose and `TileOverlay`.
