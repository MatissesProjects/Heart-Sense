# Track 002-data-sync: Configuration & Sync

- [Implementation Plan](./plan.md)

## Summary
Implement a configuration UI on the phone to set HR thresholds and health state, and sync this data to the watch using the Wearable Data Layer API.

## Progress
- [x] Define Settings data model
- [x] Create Mobile Settings ViewModel and Repository
- [x] Build Mobile Settings UI (Jetpack Compose)
- [x] Implement DataClient sync logic on Mobile
- [x] Create Watch Data Layer Listener
- [x] Store synced settings on Watch (DataStore)
