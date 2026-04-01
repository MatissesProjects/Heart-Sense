# Track 002-data-sync: Configuration & Sync

## Goal
Implement a configuration UI on the phone to set HR thresholds and health state, and sync this data to the watch using the Wearable Data Layer API.

## Tasks
1.  **Mobile Configuration UI:**
    -   Create settings screen on Pixel 9.
    -   Inputs for `High HR Threshold` and a toggle/selection for `Health State` (e.g., Normal, Sick).
2.  **Data Layer Implementation:**
    -   Use `DataClient` to sync a `Settings` object via a data map (path: `/settings`).
3.  **Watch Listener:**
    -   Implement `WearableListenerService` on the watch to receive and store settings in a local repository (e.g., using `DataStore`).
