# Track 006-sick-mode: Health Context & Dynamic Logic

## Goal
Implement the logic to dynamically adjust thresholds and triggers based on health state.

## Tasks
1.  **Dynamic Thresholding:**
    -   Logic to adjust the `highHRThreshold` based on `healthState` (e.g., subtract 10bpm if sick).
2.  **Sit-Down Warning Logic:**
    -   Monitor activity state more closely when sick.
    -   Trigger "Sit Down" alerts if activity is anything but passive and HR is elevated.
3.  **Integrated Testing:**
    -   Verify that marking "sick" on the phone updates the monitoring behavior on the watch immediately.
