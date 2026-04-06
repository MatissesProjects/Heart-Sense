# Track 020: Behavioral Pattern Recognition

## Overview
This track focuses on recognizing complex behaviors like pacing or sudden standing/agitation by combining activity state transitions and motion data.

## Goals
-   [ ] Implement a `BehavioralPatternRecognizer` to detect pacing.
-   [ ] Develop logic for "Sudden Agitation" alerts (high HR + sudden transition to walking).
-   [ ] Allow for custom behavioral "rules" to be set from the mobile dashboard.
-   [ ] Integrate behavioral alerts into the standard notification system.

## Next Steps
1.  Define what constitutes a "Pacing" pattern in terms of motion and duration.
2.  Combine `PassiveMonitoringService`'s activity states with raw IMU data.
3.  Add "Pacing Detected" to the mobile alert system.
