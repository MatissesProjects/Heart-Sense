# Track 019: Motion & Fidgeting Analytics

## Overview
This track focuses on using raw sensor data (Accelerometer, Gyroscope) to detect repetitive hand movements often associated with fidgeting or stimming in neurodivergent individuals.

## Goals
-   [ ] Implement a `MotionSensorRepository` to access raw IMU data.
-   [ ] Develop a `FidgetDetector` logic based on frequency analysis (FFT) or heuristic filters.
-   [ ] Integrate fidgeting intensity metrics into the daily health sync.
-   [ ] Correlate motion data with heart rate spikes to identify behavioral triggers.

## Next Steps
1.  Verify raw sensor access permissions and battery impact.
2.  Implement a basic movement frequency filter.
3.  Add "Fidgeting" as a trackable metric in the Room database.
