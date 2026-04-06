# Track 021: Environmental Context & Sensory Triggers

## Overview
This track implements environmental monitoring (Ambient Noise, Light) to correlate external sensory triggers with internal physiological stress responses.

## Goals
-   [ ] Monitor ambient noise levels (dB) using the watch microphone.
-   [ ] Monitor ambient light levels via the onboard light sensor.
-   [ ] Implement a "Correlation Engine" that links sensory spikes to Heart Rate/HRV changes.
-   [ ] Provide actionable reports: "User is 3x more likely to have a stress alert in environments > 85dB."

## Next Steps
1.  Implement a battery-efficient `AmbientNoiseService`.
2.  Store environmental snapshots during physiological alerts.
3.  Add "Environmental Context" to the Clinician Dashboard.
